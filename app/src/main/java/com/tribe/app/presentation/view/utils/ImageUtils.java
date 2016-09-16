package com.tribe.app.presentation.view.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Utility methods for manipulating images.
 * Created by horatiothomas on 8/25/16.
 */
public class ImageUtils {

    public static final int AVATAR_SIZE = 500;

    public static Bitmap loadFromPath(String imagePath) {
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        //inJustDecodeBounds = true <-- will not load the bitmap into memory
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / AVATAR_SIZE, photoH / AVATAR_SIZE);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
        return(bitmap);
    }

    public static Bitmap formatForUpload(Bitmap srcBmp) {
        srcBmp = centerCropBitmap(srcBmp);
        srcBmp = Bitmap.createScaledBitmap(srcBmp, AVATAR_SIZE, AVATAR_SIZE, true);
        return srcBmp;
    }

    public static Bitmap centerCropBitmap(Bitmap srcBmp) {
        Bitmap dstBmp;

        if (srcBmp.getWidth() >= srcBmp.getHeight()){
            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    srcBmp.getWidth() / 2 - srcBmp.getHeight() / 2,
                    0,
                    srcBmp.getHeight(),
                    srcBmp.getHeight()
            );
        } else {
            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    0,
                    srcBmp.getHeight()/2 - srcBmp.getWidth()/2,
                    srcBmp.getWidth(),
                    srcBmp.getWidth()
            );
        }

        srcBmp.recycle();

        return dstBmp;
    }

}
