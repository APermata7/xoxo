package com.example.xoxo;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xoxo.bioskop.BioskopActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private String userId;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ProfileAdapter profileAdapter;
    private List<String> favoriteMovies = new ArrayList<>();
    private TextView tvUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        // Find views
        tvUsername = findViewById(R.id.tvUsername);
        TextView tvEmail = findViewById(R.id.tvEmail);
        View menuPengaturan = findViewById(R.id.menuPengaturan);
        View menuFilmSaya = findViewById(R.id.menuFilmSaya);
        View menuLogout = findViewById(R.id.menuLogout);
        RecyclerView recyclerViewFavorites = findViewById(R.id.recyclerViewFavorites);

        // Setup RecyclerView for favorites
        recyclerViewFavorites.setLayoutManager(new LinearLayoutManager(this));
        profileAdapter = new ProfileAdapter(favoriteMovies);
        recyclerViewFavorites.setAdapter(profileAdapter);

        // Set click listeners
        menuPengaturan.setOnClickListener(v -> {
            Toast.makeText(this, "Menu Pengaturan", Toast.LENGTH_SHORT).show();
        });

        menuFilmSaya.setOnClickListener(v -> {
            toggleFavoritesVisibility(recyclerViewFavorites);
        });

        menuLogout.setOnClickListener(v -> logoutUser());

        // Set click listener for username to enable editing
        tvUsername.setOnClickListener(v -> showEditUsernameDialog());

        // Load user data
        loadUserData(tvEmail);

        // Load favorite movies
        loadFavoriteMovies();
    }

    private void showEditUsernameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        builder.setTitle("Edit Username");

        // Set up the input
        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_edit_username, null);
        final EditText input = viewInflated.findViewById(R.id.etUsername);
        input.setText(tvUsername.getText().toString());

        builder.setView(viewInflated);

        // Set up the buttons
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newUsername = input.getText().toString().trim();
            if (!newUsername.isEmpty()) {
                updateUsername(newUsername);
            } else {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateUsername(String newUsername) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("username", newUsername);

        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    tvUsername.setText(newUsername);
                    Toast.makeText(this, "Username updated successfully", Toast.LENGTH_SHORT).show();

                    // Broadcast update to refresh other activities
                    sendBroadcast(new Intent("USERNAME_UPDATED"));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update username", Toast.LENGTH_SHORT).show();
                });
    }

    private void toggleFavoritesVisibility(RecyclerView recyclerViewFavorites) {
        if (favoriteMovies.isEmpty()) {
            Toast.makeText(this, "You don't have any favorite cinemas yet", Toast.LENGTH_SHORT).show();
        } else {
            recyclerViewFavorites.setVisibility(
                    recyclerViewFavorites.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE
            );
        }
    }

    private void loadUserData(TextView tvEmail) {
        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String username = document.getString("username");
                            String email = document.getString("email");

                            tvUsername.setText(username != null ? username : "No Username");
                            tvEmail.setText(email != null ? email : mAuth.getCurrentUser().getEmail());
                        } else {
                            // Create user document if doesn't exist
                            createUserDocument(tvEmail);
                        }
                    } else {
                        Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createUserDocument(TextView tvEmail) {
        Map<String, Object> user = new HashMap<>();
        user.put("email", mAuth.getCurrentUser().getEmail());
        user.put("username", "User" + System.currentTimeMillis() % 1000); // Default username

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    tvUsername.setText(user.get("username").toString());
                    tvEmail.setText(user.get("email").toString());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to create user profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadFavoriteMovies() {
        db.collection("users").document(userId).collection("favorites")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        favoriteMovies.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            db.collection("cinemas").document(document.getId())
                                    .get()
                                    .addOnSuccessListener(cinemaDoc -> {
                                        if (cinemaDoc.exists()) {
                                            String cinemaName = cinemaDoc.getString("nama");
                                            if (cinemaName != null) {
                                                favoriteMovies.add(cinemaName);
                                                profileAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void logoutUser() {
        mAuth.signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.home) {
            startActivity(new Intent(this, HomeActivity.class));
            overridePendingTransition(0, 0);
        } else if (id == R.id.bioskop) {
            startActivity(new Intent(this, BioskopActivity.class));
            overridePendingTransition(0, 0);
        } else if (id == R.id.profile) {
            Toast.makeText(this, "You are already on profile page", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}