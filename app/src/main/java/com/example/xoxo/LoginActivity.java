package com.example.xoxo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.xoxo.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String username = getIntent().getStringExtra("USERNAME");
        if (username != null)
            binding.etUsername.setText(username);

        binding.btnLogin.setOnClickListener(this);
        binding.txtForgotPassword.setOnClickListener(this);
        binding.txtRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnLogin)
            handleLogin();
        else if (id == R.id.txtForgotPassword)
            Toast.makeText(this, "Fitur Lupa Password belum Tersedia", Toast.LENGTH_SHORT).show();
        else if (id == R.id.txtRegister) {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void handleLogin() {
        String username = binding.etUsername.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String validUsername = getIntent().getStringExtra("USERNAME");
        String validPassword = getIntent().getStringExtra("PASSWORD");

        if (username.isEmpty() || password.isEmpty())
            Toast.makeText(this, "Email dan Password tidak boleh kosong!", Toast.LENGTH_SHORT).show();
        else {
            if (username.equals(validUsername) && password.equals(validPassword)) {
                Toast.makeText(this, "Login Berhasil!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, HomeActivity.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
                finish();
            } else
                Toast.makeText(this, "Email atau Password tidak valid!", Toast.LENGTH_SHORT).show();
        }
    }
}
