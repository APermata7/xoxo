package com.example.xoxo;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import android.app.AlertDialog;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;

public class BioskopDetailActivity extends AppCompatActivity {

    private static final int REQUEST_EDIT_CINEMA = 101;
    private static final int RESULT_FAVORITE_CHANGED = 201;
    private static final int RESULT_CINEMA_EDITED = 202;
    private static final int RESULT_CINEMA_DELETED = 203;

    private static final String TAG = "BioskopDetail";

    private TextView tvCinemaName, tvCinemaAddress, tvCinemaInfo, tvCinemaPhone;
    private ImageView btnFavoriteDetail, imgCinema;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String cinemaId;
    private boolean isFavorite;
    private boolean canEditCinemas;
    private Bioskop currentBioskop;
    private BioskopManager bioskopManager;
    private String username;
    private String userId;
    private boolean cinemaWasEdited = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bioskop_detail);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        bioskopManager = new BioskopManager(this);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        userId = currentUser != null ? currentUser.getUid() : "";

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

        cinemaId = getIntent().getStringExtra("CINEMA_ID");
        String cinemaName = getIntent().getStringExtra("CINEMA_NAME");
        String cinemaAddress = getIntent().getStringExtra("CINEMA_ADDRESS");
        String cinemaInfo = getIntent().getStringExtra("CINEMA_INFO");
        String cinemaPhone = getIntent().getStringExtra("CINEMA_PHONE");
        String cinemaImage = getIntent().getStringExtra("CINEMA_IMAGE");
        String cinemaCity = getIntent().getStringExtra("CITY");
        isFavorite = getIntent().getBooleanExtra("IS_FAVORITE", false);
        canEditCinemas = getIntent().getBooleanExtra("CAN_EDIT", false);
        username = getIntent().getStringExtra("USERNAME");

        if (username == null || username.isEmpty()) {
            username = currentUser.getDisplayName();
            if (username == null || username.isEmpty()) {
                username = currentUser.getEmail();
                if (username == null || username.isEmpty()) {
                    username = userId.substring(0, Math.min(8, userId.length()));
                }
            }
        }


        currentBioskop = new Bioskop();
        currentBioskop.setId(cinemaId);
        currentBioskop.setNama(cinemaName);
        currentBioskop.setAddress(cinemaAddress);
        currentBioskop.setInfo(cinemaInfo);
        currentBioskop.setPhoneNumber(cinemaPhone);
        currentBioskop.setImageUrl(cinemaImage);
        currentBioskop.setCity(cinemaCity);
        currentBioskop.setFavorite(isFavorite);

        Log.d(TAG, "Created current bioskop: " + currentBioskop.getNama() + " in city: " + currentBioskop.getCity());

        updateUIWithCinemaData(currentBioskop);

        btnFavoriteDetail.setOnClickListener(v -> toggleFavorite());
    }

    private void updateUIWithCinemaData(Bioskop bioskop) {
        tvCinemaName.setText(bioskop.getNama());
        tvCinemaAddress.setText(bioskop.getAddress());
        tvCinemaInfo.setText(bioskop.getInfo());

        if (bioskop.getPhoneNumber() != null && !bioskop.getPhoneNumber().isEmpty()) {
            tvCinemaPhone.setText(bioskop.getPhoneNumber());
            findViewById(R.id.layoutPhone).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.layoutPhone).setVisibility(View.GONE);
        }

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

        updateFavoriteIcon(bioskop.isFavorite());

        Log.d(TAG, "UI updated with cinema data: " + bioskop.getNama());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (canEditCinemas) {
            getMenuInflater().inflate(R.menu.menu_cinema_detail, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finishWithProperResult();
            return true;
        } else if (id == R.id.action_edit) {
            Intent intent = new Intent(this, BioskopFormActivity.class);
            intent.putExtra("CINEMA_EDIT", currentBioskop);
            intent.putExtra("USERNAME", username);
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
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_EDIT_CINEMA && resultCode == RESULT_OK) {
            Log.d(TAG, "Cinema was edited, refreshing data");
            cinemaWasEdited = true;
            loadCinemaData();
        }
    }

    private void loadCinemaData() {
        Log.d(TAG, "Loading cinema data for ID: " + cinemaId);
        db.collection("cinemas").document(cinemaId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Bioskop bioskop = documentSnapshot.toObject(Bioskop.class);
                        bioskop.setId(documentSnapshot.getId());
                        bioskop.setFavorite(isFavorite);

                        updateUIWithCinemaData(bioskop);
                        currentBioskop = bioskop;

                        Log.d(TAG, "Cinema data refreshed: " + bioskop.getNama() + " in city: " + bioskop.getCity());
                    } else {
                        Log.e(TAG, "Document does not exist for ID: " + cinemaId);
                        Toast.makeText(this, "Cinema no longer exists", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load cinema data", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to load cinema data", e);
                });
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Cinema")
                .setMessage("Are you sure you want to delete " + currentBioskop.getNama() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    bioskopManager.deleteBioskop(currentBioskop, userId, username,
                            new BioskopManager.BioskopOperationCallback() {
                                @Override
                                public void onSuccess(Bioskop deletedBioskop) {
                                    Intent resultIntent = new Intent();
                                    resultIntent.putExtra("CINEMA_ID", cinemaId);
                                    resultIntent.putExtra("ACTION", "DELETED");
                                    setResult(RESULT_CINEMA_DELETED, resultIntent);

                                    Log.d(TAG, "Cinema deleted successfully: " + cinemaId);
                                    finish();
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Toast.makeText(BioskopDetailActivity.this,
                                            "Failed to delete: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Failed to delete cinema", e);
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void toggleFavorite() {
        isFavorite = !isFavorite;
        updateFavoriteIcon(isFavorite);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "You need to be logged in to favorite cinemas", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        if (isFavorite) {
            db.collection("users").document(userId).collection("favorites")
                    .document(cinemaId)
                    .set(new HashMap<String, Object>())
                    .addOnSuccessListener(aVoid -> {
                        currentBioskop.setFavorite(true);
                        Log.d(TAG, "Added to favorites: " + cinemaId);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to add to favorites", Toast.LENGTH_SHORT).show();
                        isFavorite = false;
                        updateFavoriteIcon(isFavorite);
                        Log.e(TAG, "Failed to add to favorites", e);
                    });
        } else {
            db.collection("users").document(userId).collection("favorites")
                    .document(cinemaId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        currentBioskop.setFavorite(false);
                        Log.d(TAG, "Removed from favorites: " + cinemaId);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to remove from favorites", Toast.LENGTH_SHORT).show();
                        isFavorite = true;
                        updateFavoriteIcon(isFavorite);
                        Log.e(TAG, "Failed to remove from favorites", e);
                    });
        }
    }

    private void updateFavoriteIcon(boolean isFavorite) {
        btnFavoriteDetail.setImageResource(isFavorite ?
                R.drawable.ic_star_filled : R.drawable.ic_star);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishWithProperResult();
    }

    private void finishWithProperResult() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("CINEMA_ID", cinemaId);

        if (cinemaWasEdited) {
            resultIntent.putExtra("ACTION", "EDITED");
            resultIntent.putExtra("UPDATED_CINEMA", currentBioskop);
            setResult(RESULT_CINEMA_EDITED, resultIntent);
            Log.d(TAG, "Finishing with RESULT_CINEMA_EDITED: " + currentBioskop.getNama() + " in city: " + currentBioskop.getCity());
        }
        else if (isFavorite != getIntent().getBooleanExtra("IS_FAVORITE", false)) {
            resultIntent.putExtra("ACTION", "FAVORITE_CHANGED");
            resultIntent.putExtra("IS_FAVORITE", isFavorite);
            setResult(RESULT_FAVORITE_CHANGED, resultIntent);
            Log.d(TAG, "Finishing with RESULT_FAVORITE_CHANGED: " + isFavorite);
        } else {
            Log.d(TAG, "Finishing with no changes");
        }

        finish();
    }
}