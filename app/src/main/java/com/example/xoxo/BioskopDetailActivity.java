package com.example.xoxo;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class BioskopDetailActivity extends AppCompatActivity {

    private TextView tvCinemaName, tvCinemaAddress, tvCinemaInfo;
    private ImageView btnFavoriteDetail, btnBack;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String cinemaId;
    private boolean isFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bioskop_detail);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        tvCinemaName = findViewById(R.id.tvCinemaName);
        tvCinemaAddress = findViewById(R.id.tvCinemaAddress);
        tvCinemaInfo = findViewById(R.id.tvCinemaInfo);
        btnFavoriteDetail = findViewById(R.id.btnFavoriteDetail);
        btnBack = findViewById(R.id.btnBack);

        cinemaId = getIntent().getStringExtra("CINEMA_ID");
        String cinemaName = getIntent().getStringExtra("CINEMA_NAME");
        String cinemaAddress = getIntent().getStringExtra("CINEMA_ADDRESS");
        String cinemaInfo = getIntent().getStringExtra("CINEMA_INFO");
        isFavorite = getIntent().getBooleanExtra("IS_FAVORITE", false);

        tvCinemaName.setText(cinemaName);
        tvCinemaAddress.setText(cinemaAddress);
        tvCinemaInfo.setText(cinemaInfo);
        updateFavoriteIcon(isFavorite);

        btnFavoriteDetail.setOnClickListener(v -> toggleFavorite());

        btnBack.setOnClickListener(v -> finish());
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