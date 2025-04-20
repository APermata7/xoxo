package com.example.xoxo;


public class Bioskop {
    private String nama;
    private boolean isFavorite;
    private String address;
    private String info;

    public Bioskop(String nama) {
        this.nama = nama;
        this.isFavorite = false;

        // Default values
        this.address = "Alamat " + nama;
        this.info = "Pesan tiap Rabu terbaik!";
    }

    public Bioskop(String nama, boolean isFavorite, String address, String info) {
        this.nama = nama;
        this.isFavorite = isFavorite;
        this.address = address;
        this.info = info;
    }

    public String getNama() {
        return nama;
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
}
