package com.example.xoxo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.xoxo.databinding.ItemFilmsBinding;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class FilmAdapter extends RecyclerView.Adapter<FilmAdapter.FilmViewHolder> {

    private List<Film> films;
    private OnFilmActionListener actionListener;

    public interface OnFilmActionListener {
        void onEditFilm(Film film);
        void onDeleteFilm(Film film);
    }

    public FilmAdapter(List<Film> films, OnFilmActionListener actionListener) {
        this.films = films;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public FilmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFilmsBinding binding = ItemFilmsBinding.inflate(
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

    public void updateData(List<Film> newFilms) {
        films.clear();
        films.addAll(newFilms);
        notifyDataSetChanged();
    }

    public class FilmViewHolder extends RecyclerView.ViewHolder {
        private final ItemFilmsBinding binding;

        public FilmViewHolder(ItemFilmsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Film film) {
            binding.filmTitle.setText(film.getTitle());
            binding.filmBioskop.setText(film.getBioskop());

            try {
                String cleanHarga = film.getHarga().replaceAll("[^\\d]", "");
                double harga = Double.parseDouble(cleanHarga);

                Locale localeID = new Locale("in", "ID");
                NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
                formatRupiah.setMaximumFractionDigits(0);

                String formattedHarga = formatRupiah.format(harga)
                        .replace(",", ".");

                binding.filmHarga.setText(formattedHarga);
            } catch (Exception e) {
                binding.filmHarga.setText(film.getHarga());
            }

            Glide.with(itemView.getContext())
                    .load(film.getImageUrl())
                    .centerCrop()
                    .into(binding.filmImage);

            binding.btnEdit.setOnClickListener(v -> actionListener.onEditFilm(film));
            binding.btnDelete.setOnClickListener(v -> actionListener.onDeleteFilm(film));
        }
    }
}