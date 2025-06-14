package com.example.xoxo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xoxo.bioskop.BioskopActivity;
import com.example.xoxo.databinding.ActivityFilmBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FilmActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityFilmBinding binding;
    private FirebaseFirestore db;
    private FilmAdapter filmAdapter;
    private List<Film> filmList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFilmBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inisialisasi FirebaseFirestore
        db = FirebaseFirestore.getInstance();

        // Initialize navbar click listeners
        binding.home.setOnClickListener(this);
        binding.film.setOnClickListener(this);
        binding.bioskop.setOnClickListener(this);
        binding.profile.setOnClickListener(this);
        binding.ivProfile.setOnClickListener(this);

        // Setup RecyclerView
        setupRecyclerView();
        loadFilmsFromFirestore();
    }

    private void setupRecyclerView() {
        filmAdapter = new FilmAdapter(filmList);
        binding.rvFilm.setLayoutManager(new LinearLayoutManager(this));
        binding.rvFilm.setAdapter(filmAdapter);
    }

    private void loadFilmsFromFirestore() {
        db.collection("films")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        filmList.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Film film = document.toObject(Film.class);
                                film.setId(document.getId());
                                filmList.add(film);
                            } catch (Exception e) {
                                Log.e("FirestoreError", "Error parsing film data", e);
                            }
                        }

                        // Update adapter setelah data berubah
                        filmAdapter.notifyDataSetChanged();

                    } else {
                        Toast.makeText(this, "Gagal memuat film: " +
                                        (task.getException() != null ?
                                                task.getException().getMessage() : "Unknown error"),
                                Toast.LENGTH_SHORT).show();
                        Log.e("FirestoreError", "Error loading films", task.getException());
                    }
                });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.home) {
            startActivity(new Intent(this, HomeActivity.class));
            overridePendingTransition(0, 0);
            finish();
        } else if (id == R.id.film) {
            Toast.makeText(this, "Anda sedang di halaman film.", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.bioskop) {
            startActivity(new Intent(this, BioskopActivity.class));
            overridePendingTransition(0, 0);
            finish();
        } else if (id == R.id.profile || id == R.id.ivProfile) {
            startActivity(new Intent(this, ProfileActivity.class));
            overridePendingTransition(0, 0);
            finish();
        }
    }
}