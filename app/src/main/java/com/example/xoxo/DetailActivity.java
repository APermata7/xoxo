package com.example.xoxo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

    private TextView textJudul, textInfo, textDeskripsi, textPemain, textSutradara;
    private ImageView imagePoster;

    private ImageButton btnFav, btnBookmark, btnShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Inisialisasi komponen
        textJudul = findViewById(R.id.textJudul);
        textInfo = findViewById(R.id.textInfo);
        textDeskripsi = findViewById(R.id.textDeskripsi);
        textPemain = findViewById(R.id.textPemain);
        textSutradara = findViewById(R.id.textSutradara);
        imagePoster = findViewById(R.id.imagePoster);

        btnFav = findViewById(R.id.btnFav);
        btnBookmark = findViewById(R.id.btnBookmark);
        btnShare = findViewById(R.id.btnShare);

        // Ambil data dari intent
        Intent intent = getIntent();
        if (intent != null) {
            String judul = intent.getStringExtra("judul");
            String info = intent.getStringExtra("info");
            String deskripsi = intent.getStringExtra("deskripsi");
            String pemain = intent.getStringExtra("pemain");
            String sutradara = intent.getStringExtra("sutradara");
            int posterResId = intent.getIntExtra("poster", R.drawable.logo); // default image

            textJudul.setText(judul);
            textInfo.setText(info);
            textDeskripsi.setText(deskripsi);
            textPemain.setText("Pemain: " + pemain);
            textSutradara.setText("Sutradara: " + sutradara);
            imagePoster.setImageResource(posterResId);
        }

        // Aksi tombol favorit
        btnFav.setOnClickListener(view ->
                Toast.makeText(this, "Ditambahkan ke Favorit", Toast.LENGTH_SHORT).show());

        btnBookmark.setOnClickListener(view ->
                Toast.makeText(this, "Ditambahkan ke Bookmark", Toast.LENGTH_SHORT).show());

        btnShare.setOnClickListener(view -> {
            String shareText = "Tonton \"" + textJudul.getText().toString() + "\" sekarang!";
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(shareIntent, "Bagikan Film"));
        });
    }

    // Fungsi onClick untuk navigasi navbar bawah
    public void onClick(View view) {
        int id = view.getId();
        Intent intent = null;

        if (id == R.id.home) {
            intent = new Intent(this, HomeActivity.class);
        } else if (id == R.id.bioskop) {
            intent = new Intent(this, BioskopActivity.class);
        } else if (id == R.id.profile) {
            intent = new Intent(this, ProfileActivity.class);
        }

        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }
}
