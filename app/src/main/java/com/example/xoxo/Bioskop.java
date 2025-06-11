package com.example.xoxo;

public class Bioskop {
    private String id; // ID dokumen Firestore
    private String nama;
    private boolean isFavorite;
    private String address;
    private String info;
    private String city;

    public Bioskop() {
    }

    public Bioskop(String id, String nama, String city, String address, String info) {
        this.id = id;
        this.nama = nama;
        this.city = city;
        this.address = address;
        this.info = info;
        this.isFavorite = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}