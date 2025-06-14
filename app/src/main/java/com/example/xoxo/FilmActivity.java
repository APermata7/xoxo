package com.example.xoxo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.xoxo.bioskop.BioskopActivity;
import com.example.xoxo.databinding.ActivityFilmBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FilmActivity extends AppCompatActivity implements View.OnClickListener, FilmAdapter.OnFilmActionListener {

    private ActivityFilmBinding binding;
    private FirebaseFirestore db;
    private FilmAdapter filmAdapter;
    private List<Film> filmList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFilmBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize FirebaseFirestore
        db = FirebaseFirestore.getInstance();

        // Initialize navbar click listeners
        binding.home.setOnClickListener(this);
        binding.film.setOnClickListener(this);
        binding.bioskop.setOnClickListener(this);
        binding.profile.setOnClickListener(this);
        binding.ivProfile.setOnClickListener(this);

        // Setup FAB for adding new film
        binding.fabAddChallenge.setOnClickListener(v -> {
            Intent intent = new Intent(FilmActivity.this, FilmFormActivity.class);
            startActivity(intent);
        });

        // Setup RecyclerView
        setupRecyclerView();
        loadFilmsFromFirestore();
    }

    private void setupRecyclerView() {
        filmAdapter = new FilmAdapter(filmList, this);
        binding.rvFilm.setLayoutManager(new LinearLayoutManager(this));
        binding.rvFilm.setAdapter(filmAdapter);
    }

    private void loadFilmsFromFirestore() {
        binding.progressBar.setVisibility(View.VISIBLE);

        db.collection("films")
                .get()
                .addOnCompleteListener(task -> {
                    binding.progressBar.setVisibility(View.GONE);

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

    @Override
    public void onEditFilm(Film film) {
        Intent intent = new Intent(this, FilmFormActivity.class);
        intent.putExtra("film", film);
        startActivity(intent);
    }

    @Override
    public void onDeleteFilm(Film film) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Film")
                .setMessage("Apakah Anda yakin ingin menghapus film ini?")
                .setPositiveButton("Ya", (dialog, which) -> deleteFilm(film))
                .setNegativeButton("Tidak", null)
                .show();
    }

    private void deleteFilm(Film film) {
        binding.progressBar.setVisibility(View.VISIBLE);

        db.collection("films")
                .document(film.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Film berhasil dihapus", Toast.LENGTH_SHORT).show();
                    loadFilmsFromFirestore(); // Refresh the list
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Gagal menghapus film: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFilmsFromFirestore(); // Refresh data when returning from form activity
    }
}