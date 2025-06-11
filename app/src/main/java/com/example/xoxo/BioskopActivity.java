package com.example.xoxo;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BioskopActivity extends AppCompatActivity implements BioskopAdapter.BioskopFavoriteListener {

    private Spinner spinnerCity;
    private RecyclerView recyclerView;
    private BioskopAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

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

        username = getIntent().getStringExtra("USERNAME");
        email = getIntent().getStringExtra("EMAIL");
        userId = mAuth.getCurrentUser().getUid();

        spinnerCity = findViewById(R.id.spinnerCity);
        recyclerView = findViewById(R.id.recyclerViewBioskop);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

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
    }

    private void loadCinemasFromFirestore() {
        // Ambil data bioskop dari Firestore
        db.collection("cinemas")
                .get()
                .addOnCompleteListener(task -> {
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
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, cities);
        spinnerCity.setAdapter(cityAdapter);

        if (currentCity != null && cities.contains(currentCity)) {
            int position = cities.indexOf(currentCity);
            spinnerCity.setSelection(position);
        }
    }

    private void updateCinemaList(String city) {
        if (bioskopPerKota.containsKey(city)) {
            List<Bioskop> bioskops = bioskopPerKota.get(city);
            adapter = new BioskopAdapter(bioskops, this);
            recyclerView.setAdapter(adapter);
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