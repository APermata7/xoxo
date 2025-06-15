package com.example.xoxo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import android.view.MenuItem;
import android.widget.PopupMenu;

import com.bumptech.glide.Glide;

public class BioskopAdapter extends RecyclerView.Adapter<BioskopAdapter.ViewHolder> {
    private static final String TAG = "BioskopAdapter";
    private List<Bioskop> bioskopList;
    private Context context;
    private BioskopListener listener;
    private boolean canEdit;
    private boolean isClickable = true;

    public interface BioskopListener {
        void onFavoriteChanged(Bioskop bioskop, boolean isFavorite);
        void onEditCinema(Bioskop bioskop);
        void onDeleteCinema(Bioskop bioskop);
        void onItemClick(Bioskop bioskop);
    }

    public BioskopAdapter(List<Bioskop> bioskopList, BioskopListener listener, boolean canEdit) {
        this.bioskopList = bioskopList;
        this.listener = listener;
        this.canEdit = canEdit;
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

        holder.favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Favorite button clicked for: " + bioskop.getNama());
                boolean newFavoriteStatus = !bioskop.isFavorite();
                if (listener != null) {
                    listener.onFavoriteChanged(bioskop, newFavoriteStatus);
                }
            }
        });

        holder.btnMore.setVisibility(canEdit ? View.VISIBLE : View.GONE);

        holder.btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "More button clicked for: " + bioskop.getNama());
                if (canEdit) {
                    showPopupMenu(v, bioskop);
                }
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isClickable) return;

                isClickable = false;
                Log.d(TAG, "Cinema item clicked: " + bioskop.getNama());

                if (listener != null) {
                    listener.onItemClick(bioskop);
                }

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isClickable = true;
                    }
                }, 500);
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
                    Log.d(TAG, "Edit menu selected for: " + bioskop.getNama());
                    if (listener != null) {
                        listener.onEditCinema(bioskop);
                    }
                    return true;
                } else if (id == R.id.action_delete) {
                    Log.d(TAG, "Delete menu selected for: " + bioskop.getNama());
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