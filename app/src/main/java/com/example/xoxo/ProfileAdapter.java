package com.example.xoxo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {

    private List<String> favoriteItems;
    private OnFavoriteClickListener listener;

    public interface OnFavoriteClickListener {
        void onFavoriteClick(int position);
    }

    public ProfileAdapter(List<String> favoriteItems) {
        this.favoriteItems = favoriteItems;
    }

    public void setOnFavoriteClickListener(OnFavoriteClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_profile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String item = favoriteItems.get(position);
        holder.textView.setText(item);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFavoriteClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return favoriteItems.size();
    }

    public void updateData(List<String> newItems) {
        favoriteItems = newItems;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public ImageView iconView;

        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(R.id.textItem);
            iconView = view.findViewById(R.id.iconItem);
        }
    }
}