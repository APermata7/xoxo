package com.example.xoxo;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userId;
    private TextView tvUsername;
    private TextView tvEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check if user is logged in
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        userId = mAuth.getCurrentUser().getUid();

        // Initialize views
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);

        // Set click listeners for navigation
        findViewById(R.id.home).setOnClickListener(this);
        findViewById(R.id.film).setOnClickListener(this);
        findViewById(R.id.bioskop).setOnClickListener(this);
        findViewById(R.id.profile).setOnClickListener(this);

        // Set click listeners for menu items
        findViewById(R.id.menuFilmFavorit).setOnClickListener(v -> {
            startActivity(new Intent(this, FavoriteMoviesActivity.class));
            overridePendingTransition(0, 0);
        });

        findViewById(R.id.menuBioskopStar).setOnClickListener(v -> {
            startActivity(new Intent(this, StarredCinemaActivity.class));
            overridePendingTransition(0, 0);
        });

        findViewById(R.id.menuPengaturan).setOnClickListener(v -> {
            startActivity(new Intent(this, SettingActivity.class));
            overridePendingTransition(0, 0);
        });


        findViewById(R.id.btnLogout).setOnClickListener(v -> logoutUser());

        // Enable username editing
        tvUsername.setOnClickListener(v -> showEditUsernameDialog());

        // Load user data
        loadUserData();
    }

    private void showEditUsernameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);
        builder.setTitle("Edit Username");

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_edit_username, null);
        final EditText input = viewInflated.findViewById(R.id.etUsername);
        input.setText(tvUsername.getText().toString());

        builder.setView(viewInflated);

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

                    // Broadcast username update
                    Intent intent = new Intent("USERNAME_UPDATED");
                    intent.putExtra("newUsername", newUsername);
                    sendBroadcast(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update username: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadUserData() {
        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            String username = document.getString("username");
                            String email = document.getString("email");

                            tvUsername.setText(username != null ? username : "No Username");
                            tvEmail.setText(email != null ? email : mAuth.getCurrentUser().getEmail());
                        } else {
                            createUserDocument();
                        }
                    } else {
                        Toast.makeText(this, "Failed to load user data: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createUserDocument() {
        Map<String, Object> user = new HashMap<>();
        user.put("email", mAuth.getCurrentUser().getEmail());
        user.put("username", "User" + System.currentTimeMillis() % 1000);

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    tvUsername.setText(user.get("username").toString());
                    tvEmail.setText(user.get("email").toString());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to create user profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
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
        Intent intent = null;

        if (id == R.id.home) {
            intent = new Intent(this, HomeActivity.class);
        }
        else if (id == R.id.film) {
            intent = new Intent(this, FilmActivity.class);
        }
        else if (id == R.id.bioskop) {
            intent = new Intent(this, BioskopActivity.class);
        }
        else if (id == R.id.profile) {
            Toast.makeText(this, "You're already on profile page", Toast.LENGTH_SHORT).show();
            return;
        }

        if (intent != null) {
            // Pass user data if needed
            intent.putExtra("USERNAME", tvUsername.getText().toString());
            intent.putExtra("EMAIL", tvEmail.getText().toString());
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to profile
        loadUserData();
    }
}