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

    private String username;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeViews();
        setupClickListeners();
        setupRecyclerViews();
        loadDummyData();

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }

        username = getIntent().getStringExtra("USERNAME");
        email = getIntent().getStringExtra("EMAIL");
        String welcomeMessage = getString(R.string.welcome_message, username);
        binding.tvUsername.setText(welcomeMessage);
    }

    private void initializeViews() {
        binding.rvFilm.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
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
        allFilms.add(new Film(1, "The Girl From the Other Side", "CINEPOLIS MATOS", "Rp100.000", R.drawable.film1, "The Girl from the Other Side: Siúil, a Rún is a Japanese manga series written and illustrated by Nagabe. It was serialized in Mag Garden's shōnen manga magazine Monthly Comic Garden from September 2015 to March 2021, with its chapters collected into eleven tankōbon volumes.", "Drama/Animation | 2019 | 10 minutes", "Teacher & Shiva", "Yutaro Kubo, Satomi Maiya"));
        allFilms.add(new Film(2, "Kiki's Delivery Service", "CGV CINEMAS", "Rp120.000", R.drawable.film2, "Thirteen-year-old Kiki tries to become an independent witch and gets a job at a delivery service. She wakes up one day to find that she can neither fly her broom nor talk to her cat.", "Family/Fantasy | 1989 | 1 hour 42 minutes", "Kiki", "Hayao Miyazaki"));
        allFilms.add(new Film(3, "My Neighbor Totoro", "XXI THEATER", "Rp110.000", R.drawable.film3, "Two sisters relocate to rural Japan with their father to spend time with their ill mother. They face a mythical forest sprite and its woodland friends with whom they have many magical adventures.", "Fantasy/Adventure | 1988 | 1 hour 26 minutes", "Totoro", "Hayao Miyazaki"));
        allFilms.add(new Film(4, "Spirited Away", "XXI TRANSMART", "Rp130.000", R.drawable.film4, "Ten-year-old Chihiro and her parents end up at an abandoned amusement park inhabited by supernatural beings. Soon, she learns that she must work to free her parents who have been turned into pigs.", "Fantasy/Adventure | 2001 | 2 hours 5 minutes", "Chihiro", "Hayao Miyazaki"));

        filmAdapter.updateFilms(allFilms);
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        List<Film> savedFavorites = (ArrayList<Film>) savedInstanceState.getSerializable("favorite_films");
        if (savedFavorites != null) {
            favoriteFilms.clear();
            favoriteFilms.addAll(savedFavorites);
        }

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
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("film_title", film.getTitle());
        intent.putExtra("film_image", film.getImageRes());
        intent.putExtra("film_desc", film.getDesc());
        intent.putExtra("film_info", film.getInfo());
        intent.putExtra("film_pemain", film.getPemain());
        intent.putExtra("film_sutradara", film.getSutradara());
        startActivity(intent);
    }

    private void handleClick(View view) {
        int id = view.getId();
        if (id == R.id.home) {
            Toast.makeText(this, "Anda sudah di home.", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.bioskop) {
            Intent intent = new Intent(this, BioskopActivity.class);
            intent.putExtra("USERNAME", username);
            intent.putExtra("EMAIL", email);
            startActivity(intent);
            overridePendingTransition(0, 0);
        } else if (id == R.id.profile || id == R.id.ivProfile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("USERNAME", username);
            intent.putExtra("EMAIL", email);
            startActivity(intent);
            overridePendingTransition(0, 0);
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
