package com.tribe.app.presentation.view.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.NinePatchDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import com.tribe.app.R;
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

  private final static String TRIBE_DIRECTORY = "Tribe";
  private final static String EXTERNAL_STORAGE_DEFAULT_DIRECTORY =
      Environment.getExternalStorageDirectory().toString();
  private final static String PATH_DEFAULT_DIRECTORY =
      EXTERNAL_STORAGE_DEFAULT_DIRECTORY + File.separator + TRIBE_DIRECTORY + File.separator;

  public static Bitmap watermarkBitmap(ScreenUtils screenUtils, Resources res, Bitmap bitmap, Context context) {
    Bitmap bmOverlay =
        Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
    Bitmap watermark = BitmapFactory.decodeResource(res, R.drawable.watermark_tribe);

    Paint paint = new Paint();
    paint.setDither(true);
    paint.setAntiAlias(true);

    float left = (bitmap.getWidth() - watermark.getWidth()) >> 1;
    float margin = context.getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small);
    //  float top = (bitmap.getHeight() - watermark.getHeight());
    float top = (bitmap.getHeight() - watermark.getHeight() - margin);
    Canvas canvas = new Canvas(bmOverlay);
    canvas.drawBitmap(bitmap, 0, 0, paint);
    canvas.drawBitmap(watermark, left, top, paint);
    return bmOverlay;
  }

  public static boolean saveScreenshotToDefaultDirectory(Context context, Bitmap bitmap) {

    try {
      File defaultStorageDirectory = createDirectory(PATH_DEFAULT_DIRECTORY);
      File file = getTimeStampFileName(defaultStorageDirectory);
      saveBitmap(bitmap, file, context);
      Timber.d("take screen shot " + getTimeStampFileName(defaultStorageDirectory).toString());
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        final Uri contentUri = Uri.fromFile(file);
        scanIntent.setData(contentUri);
        context.sendBroadcast(scanIntent);
      } else {
        final Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED,
            Uri.parse("file://" + Environment.getExternalStorageDirectory()));
        context.sendBroadcast(intent);
      }
    } catch (Throwable e) {
      e.printStackTrace();
      return false;
    }

    if (bitmap != null) bitmap.recycle();

    return true;
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

  public Bitmap generateNewPostIt(Context context, String text, int textSize, int textColor,
      int backgroundId) {
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    paint.setTextSize(textSize);
    paint.setColor(textColor);
    paint.setTextAlign(Paint.Align.CENTER);
    float baseline = -paint.ascent();
    int width = (int) (paint.measureText(text) + 0.5f);
    int height = (int) (baseline + paint.descent() + 0.5f);
    Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    NinePatchDrawable bg = (NinePatchDrawable) ContextCompat.getDrawable(context, backgroundId);

    if (bg != null) {
      bg.setBounds(0, 0, width, height);
    }

    Canvas canvas = new Canvas(image);
    bg.draw(canvas);
    canvas.drawText(text, 0, baseline, paint);
    return image;
  }

  public static Bitmap generateGameIconWithBorder(Bitmap icon, int marginBorder) {
    Paint paintCircle = new Paint();
    paintCircle.setAntiAlias(true);
    paintCircle.setDither(true);
    paintCircle.setColor(Color.WHITE);

    Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    int size = icon.getWidth() + marginBorder;
    Bitmap dest =
        Bitmap.createBitmap(icon.getWidth() + marginBorder, icon.getHeight() + marginBorder,
            icon.getConfig());
    Canvas canvas = new Canvas(dest);
    canvas.drawCircle(size >> 1, size >> 1, size >> 1, paintCircle);
    canvas.drawBitmap(icon, (size - icon.getWidth()) >> 1, (size - icon.getHeight()) >> 1,
        bitmapPaint);
    return dest;
  }

  public static Bitmap bitmapFromResources(Resources resources, int resourceId) {
    return BitmapFactory.decodeResource(resources, resourceId);
  }
}
