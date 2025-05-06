package com.omeryalap.myapplication;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class EncodeAndDecodeHelper {
    private static final double a = 3.6;
    private static final double b = 0.4;
    static ArrayList<Integer> siralar= new ArrayList<>();
    static double x0=0.1461114611461;

    public static int siraGetir(){
        x0 = chaoticMap(x0);
        int arasira=((int) (x0*10000000000000000.))%siralar.size();
        int sira =siralar.get(arasira);
        siralar.remove(arasira);
        return sira;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("SuspiciousIndentation")
    public static Bitmap Encode(Bitmap bitmap, String textToHide) {
        // Boyut kontrolü
        bitmap = ImageHelper.resizeIfNeeded(bitmap);
        
        // Karakter limiti kontrolü
        if (!ImageHelper.checkTextLength(textToHide)) {
            throw new IllegalArgumentException("Maksimum " + ImageHelper.getMaxCharacters() + " karakter girebilirsiniz!");
        }

        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];

        for(int i=0;i<pixels.length;i++)
            siralar.add(i);

        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        // ... existing code ...

        Bitmap bitmap2 = Bitmap.createBitmap(pixels, bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        // Görüntüyü ve indisleri Firebase'e kaydet (gizli resim olduğu için true)
        ImageHelper.saveToFirebase(bitmap2, siralar, true);

        // Kullanım örneği
        Bitmap yourBitmap = bitmap2; // Kaydetmek istediğiniz Bitmap nesnesi
        String fileName = "encoded.png"; // Dosya adı
        saveBitmapToStorage(yourBitmap, fileName);
        return yourBitmap;
    }
} 