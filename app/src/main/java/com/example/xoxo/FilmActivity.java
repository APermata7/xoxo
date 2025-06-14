package com.example.xoxo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xoxo.bioskop.BioskopActivity;
import com.example.xoxo.databinding.ActivityFilmBinding;

import java.util.ArrayList;
import java.util.List;

public class FilmActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityFilmBinding binding;
    private FilmAdapter filmAdapter;
    private List<Film> filmList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFilmBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize navbar click listeners
        binding.home.setOnClickListener(this);
        binding.film.setOnClickListener(this);
        binding.bioskop.setOnClickListener(this);
        binding.profile.setOnClickListener(this);

        // Initialize profile click listener
        binding.ivProfile.setOnClickListener(this);

        // Setup RecyclerView
        setupRecyclerView();
    }

    private void setupRecyclerView() {
        filmList.add(new Film(
                "The Girl From the Other Side",
                "Once upon a time...", // deskripsi lengkap
                "Rp100.000",
                "http://res.cloudinary.com/.../xsa1plqyzy0ii1wt0u7w.png",
                "Drama/Animation | 2019 | 10 minutes",
                "Teacher & Shiva",
                "Yutaro Kubo, Satomi Maiya"
        ));

        filmAdapter = new FilmAdapter(filmList);
        binding.rvFilm.setLayoutManager(new LinearLayoutManager(this));
        binding.rvFilm.setAdapter(filmAdapter);

    }

    @Override
    public void onClick(View view) { // Harus public dengan parameter View
        int id = view.getId();
        if (id == R.id.home) {
            startActivity(new Intent(this, HomeActivity.class));
            overridePendingTransition(0, 0);
            finish();
        } else if (id == R.id.film) {
            Toast.makeText(this, "You're already on films", Toast.LENGTH_SHORT).show();
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