package com.tribe.app.presentation.view.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import timber.log.Timber;

import static android.content.ContentValues.TAG;

/**
 * Created by madaaflak on 31/03/2017.
 */

public class BitmapUtils {

  private final static String TRIBE_DIRTECTORY = "Tribe";
  private final static String EXTERNAL_STORAGE_DEFAULT_DIRECTORY =
      Environment.getExternalStorageDirectory().toString();
  private final static String PATH_DEFAULT_DIRECTORY =
      EXTERNAL_STORAGE_DEFAULT_DIRECTORY + File.separator + TRIBE_DIRTECTORY + File.separator;

  public static void saveScreenshotToDefaultDirectory(Context context, Bitmap bitmap) {

    try {
      File defaultStorageDirectory = createDirectory(PATH_DEFAULT_DIRECTORY);
      saveBitmap(bitmap, getTimeStampFileName(defaultStorageDirectory), context);
      Timber.d("take screen shot " + getTimeStampFileName(defaultStorageDirectory).toString());
      context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
          Uri.parse("file://" + getTimeStampFileName(defaultStorageDirectory))));
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  ////////////////
  //   PRIVATE  //
  ////////////////

  private static void saveBitmap(Bitmap bitmap, File bitmapFile, Context context) {
    try {

      FileOutputStream outputStream = new FileOutputStream(bitmapFile);
      int quality = 100;
      bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
      outputStream.flush();
      outputStream.close();
      // Initiate media scanning to make the image available in gallery apps
      MediaScannerConnection.scanFile(context, new String[] { bitmapFile.getPath() },
          new String[] { "image/jpeg" }, null);
    } catch (FileNotFoundException e) {
      Timber.e(TAG, "File not found: " + e.getMessage());
    } catch (IOException e) {
      Timber.e(TAG, "Error accessing file: " + e.getMessage());
    }
  }

  private static File createDirectory(String mPath) {
    // Create the storage directory if it does not exist
    File myDir = new File(mPath);
    if (!myDir.exists()) {
      if (!myDir.mkdirs()) {
        Timber.d("create directory " + myDir.toString());
      }
    }
    return myDir;
  }

  private static File getTimeStampFileName(File mediaStorageDirectory) {
    Long timeStamp = System.currentTimeMillis();
    String ts = timeStamp.toString();

    File mediaFile;
    String mImageName = ts + ".jpg";
    mediaFile = new File(mediaStorageDirectory.getPath() + File.separator + mImageName);
    return mediaFile;
  }
}
