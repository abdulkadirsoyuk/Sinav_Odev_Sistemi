package com.example.hackathon_ticaret_mektebi;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.hackathon_ticaret_mektebi.Models.Student;
import com.example.hackathon_ticaret_mektebi.Models.Teacher;
import com.example.hackathon_ticaret_mektebi.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class IcerikDetayActivity extends AppCompatActivity {

    FirebaseUser user;
    String userID;
    TextView icerigin_adi_dt, paylasan_kisi_dt,paylasilan_tarih_dt,icerik_bolum_adi_dt,icerik_sayfa_boyut_dt;
    TextView icerik_detay_ad_soyad_text;
    private DatabaseReference databaseReference;
    private Button icerigiIndirBtn;
    private DatabaseReference contentDatabaseRef;
    private String contentName;
    ImageView icerik_detay_pp;
    ImageButton gemini;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.icerik_detay);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.icerik_detay_l), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Intent'ten gelen verileri al
        Intent intent = getIntent();
        String contentName_str = intent.getStringExtra("contentName");
        String contentSharedDate_str = intent.getStringExtra("contentSharedDate");
        String contentDepartment_str = intent.getStringExtra("contentDepartment");
        String contentSize_str = intent.getStringExtra("contentSize");
        String contentProviderID = intent.getStringExtra("contentProvider");

        gemini=findViewById(R.id.imageButton);
        gemini.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IcerikDetayActivity.this, GeminiActivity.class);
                startActivity(intent);
            }
        });


        // View tanımlamaları
        icerik_detay_pp = findViewById(R.id.icerik_detay_pp);
        icerigin_adi_dt = findViewById(R.id.icerigin_adi_dt);
        paylasan_kisi_dt = findViewById(R.id.paylasan_kisi_dt);
        paylasilan_tarih_dt = findViewById(R.id.paylasilan_tarih_dt);
        icerik_bolum_adi_dt = findViewById(R.id.icerik_bolum_adi_dt);
        icerik_sayfa_boyut_dt = findViewById(R.id.icerik_sayfa_boyut_dt);
        icerik_detay_ad_soyad_text=findViewById(R.id.icerik_detay_ad_soyad_text);

        // Ders bilgilerini doğrudan atayın
        icerigin_adi_dt.setText(contentName_str);
        paylasilan_tarih_dt.setText(contentSharedDate_str);
        icerik_bolum_adi_dt.setText(contentDepartment_str);
        icerik_sayfa_boyut_dt.setText(contentSize_str);
        paylasan_kisi_dt.setText(contentProviderID);
        // contentProviderID'nin null olup olmadığını kontrol et
        if (contentProviderID != null && !contentProviderID.isEmpty()) {
            // Firebase veritabanı referansı
            databaseReference = FirebaseDatabase.getInstance().getReference("teachers");

            // teacherID'yi kullanarak öğretmenin adını almak için veritabanında arama yapın
            databaseReference.child(contentProviderID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Öğretmenin adını aldıktan sonra TextView'e ayarla
                        String contentProvider = dataSnapshot.child("nameSurname").getValue(String.class);
                        paylasan_kisi_dt.setText(contentProvider);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(IcerikDetayActivity.this, "Veritabanı hatası: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(IcerikDetayActivity.this, "Öğretmen ID'si geçerli değil.", Toast.LENGTH_SHORT).show();
        }


        icerigiIndirBtn = findViewById(R.id.icerigi_indir_btn);
        // Firebase database referansı
        contentDatabaseRef = FirebaseDatabase.getInstance().getReference("contents");
        // TextView'den içerik adını al
        contentName = icerigin_adi_dt.getText().toString();
        getCurrentUser();
        getUserInfoFromDatabase();
        icerigiIndirBtn.setOnClickListener(view -> searchContentAndDownload(contentName));

        icerik_detay_pp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                user = getCurrentUser();
                if (user != null) {
                    String userID = user.getUid();
                    DatabaseReference teacherRef = FirebaseDatabase.getInstance().getReference("teachers").child(userID);
                    teacherRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Eğer kullanıcı teacher ise OgretmenArayuzActivity'i aç
                                Intent intent = new Intent(IcerikDetayActivity.this, OgretmenArayuzActivity.class);
                                startActivity(intent);
                            } else {
                                // Eğer kullanıcı teacher değilse, student'ı kontrol et
                                DatabaseReference studentRef = FirebaseDatabase.getInstance().getReference("students").child(userID);
                                studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot studentSnapshot) {
                                        if (studentSnapshot.exists()) {
                                            // Eğer kullanıcı student ise OgrenciArayuzActivity'i aç
                                            Intent intent = new Intent(IcerikDetayActivity.this, OgrenciArayuzActivity.class);
                                            startActivity(intent);
                                        } else {
                                            // Kullanıcı ne öğretmen ne de öğrenci
                                            Log.d("UserInfo", "Kullanıcı ne öğretmen ne de öğrenci.");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.e("UserInfo", "Error: " + databaseError.getMessage());
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e("UserInfo", "Error: " + databaseError.getMessage());
                        }
                    });
                } else {
                    Log.d("UserInfo", "No user is signed in.");
                }
            }
        });
    }

    private void searchContentAndDownload(String contentName) {
        contentDatabaseRef.orderByChild("contentName").equalTo(contentName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot contentSnapshot : dataSnapshot.getChildren()) {
                        String fileUrl = contentSnapshot.child("contentURL").getValue(String.class);
                        if (fileUrl != null) {
                            downloadPdf(fileUrl);
                        } else {
                            Toast.makeText(IcerikDetayActivity.this, "Dosya URL'si bulunamadı.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(IcerikDetayActivity.this, "Veritabanı hatası: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadPdf(String fileUrl) {
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(fileUrl);

        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle("İndirilen PDF Dosyası");
        request.setDescription("PDF dosyası indiriliyor...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        // Dosyayı 'Download' klasörüne indir
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "indirilen_dosya.pdf");

        // İndirme işlemini başlat
        if (downloadManager != null) {
            downloadManager.enqueue(request);
            Toast.makeText(this, "İndirme işlemi başlatıldı.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "İndirme işlemi başlatılamadı.", Toast.LENGTH_SHORT).show();
        }
    }

    public void getUserInfoFromDatabase() {
        user = getCurrentUser();
        if (user != null) {
            userID = user.getUid();

            // Önce teachers düğümünde kontrol edelim
            DatabaseReference teacherRef = FirebaseDatabase.getInstance().getReference("teachers").child(userID);
            teacherRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Kullanıcı teacher düğümündeyse
                        Teacher teacher = dataSnapshot.getValue(Teacher.class);
                        if (teacher != null) {

                            String userName = teacher.getNameSurname();
                            String userDepartment = teacher.getUserDepartment();
                            String userPhotoUrl = teacher.getProfilePictureURL();
                            if (userPhotoUrl != null) {
                                Glide.with(IcerikDetayActivity.this).load(userPhotoUrl).into(icerik_detay_pp);
                            } else {
                                Log.e("ImageLoadError", "Profil resmi URL'si null."); // URL'nin null olup olmadığını kontrol et
                            }
                            icerik_detay_ad_soyad_text.setText(userName);
                            Log.d("UserInfo", "Teacher - User ID: " + userID + ", Name: " + userName + ", Department: " + userDepartment + ", Photo URL: " + userPhotoUrl);
                        }
                    } else {
                        // Eğer teacher düğümünde değilse, students düğümünü kontrol et
                        DatabaseReference studentRef = FirebaseDatabase.getInstance().getReference("students").child(userID);
                        studentRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot studentSnapshot) {
                                if (studentSnapshot.exists()) {
                                    // Kullanıcı student düğümündeyse
                                    Student student = studentSnapshot.getValue(Student.class);
                                    if (student != null) {
                                        String userName = student.getNameSurname();
                                        String userClass = student.getUserDepartment();
                                        String userPhotoUrl = student.getProfilePictureURL();

                                        if (userPhotoUrl != null) {
                                            Glide.with(IcerikDetayActivity.this).load(userPhotoUrl).into(icerik_detay_pp);
                                        } else {
                                            Log.e("ImageLoadError", "Profil resmi URL'si null."); // URL'nin null olup olmadığını kontrol et
                                        }
                                        icerik_detay_ad_soyad_text.setText(userName);
                                        Log.d("UserInfo", "Student - User ID: " + userID + ", Name: " + userName + ", Class: " + userClass + ", Photo URL: " + userPhotoUrl);
                                    }
                                } else {
                                    Log.d("UserInfo", "Kullanıcı ne öğretmen ne de öğrenci.");
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e("UserInfo", "Error: " + databaseError.getMessage());
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("UserInfo", "Error: " + databaseError.getMessage());
                }
            });
        } else {
            Log.d("UserInfo", "No user is signed in.");
        }


    }


    // Firebase'den giriş yapmış kullanıcıyı döndürür
    public FirebaseUser getCurrentUser() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        return mAuth.getCurrentUser();
    }
}
