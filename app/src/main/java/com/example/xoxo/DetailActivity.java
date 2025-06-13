package com.example.xoxo;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DetailActivity extends AppCompatActivity {

    private ImageView imagePoster;
    private Button btnUploadImage, btnEdit, btnSave, btnDelete;
    private ImageButton btnFav, btnBookmark, btnShare;
    private EditText textJudul, textInfo, textDeskripsi, textPemain, textSutradara;
    private ProgressBar progressBar;

    private DatabaseReference databaseRef;
    private StorageReference storageRef;

    private String filmId;
    private Uri imageUri;
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Inisialisasi View
        imagePoster = findViewById(R.id.imagePoster);
        btnUploadImage = findViewById(R.id.btnUploadImage);
        btnEdit = findViewById(R.id.btnEdit);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
        btnFav = findViewById(R.id.btnFav);
        btnBookmark = findViewById(R.id.btnBookmark);
        btnShare = findViewById(R.id.btnShare);
        progressBar = findViewById(R.id.progressBar);

        textJudul = findViewById(R.id.textJudul);
        textInfo = findViewById(R.id.textInfo);
        textDeskripsi = findViewById(R.id.textDeskripsi);
        textPemain = findViewById(R.id.textPemain);
        textSutradara = findViewById(R.id.textSutradara);

        // Ambil ID film dari intent
        filmId = getIntent().getStringExtra("filmId");
        if (filmId == null) {
            Toast.makeText(this, "Film ID tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
        }

        databaseRef = FirebaseDatabase.getInstance().getReference("film").child(filmId);
        storageRef = FirebaseStorage.getInstance().getReference("film_images");

        loadFilmDetails();

        btnEdit.setOnClickListener(v -> toggleEdit(true));
        btnSave.setOnClickListener(v -> saveChanges());
        btnDelete.setOnClickListener(v -> deleteFilm());

        btnShare.setOnClickListener(v -> {
            String shareText = "Lihat film ini: " + textJudul.getText().toString();
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(shareIntent, "Bagikan film via"));
        });

        btnFav.setOnClickListener(v -> Toast.makeText(this, "Ditambahkan ke Favorit", Toast.LENGTH_SHORT).show());
        btnBookmark.setOnClickListener(v -> Toast.makeText(this, "Ditandai", Toast.LENGTH_SHORT).show());

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void loadFilmDetails() {
        progressBar.setVisibility(View.VISIBLE);
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String judul = snapshot.child("judul").getValue(String.class);
                    String info = snapshot.child("info").getValue(String.class);
                    String deskripsi = snapshot.child("deskripsi").getValue(String.class);
                    String pemain = snapshot.child("pemain").getValue(String.class);
                    String sutradara = snapshot.child("sutradara").getValue(String.class);
                    String posterUrl = snapshot.child("poster").getValue(String.class);

                    textJudul.setText(judul);
                    textInfo.setText(info);
                    textDeskripsi.setText(deskripsi);
                    textPemain.setText(pemain);
                    textSutradara.setText(sutradara);

                    if (posterUrl != null && !posterUrl.isEmpty()) {
                        Glide.with(DetailActivity.this)
                                .load(posterUrl)
                                .placeholder(R.drawable.placeholder_movie)
                                .into(imagePoster);
                    }
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void toggleEdit(boolean editMode) {
        isEditing = editMode;

        textJudul.setEnabled(editMode);
        textInfo.setEnabled(editMode);
        textDeskripsi.setEnabled(editMode);
        textPemain.setEnabled(editMode);
        textSutradara.setEnabled(editMode);

        btnUploadImage.setVisibility(editMode ? View.VISIBLE : View.GONE);
        btnSave.setVisibility(editMode ? View.VISIBLE : View.GONE);
        btnEdit.setVisibility(editMode ? View.GONE : View.VISIBLE);
    }

    private void saveChanges() {
        String judul = textJudul.getText().toString();
        String info = textInfo.getText().toString();
        String deskripsi = textDeskripsi.getText().toString();
        String pemain = textPemain.getText().toString();
        String sutradara = textSutradara.getText().toString();

        if (judul.isEmpty() || info.isEmpty()) {
            Toast.makeText(this, "Judul dan Info wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseRef.child("judul").setValue(judul);
        databaseRef.child("info").setValue(info);
        databaseRef.child("deskripsi").setValue(deskripsi);
        databaseRef.child("pemain").setValue(pemain);
        databaseRef.child("sutradara").setValue(sutradara);

        toggleEdit(false);
        Toast.makeText(this, "Perubahan disimpan", Toast.LENGTH_SHORT).show();
    }

    private void deleteFilm() {
        databaseRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Film dihapus", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Gagal menghapus film", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
