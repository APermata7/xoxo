package com.example.xoxo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class FavoriteMoviesAdapter extends RecyclerView.Adapter<FavoriteMoviesAdapter.ViewHolder> {
    private final List<Film> films;
    private final FirebaseFirestore db;
    private final String userId;

    public FavoriteMoviesAdapter(List<Film> films) {
        this.films = films;
        this.db = FirebaseFirestore.getInstance();
        this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_film_star, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Film film = films.get(position);

        // Set movie data
        holder.tvTitle.setText(film.getTitle());
        holder.tvInfo.setText(film.getInfo());
        holder.tvPrice.setText(film.getHarga());
        holder.tvBioskop.setText(film.getBioskop());
        holder.tvCast.setText("Pemain: " + film.getPemain());
        holder.tvDirector.setText("Sutradara: " + film.getSutradara());

        // Load image with Glide
        Glide.with(holder.itemView.getContext())
                .load(film.getImageUrl())
                .placeholder(R.drawable.placeholder_movie)
                .error(R.drawable.error_movie)
                .into(holder.ivPoster);

        // Remove button click listener
        holder.btnRemove.setOnClickListener(v -> {
            removeFromFavorites(film.getId(), position, v.getContext());
        });
    }

    private void removeFromFavorites(String filmId, int position, Context context) {
        db.collection("users").document(userId)
                .collection("favorites")
                .document(filmId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    films.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, films.size());
                    Toast.makeText(context, "Film dihapus dari favorit", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Gagal menghapus: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    public int getItemCount() {
        return films.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPoster;
        TextView tvTitle, tvInfo, tvPrice, tvBioskop, tvCast, tvDirector;
        Button btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.imagePoster);
            tvTitle = itemView.findViewById(R.id.textJudul);
            tvInfo = itemView.findViewById(R.id.textInfo);
            tvPrice = itemView.findViewById(R.id.textHarga);
            tvBioskop = itemView.findViewById(R.id.textBioskop);
            tvCast = itemView.findViewById(R.id.textCast);
            tvDirector = itemView.findViewById(R.id.textDirector);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}