package com.example.xoxo;

import static androidx.databinding.DataBindingUtil.setContentView;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

// Untuk activity_us.xml
public class TentangKamiActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_us);

        // Tombol back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}
