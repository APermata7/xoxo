package com.example.xoxo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // Navigasi item setting
        findViewById(R.id.hubungiKami).setOnClickListener(v -> {
            startActivity(new Intent(this, HubungiKamiActivity.class));
            overridePendingTransition(0, 0);
        });

        findViewById(R.id.tentangKami).setOnClickListener(v -> {
            startActivity(new Intent(this, TentangKamiActivity.class));
            overridePendingTransition(0, 0);
        });

        // Tombol back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}
