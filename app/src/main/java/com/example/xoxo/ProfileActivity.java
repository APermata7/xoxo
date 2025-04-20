package com.example.xoxo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView textUsername, textEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        textUsername = findViewById(R.id.username);
        textEmail = findViewById(R.id.email);

        Intent intent = getIntent();
        String username = intent.getStringExtra("USERNAME");
        String email = intent.getStringExtra("EMAIL");

        if (username != null) textUsername.setText(username);
        if (email != null) textEmail.setText(email);
    }

    public void handleClick(View view) {
        int id = view.getId();
        if (id == R.id.home) {
            Toast.makeText(this, "Anda sudah di home.", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.bioskop) {
            Intent intent = new Intent(this, BioskopActivity.class);
            startActivity(intent);
        } else if (id == R.id.profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        }
    }
}
