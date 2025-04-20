package com.example.xoxo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView editNamaLengkap, editNoHP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize EditTexts
        editNamaLengkap = findViewById(R.id.editNamaLengkap);
        editNoHP = findViewById(R.id.editEmail);

        ImageView backButton = findViewById(R.id.imageView);
        // Get data from Intent
        Intent intent = getIntent();
        String username = intent.getStringExtra("USERNAME");
        String email = intent.getStringExtra("EMAIL");

        // Set data to UI elements
        editNamaLengkap.setText(username); // or another data field if needed
        editNoHP.setText(email);  // or set other user details as needed

        backButton.setOnClickListener(v -> {
            finish();
        });
    }
}
