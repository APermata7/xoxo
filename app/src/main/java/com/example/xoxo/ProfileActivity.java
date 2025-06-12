package com.example.xoxo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private String userId;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ProfileAdapter profileAdapter;
    private List<String> favoriteMovies = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        // Find views
        TextView tvUsername = findViewById(R.id.tvUsername);
        TextView tvEmail = findViewById(R.id.tvEmail);
        View menuPengaturan = findViewById(R.id.menuPengaturan);
        View menuFilmSaya = findViewById(R.id.menuFilmSaya);
        View menuLogout = findViewById(R.id.menuLogout);
        RecyclerView recyclerViewFavorites = findViewById(R.id.recyclerViewFavorites);

        // Setup RecyclerView for favorites (optional)
        recyclerViewFavorites.setLayoutManager(new LinearLayoutManager(this));
        profileAdapter = new ProfileAdapter(favoriteMovies);
        recyclerViewFavorites.setAdapter(profileAdapter);

        // Set listeners
        menuPengaturan.setOnClickListener(v -> {
            Toast.makeText(this, "Menu Pengaturan", Toast.LENGTH_SHORT).show();
            // Navigate to settings if needed
        });

        menuFilmSaya.setOnClickListener(v -> {
            Toast.makeText(this, "Menu Film Saya", Toast.LENGTH_SHORT).show();
            // Show/hide favorites section
            recyclerViewFavorites.setVisibility(
                    recyclerViewFavorites.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE
            );
        });

        menuLogout.setOnClickListener(v -> logoutUser());

        // Load user data from Firestore
        loadUserData(tvUsername, tvEmail);

        // Load favorite movies (optional)
        loadFavoriteMovies();
    }

    private void loadUserData(TextView tvUsername, TextView tvEmail) {
        db.collection("users").document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Get data from Firestore
                            String username = document.getString("username");
                            String email = document.getString("email");

                            // Set data to views
                            tvUsername.setText(username != null ? username : "No Username");
                            tvEmail.setText(email != null ? email : "No Email");
                        } else {
                            // If document doesn't exist, use email from Firebase Auth
                            tvEmail.setText(mAuth.getCurrentUser().getEmail());
                        }
                    } else {
                        Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadFavoriteMovies() {
        db.collection("users").document(userId).collection("favorites")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        favoriteMovies.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            // Get cinema name from main cinemas collection
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
        // Redirect to login
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
}