package com.example.praktik;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.FilmViewHolder> {
    private List<Film> filmList;
    private boolean isFavoriteList;
    private OnFavoriteChangeListener listener;

    public interface OnFavoriteChangeListener {
        void onFavoriteChanged(Film film, boolean isFavorite);
    }

    public HomeAdapter(List<Film> filmList, boolean isFavoriteList, OnFavoriteChangeListener listener) {
        this.filmList = new ArrayList<>(filmList); // Gunakan copy constructor
        this.isFavoriteList = isFavoriteList;
        this.listener = listener;
    }

    // Tambahkan method untuk update data
    public void updateFilms(List<Film> newFilms) {
        this.filmList.clear();
        this.filmList.addAll(newFilms);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FilmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_film, parent, false);
        return new FilmViewHolder(view);
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
        private ImageView filmImage;
        private Switch switchFavorite;
        private TextView filmTitle, filmBioskop, filmHarga;

        public FilmViewHolder(@NonNull View itemView) {
            super(itemView);
            filmImage = itemView.findViewById(R.id.filmImage);
            switchFavorite = itemView.findViewById(R.id.switch1);
            filmTitle = itemView.findViewById(R.id.filmTitle);
            filmBioskop = itemView.findViewById(R.id.filmBioskop);
            filmHarga = itemView.findViewById(R.id.filmHarga);
        }

        public void bind(Film film) {
            filmImage.setImageResource(film.getImageRes());
            filmTitle.setText(film.getTitle());

            // Atur tampilan berdasarkan jenis list
            if (isFavoriteList) {
                filmBioskop.setVisibility(View.GONE);
                filmHarga.setVisibility(View.GONE);
                switchFavorite.setText("Hapus Favorite");
            } else {
                filmBioskop.setVisibility(View.VISIBLE);
                filmHarga.setVisibility(View.VISIBLE);
                filmBioskop.setText(film.getBioskop());
                filmHarga.setText(film.getHarga());
                switchFavorite.setText("Tambah Favorite");
            }

            switchFavorite.setOnCheckedChangeListener(null); // Reset dulu
            switchFavorite.setChecked(film.isFavorite());
            switchFavorite.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isFavoriteList && !isChecked) {
                    // Khusus list favorit, langsung hapus saat switch dimatikan
                    listener.onFavoriteChanged(film, false);
                } else if (!isFavoriteList) {
                    // Untuk list biasa, biarkan normal
                    film.setFavorite(isChecked);
                    listener.onFavoriteChanged(film, isChecked);
                }
            });
        }
    }
}