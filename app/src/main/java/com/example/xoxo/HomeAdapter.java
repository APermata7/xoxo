package com.example.xoxo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.xoxo.databinding.ItemFilmBinding;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.FilmViewHolder> {
    private List<Film> filmList;
    private boolean isFavoriteList;
    private OnFavoriteChangeListener favoriteListener;
    private OnFilmClickListener filmClickListener;

    public interface OnFavoriteChangeListener {
        void onFavoriteChanged(Film film, boolean isFavorite);
    }

    public interface OnFilmClickListener {
        void onFilmClicked(Film film);
    }

    public HomeAdapter(List<Film> filmList, boolean isFavoriteList,
                       OnFavoriteChangeListener favoriteListener,
                       OnFilmClickListener filmClickListener) {
        this.filmList = filmList;
        this.isFavoriteList = isFavoriteList;
        this.favoriteListener = favoriteListener;
        this.filmClickListener = filmClickListener;
    }

    public void updateFilms(List<Film> newFilms) {
        this.filmList = newFilms;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FilmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFilmBinding binding = ItemFilmBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new FilmViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FilmViewHolder holder, int position) {
        Film film = filmList.get(position);
        holder.bind(film);
    }

    @Override
    public int getItemCount() {
        return filmList.size();
    }

    class FilmViewHolder extends RecyclerView.ViewHolder {
        private final ItemFilmBinding binding;
        private final int cornerRadius;

        public FilmViewHolder(ItemFilmBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.cornerRadius = itemView.getContext().getResources()
                    .getDimensionPixelSize(R.dimen.film_image_corner_radius);
        }

        public void bind(Film film) {
            Glide.with(itemView.getContext())
                    .load(film.getImageUrl())
                    .apply(new RequestOptions()
                            .transform(new RoundedCorners(cornerRadius))
                            .placeholder(R.drawable.placeholder_movie)
                            .error(R.drawable.error_movie))
                    .into(binding.filmImage);

            binding.filmTitle.setText(film.getTitle());
            binding.filmBioskop.setText(film.getBioskop());
            try {
                // Bersihkan string dari karakter non-numerik
                String cleanHarga = film.getHarga().replaceAll("[^\\d]", "");
                double harga = Double.parseDouble(cleanHarga);

                // Buat format Rupiah
                Locale localeID = new Locale("in", "ID");
                NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
                formatRupiah.setMaximumFractionDigits(0); // Tanpa desimal

                String formattedHarga = formatRupiah.format(harga)
                        .replace(",", ".");    // Ganti koma dengan titik

                binding.filmHarga.setText(formattedHarga);
            } catch (Exception e) {
                // Jika parsing gagal, tampilkan harga asli
                binding.filmHarga.setText(film.getHarga());
            }

            itemView.setOnClickListener(v -> {
                if (filmClickListener != null) {
                    filmClickListener.onFilmClicked(film);
                }
            });
        }
    }
}