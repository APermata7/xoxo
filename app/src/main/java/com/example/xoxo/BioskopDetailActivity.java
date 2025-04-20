package com.example.xoxo;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BioskopDetailActivity extends AppCompatActivity {

    private TextView tvCinemaName;
    private ImageView btnBack;
    private ImageView btnFavoriteDetail;
    private TextView tvCinemaAddress;
    private TextView tvCinemaInfo;

    private Bioskop currentBioskop;
    private boolean isFavorite = false;
    private static Map<String, List<Bioskop>> bioskopPerKota = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bioskop_detail);

        // Initialize views
        tvCinemaName = findViewById(R.id.tvCinemaName);
        btnBack = findViewById(R.id.btnBack);
        btnFavoriteDetail = findViewById(R.id.btnFavoriteDetail);
        tvCinemaAddress = findViewById(R.id.tvCinemaAddress);
        tvCinemaInfo = findViewById(R.id.tvCinemaInfo);

        // Get data from intent
        String cinemaName = getIntent().getStringExtra("CINEMA_NAME");
        isFavorite = getIntent().getBooleanExtra("IS_FAVORITE", false);

        currentBioskop = BioskopActivity.getBioskopByName(cinemaName);

        if (currentBioskop != null) {
            // Set data ke tampilan
            tvCinemaName.setText(currentBioskop.getNama());
            tvCinemaAddress.setText(currentBioskop.getAddress());
            tvCinemaInfo.setText(currentBioskop.getInfo());
        } else {
            tvCinemaName.setText(cinemaName);
            tvCinemaAddress.setText("Alamat tidak ditemukan");
            tvCinemaInfo.setText("Info tidak tersedia");
        }

        updateFavoriteIcon();

        btnBack.setOnClickListener(v -> finish());
        btnFavoriteDetail.setOnClickListener(v -> {
            isFavorite = !isFavorite;
            updateFavoriteIcon();

            if (currentBioskop != null) {
                currentBioskop.setFavorite(isFavorite);
            }
        });
    }

    private void updateFavoriteIcon() {
        btnFavoriteDetail.setImageResource(
                isFavorite ? R.drawable.ic_star_filled : R.drawable.ic_star
        );
    }
}
