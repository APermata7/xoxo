package com.example.xoxo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.xoxo.databinding.ActivityDetailBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class DetailActivity extends AppCompatActivity {
    private ActivityDetailBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;
    private Film currentFilm;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

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