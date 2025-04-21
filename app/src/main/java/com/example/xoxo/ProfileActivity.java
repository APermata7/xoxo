package com.example.xoxo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.xoxo.databinding.ActivityLoginBinding;
import com.example.xoxo.databinding.ActivityProfileBinding;
import com.example.xoxo.databinding.ActivityRegisterBinding;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private String username, email;
    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        username = getIntent().getStringExtra("USERNAME");
        email = getIntent().getStringExtra("EMAIL");

        if (username != null)
            binding.tvUsername.setText(username);
        if (username != null)
            binding.tvEmail.setText(email);
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.home) {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra("USERNAME", username);
            intent.putExtra("EMAIL", email);
            startActivity(intent);
            overridePendingTransition(0, 0);
        } else if (id == R.id.bioskop) {
            Intent intent = new Intent(this, BioskopActivity.class);
            intent.putExtra("USERNAME", username);
            intent.putExtra("EMAIL", email);
            startActivity(intent);
            overridePendingTransition(0, 0);
        } else if (id == R.id.profile) {
            Toast.makeText(this, "Anda sudah di profile.", Toast.LENGTH_SHORT).show();
        }
    }
}
