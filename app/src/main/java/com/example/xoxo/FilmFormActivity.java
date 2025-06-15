package com.example.xoxo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.xoxo.CloudinaryManager;
import com.example.xoxo.databinding.ActivityFilmFormBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.IOException;

public class FilmFormActivity extends AppCompatActivity {

    private ActivityFilmFormBinding binding;
    private FirebaseFirestore db;
    private Film currentFilm;
    private Uri selectedImageUri;
    private boolean isEditMode = false;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    binding.ivFilm.setImageURI(selectedImageUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFilmFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        if (getIntent().hasExtra("film")) {
            isEditMode = true;
            currentFilm = (Film) getIntent().getSerializableExtra("film");
            populateForm(currentFilm);
        }

        setupClickListeners();
    }

    private void populateForm(Film film) {
        binding.etBioskop.setText(film.getBioskop());
        binding.etHarga.setText(film.getHarga());
        binding.etJudul.setText(film.getTitle());
        binding.etDeskripsi.setText(film.getDesc());
        binding.etInfo.setText(film.getInfo());
        binding.etPemain.setText(film.getPemain());
        binding.etSutradara.setText(film.getSutradara());

        if (film.getImageUrl() != null && !film.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(film.getImageUrl())
                    .centerCrop()
                    .into(binding.ivFilm);
        }
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.ivFilm.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        binding.btnCancel.setOnClickListener(v -> finish());

        binding.btnSave.setOnClickListener(v -> {
            if (validateForm()) {
                if (selectedImageUri != null) {
                    uploadImageAndSaveFilm();
                } else if (isEditMode && currentFilm.getImageUrl() != null) {
                    saveFilm(currentFilm.getImageUrl());
                } else {
                    Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean validateForm() {
        boolean isValid = true;

        if (binding.etJudul.getText().toString().trim().isEmpty()) {
            binding.etJudul.setError("Judul film harus diisi");
            isValid = false;
        }

        if (binding.etBioskop.getText().toString().trim().isEmpty()) {
            binding.etBioskop.setError("Nama bioskop harus diisi");
            isValid = false;
        }

        if (binding.etHarga.getText().toString().trim().isEmpty()) {
            binding.etHarga.setError("Harga tiket harus diisi");
            isValid = false;
        }

        return isValid;
    }

    private void uploadImageAndSaveFilm() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnSave.setEnabled(false);

        try {
            File imageFile = CloudinaryManager.uriToFile(this, selectedImageUri);
            CloudinaryManager.uploadImage(imageFile, new CloudinaryManager.UploadCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    saveFilm(imageUrl);
                }

                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnSave.setEnabled(true);
                        Toast.makeText(FilmFormActivity.this, "Gagal mengupload gambar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } catch (IOException e) {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnSave.setEnabled(true);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveFilm(String imageUrl) {
        String bioskop = binding.etBioskop.getText().toString().trim();
        String harga = binding.etHarga.getText().toString().trim();
        String judul = binding.etJudul.getText().toString().trim();
        String deskripsi = binding.etDeskripsi.getText().toString().trim();
        String info = binding.etInfo.getText().toString().trim();
        String pemain = binding.etPemain.getText().toString().trim();
        String sutradara = binding.etSutradara.getText().toString().trim();

        Film film = new Film(judul, bioskop, harga, imageUrl, deskripsi, info, pemain, sutradara);

        if (isEditMode) {
            film.setId(currentFilm.getId());
            updateFilmInFirestore(film);
        } else {
            addFilmToFirestore(film);
        }
    }

    private void addFilmToFirestore(Film film) {
        db.collection("films")
                .add(film)
                .addOnSuccessListener(documentReference -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Film berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnSave.setEnabled(true);
                    Toast.makeText(this, "Gagal menambahkan film: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateFilmInFirestore(Film film) {
        db.collection("films")
                .document(film.getId())
                .set(film)
                .addOnSuccessListener(aVoid -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Film berhasil diperbarui", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnSave.setEnabled(true);
                    Toast.makeText(this, "Gagal memperbarui film: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}