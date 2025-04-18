package com.example.xoxo;

import java.io.Serializable;

public class Film implements Serializable {
    private int id;
    private String title;
    private String bioskop;
    private String harga;
    private int imageRes;
    private boolean isFavorite;
    private static final long serialVersionUID = 1L;

    // Constructor, getters, and setters
    public Film(int id, String title, String bioskop, String harga, int imageRes) {
        this.id = id;
        this.title = title;
        this.bioskop = bioskop;
        this.harga = harga;
        this.imageRes = imageRes;
        this.isFavorite = false;
    }

    // Add all getters and setters...
    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getBioskop() {
        return bioskop;
    }

    public String getHarga() {
        return harga;
    }

    public int getImageRes() {
        return imageRes;
    }
}
