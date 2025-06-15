package com.example.xoxo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseAuth;
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
    private static final int REQUEST_VIEW_DETAIL = 102;
    private static final int RESULT_FAVORITE_CHANGED = 201;
    private static final int RESULT_CINEMA_EDITED = 202;
    private static final int RESULT_CINEMA_DELETED = 203;
    private static final String TAG = "BioskopActivity";

    private Spinner spinnerCity;
    private RecyclerView recyclerView;
    private BioskopAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FloatingActionButton fabAdd;
    private BioskopManager bioskopManager;
    private boolean canEditCinemas = true;

    private Map<String, List<Bioskop>> bioskopPerKota = new HashMap<>();
    private String currentCity;
    private String username;
    private String email;
    private String userId;
    private View progressBar;
    private View tvNoData;
    private boolean dataLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bioskop);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        bioskopManager = new BioskopManager(this);

        username = getIntent().getStringExtra("USERNAME");
        email = getIntent().getStringExtra("EMAIL");
        userId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Cinemas");

        spinnerCity = findViewById(R.id.spinnerCity);
        recyclerView = findViewById(R.id.recyclerViewBioskop);
        fabAdd = findViewById(R.id.fabAddCinema);
        progressBar = findViewById(R.id.progressBar);
        tvNoData = findViewById(R.id.tvNoData);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        checkUserAuthentication();

        spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentCity = parent.getItemAtPosition(position).toString();
                Log.d(TAG, "City selected: " + currentCity);
                updateCinemaList(currentCity);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(BioskopActivity.this, BioskopFormActivity.class);
            intent.putExtra("USERNAME", username);
            startActivityForResult(intent, REQUEST_ADD_CINEMA);
        });

        loadCinemasFromFirestore();
    }

    private void checkUserAuthentication() {
        if (mAuth.getCurrentUser() != null) {
            canEditCinemas = true;
            fabAdd.setVisibility(View.VISIBLE);
        } else {
            canEditCinemas = false;
            fabAdd.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (data == null && (requestCode == REQUEST_ADD_CINEMA || requestCode == REQUEST_EDIT_CINEMA)
                && resultCode == RESULT_OK) {
            Log.d(TAG, "Result received without data, refreshing from Firestore");
            loadCinemasFromFirestore();
            return;
        }

        if (data == null) {
            Log.d(TAG, "onActivityResult: data is null");
            return;
        }

        if (requestCode == REQUEST_VIEW_DETAIL) {
            String cinemaId = data.getStringExtra("CINEMA_ID");
            String action = data.getStringExtra("ACTION");

            Log.d(TAG, "Detail result - cinemaId: " + cinemaId + ", action: " + action);

            if (resultCode == RESULT_FAVORITE_CHANGED) {
                boolean isFavorite = data.getBooleanExtra("IS_FAVORITE", false);
                updateFavoriteStatusInList(cinemaId, isFavorite);
                Log.d(TAG, "Updated favorite status for " + cinemaId + ": " + isFavorite);
            }
            else if (resultCode == RESULT_CINEMA_EDITED) {
                Bioskop updatedCinema = (Bioskop) data.getSerializableExtra("UPDATED_CINEMA");
                if (updatedCinema != null) {
                    updateCinemaInList(updatedCinema);
                    Log.d(TAG, "Updated cinema in list: " + updatedCinema.getNama());
                } else {
                    Log.d(TAG, "Couldn't get updated cinema object, refreshing data from Firestore");
                    loadCinemasFromFirestore();
                }
            }
            else if (resultCode == RESULT_CINEMA_DELETED) {
                Log.d(TAG, "Cinema was deleted, refreshing data from Firestore");
                loadCinemasFromFirestore();
            }
        }
        else if ((requestCode == REQUEST_ADD_CINEMA || requestCode == REQUEST_EDIT_CINEMA)
                && resultCode == RESULT_OK) {
            Log.d(TAG, "Cinema added/edited directly, refreshing data from Firestore");
            loadCinemasFromFirestore();
        }
    }

    private void updateFavoriteStatusInList(String cinemaId, boolean isFavorite) {
        boolean found = false;
        for (List<Bioskop> cityBioskops : bioskopPerKota.values()) {
            for (Bioskop bioskop : cityBioskops) {
                if (bioskop.getId().equals(cinemaId)) {
                    bioskop.setFavorite(isFavorite);
                    found = true;
                    Log.d(TAG, "Found and updated favorite status in list for: " + cinemaId);
                }
            }
        }

        if (found && adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void loadCinemasFromFirestore() {
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Loading cinemas from Firestore...");

        db.collection("cinemas")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        bioskopPerKota.clear();

                        int totalCinemas = task.getResult().size();
                        Log.d(TAG, "Retrieved " + totalCinemas + " cinemas from Firestore");

                        db.collection("users").document(userId).collection("favorites")
                                .get()
                                .addOnCompleteListener(favTask -> {
                                    Map<String, Boolean> favoritesMap = new HashMap<>();
                                    if (favTask.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : favTask.getResult()) {
                                            favoritesMap.put(document.getId(), true);
                                        }
                                        Log.d(TAG, "User has " + favoritesMap.size() + " favorites");
                                    } else {
                                        Log.e(TAG, "Error getting favorites", favTask.getException());
                                    }

                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Bioskop bioskop = document.toObject(Bioskop.class);
                                        bioskop.setId(document.getId());

                                        if (favoritesMap.containsKey(bioskop.getId())) {
                                            bioskop.setFavorite(true);
                                        }

                                        String city = bioskop.getCity();
                                        if (city == null || city.isEmpty()) {
                                            city = "Other";
                                            bioskop.setCity(city);
                                        }

                                        if (!bioskopPerKota.containsKey(city)) {
                                            bioskopPerKota.put(city, new ArrayList<>());
                                        }
                                        bioskopPerKota.get(city).add(bioskop);
                                        Log.d(TAG, "Added cinema: " + bioskop.getNama() + " to city: " + city);
                                    }

                                    dataLoaded = true;
                                    updateCitySpinner();
                                });
                    } else {
                        Toast.makeText(this, "Gagal memuat data bioskop", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error loading cinemas", task.getException());
                    }
                });
    }

    private void updateCitySpinner() {
        List<String> cities = new ArrayList<>(bioskopPerKota.keySet());
        Log.d(TAG, "Updating city spinner with " + cities.size() + " cities");

        if (cities.isEmpty()) {
            tvNoData.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);

            cities.add("Jakarta");
            cities.add("Bandung");
            cities.add("Surabaya");
            Log.d(TAG, "No cities found, added default cities");
        } else {
            tvNoData.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, cities);
        spinnerCity.setAdapter(cityAdapter);

        if (currentCity != null && cities.contains(currentCity)) {
            int position = cities.indexOf(currentCity);
            spinnerCity.setSelection(position);
            Log.d(TAG, "Selected existing city: " + currentCity);
        } else if (!cities.isEmpty()) {
            currentCity = cities.get(0);
            spinnerCity.setSelection(0);
            Log.d(TAG, "Selected first city: " + currentCity);
        }

        updateCinemaList(currentCity);
    }

    private void updateCinemaInList(Bioskop updatedCinema) {
        Log.d(TAG, "Updating cinema in list: " + updatedCinema.getNama() + " in city: " + updatedCinema.getCity());

        boolean found = false;
        boolean cityChanged = false;
        String oldCity = null;

        for (Map.Entry<String, List<Bioskop>> entry : bioskopPerKota.entrySet()) {
            List<Bioskop> cityBioskops = entry.getValue();
            for (int i = 0; i < cityBioskops.size(); i++) {
                Bioskop bioskop = cityBioskops.get(i);
                if (bioskop.getId().equals(updatedCinema.getId())) {
                    oldCity = entry.getKey();
                    Log.d(TAG, "Found cinema in city: " + oldCity);

                    if (!oldCity.equals(updatedCinema.getCity())) {
                        cityChanged = true;
                        Log.d(TAG, "City changed from " + oldCity + " to " + updatedCinema.getCity());
                    } else {
                        cityBioskops.set(i, updatedCinema);
                        found = true;
                        Log.d(TAG, "Updated cinema in place");
                    }
                    break;
                }
            }
            if (found || cityChanged) break;
        }

        if (cityChanged && oldCity != null) {
            List<Bioskop> oldCityList = bioskopPerKota.get(oldCity);
            if (oldCityList != null) {
                for (int i = 0; i < oldCityList.size(); i++) {
                    if (oldCityList.get(i).getId().equals(updatedCinema.getId())) {
                        oldCityList.remove(i);
                        Log.d(TAG, "Removed cinema from old city: " + oldCity);
                        break;
                    }
                }

                if (oldCityList.isEmpty()) {
                    bioskopPerKota.remove(oldCity);
                    Log.d(TAG, "Removed empty city: " + oldCity);
                    updateCitySpinner();
                    return;
                }
            }

            String newCity = updatedCinema.getCity();
            if (!bioskopPerKota.containsKey(newCity)) {
                bioskopPerKota.put(newCity, new ArrayList<>());
                Log.d(TAG, "Created new city entry: " + newCity);
            }
            bioskopPerKota.get(newCity).add(updatedCinema);
            Log.d(TAG, "Added cinema to new city: " + newCity);

            updateCitySpinner();
        }
        else if (found) {
            if (adapter != null) {
                adapter.notifyDataSetChanged();
                Log.d(TAG, "Notified adapter of changes");
            }
        }
        else {
            Log.d(TAG, "Cinema not found or city handling issue, reloading from Firestore");
            loadCinemasFromFirestore();
        }
    }

    private void updateCinemaList(String city) {
        if (city == null) {
            Log.d(TAG, "updateCinemaList: city is null");
            return;
        }

        Log.d(TAG, "Updating cinema list for city: " + city);

        if (bioskopPerKota.containsKey(city)) {
            List<Bioskop> bioskops = bioskopPerKota.get(city);
            Log.d(TAG, "Found " + bioskops.size() + " cinemas for city: " + city);

            adapter = new BioskopAdapter(bioskops, this, canEditCinemas);
            recyclerView.setAdapter(adapter);

            if (bioskops.isEmpty()) {
                tvNoData.setVisibility(View.VISIBLE);
                Log.d(TAG, "No cinemas for city, showing empty message");
            } else {
                tvNoData.setVisibility(View.GONE);
            }
        } else {
            Log.d(TAG, "No city entry found for: " + city);
            adapter = new BioskopAdapter(new ArrayList<>(), this, canEditCinemas);
            recyclerView.setAdapter(adapter);
            tvNoData.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onFavoriteChanged(Bioskop bioskop, boolean isFavorite) {
        if (isFavorite) {
            db.collection("users").document(userId).collection("favorites")
                    .document(bioskop.getId())
                    .set(new HashMap<String, Object>())
                    .addOnSuccessListener(aVoid -> {
                        bioskop.setFavorite(true);
                        adapter.notifyDataSetChanged();
                        Log.d(TAG, "Added to favorites: " + bioskop.getId());
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Gagal menyimpan favorit", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Failed to add favorite", e);
                    });
        } else {
            db.collection("users").document(userId).collection("favorites")
                    .document(bioskop.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        bioskop.setFavorite(false);
                        adapter.notifyDataSetChanged();
                        Log.d(TAG, "Removed from favorites: " + bioskop.getId());
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Gagal menghapus favorit", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Failed to remove favorite", e);
                    });
        }
    }

    @Override
    public void onEditCinema(Bioskop bioskop) {
        Intent intent = new Intent(this, BioskopFormActivity.class);
        intent.putExtra("CINEMA_EDIT", bioskop);
        intent.putExtra("USERNAME", username);
        startActivityForResult(intent, REQUEST_EDIT_CINEMA);
    }

    @Override
    public void onDeleteCinema(Bioskop bioskop) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Cinema")
                .setMessage("Are you sure you want to delete " + bioskop.getNama() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    bioskopManager.deleteBioskop(bioskop, userId, username,
                            new BioskopManager.BioskopOperationCallback() {
                                @Override
                                public void onSuccess(Bioskop deletedBioskop) {
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
        Log.d(TAG, "onItemClick: " + bioskop.getNama());

        Intent intent = new Intent(this, BioskopDetailActivity.class);
        intent.putExtra("CINEMA_ID", bioskop.getId());
        intent.putExtra("CINEMA_NAME", bioskop.getNama());
        intent.putExtra("CINEMA_ADDRESS", bioskop.getAddress());
        intent.putExtra("CINEMA_INFO", bioskop.getInfo());
        intent.putExtra("CINEMA_PHONE", bioskop.getPhoneNumber());
        intent.putExtra("CINEMA_IMAGE", bioskop.getImageUrl());
        intent.putExtra("IS_FAVORITE", bioskop.isFavorite());
        intent.putExtra("CAN_EDIT", canEditCinemas);
        intent.putExtra("USERNAME", username);
        intent.putExtra("CITY", bioskop.getCity()); // Add city for completeness

        startActivityForResult(intent, REQUEST_VIEW_DETAIL);
    }

    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.home) {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra("USERNAME", username);
            intent.putExtra("EMAIL", email);
            startActivity(intent);
            overridePendingTransition(0, 0);
        } else if (id == R.id.film) {
            Intent intent = new Intent(this, FilmActivity.class);
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

    @Override
    protected void onResume() {
        super.onResume();
        if (dataLoaded && mAuth.getCurrentUser() != null) {
            updateFavoritesOnly();
        }
    }

    private void updateFavoritesOnly() {
        if (userId.isEmpty()) return;

        db.collection("users").document(userId).collection("favorites")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (List<Bioskop> cityBioskops : bioskopPerKota.values()) {
                        for (Bioskop bioskop : cityBioskops) {
                            bioskop.setFavorite(false);
                        }
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String cinemaId = document.getId();
                        updateFavoriteStatusInList(cinemaId, true);
                    }

                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error updating favorites", e));
    }
}