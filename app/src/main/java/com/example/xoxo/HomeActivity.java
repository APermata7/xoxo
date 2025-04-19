package com.example.xoxo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.xoxo.databinding.ActivityHomeBinding;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements
        HomeAdapter.OnFavoriteChangeListener,
        HomeAdapter.OnFilmClickListener {

    private ActivityHomeBinding binding;
    private HomeAdapter filmAdapter, favoriteAdapter;
    private List<Film> allFilms = new ArrayList<>();
    private List<Film> favoriteFilms = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inisialisasi view menggunakan binding
        initializeViews();
        setupClickListeners();
        setupRecyclerViews();
        loadDummyData();

        // Restore state jika ada
        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }

        String username = getIntent().getStringExtra("USERNAME");
        String welcomeMessage = getString(R.string.welcome_message, username);
        binding.tvUsername.setText(welcomeMessage);
    }

    private void initializeViews() {
        // Tidak perlu findViewById karena menggunakan ViewBinding
        binding.rvFilm.setLayoutManager(new LinearLayoutManager(this));
        binding.rvFavorite.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupClickListeners() {
        binding.home.setOnClickListener(this::handleClick);
        binding.bioskop.setOnClickListener(this::handleClick);
        binding.profile.setOnClickListener(this::handleClick);
        binding.ivProfile.setOnClickListener(this::handleClick);
    }

    private void setupRecyclerViews() {
        filmAdapter = new HomeAdapter(allFilms, false, this, this);
        favoriteAdapter = new HomeAdapter(favoriteFilms, true, this, this);

        binding.rvFilm.setAdapter(filmAdapter);
        binding.rvFavorite.setAdapter(favoriteAdapter);
    }

    private void loadDummyData() {
        allFilms.clear();
        allFilms.add(new Film(1, "The Girl From the Other Side", "CINEPOLIS MATOS", "Rp100.000", R.drawable.film1));
        allFilms.add(new Film(2, "Kiki's Delivery Service", "CGV CINEMAS", "Rp120.000", R.drawable.film2));
        allFilms.add(new Film(3, "My Neighbor Totoro", "XXI THEATER", "Rp110.000", R.drawable.film3));
        allFilms.add(new Film(4, "Spirited Away", "XXI TRANSMART", "Rp130.000", R.drawable.film4));

        filmAdapter.updateFilms(allFilms);
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        // Restore favorite films
        List<Film> savedFavorites = (ArrayList<Film>) savedInstanceState.getSerializable("favorite_films");
        if (savedFavorites != null) {
            favoriteFilms.clear();
            favoriteFilms.addAll(savedFavorites);
        }

        // Restore favorite status for all films
        for (Film film : allFilms) {
            boolean isFavorite = savedInstanceState.getBoolean("film_" + film.getId(), false);
            film.setFavorite(isFavorite);
        }

        favoriteAdapter.updateFilms(favoriteFilms);
    }

    @Override
    public void onFavoriteChanged(Film film, boolean isFavorite) {
        runOnUiThread(() -> {
            film.setFavorite(isFavorite);

            if (isFavorite) {
                if (!favoriteFilms.contains(film)) {
                    favoriteFilms.add(film);
                }
            } else {
                favoriteFilms.remove(film);
            }

            favoriteAdapter.updateFilms(favoriteFilms);
            filmAdapter.updateFilms(allFilms);
        });
    }

    @Override
    public void onFilmClicked(Film film) {
        // Intent ke detail film
//        Intent intent = new Intent(this, FilmDetailActivity.class);
//        intent.putExtra("film_id", film.getId());
//        intent.putExtra("film_title", film.getTitle());
//        intent.putExtra("film_bioskop", film.getBioskop());
//        intent.putExtra("film_harga", film.getHarga());
//        intent.putExtra("film_image", film.getImageRes());
//        startActivity(intent);
    }

    private void handleClick(View view) {
        int id = view.getId();
        if (id == R.id.home) {
            Toast.makeText(this, "Anda sudah di home.", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.bioskop) {
            Toast.makeText(this, "Halaman belum ada", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.profile || id == R.id.ivProfile) {
            Toast.makeText(this, "Halaman belum ada", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("favorite_films", new ArrayList<>(favoriteFilms));

        for (Film film : allFilms) {
            outState.putBoolean("film_" + film.getId(), film.isFavorite());
        }
    }
}
