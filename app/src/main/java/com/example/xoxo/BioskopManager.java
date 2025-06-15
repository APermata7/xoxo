package com.example.xoxo;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.xoxo.CloudinaryManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class BioskopManager {
    private static final String TAG = "BioskopManager";
    private final FirebaseFirestore db;
    private final Context context;
    private final String COLLECTION_NAME = "cinemas";

    public interface BioskopOperationCallback {
        void onSuccess(Bioskop bioskop);
        void onFailure(Exception e);
    }

    public interface UserAuthCallback {
        void onResult(boolean isAuthenticated);
    }

    public BioskopManager(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    public void addBioskop(Bioskop bioskop, Uri imageUri, String userId, String username, BioskopOperationCallback callback) {

        long currentTime = System.currentTimeMillis();
        bioskop.setCreatedAt(currentTime);
        bioskop.setUpdatedAt(currentTime);
        bioskop.setCreatedBy(userId);
        bioskop.setUpdatedBy(userId);
        bioskop.setCreatedByUsername(username);
        bioskop.setUpdatedByUsername(username);

        Log.d(TAG, "Adding bioskop: " + bioskop.getNama() + " in city: " + bioskop.getCity());

        if (imageUri != null) {
            try {
                File imageFile = CloudinaryManager.uriToFile(context, imageUri);
                CloudinaryManager.uploadImage(imageFile, new CloudinaryManager.UploadCallback() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        bioskop.setImageUrl(imageUrl);
                        saveBioskop(bioskop, callback);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Failed to upload image", e);
                        callback.onFailure(e);
                    }
                });
            } catch (IOException e) {
                Log.e(TAG, "Failed to convert image", e);
                callback.onFailure(e);
            }
        } else {
            saveBioskop(bioskop, callback);
        }
    }

    private void saveBioskop(Bioskop bioskop, BioskopOperationCallback callback) {
        db.collection(COLLECTION_NAME)
                .add(bioskop)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        String id = documentReference.getId();
                        bioskop.setId(id);
                        Log.d(TAG, "Successfully added bioskop with ID: " + id);
                        callback.onSuccess(bioskop);
                        Toast.makeText(context, "Bioskop berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to add bioskop", e);
                        callback.onFailure(e);
                        Toast.makeText(context, "Gagal menambahkan bioskop: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void updateBioskop(Bioskop bioskop, Uri newImageUri, String userId, String username, BioskopOperationCallback callback) {
        bioskop.setUpdatedAt(System.currentTimeMillis());
        bioskop.setUpdatedBy(userId);
        bioskop.setUpdatedByUsername(username);

        Log.d(TAG, "Updating bioskop: " + bioskop.getNama() + " (ID: " + bioskop.getId() + ") in city: " + bioskop.getCity());

        if (newImageUri != null) {
            try {
                File imageFile = CloudinaryManager.uriToFile(context, newImageUri);
                CloudinaryManager.uploadImage(imageFile, new CloudinaryManager.UploadCallback() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        bioskop.setImageUrl(imageUrl);
                        updateBioskopData(bioskop, callback);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Failed to upload new image", e);
                        callback.onFailure(e);
                    }
                });
            } catch (IOException e) {
                Log.e(TAG, "Failed to convert new image", e);
                callback.onFailure(e);
            }
        } else {
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
                        Log.d(TAG, "Successfully updated bioskop: " + bioskop.getId());
                        callback.onSuccess(bioskop);
                        Toast.makeText(context, "Bioskop berhasil diperbarui", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to update bioskop", e);
                        callback.onFailure(e);
                        Toast.makeText(context, "Gagal memperbarui bioskop: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void deleteBioskop(Bioskop bioskop, String userId, String username, BioskopOperationCallback callback) {
        Log.d(TAG, "Deleting bioskop: " + bioskop.getNama() + " (ID: " + bioskop.getId() + ")");

        db.collection(COLLECTION_NAME)
                .document(bioskop.getId())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "Successfully deleted bioskop: " + bioskop.getId());
                        callback.onSuccess(bioskop);
                        Toast.makeText(context, "Bioskop berhasil dihapus", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to delete bioskop", e);
                        callback.onFailure(e);
                        Toast.makeText(context, "Gagal menghapus bioskop: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static void checkUserAuthentication(String userId, UserAuthCallback callback) {
        boolean isAuthenticated = (userId != null && !userId.isEmpty());
        callback.onResult(isAuthenticated);
    }

    public static String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date(timestamp));
    }

    public static String getCurrentFormattedTimestamp() {
        return formatTimestamp(System.currentTimeMillis());
    }
}