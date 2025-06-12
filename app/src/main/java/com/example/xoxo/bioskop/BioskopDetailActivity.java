package com.example.xoxo.bioskop;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.xoxo.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;

import java.util.HashMap;

public class BioskopDetailActivity extends AppCompatActivity {

    private static final int REQUEST_EDIT_CINEMA = 101;

    private TextView tvCinemaName, tvCinemaAddress, tvCinemaInfo, tvCinemaPhone;
    private ImageView btnFavoriteDetail, imgCinema;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String cinemaId;
    private boolean isFavorite;
    private boolean isAdmin;
    private Bioskop currentBioskop;
    private BioskopAdminManager adminManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bioskop_detail);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        adminManager = new BioskopAdminManager(this);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        tvCinemaName = findViewById(R.id.tvCinemaName);
        tvCinemaAddress = findViewById(R.id.tvCinemaAddress);
        tvCinemaInfo = findViewById(R.id.tvCinemaInfo);
        tvCinemaPhone = findViewById(R.id.tvCinemaPhone);
        btnFavoriteDetail = findViewById(R.id.btnFavoriteDetail);
        imgCinema = findViewById(R.id.imgCinema);

        // Get data from intent
        cinemaId = getIntent().getStringExtra("CINEMA_ID");
        String cinemaName = getIntent().getStringExtra("CINEMA_NAME");
        String cinemaAddress = getIntent().getStringExtra("CINEMA_ADDRESS");
        String cinemaInfo = getIntent().getStringExtra("CINEMA_INFO");
        String cinemaPhone = getIntent().getStringExtra("CINEMA_PHONE");
        String cinemaImage = getIntent().getStringExtra("CINEMA_IMAGE");
        isFavorite = getIntent().getBooleanExtra("IS_FAVORITE", false);
        isAdmin = getIntent().getBooleanExtra("IS_ADMIN", false);

        // Create Bioskop object for edit purposes
        currentBioskop = new Bioskop();
        currentBioskop.setId(cinemaId);
        currentBioskop.setNama(cinemaName);
        currentBioskop.setAddress(cinemaAddress);
        currentBioskop.setInfo(cinemaInfo);
        currentBioskop.setPhoneNumber(cinemaPhone);
        currentBioskop.setImageUrl(cinemaImage);
        currentBioskop.setFavorite(isFavorite);

        // Set data to views
        tvCinemaName.setText(cinemaName);
        tvCinemaAddress.setText(cinemaAddress);
        tvCinemaInfo.setText(cinemaInfo);
        if (cinemaPhone != null && !cinemaPhone.isEmpty()) {
            tvCinemaPhone.setText(cinemaPhone);
            findViewById(R.id.layoutPhone).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.layoutPhone).setVisibility(View.GONE);
        }

        // Load image if available
        if (cinemaImage != null && !cinemaImage.isEmpty()) {
            imgCinema.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(cinemaImage)
                    .placeholder(R.drawable.ic_cinema_placeholder)
                    .error(R.drawable.ic_cinema_placeholder)
                    .into(imgCinema);
        } else {
            imgCinema.setVisibility(View.GONE);
        }

        updateFavoriteIcon(isFavorite);

        btnFavoriteDetail.setOnClickListener(v -> toggleFavorite());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isAdmin) {
            getMenuInflater().inflate(R.menu.menu_cinema_detail, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_edit) {
            Intent intent = new Intent(this, BioskopFormActivity.class);
            intent.putExtra("CINEMA_EDIT", currentBioskop);
            startActivityForResult(intent, REQUEST_EDIT_CINEMA);
            return true;
        } else if (id == R.id.action_delete) {
            confirmDelete();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_CINEMA && resultCode == RESULT_OK) {
            // Refresh data
            loadCinemaData();
        }
    }

    private void loadCinemaData() {
        db.collection("cinemas").document(cinemaId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Bioskop bioskop = documentSnapshot.toObject(Bioskop.class);
                        bioskop.setId(documentSnapshot.getId());

                        // Update UI
                        tvCinemaName.setText(bioskop.getNama());
                        tvCinemaAddress.setText(bioskop.getAddress());
                        tvCinemaInfo.setText(bioskop.getInfo());

                        if (bioskop.getPhoneNumber() != null && !bioskop.getPhoneNumber().isEmpty()) {
                            tvCinemaPhone.setText(bioskop.getPhoneNumber());
                            findViewById(R.id.layoutPhone).setVisibility(View.VISIBLE);
                        } else {
                            findViewById(R.id.layoutPhone).setVisibility(View.GONE);
                        }

                        // Load image if available
                        if (bioskop.getImageUrl() != null && !bioskop.getImageUrl().isEmpty()) {
                            imgCinema.setVisibility(View.VISIBLE);
                            Glide.with(this)
                                    .load(bioskop.getImageUrl())
                                    .placeholder(R.drawable.ic_cinema_placeholder)
                                    .error(R.drawable.ic_cinema_placeholder)
                                    .into(imgCinema);
                        } else {
                            imgCinema.setVisibility(View.GONE);
                        }

                        // Update current bioskop object
                        currentBioskop = bioskop;
                        currentBioskop.setFavorite(isFavorite);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load cinema data", Toast.LENGTH_SHORT).show();
                });
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Cinema")
                .setMessage("Are you sure you want to delete " + currentBioskop.getNama() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    adminManager.deleteBioskop(currentBioskop, new BioskopAdminManager.BioskopOperationCallback() {
                        @Override
                        public void onSuccess(Bioskop deletedBioskop) {
                            finish();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(BioskopDetailActivity.this,
                                    "Failed to delete: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void toggleFavorite() {
        isFavorite = !isFavorite;
        updateFavoriteIcon(isFavorite);

        String userId = mAuth.getCurrentUser().getUid();
        if (isFavorite) {
            db.collection("users").document(userId).collection("favorites")
                    .document(cinemaId)
                    .set(new HashMap<String, Object>());
        } else {
            db.collection("users").document(userId).collection("favorites")
                    .document(cinemaId)
                    .delete();
        }
    }

    private void updateFavoriteIcon(boolean isFavorite) {
        btnFavoriteDetail.setImageResource(isFavorite ?
                R.drawable.ic_star_filled : R.drawable.ic_star);
    }
}