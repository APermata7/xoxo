package com.example.xoxo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class HubungiKamiActivity extends AppCompatActivity {

    // Inisialisasi tombol
    Button btnEmail, btnWhatsapp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hub);

        // 🛠️ Tambahkan inisialisasi tombol berdasarkan ID di XML
        btnEmail = findViewById(R.id.btnEmail);
        btnWhatsapp = findViewById(R.id.btnWhatsapp);

        // Tombol Email
        btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:support@xoxoapp.com"));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Bantuan dari Aplikasi XOXO");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Halo tim support, saya ingin bertanya tentang...");
                startActivity(Intent.createChooser(emailIntent, "Pilih aplikasi email:"));
            }
        });

        // Tombol WhatsApp
        btnWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nomor = "+6281234567890";
                String pesan = "Halo tim XOXO, saya butuh bantuan tentang aplikasi ini.";
                String url = "https://wa.me/" + nomor.replace("+", "") + "?text=" + Uri.encode(pesan);

                Intent waIntent = new Intent(Intent.ACTION_VIEW);
                waIntent.setData(Uri.parse(url));
                startActivity(waIntent);
            }
        });

        // Tombol back
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}
