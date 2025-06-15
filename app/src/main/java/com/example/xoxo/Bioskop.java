package com.example.xoxo;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

public class Bioskop implements Serializable {
    @DocumentId
    private String id;
    private String nama;
    private boolean isFavorite;
    private String address;
    private String info;
    private String city;
    private String phoneNumber;
    private String imageUrl;
    private long createdAt;
    private long updatedAt;

    private String createdBy;
    private String updatedBy;
    private String createdByUsername;
    private String updatedByUsername;

    public Bioskop() {
        // empty constructor for Firestore
    }

    public Bioskop(String id, String nama, String city, String address, String info, String phoneNumber) {
        this.id = id;
        this.nama = nama;
        this.city = city;
        this.address = address;
        this.info = info;
        this.phoneNumber = phoneNumber;
        this.isFavorite = false;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
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

    @Exclude
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
    }

    public String getUpdatedByUsername() {
        return updatedByUsername;
    }

    public void setUpdatedByUsername(String updatedByUsername) {
        this.updatedByUsername = updatedByUsername;
    }

    // Clone method for editing
    public Bioskop clone() {
        Bioskop clone = new Bioskop();
        clone.id = this.id;
        clone.nama = this.nama;
        clone.city = this.city;
        clone.address = this.address;
        clone.info = this.info;
        clone.phoneNumber = this.phoneNumber;
        clone.imageUrl = this.imageUrl;
        clone.isFavorite = this.isFavorite;
        clone.createdAt = this.createdAt;
        clone.updatedAt = System.currentTimeMillis();
        clone.createdBy = this.createdBy;
        clone.createdByUsername = this.createdByUsername;
        return clone;
    }
}