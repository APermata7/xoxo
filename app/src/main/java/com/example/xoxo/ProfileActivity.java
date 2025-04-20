package com.example.xoxo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView textUsername, textEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Inisialisasi TextViews
        textUsername = findViewById(R.id.username);
        textEmail = findViewById(R.id.email);

        // Ambil data dari Intent
        Intent intent = getIntent();
        String username = intent.getStringExtra("USERNAME");
        String email = intent.getStringExtra("EMAIL");

        // Masukkan ke UI jika datanya ada
        if (username != null) textUsername.setText(username);
        if (email != null) textEmail.setText(email);

        // Tombol kembali
        ImageView backButton = findViewById(R.id.imageView);
        backButton.setOnClickListener(v -> finish());
    }
}
