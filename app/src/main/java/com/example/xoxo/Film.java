package com.example.xoxo;

import java.io.Serializable;

public class Film implements Serializable {
    private int id;
    private String title;
    private String bioskop;
    private String harga;
    private String desc;
    private String info;
    private String pemain;
    private String sutradara;
    private int imageRes;
    private boolean isFavorite;
    private static final long serialVersionUID = 1L;

    // Constructor, getters, and setters
    public Film(int id, String title, String bioskop, String harga, int imageRes, String desc, String info, String pemain, String sutradara) {
        this.id = id;
        this.title = title;
        this.bioskop = bioskop;
        this.harga = harga;
        this.imageRes = imageRes;
        this.isFavorite = false;
        this.desc = desc;
        this.info = info;
        this.pemain = pemain;
        this.sutradara = sutradara;
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

    public String getDesc() {
        return desc;
    }

    public String getInfo() {
        return info;
    }

    public String getPemain() {
        return pemain;
    }

    public String getSutradara() {
        return sutradara;
    }

    public int getImageRes() {
        return imageRes;
    }
}
