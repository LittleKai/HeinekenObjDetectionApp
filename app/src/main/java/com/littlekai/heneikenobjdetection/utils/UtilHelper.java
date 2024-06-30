package com.littlekai.heneikenobjdetection.utils;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.media.Image;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.littlekai.heneikenobjdetection.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class UtilHelper {
    private static final String TAG = "Kai";

    private void imageToBitmap(Image image) {
//    Image image = reader.acquireLatestImage();
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
    }

    public static Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > ratioBitmap) {
                finalWidth = (int) ((float) maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float) maxWidth / ratioBitmap);
            }

            return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, false);
        }
        return bitmap;
    }

    public static RectF fixLocation(RectF location, Bitmap bitmap) {
        float x1 = location.left;
        float y1 = location.top;
        float x2 = location.right;
        float y2 = location.bottom;
        if (x1 < 0) x1 = 1;
        if (y1 < 0) y1 = 1;
        if (x2 >= bitmap.getWidth())
            x2 = (float) bitmap.getWidth() - 1;
        if (y2 >= bitmap.getHeight())
            y2 = (float) bitmap.getHeight() - 1;
//        Log.d(TAG, "fixLocation: "+ new RectF(x1, y1, x2, y2));
        return new RectF(x1, y1, x2, y2);

    }

    public static boolean checkAndRequestPermissions(Context context) {
        int writePermission = ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int cameraPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (writePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions((Activity) context, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 123);
            return false;
        }
        return true;
    }

    static public float limitDecimalPlaces(float value) {
        // Sử dụng DecimalFormat để định dạng số
        DecimalFormat df = new DecimalFormat("0.00");

        // Chuyển đổi giá trị score sang chuỗi
        String formattedScore = df.format(value);

        // Chuyển đổi chuỗi định dạng sang float và trả về
        return Float.parseFloat(formattedScore);
    }


}
