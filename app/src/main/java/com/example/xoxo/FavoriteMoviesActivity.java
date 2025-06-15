package com.example.xoxo;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FavoriteMoviesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FavoriteMoviesAdapter adapter;
    private List<Film> favoriteMovies = new ArrayList<>();
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_movies);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize views
        recyclerView = findViewById(R.id.rvFilm);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FavoriteMoviesAdapter(favoriteMovies);
        recyclerView.setAdapter(adapter);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Load favorite movies
        loadFavoriteMovies();
    }

    private void loadFavoriteMovies() {
        db.collection("users").document(userId).collection("favorites")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        favoriteMovies.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String filmId = document.getId();
                            getFilmDetails(filmId);
                        }
                    } else {
                        Toast.makeText(this, "Gagal memuat film favorit", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getFilmDetails(String filmId) {
        db.collection("films").document(filmId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Film film = document.toObject(Film.class);
                        if (film != null) {
                            film.setId(document.getId());
                            favoriteMovies.add(film);
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
    }
}