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
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
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
    private Bitmap filmBitmap;

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

        // Pre-load film image for ticket
        loadFilmImageForTicket();
    }

    private Film getFilmFromIntent() {
        return new Film(
                getIntent().getStringExtra("film_id"),
                getIntent().getStringExtra("film_title"),
                getIntent().getStringExtra("film_bioskop"),
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

        formatAndSetHarga();
    }

    private void formatAndSetHarga() {
        try {
            String cleanHarga = currentFilm.getHarga().replaceAll("[^\\d]", "");
            double harga = Double.parseDouble(cleanHarga);

            Locale localeID = new Locale("in", "ID");
            NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
            formatRupiah.setMaximumFractionDigits(0);

            String formattedHarga = formatRupiah.format(harga)
                    .replace("Rp", "Rp ")
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

    private void loadFilmImageForTicket() {
        Glide.with(this)
                .asBitmap()
                .load(currentFilm.getImageUrl())
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        filmBitmap = resource;
                    }
                });
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
            if (filmBitmap != null) {
                Bitmap ticketBitmap = createTicketBitmap();
                File ticketFile = saveTicketToCache(ticketBitmap);

                if (ticketFile != null) {
                    shareTicket(ticketFile);
                    showToast("Tiket berhasil dibuat");
                } else {
                    showToast("Gagal menyimpan tiket");
                }
            } else {
                showToast("Sedang memuat gambar film...");
                loadFilmImageForTicket();
            }
        } catch (Exception e) {
            showToast("Error: " + e.getMessage());
            Log.e("TICKET_ERROR", "Error creating ticket", e);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
    private Bitmap createTicketBitmap() {
        int imageWidthPx = dpToPx(220);
        int imageHeightPx = dpToPx(320);

        int width = imageWidthPx + 200;
        int height = 1500;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        canvas.drawColor(Color.WHITE);

        Paint paint = new Paint();

        paint.setColor(Color.BLACK);
        paint.setTextSize(50f);
        paint.setFakeBoldText(true);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        String mainTitle = "XOXO - TIKET BIOSKOP";
        float titleWidth = paint.measureText(mainTitle);
        float titleX = (width - titleWidth) / 2;
        canvas.drawText(mainTitle, titleX, 100, paint);

        paint.setTextSize(35f);
        paint.setFakeBoldText(true);
        String cinemaName = currentFilm.getBioskop();
        if (cinemaName == null || cinemaName.isEmpty()) {
            cinemaName = "Bioskop XOXO";
        }
        float cinemaWidth = paint.measureText(cinemaName);
        float cinemaX = (width - cinemaWidth) / 2;
        canvas.drawText(cinemaName, cinemaX, 150, paint);

        paint.setStrokeWidth(4f);
        paint.setColor(Color.BLACK);
        canvas.drawLine(50, 180, width-50, 180, paint);

        if (filmBitmap != null) {
            int imageLeft = (width - imageWidthPx) / 2;
            Bitmap centerCropped = getCenterCroppedBitmap(filmBitmap, imageWidthPx, imageHeightPx);
            canvas.drawBitmap(centerCropped, imageLeft, 210, paint);
        }

        int yPos = 210 + imageHeightPx + 60;

        paint.setColor(Color.BLACK);
        paint.setTextSize(40f);
        paint.setFakeBoldText(true);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        yPos = drawMultilineTextAndGetY(canvas, paint, currentFilm.getTitle(), 50, yPos, width - 100);

        yPos += 40;

        paint.setTextSize(32f);
        paint.setFakeBoldText(false);
        String formattedHarga = formatHargaForTicket(currentFilm.getHarga());
        canvas.drawText(formattedHarga, 50, yPos, paint);

        yPos += 60;

        paint.setTextSize(25f);
        paint.setFakeBoldText(false);
        yPos = drawMultilineTextAndGetY(canvas, paint, currentFilm.getInfo(), 50, yPos, width - 100);

        paint.setTextSize(30f);
        paint.setFakeBoldText(true);
        canvas.drawText("Terima kasih telah memesan!", 50, height-100, paint);

        return bitmap;
    }

    private int drawMultilineTextAndGetY(Canvas canvas, Paint paint, String text, float x, float y, float maxWidth) {
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        float lineHeight = paint.descent() - paint.ascent();

        for (String word : words) {
            String testLine = currentLine.toString().isEmpty() ? word : currentLine + " " + word;
            float testWidth = paint.measureText(testLine);

            if (testWidth > maxWidth) {
                canvas.drawText(currentLine.toString(), x, y, paint);
                y += lineHeight;
                currentLine = new StringBuilder(word);
            } else {
                currentLine = new StringBuilder(testLine);
            }
        }

        if (!currentLine.toString().isEmpty()) {
            canvas.drawText(currentLine.toString(), x, y, paint);
        }

        return (int) (y + lineHeight);
    }

    private Bitmap getCenterCroppedBitmap(Bitmap source, int newWidth, int newHeight) {
        float sourceRatio = (float) source.getWidth() / source.getHeight();
        float targetRatio = (float) newWidth / newHeight;

        int x = 0, y = 0;
        int cropWidth = source.getWidth();
        int cropHeight = source.getHeight();

        if (sourceRatio > targetRatio) {
            cropWidth = (int) (source.getHeight() * targetRatio);
            x = (source.getWidth() - cropWidth) / 2;
        } else {
            cropHeight = (int) (source.getWidth() / targetRatio);
            y = (source.getHeight() - cropHeight) / 2;
        }

        Bitmap cropped = Bitmap.createBitmap(source, x, y, cropWidth, cropHeight);
        return Bitmap.createScaledBitmap(cropped, newWidth, newHeight, true);
    }

    private String formatHargaForTicket(String harga) {
        try {
            String cleanHarga = harga.replaceAll("[^\\d]", "");
            double hargaValue = Double.parseDouble(cleanHarga);

            Locale localeID = new Locale("in", "ID");
            NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
            formatRupiah.setMaximumFractionDigits(0);

            return formatRupiah.format(hargaValue)
                    .replace(",", ".");
        } catch (Exception e) {
            return harga;
        }
    }

    private void drawMultilineText(Canvas canvas, Paint paint, String text, float x, float y, float maxWidth) {
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();
        float lineHeight = paint.descent() - paint.ascent();

        for (String word : words) {
            String testLine = currentLine.toString().isEmpty() ? word : currentLine + " " + word;
            float testWidth = paint.measureText(testLine);

            if (testWidth > maxWidth) {
                // Draw the current line and move to next line
                canvas.drawText(currentLine.toString(), x, y, paint);
                y += lineHeight;
                currentLine = new StringBuilder(word);
            } else {
                currentLine = new StringBuilder(testLine);
            }
        }

        // Draw the last line
        if (!currentLine.toString().isEmpty()) {
            canvas.drawText(currentLine.toString(), x, y, paint);
        }
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
                "Harga tiket: " + formatHargaForTicket(currentFilm.getHarga());
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}