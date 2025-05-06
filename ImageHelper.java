package com.omeryalap.myapplication;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ImageHelper {
    private static final int MAX_SIZE = 512;
    private static final int MAX_CHARACTERS = 41000;
    private static final int MAX_STORAGE_SIZE = 1024 * 1024; // 1MB
    private static DatabaseReference databaseReference;
    private static StorageReference storageReference;

    public static Bitmap resizeIfNeeded(Bitmap originalBitmap) {
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();

        if (width > MAX_SIZE || height > MAX_SIZE) {
            float scaleWidth = ((float) MAX_SIZE) / width;
            float scaleHeight = ((float) MAX_SIZE) / height;
            float scale = Math.min(scaleWidth, scaleHeight);

            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);

            return Bitmap.createBitmap(originalBitmap, 0, 0, width, height, matrix, true);
        }
        return originalBitmap;
    }

    public static void saveToFirebase(Bitmap bitmap, ArrayList<Integer> indices, boolean isEncoded) {
        if (databaseReference == null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("steganography_data");
        }

        String key = databaseReference.push().getKey();
        if (key == null) return;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        if (isEncoded) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        } else {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        }
        
        byte[] imageData = baos.toByteArray();
        int imageSize = imageData.length;

        Map<String, Object> data = new HashMap<>();
        data.put("indices", indices);
        data.put("timestamp", System.currentTimeMillis());
        data.put("isEncoded", isEncoded);
        data.put("format", isEncoded ? "PNG" : "JPEG");
        data.put("imageSize", imageSize);

        if (imageSize <= MAX_STORAGE_SIZE) {
            if (storageReference == null) {
                storageReference = FirebaseStorage.getInstance().getReference("steganography_images");
            }

            String fileExtension = isEncoded ? ".png" : ".jpg";
            StorageReference imageRef = storageReference.child(key + fileExtension);
            UploadTask uploadTask = imageRef.putBytes(imageData);

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    data.put("imageUrl", uri.toString());
                    data.put("storageType", "Storage");
                    
                    databaseReference.child(key).setValue(data)
                        .addOnFailureListener(e -> Log.e("Firebase", "Veri kaydedilemedi: " + e.getMessage()));
                });
            }).addOnFailureListener(e -> {
                Log.e("Firebase", "Görüntü yüklenemedi: " + e.getMessage());
                saveOnlyIndices(key, data);
            });
        } else {
            data.put("storageType", "Database");
            saveOnlyIndices(key, data);
        }
    }

    private static void saveOnlyIndices(String key, Map<String, Object> data) {
        databaseReference.child(key).setValue(data)
            .addOnFailureListener(e -> Log.e("Firebase", "İndisler kaydedilemedi: " + e.getMessage()));
    }

    public static boolean checkTextLength(String text) {
        return text.length() <= MAX_CHARACTERS;
    }

    public static int getMaxCharacters() {
        return MAX_CHARACTERS;
    }
} 