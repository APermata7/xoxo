package com.example.xoxo;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Film {
    private String id;
    private String title;
    private String bioskop;
    private String harga;
    private String imageUrl;
    private String desc;
    private String info;
    private String pemain;
    private String sutradara;

    @Exclude
    private boolean isFavorite;

    public Film() {}

    public Film(String id, String title, String bioskop, String harga, String imageUrl,
                String desc, String info, String pemain, String sutradara) {
        this.id = id;
        this.title = title;
        this.bioskop = bioskop;
        this.harga = harga;
        this.imageUrl = imageUrl;
        this.desc = desc;
        this.info = info;
        this.pemain = pemain;
        this.sutradara = sutradara;
    }

    // Getters and setters for all fields
    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl (String imageUrl) {
        this.imageUrl = imageUrl;
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

    @Exclude
    public boolean isFavorite() {
        return isFavorite;
    }

    @Exclude
    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}