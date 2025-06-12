package com.example.xoxo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.xoxo.databinding.ActivityDetailBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DetailActivity extends AppCompatActivity {
    private ActivityDetailBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;
    private Film currentFilm;
    private boolean isFavorite = false;
    private boolean isCloudinaryInitialized = false;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    uploadImageToCloudinary(uri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        // Initialize Cloudinary
        initCloudinary();

        // Get film data from intent
        currentFilm = new Film(
                getIntent().getStringExtra("film_id"),
                getIntent().getStringExtra("film_title"),
                "",
                getIntent().getStringExtra("film_harga"),
                getIntent().getStringExtra("film_image_url"),
                getIntent().getStringExtra("film_desc"),
                getIntent().getStringExtra("film_info"),
                getIntent().getStringExtra("film_pemain"),
                getIntent().getStringExtra("film_sutradara")
        );

        setupViews();
        checkIfFavorite();
    }

    private void initCloudinary() {
        if (!isCloudinaryInitialized) {
            try {
                Map<String, String> config = new HashMap<>();
                config.put("cloud_name", "dl2edswaa");
                config.put("api_key", "922571496894783");
                // Note: Don't include api_secret in client-side code
                MediaManager.init(this, config);
                isCloudinaryInitialized = true;
            } catch (Exception e) {
                Log.e("Cloudinary", "Initialization error: " + e.getMessage());
            }
        }
    }

    private void setupViews() {
        // Set film data
        binding.textJudul.setText(currentFilm.getTitle());
        binding.textInfo.setText(currentFilm.getInfo());
        binding.textDeskripsi.setText(currentFilm.getDesc());
        binding.textPemain.setText("Pemain: " + currentFilm.getPemain());
        binding.textSutradara.setText("Sutradara: " + currentFilm.getSutradara());

        // Load image with Glide
        Glide.with(this)
                .load(currentFilm.getImageUrl())
                .placeholder(R.drawable.placeholder_movie)
                .error(R.drawable.error_movie)
                .into(binding.imagePoster);

        // Back button
        binding.btnBack.setOnClickListener(v -> finish());

        // Favorite button
        binding.btnFav.setOnClickListener(v -> toggleFavorite());

        // Bookmark button
        binding.btnBookmark.setOnClickListener(v ->
                Toast.makeText(this, "Added to Bookmarks", Toast.LENGTH_SHORT).show());

        // Share button
        binding.btnShare.setOnClickListener(v -> shareFilm());

        // Upload image button
        binding.btnUploadImage.setOnClickListener(v -> openImagePicker());
    }

    private void openImagePicker() {
        imagePickerLauncher.launch("image/*");
    }

    private void uploadImageToCloudinary(Uri imageUri) {
        binding.progressBar.setVisibility(View.VISIBLE);

        MediaManager.get()
                .upload(imageUri)
                .unsigned("xoxo_cloud") // Your unsigned upload preset
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d("Cloudinary", "Upload started");
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        int progress = (int) ((bytes * 100) / totalBytes);
                        binding.progressBar.setProgress(progress);
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        binding.progressBar.setVisibility(View.GONE);
                        String imageUrl = (String) resultData.get("url");
                        if (imageUrl != null) {
                            updateFilmImage(imageUrl);
                            Glide.with(DetailActivity.this)
                                    .load(imageUrl)
                                    .into(binding.imagePoster);
                            Toast.makeText(DetailActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(DetailActivity.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(DetailActivity.this, "Upload rescheduled", Toast.LENGTH_SHORT).show();
                    }
                })
                .dispatch();
    }

    private void updateFilmImage(String imageUrl) {
        db.collection("films").document(currentFilm.getId())
                .update("imageUrl", imageUrl)
                .addOnSuccessListener(aVoid -> {
                    currentFilm.setImageUrl(imageUrl);
                    Toast.makeText(this, "Image updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update image", Toast.LENGTH_SHORT).show();
                });
    }

    private void checkIfFavorite() {
        db.collection("users").document(userId).collection("favorites")
                .document(currentFilm.getId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        isFavorite = task.getResult().exists();
                        updateFavoriteButton();
                    }
                });
    }

    private void toggleFavorite() {
        isFavorite = !isFavorite;

        if (isFavorite) {
            db.collection("users").document(userId).collection("favorites")
                    .document(currentFilm.getId())
                    .set(new HashMap<>())
                    .addOnSuccessListener(aVoid -> {
                        updateFavoriteButton();
                        Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT).show();
                    });
        } else {
            db.collection("users").document(userId).collection("favorites")
                    .document(currentFilm.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        updateFavoriteButton();
                        Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateFavoriteButton() {
        binding.btnFav.setImageResource(
                isFavorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite
        );
    }

    private void shareFilm() {
        String shareText = "Check out this movie: " + currentFilm.getTitle() +
                "\n\n" + currentFilm.getDesc() +
                "\n\nAvailable at: " + currentFilm.getBioskop();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share Movie"));
    }
}