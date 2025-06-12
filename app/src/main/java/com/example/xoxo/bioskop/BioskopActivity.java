package com.example.xoxo.bioskop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xoxo.HomeActivity;
import com.example.xoxo.ProfileActivity;
import com.example.xoxo.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class BioskopActivity extends AppCompatActivity implements BioskopAdapter.BioskopListener {

    private static final int REQUEST_ADD_CINEMA = 100;
    private static final int REQUEST_EDIT_CINEMA = 101;

    private Spinner spinnerCity;
    private RecyclerView recyclerView;
    private BioskopAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FloatingActionButton fabAdd;
    private BioskopAdminManager adminManager;
    private boolean isAdmin = false;

    private Map<String, List<Bioskop>> bioskopPerKota = new HashMap<>();
    private String currentCity;
    private String username;
    private String email;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bioskop);

        // Inisialisasi Firestore dan Auth
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        adminManager = new BioskopAdminManager(this);

        username = getIntent().getStringExtra("USERNAME");
        email = getIntent().getStringExtra("EMAIL");
        userId = mAuth.getCurrentUser().getUid();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Cinemas");

        spinnerCity = findViewById(R.id.spinnerCity);
        recyclerView = findViewById(R.id.recyclerViewBioskop);
        fabAdd = findViewById(R.id.fabAddCinema);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Check if user is admin
        checkAdminStatus();

        // Load data bioskop dari Firestore
        loadCinemasFromFirestore();

        // Set up spinner
        spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentCity = parent.getItemAtPosition(position).toString();
                updateCinemaList(currentCity);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Setup FAB
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(BioskopActivity.this, BioskopFormActivity.class);
            startActivityForResult(intent, REQUEST_ADD_CINEMA);
        });
    }

    private void checkAdminStatus() {
        adminManager.checkAdminAccess(userId, task -> {
            isAdmin = task;
            if (isAdmin) {
                fabAdd.setVisibility(View.VISIBLE);
            } else {
                fabAdd.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == REQUEST_ADD_CINEMA || requestCode == REQUEST_EDIT_CINEMA) && resultCode == RESULT_OK) {
            // Refresh cinema list
            loadCinemasFromFirestore();
        }
    }

    private void loadCinemasFromFirestore() {
        // Show loading indicator
        findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

        // Ambil data bioskop dari Firestore
        db.collection("cinemas")
                .get()
                .addOnCompleteListener(task -> {
                    findViewById(R.id.progressBar).setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        bioskopPerKota.clear();

                        // Ambil juga data favorit pengguna
                        db.collection("users").document(userId).collection("favorites")
                                .get()
                                .addOnCompleteListener(favTask -> {
                                    Map<String, Boolean> favoritesMap = new HashMap<>();
                                    if (favTask.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : favTask.getResult()) {
                                            favoritesMap.put(document.getId(), true);
                                        }
                                    }

                                    // Proses data bioskop
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Bioskop bioskop = document.toObject(Bioskop.class);
                                        bioskop.setId(document.getId());

                                        // Set status favorit jika ada di koleksi favorit pengguna
                                        if (favoritesMap.containsKey(bioskop.getId())) {
                                            bioskop.setFavorite(true);
                                        }

                                        String city = bioskop.getCity();
                                        if (!bioskopPerKota.containsKey(city)) {
                                            bioskopPerKota.put(city, new ArrayList<>());
                                        }
                                        bioskopPerKota.get(city).add(bioskop);
                                    }

                                    // Update spinner dengan kota yang tersedia
                                    updateCitySpinner();
                                });
                    } else {
                        Toast.makeText(this, "Gagal memuat data bioskop", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateCitySpinner() {
        List<String> cities = new ArrayList<>(bioskopPerKota.keySet());
        if (cities.isEmpty()) {
            // If no cinemas, show message
            findViewById(R.id.tvNoData).setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);

            // Add default cities to spinner
            cities.add("Jakarta");
            cities.add("Bandung");
            cities.add("Surabaya");
        } else {
            findViewById(R.id.tvNoData).setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, cities);
        spinnerCity.setAdapter(cityAdapter);

        if (currentCity != null && cities.contains(currentCity)) {
            int position = cities.indexOf(currentCity);
            spinnerCity.setSelection(position);
        } else if (!cities.isEmpty()) {
            // Select first city by default
            currentCity = cities.get(0);
            updateCinemaList(currentCity);
        }
    }

    private void updateCinemaList(String city) {
        if (bioskopPerKota.containsKey(city)) {
            List<Bioskop> bioskops = bioskopPerKota.get(city);
            adapter = new BioskopAdapter(bioskops, this, isAdmin);
            recyclerView.setAdapter(adapter);

            if (bioskops.isEmpty()) {
                findViewById(R.id.tvNoData).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.tvNoData).setVisibility(View.GONE);
            }
        } else {
            // If no cinemas for this city
            adapter = new BioskopAdapter(new ArrayList<>(), this, isAdmin);
            recyclerView.setAdapter(adapter);
            findViewById(R.id.tvNoData).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onFavoriteChanged(Bioskop bioskop, boolean isFavorite) {
        // Update status favorit di Firestore
        if (isFavorite) {
            // Tambahkan ke favorit
            db.collection("users").document(userId).collection("favorites")
                    .document(bioskop.getId())
                    .set(new HashMap<String, Object>())
                    .addOnSuccessListener(aVoid -> {
                        bioskop.setFavorite(true);
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Gagal menyimpan favorit", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Hapus dari favorit
            db.collection("users").document(userId).collection("favorites")
                    .document(bioskop.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        bioskop.setFavorite(false);
                        adapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Gagal menghapus favorit", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    public void onEditCinema(Bioskop bioskop) {
        Intent intent = new Intent(this, BioskopFormActivity.class);
        intent.putExtra("CINEMA_EDIT", bioskop);
        startActivityForResult(intent, REQUEST_EDIT_CINEMA);
    }

    @Override
    public void onDeleteCinema(Bioskop bioskop) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Cinema")
                .setMessage("Are you sure you want to delete " + bioskop.getNama() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    adminManager.deleteBioskop(bioskop, new BioskopAdminManager.BioskopOperationCallback() {
                        @Override
                        public void onSuccess(Bioskop deletedBioskop) {
                            // Refresh the data
                            loadCinemasFromFirestore();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(BioskopActivity.this,
                                    "Failed to delete: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onItemClick(Bioskop bioskop) {
        Intent intent = new Intent(this, BioskopDetailActivity.class);
        intent.putExtra("CINEMA_ID", bioskop.getId());
        intent.putExtra("CINEMA_NAME", bioskop.getNama());
        intent.putExtra("CINEMA_ADDRESS", bioskop.getAddress());
        intent.putExtra("CINEMA_INFO", bioskop.getInfo());
        intent.putExtra("CINEMA_PHONE", bioskop.getPhoneNumber());
        intent.putExtra("CINEMA_IMAGE", bioskop.getImageUrl());
        intent.putExtra("IS_FAVORITE", bioskop.isFavorite());
        intent.putExtra("IS_ADMIN", isAdmin);
        startActivity(intent);
    }

    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.home) {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra("USERNAME", username);
            intent.putExtra("EMAIL", email);
            startActivity(intent);
            overridePendingTransition(0, 0);
        } else if (id == R.id.bioskop) {
            Toast.makeText(this, "Kamu sedang di halaman Bioskop", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.profile || id == R.id.bioskop_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("USERNAME", username);
            intent.putExtra("EMAIL", email);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
    }
}