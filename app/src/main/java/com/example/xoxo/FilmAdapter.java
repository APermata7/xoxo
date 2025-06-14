package com.example.xoxo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.xoxo.Film;
import com.example.xoxo.databinding.ItemFilmBinding;

import java.util.List;

public class FilmAdapter extends RecyclerView.Adapter<FilmAdapter.FilmViewHolder> {

    private List<Film> films;

    public FilmAdapter(List<Film> films) {
        this.films = films;
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
        Film film = films.get(position);
        holder.bind(film);
    }

    @Override
    public int getItemCount() {
        return films.size();
    }

    public static class FilmViewHolder extends RecyclerView.ViewHolder {
        private final ItemFilmBinding binding;

        public FilmViewHolder(ItemFilmBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Film film) {
            binding.filmTitle.setText(film.getTitle());
            binding.filmBioskop.setText(film.getBioskop());
            binding.filmHarga.setText(film.getHarga());

            // Load image using Glide
            Glide.with(itemView.getContext())
                    .load(film.getImageUrl())
                    .centerCrop()
                    .into(binding.filmImage);
        }
    }
}