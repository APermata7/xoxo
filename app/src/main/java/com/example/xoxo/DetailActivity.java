package com.example.xoxo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.xoxo.databinding.ActivityDetailBinding;

public class DetailActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String judul = getIntent().getStringExtra("film_title");
        String info = getIntent().getStringExtra("film_info");
        String desc = getIntent().getStringExtra("film_desc");
        String pemain = getIntent().getStringExtra("film_pemain");
        String sutradara = getIntent().getStringExtra("film_sutradara");
        int gambar = getIntent().getIntExtra("film_image",0);

        binding.textJudul.setText(judul);
        binding.textInfo.setText(info);
        binding.textDeskripsi.setText(desc);
        binding.textPemain.setText("Pemain: " + pemain);
        binding.textSutradara.setText("Sutradara: " + sutradara);
        binding.imagePoster.setImageResource(gambar);
        binding.btnBack.setOnClickListener(this);

        // Aksi tombol favorit
        binding.btnFav.setOnClickListener(view ->
                Toast.makeText(this, "Ditambahkan ke Favorit", Toast.LENGTH_SHORT).show());

        binding.btnBookmark.setOnClickListener(view ->
                Toast.makeText(this, "Ditambahkan ke Bookmark", Toast.LENGTH_SHORT).show());

        binding.btnShare.setOnClickListener(view -> {
            String shareText = "Tonton \"" + judul + "\" sekarang!";
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(shareIntent, "Bagikan Film"));
        });
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btnBack)
            finish();
    }
}
