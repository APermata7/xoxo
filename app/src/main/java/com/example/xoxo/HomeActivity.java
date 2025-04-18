package com.example.praktik;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements HomeAdapter.OnFavoriteChangeListener {
    private RecyclerView rvFilm, rvFavorite;
    private HomeAdapter filmAdapter, favoriteAdapter;
    private List<Film> allFilms = new ArrayList<>();
    private List<Film> favoriteFilms = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize views
        rvFilm = findViewById(R.id.rvFilm);
        rvFavorite = findViewById(R.id.rvFavorite);

        if (savedInstanceState != null) {
            // Pulihkan daftar favorit
            favoriteFilms = (ArrayList<Film>) savedInstanceState.getSerializable("favorite_films");

            // Pulihkan status favorit di semua film
            for (Film film : allFilms) {
                boolean isFavorite = savedInstanceState.getBoolean("film_" + film.getId(), false);
                film.setFavorite(isFavorite);
            }
        }

        // Setup RecyclerViews
        setupRecyclerViews();

        // Load dummy data
        loadDummyData();
    }

    private void setupRecyclerViews() {
        // Inisialisasi adapter dengan list yang sudah ada
        filmAdapter = new HomeAdapter(new ArrayList<>(allFilms), false, this);
        favoriteAdapter = new HomeAdapter(new ArrayList<>(favoriteFilms), true, this);

        rvFilm.setLayoutManager(new LinearLayoutManager(this));
        rvFilm.setAdapter(filmAdapter);

        rvFavorite.setLayoutManager(new LinearLayoutManager(this));
        rvFavorite.setAdapter(favoriteAdapter);
    }

    private void loadDummyData() {
        // Add your dummy data here
        allFilms.add(new Film(1, "The Girl From the Other Side", "CINEPOLIS MATOS", "Rp100.000", R.drawable.film1));
        allFilms.add(new Film(2, "Kiki's Delivery Service", "CGV CINEMAS", "Rp120.000", R.drawable.film2));
        allFilms.add(new Film(3, "My Neighbor Totoro", "XXI THEATER", "Rp110.000", R.drawable.film3));
        allFilms.add(new Film(4, "Spirited Away", "XXI TRANSMART", "Rp130.000", R.drawable.film4));
        filmAdapter.updateFilms(allFilms);;
    }

    @Override
    public void onFavoriteChanged(Film film, boolean isFavorite) {
        runOnUiThread(() -> {
            film.setFavorite(isFavorite);

            if (isFavorite) {
                if (!favoriteFilms.contains(film)) {
                    favoriteFilms.add(film);
                    favoriteAdapter.updateFilms(favoriteFilms);
                }
            } else {
                favoriteFilms.remove(film);
                favoriteAdapter.updateFilms(favoriteFilms);
            }

            // Update film list
            filmAdapter.updateFilms(allFilms);
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Simpan daftar favorit
        outState.putSerializable("favorite_films", new ArrayList<>(favoriteFilms));
        // Simpan status favorit di semua film
        for (Film film : allFilms) {
            outState.putBoolean("film_" + film.getId(), film.isFavorite());
        }
    }

    // Handle click events (if needed)
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.home)
            Toast.makeText(this, "Anda sudah di home.", Toast.LENGTH_SHORT).show();
        else if (id == R.id.bioskop)
            Toast.makeText(this, "Halaman belum ada", Toast.LENGTH_SHORT).show();
        else if (id == R.id.profile || id == R.id.ivProfile)
            Toast.makeText(this, "Halaman belum ada", Toast.LENGTH_SHORT).show();
        else if (id == R.id.filmImage || id == R.id.filmTitle || id == R.id.filmBioskop || id == R.id.filmHarga)
            Toast.makeText(this, "Halaman belum ada", Toast.LENGTH_SHORT).show();
    }
}
