package com.example.xoxo;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BioskopAdapter extends RecyclerView.Adapter<BioskopAdapter.ViewHolder> {

    private List<Bioskop> bioskopList;
    private Context context;
    private BioskopFavoriteListener favoriteListener;

    public interface BioskopFavoriteListener {
        void onFavoriteChanged(Bioskop bioskop, boolean isFavorite);
    }

    public BioskopAdapter(List<Bioskop> bioskopList, BioskopFavoriteListener listener) {
        this.bioskopList = bioskopList;
        this.favoriteListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_location, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Bioskop bioskop = bioskopList.get(position);
        holder.nama.setText(bioskop.getNama());

        updateFavoriteIcon(holder.favorite, bioskop.isFavorite());

        holder.favorite.setOnClickListener(v -> {
            boolean newFavoriteStatus = !bioskop.isFavorite();
            if (favoriteListener != null) {
                favoriteListener.onFavoriteChanged(bioskop, newFavoriteStatus);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, BioskopDetailActivity.class);
            intent.putExtra("CINEMA_ID", bioskop.getId());
            intent.putExtra("CINEMA_NAME", bioskop.getNama());
            intent.putExtra("CINEMA_ADDRESS", bioskop.getAddress());
            intent.putExtra("CINEMA_INFO", bioskop.getInfo());
            intent.putExtra("IS_FAVORITE", bioskop.isFavorite());
            context.startActivity(intent);
        });
    }

    private void updateFavoriteIcon(ImageView imageView, boolean isFavorite) {
        imageView.setImageResource(isFavorite ?
                R.drawable.ic_star_filled : R.drawable.ic_star);
    }

    @Override
    public int getItemCount() {
        return bioskopList.size();
    }

    public void updateData(List<Bioskop> newBioskopList) {
        bioskopList = newBioskopList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nama;
        ImageView favorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nama = itemView.findViewById(R.id.tvNamaBioskop);
            favorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
}