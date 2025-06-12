package com.example.xoxo.bioskop;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.xoxo.cloudinary.CloudinaryManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BioskopAdminManager {
    private final FirebaseFirestore db;
    private final Context context;
    private final String COLLECTION_NAME = "cinemas";

    public interface BioskopOperationCallback {
        void onSuccess(Bioskop bioskop);
        void onFailure(Exception e);
    }

    // Simplified interface for admin check
    public interface AdminCheckCallback {
        void onResult(boolean isAdmin);
    }

    public BioskopAdminManager(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    // CREATE: Add a new cinema
    public void addBioskop(Bioskop bioskop, Uri imageUri, BioskopOperationCallback callback) {
        bioskop.setCreatedAt(System.currentTimeMillis());
        bioskop.setUpdatedAt(System.currentTimeMillis());

        if (imageUri != null) {
            // Upload image to Cloudinary
            try {
                File imageFile = CloudinaryManager.uriToFile(context, imageUri);
                CloudinaryManager.uploadImage(imageFile, new CloudinaryManager.UploadCallback() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        bioskop.setImageUrl(imageUrl);
                        // Then save cinema data
                        saveBioskop(bioskop, callback);
                    }

                    @Override
                    public void onError(Exception e) {
                        callback.onFailure(e);
                    }
                });
            } catch (IOException e) {
                callback.onFailure(e);
            }
        } else {
            // No image, just save cinema data
            saveBioskop(bioskop, callback);
        }
    }

    private void saveBioskop(Bioskop bioskop, BioskopOperationCallback callback) {
        db.collection(COLLECTION_NAME)
                .add(bioskop)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        bioskop.setId(documentReference.getId());
                        callback.onSuccess(bioskop);
                        Toast.makeText(context, "Bioskop berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e);
                        Toast.makeText(context, "Gagal menambahkan bioskop: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // UPDATE: Update an existing cinema
    public void updateBioskop(Bioskop bioskop, Uri newImageUri, BioskopOperationCallback callback) {
        bioskop.setUpdatedAt(System.currentTimeMillis());

        if (newImageUri != null) {
            // Upload new image to Cloudinary
            try {
                File imageFile = CloudinaryManager.uriToFile(context, newImageUri);
                CloudinaryManager.uploadImage(imageFile, new CloudinaryManager.UploadCallback() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        // Old image is left on Cloudinary (no deletion in this simplified approach)
                        bioskop.setImageUrl(imageUrl);
                        // Then update cinema data
                        updateBioskopData(bioskop, callback);
                    }

                    @Override
                    public void onError(Exception e) {
                        callback.onFailure(e);
                    }
                });
            } catch (IOException e) {
                callback.onFailure(e);
            }
        } else {
            // No new image, just update cinema data
            updateBioskopData(bioskop, callback);
        }
    }

    private void updateBioskopData(Bioskop bioskop, BioskopOperationCallback callback) {
        db.collection(COLLECTION_NAME)
                .document(bioskop.getId())
                .set(bioskop)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        callback.onSuccess(bioskop);
                        Toast.makeText(context, "Bioskop berhasil diperbarui", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e);
                        Toast.makeText(context, "Gagal memperbarui bioskop: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // DELETE: Remove a cinema
    public void deleteBioskop(Bioskop bioskop, BioskopOperationCallback callback) {
        db.collection(COLLECTION_NAME)
                .document(bioskop.getId())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        callback.onSuccess(bioskop);
                        Toast.makeText(context, "Bioskop berhasil dihapus", Toast.LENGTH_SHORT).show();

                        // Note: The image remains on Cloudinary with this simplified approach
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onFailure(e);
                        Toast.makeText(context, "Gagal menghapus bioskop: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Simplified method to check if user is admin
    public void checkAdminAccess(String userId, AdminCheckCallback callback) {
        db.collection("admin_users").document(userId).get()
                .addOnCompleteListener(task -> {
                    boolean isAdmin = false;
                    if (task.isSuccessful() && task.getResult().exists()) {
                        isAdmin = true;
                    }
                    callback.onResult(isAdmin);
                });
    }
}