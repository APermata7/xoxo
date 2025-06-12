package com.example.xoxo.bioskop;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.xoxo.R;

import java.util.List;

import android.view.MenuItem;
import android.widget.PopupMenu;

import com.bumptech.glide.Glide;

public class BioskopAdapter extends RecyclerView.Adapter<BioskopAdapter.ViewHolder> {

    private List<Bioskop> bioskopList;
    private Context context;
    private BioskopListener listener;
    private boolean isAdmin;

    public interface BioskopListener {
        void onFavoriteChanged(Bioskop bioskop, boolean isFavorite);
        void onEditCinema(Bioskop bioskop);
        void onDeleteCinema(Bioskop bioskop);
        void onItemClick(Bioskop bioskop);
    }

    public BioskopAdapter(List<Bioskop> bioskopList, BioskopListener listener, boolean isAdmin) {
        this.bioskopList = bioskopList;
        this.listener = listener;
        this.isAdmin = isAdmin;
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

        // Load cinema image if available
        if (bioskop.getImageUrl() != null && !bioskop.getImageUrl().isEmpty()) {
            holder.image.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(bioskop.getImageUrl())
                    .placeholder(R.drawable.ic_cinema_placeholder)
                    .error(R.drawable.ic_cinema_placeholder)
                    .into(holder.image);
        } else {
            holder.image.setVisibility(View.GONE);
        }

        updateFavoriteIcon(holder.favorite, bioskop.isFavorite());

        holder.favorite.setOnClickListener(v -> {
            boolean newFavoriteStatus = !bioskop.isFavorite();
            if (listener != null) {
                listener.onFavoriteChanged(bioskop, newFavoriteStatus);
            }
        });

        // Show admin options
        holder.btnMore.setVisibility(isAdmin ? View.VISIBLE : View.GONE);

        holder.btnMore.setOnClickListener(v -> {
            if (isAdmin) {
                showPopupMenu(v, bioskop);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(bioskop);
            }
        });
    }

    private void showPopupMenu(View view, Bioskop bioskop) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.inflate(R.menu.menu_cinema_options);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.action_edit) {
                    if (listener != null) {
                        listener.onEditCinema(bioskop);
                    }
                    return true;
                } else if (id == R.id.action_delete) {
                    if (listener != null) {
                        listener.onDeleteCinema(bioskop);
                    }
                    return true;
                }
                return false;
            }
        });
        popup.show();
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
        ImageView favorite, image, btnMore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nama = itemView.findViewById(R.id.tvNamaBioskop);
            favorite = itemView.findViewById(R.id.btnFavorite);
            image = itemView.findViewById(R.id.imgCinema);
            btnMore = itemView.findViewById(R.id.btnMore);
        }
    }
}