package com.example.xoxo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.core.content.FileProvider;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.xoxo.databinding.ActivityDetailBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {
    private ActivityDetailBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;
    private Film currentFilm;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();

        currentFilm = getFilmFromIntent();

        setupViews();
        checkIfFavorite();
    }

    private Film getFilmFromIntent() {
        return new Film(
                getIntent().getStringExtra("film_id"),
                getIntent().getStringExtra("film_title"),
                "",
                getIntent().getStringExtra("film_harga"),
                getIntent().getStringExtra("film_image_url"),
                getIntent().getStringExtra("film_desc"),
                getIntent().getStringExtra("film_info"),
                getIntent().getStringExtra("film_pemain"),
                getIntent().getStringExtra("film_sutradara")
        );
    }

    private void setupViews() {
        setFilmDetails();
        loadFilmImage();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnFav.setOnClickListener(v -> toggleFavorite());
        binding.btnDownload.setOnClickListener(v -> createAndShareTicket());
        binding.btnShare.setOnClickListener(v -> shareFilm());
    }

    private void setFilmDetails() {
        binding.textJudul.setText(currentFilm.getTitle());
        binding.textInfo.setText(currentFilm.getInfo());
        binding.textDeskripsi.setText(currentFilm.getDesc());
        binding.textPemain.setText("Pemain: " + currentFilm.getPemain());
        binding.textSutradara.setText("Sutradara: " + currentFilm.getSutradara());

        try {
            String cleanHarga = currentFilm.getHarga().replaceAll("[^\\d]", "");
            double harga = Double.parseDouble(cleanHarga);

            Locale localeID = new Locale("in", "ID");
            NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
            formatRupiah.setMaximumFractionDigits(0);

            String formattedHarga = formatRupiah.format(harga)
                    .replace(",", ".");

            binding.textHarga.setText(formattedHarga);
        } catch (Exception e) {
            binding.textHarga.setText(currentFilm.getHarga());
        }
    }

    private void loadFilmImage() {
        Glide.with(this)
                .load(currentFilm.getImageUrl())
                .placeholder(R.drawable.placeholder_movie)
                .error(R.drawable.error_movie)
                .into(binding.imagePoster);
    }

    private void checkIfFavorite() {
        db.collection("users").document(userId).collection("favorites")
                .document(currentFilm.getId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        isFavorite = task.getResult().exists();
                        updateFavoriteButton();
                    }
                });
    }

    private void toggleFavorite() {
        isFavorite = !isFavorite;

        if (isFavorite) {
            addToFavorites();
        } else {
            removeFromFavorites();
        }
    }

    private void addToFavorites() {
        db.collection("users").document(userId).collection("favorites")
                .document(currentFilm.getId())
                .set(new HashMap<>())
                .addOnSuccessListener(aVoid -> {
                    updateFavoriteButton();
                    showToast("Ditambahkan ke Favorit");
                });
    }

    private void removeFromFavorites() {
        db.collection("users").document(userId).collection("favorites")
                .document(currentFilm.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    updateFavoriteButton();
                    showToast("Dihapus dari Favorit");
                });
    }

    private void updateFavoriteButton() {
        binding.btnFav.setImageResource(
                isFavorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite
        );
    }

    private void createAndShareTicket() {
        try {
            Bitmap ticketBitmap = createTicketBitmap();

            File ticketFile = saveTicketToCache(ticketBitmap);

            if (ticketFile != null) {
                shareTicket(ticketFile);
                showToast("Tiket berhasil dibuat");
            } else {
                showToast("Gagal menyimpan tiket");
            }
        } catch (Exception e) {
            showToast("Error: " + e.getMessage());
            Log.e("TICKET_ERROR", "Error creating ticket", e);
        }
    }

    private Bitmap createTicketBitmap() {
        int width = 800;
        int height = 1200;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        canvas.drawColor(Color.WHITE);

        Paint paint = new Paint();

        paint.setColor(Color.parseColor("#FF6200EE"));
        paint.setTextSize(50f);
        paint.setFakeBoldText(true);
        canvas.drawText("TIKET BIOSKOP", 50, 100, paint);

        paint.setStrokeWidth(4f);
        canvas.drawLine(50, 130, width-50, 130, paint);

        paint.setColor(Color.BLACK);
        paint.setTextSize(35f);
        paint.setFakeBoldText(false);

        int yPos = 200;
        canvas.drawText("Judul: " + currentFilm.getTitle(), 50, yPos, paint);
        yPos += 50;
        canvas.drawText("Harga: Rp " + currentFilm.getHarga(), 50, yPos, paint);
        yPos += 50;
        canvas.drawText("Info: " + currentFilm.getInfo(), 50, yPos, paint);
        yPos += 50;
        canvas.drawText("Pemain: " + currentFilm.getPemain(), 50, yPos, paint);
        yPos += 50;
        canvas.drawText("Sutradara: " + currentFilm.getSutradara(), 50, yPos, paint);

        paint.setColor(Color.parseColor("#FF6200EE"));
        paint.setTextSize(30f);
        canvas.drawText("Terima kasih telah memesan!", 50, height-100, paint);

        return bitmap;
    }

    private File saveTicketToCache(Bitmap bitmap) throws IOException {
        File cacheDir = new File(getCacheDir(), "tickets");
        if (!cacheDir.exists()) {
            if (!cacheDir.mkdirs()) {
                Log.e("TICKET_ERROR", "Failed to create cache directory");
                return null;
            }
        }

        String filename = "ticket_" + System.currentTimeMillis() + ".png";
        File file = new File(cacheDir, filename);

        FileOutputStream out = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        out.close();

        return file;
    }

    private void shareTicket(File ticketFile) {
        Uri ticketUri = FileProvider.getUriForFile(
                this,
                getPackageName() + ".provider",
                ticketFile
        );

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/png");
        shareIntent.putExtra(Intent.EXTRA_STREAM, ticketUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent chooser = Intent.createChooser(shareIntent, "Bagikan Tiket");

        if (shareIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        } else {
            showToast("Tidak ada aplikasi yang dapat menangani tiket");
        }
    }

    private void shareFilm() {
        String shareText = buildShareText();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Bagikan Film"));
    }

    private String buildShareText() {
        return "Saya merekomendasikan film:\n\n" +
                currentFilm.getTitle() + "\n\n" +
                "Sinopsis: " + currentFilm.getDesc() + "\n\n" +
                "Pemain: " + currentFilm.getPemain() + "\n\n" +
                "Sutradara: " + currentFilm.getSutradara() + "\n\n" +
                "Harga tiket: Rp " + currentFilm.getHarga();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}