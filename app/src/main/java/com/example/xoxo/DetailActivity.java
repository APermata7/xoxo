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
        // Convert dp to px for image dimensions
        int imageWidthPx = dpToPx(220);
        int imageHeightPx = dpToPx(320);

        // Calculate ticket width (wider than image)
        int width = imageWidthPx + 200; // Add padding
        int height = 1600; // Increased height to accommodate all elements

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Draw white background
        canvas.drawColor(Color.WHITE);

        Paint paint = new Paint();

        // Draw "TIKET BIOSKOP" in black
        paint.setColor(Color.BLACK);
        paint.setTextSize(50f);
        paint.setFakeBoldText(true);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("XOXO - TIKET BIOSKOP", 50, 100, paint);

        // Draw line separator
        paint.setStrokeWidth(4f);
        paint.setColor(Color.BLACK);
        canvas.drawLine(50, 130, width-50, 130, paint);

        // Draw film image with original aspect ratio (center cropped)
        if (filmBitmap != null) {
            // Calculate position to center the image horizontally
            int imageLeft = (width - imageWidthPx) / 2;

            // Create a center-cropped version of the bitmap
            Bitmap centerCropped = getCenterCroppedBitmap(filmBitmap, imageWidthPx, imageHeightPx);
            canvas.drawBitmap(centerCropped, imageLeft, 160, paint);
        }

        // Position details below the image
        int yPos = 160 + imageHeightPx + 40;

        // Draw film details
        paint.setColor(Color.BLACK);
        paint.setTextSize(45f);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

        // Format harga for ticket
        String formattedHarga = formatHargaForTicket(currentFilm.getHarga());

        yPos += 40;
        // Draw title and get the new yPos based on how many lines it took
        yPos = drawMultilineTextAndGetY(canvas, paint, currentFilm.getTitle(), 50, yPos, width - 100);

        // Add some space between title and price
        yPos += 30;

        // Draw price
        paint.setFakeBoldText(false);
        paint.setTextSize(35f);
        canvas.drawText(formattedHarga, 50, yPos, paint);

        // Add space before info
        yPos += 50;

        paint.setTextSize(25f);
        // Draw info
        yPos = drawMultilineTextAndGetY(canvas, paint, currentFilm.getInfo(), 50, yPos, width - 100);

        // Thank you message
        paint.setColor(Color.BLACK);
        paint.setTextSize(30f);
        canvas.drawText("Terima kasih telah memesan!", 50, height-100, paint);

        return bitmap;
    }

    // New method that returns the new Y position after drawing multiline text
    private int drawMultilineTextAndGetY(Canvas canvas, Paint paint, String text, float x, float y, float maxWidth) {
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

        // Return the new Y position (current y + one more line height)
        return (int) (y + lineHeight);
    }

    private Bitmap getCenterCroppedBitmap(Bitmap source, int newWidth, int newHeight) {
        float sourceRatio = (float) source.getWidth() / source.getHeight();
        float targetRatio = (float) newWidth / newHeight;

        int x = 0, y = 0;
        int cropWidth = source.getWidth();
        int cropHeight = source.getHeight();

        if (sourceRatio > targetRatio) {
            // Source is wider - crop horizontally
            cropWidth = (int) (source.getHeight() * targetRatio);
            x = (source.getWidth() - cropWidth) / 2;
        } else {
            // Source is taller - crop vertically
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