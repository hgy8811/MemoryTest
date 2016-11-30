package com.ericpeng.memorytest.res;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Gravity;

import java.io.File;

public class BitmapUtil {

    public static Bitmap decodeFile(String filePath) {
        return decodeFile(filePath, 0, 0);
    }

    public static Bitmap decodeFile(String filePath, int width, int height) {
        return decodeFile(filePath, width, height, Gravity.CENTER);
    }

    public static Bitmap decodeFile(String filePath, int width, int height, int gravity) {
        File file = new File(filePath);

        if (file == null || !file.exists() || file.isDirectory()) return null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        options.inSampleSize = calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;

        Bitmap originBitmap = null;
        try {
            originBitmap = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }

        if (gravity == 0 || width == 0 || height == 0) return originBitmap;

        if (originBitmap != null) {
            float reqWH = (float)width / height;
            float bitmapWH = (float)originBitmap.getWidth() / originBitmap.getHeight();
            if (reqWH == bitmapWH) return originBitmap;

            if (reqWH > bitmapWH) {
                switch (gravity) {
                    case Gravity.TOP:
                        return Bitmap.createBitmap(originBitmap, 0, 0, originBitmap.getWidth(), (int) (originBitmap.getWidth() / reqWH));
                    case Gravity.CENTER:
                        return Bitmap.createBitmap(originBitmap, 0, (originBitmap.getHeight() - (int) (originBitmap.getWidth() / reqWH)) / 2,
                                originBitmap.getWidth(), (int) (originBitmap.getWidth() / reqWH));
                    case Gravity.BOTTOM:
                        return Bitmap.createBitmap(originBitmap, 0, originBitmap.getHeight() - (int) (originBitmap.getWidth() / reqWH),
                                originBitmap.getWidth(), (int) (originBitmap.getWidth() / reqWH));
                }
            } else {
                switch (gravity) {
                    case Gravity.LEFT:
                        return Bitmap.createBitmap(originBitmap, 0, 0, (int) (originBitmap.getHeight() * reqWH) , originBitmap.getHeight());
                    case Gravity.CENTER:
                        return Bitmap.createBitmap(originBitmap, 0, 0, originBitmap.getWidth() - (int) (originBitmap.getHeight() * reqWH) / 2,
                                originBitmap.getHeight());
                    case Gravity.RIGHT:
                        return Bitmap.createBitmap(originBitmap, 0, 0, originBitmap.getWidth() - (int) (originBitmap.getHeight() * reqWH),
                                originBitmap.getHeight());
                }
            }
        }

        return originBitmap;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        if (reqHeight == 0 || reqHeight == 0) return 1;

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


}
