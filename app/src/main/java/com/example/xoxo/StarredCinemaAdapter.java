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

public class StarredCinemaAdapter extends RecyclerView.Adapter<StarredCinemaAdapter.ViewHolder> {
    private final List<Bioskop> cinemas;
    private final FirebaseFirestore db;
    private final String userId;

    public StarredCinemaAdapter(List<Bioskop> cinemas) {
        this.cinemas = cinemas;
        this.db = FirebaseFirestore.getInstance();
        this.userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cinema_star, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Bioskop bioskop = cinemas.get(position);

        // Set cinema data
        holder.tvName.setText(bioskop.getNama());
        holder.tvAddress.setText(bioskop.getAddress());
        holder.tvCity.setText(bioskop.getCity());
        holder.tvPhone.setText(bioskop.getPhoneNumber());
        holder.tvInfo.setText(bioskop.getInfo());

        // Load image with Glide
        Glide.with(holder.itemView.getContext())
                .load(bioskop.getImageUrl())
                .placeholder(R.drawable.placeholder_cinema)
                .error(R.drawable.error_cinema)
                .into(holder.ivImage);

        holder.btnUnstar.setOnClickListener(v -> {
            removeFromStarred(bioskop.getId(), position, v.getContext());
        });
    }

    private void removeFromStarred(String cinemaId, int position, Context context) {
        db.collection("users").document(userId)
                .collection("favorites") // Ubah dari starred_cinemas ke favorites
                .document(cinemaId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    cinemas.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, cinemas.size());
                    Toast.makeText(context, "Bioskop dihapus dari favorit", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Gagal menghapus: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return cinemas.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvAddress, tvCity, tvPhone, tvInfo;
        Button btnUnstar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.imageCinema);
            tvName = itemView.findViewById(R.id.textName);
            tvAddress = itemView.findViewById(R.id.textAddress);
            tvCity = itemView.findViewById(R.id.textCity);
            tvPhone = itemView.findViewById(R.id.textPhone);
            tvInfo = itemView.findViewById(R.id.textInfo);
            btnUnstar = itemView.findViewById(R.id.btnUnstar);
        }
    }
}