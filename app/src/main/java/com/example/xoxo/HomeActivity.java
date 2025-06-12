package com.example.xoxo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements
        HomeAdapter.OnFavoriteChangeListener,
        HomeAdapter.OnFilmClickListener {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private HomeAdapter filmAdapter, favoriteAdapter;
    private List<Film> allFilms = new ArrayList<>();
    private List<Film> favoriteFilms = new ArrayList<>();
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        initializeViews();
        setupClickListeners();
        setupRecyclerViews();
        loadUserData();
        loadFilmsFromFirestore();
    }

    private void initializeViews() {
        RecyclerView rvFilm = findViewById(R.id.rvFilm);
        RecyclerView rvFavorite = findViewById(R.id.rvFavorite);

        // Horizontal layout for films
        LinearLayoutManager filmLayoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);
        rvFilm.setLayoutManager(filmLayoutManager);

        // Vertical layout for favorites
        rvFavorite.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupClickListeners() {
        findViewById(R.id.home).setOnClickListener(this::handleClick);
        findViewById(R.id.bioskop).setOnClickListener(this::handleClick);
        findViewById(R.id.profile).setOnClickListener(this::handleClick);
        findViewById(R.id.ivProfile).setOnClickListener(this::handleClick);
    }

    private void setupRecyclerViews() {
        filmAdapter = new HomeAdapter(allFilms, false, this, this);
        favoriteAdapter = new HomeAdapter(favoriteFilms, true, this, this);

        ((RecyclerView) findViewById(R.id.rvFilm)).setAdapter(filmAdapter);
        ((RecyclerView) findViewById(R.id.rvFavorite)).setAdapter(favoriteAdapter);
    }

    private void loadUserData() {
        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String username = document.getString("username");
                            if (username != null) {
                                String welcomeMessage = "Welcome, " + username + "!";
                                ((TextView) findViewById(R.id.tvUsername)).setText(welcomeMessage);
                            }
                        }
                    }
                });
    }

    private void loadFilmsFromFirestore() {
        db.collection("films")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        allFilms.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Film film = document.toObject(Film.class);
                            film.setId(document.getId());
                            allFilms.add(film);
                        }

                        loadFavoriteFilms();
                    } else {
                        Toast.makeText(this, "Failed to load films", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadFavoriteFilms() {
        db.collection("users").document(userId).collection("favorites")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        favoriteFilms.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String filmId = document.getId();
                            for (Film film : allFilms) {
                                if (film.getId().equals(filmId)) {
                                    film.setFavorite(true);
                                    favoriteFilms.add(film);
                                    break;
                                }
                            }
                        }

                        updateAdapters();
                    }
                });
    }

    private void updateAdapters() {
        filmAdapter.updateFilms(allFilms);
        favoriteAdapter.updateFilms(favoriteFilms);

        // Show/hide favorites section based on content
        findViewById(R.id.tvFavorite).setVisibility(
                favoriteFilms.isEmpty() ? View.GONE : View.VISIBLE
        );
    }

    @Override
    public void onFavoriteChanged(Film film, boolean isFavorite) {
        film.setFavorite(isFavorite);

        if (isFavorite) {
            // Add to favorites in Firestore
            db.collection("users").document(userId).collection("favorites")
                    .document(film.getId())
                    .set(new HashMap<>())
                    .addOnSuccessListener(aVoid -> {
                        if (!favoriteFilms.contains(film)) {
                            favoriteFilms.add(film);
                            updateAdapters();
                        }
                    });
        } else {
            // Remove from favorites in Firestore
            db.collection("users").document(userId).collection("favorites")
                    .document(film.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        favoriteFilms.remove(film);
                        updateAdapters();
                    });
        }
    }

    @Override
    public void onFilmClicked(Film film) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("film_id", film.getId());
        intent.putExtra("film_title", film.getTitle());
        intent.putExtra("film_bioskop", film.getBioskop());
        intent.putExtra("film_harga", film.getHarga());
        intent.putExtra("film_image_url", film.getImageUrl());
        intent.putExtra("film_desc", film.getDesc());
        intent.putExtra("film_info", film.getInfo());
        intent.putExtra("film_pemain", film.getPemain());
        intent.putExtra("film_sutradara", film.getSutradara());
        startActivity(intent);
    }

    private void handleClick(View view) {
        int id = view.getId();
        if (id == R.id.home) {
            Toast.makeText(this, "You're already on home", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.bioskop) {
            startActivity(new Intent(this, BioskopActivity.class));
            overridePendingTransition(0, 0);
        } else if (id == R.id.profile || id == R.id.ivProfile) {
            startActivity(new Intent(this, ProfileActivity.class));
            overridePendingTransition(0, 0);
        }
    }
}