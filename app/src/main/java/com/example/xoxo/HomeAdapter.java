package com.example.xoxo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.xoxo.databinding.ItemFilmBinding;
import java.util.ArrayList;
import java.util.List;

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
        this.filmList = new ArrayList<>(filmList);
        this.isFavoriteList = isFavoriteList;
        this.favoriteListener = favoriteListener;
        this.filmClickListener = filmClickListener;
    }

    public void updateFilms(List<Film> newFilms) {
        this.filmList.clear();
        this.filmList.addAll(newFilms);
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

        public FilmViewHolder(ItemFilmBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && filmClickListener != null) {
                    filmClickListener.onFilmClicked(filmList.get(position));
                }
            });
        }

        public void bind(Film film) {
            binding.filmImage.setImageResource(film.getImageRes());
            binding.filmTitle.setText(film.getTitle());
            binding.filmBioskop.setText(film.getBioskop());
            binding.filmHarga.setText(film.getHarga());

            // Atur tampilan berdasarkan jenis list
            if (isFavoriteList) {
                binding.switch1.setText("Hapus Favorite");
            } else {
                binding.switch1.setText("Favorite");
            }

            // Set click listener untuk seluruh item
            itemView.setOnClickListener(v -> {
                if (filmClickListener != null) {
                    filmClickListener.onFilmClicked(film);
                }
            });

            // Set listener untuk switch favorite
            binding.switch1.setOnCheckedChangeListener(null); // Reset dulu
            binding.switch1.setChecked(film.isFavorite());
            binding.switch1.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (favoriteListener != null) {
                    favoriteListener.onFavoriteChanged(film, isChecked);
                }
            });
        }
    }
}
