package com.tribe.app.presentation.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.AnyRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by tiago on 28/06/2016.
 */
@Singleton public class FileUtils {

  @StringDef({ VIDEO, PHOTO, ZIP }) public @interface Type {
  }

  public static final String VIDEO = "video";
  public static final String PHOTO = "photo";
  public static final String ZIP = "zip";

  private static String pathEnd = "/Tribe/Sent";
  private static String pathGameTemp = "/Tribe/Games";
  private static String pathSave = "/Tribe";
  private static String pathAvatarTemp = "/Tribe/Avatars/";

  @Inject public FileUtils() {
  }

  public static String generateFile(Context context, String id, @Type String type) {
    return getFile(context, id, type).getAbsolutePath();
  }

  public static File getFile(Context context, String id, @Type String type) {
    File endDir = new File(context.getFilesDir() + pathEnd);

    if (!endDir.exists()) {
      endDir.mkdirs();
    }

    return generateOutputFile(endDir, id, type);
  }

  public static File getFileTemp(Context context, String id, @Type String type) {
    File endDir = new File(context.getFilesDir() + pathGameTemp);

    if (!endDir.exists()) {
      endDir.mkdirs();
    }

    return generateOutputFile(endDir, id, type);
  }

  public static File generateOutputFile(File dir, String id, @Type String type) {
    return new File(dir, getTribeFilenameForId(id, type));
  }

  public static String getPathForId(Context context, String id, @Type String type) {
    File endDir = new File(getCacheDir(context) + pathEnd);
    return generateOutputFile(endDir, id, type).getAbsolutePath();
  }

  public static String getTribeFilenameForId(String id, @Type String type) {
    String filename = id;

    if (type.equals(PHOTO)) {
      filename += ".jpeg";
    } else if (type.equals(VIDEO)) {
      filename += ".mp4";
    } else if (type.equals(ZIP)) filename += ".zip";

    return filename;
  }

  public static void delete(Context context, String id, @Type String type) {
    File endDir = new File(getCacheDir(context) + pathEnd);
    generateOutputFile(endDir, id, type).delete();
  }

  public static String generateIdForMessage() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  public static void copyInputStreamToFile(InputStream in, File file) throws IOException {
    OutputStream out = new FileOutputStream(file);
    byte[] buf = new byte[10 * 1024];
    int len;

    while ((len = in.read(buf)) > 0) {
      out.write(buf, 0, len);
    }

    out.close();
    in.close();
  }

  public static File bitmapToFile(String name, Bitmap bitmap, Context context) {
    File f = new File(context.getCacheDir(), name);
    try {
      f.createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return bitmapToFile(bitmap, f);
  }

  public static File bitmapToFilePublic(String name, Bitmap bitmap, Context context) {
    File dir = new File(Environment.getExternalStorageDirectory() + "/Tribe");

    if (!dir.exists()) {
      dir.mkdirs();
    }

    File f = new File(dir, name);
    try {
      f.createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return bitmapToFile(bitmap, f);
  }

  public static File bitmapToFile(Bitmap bitmap, File file) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
    byte[] bitmapData = bos.toByteArray();

    try {
      if (!file.exists()) file.createNewFile();

      FileOutputStream fos = new FileOutputStream(file);
      fos.write(bitmapData);
      fos.close();
      fos.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return file;
  }

  public static boolean deleteDir(File dir) {
    if (dir != null && dir.isDirectory()) {
      String[] children = dir.list();
      for (int i = 0; i < children.length; i++) {
        boolean success = deleteDir(new File(dir, children[i]));
        if (!success) {
          return false;
        }
      }
    }

    return dir.delete();
  }

  public static void copyFile(String inputPath, String outputPath) {
    InputStream in = null;
    OutputStream out = null;

    try {
      in = new FileInputStream(inputPath);
      out = new FileOutputStream(outputPath);

      byte[] buffer = new byte[1024];
      int read;
      while ((read = in.read(buffer)) != -1) {
        out.write(buffer, 0, read);
      }
      in.close();
      in = null;

      out.flush();
      out.close();
      out = null;
    } catch (FileNotFoundException fnfe1) {
      Log.e("tag", fnfe1.getMessage());
    } catch (Exception e) {
      Log.e("tag", e.getMessage());
    }
  }

  public static File getAvatarForGroupId(Context context, String id, @Type String type) {
    File endDir = new File(getCacheDir(context) + pathAvatarTemp);

    if (!endDir.exists()) {
      endDir.mkdirs();
    }

    return generateOutputFile(endDir, id, type);
  }

  public static File getCacheDir(Context context) {
    return context.getFilesDir();
  }

  /**
   * get uri to drawable or any other resource type if u wish
   *
   * @param context - context
   * @param drawableId - drawable res id
   * @return - uri
   */
  public static final Uri getUriToDrawable(@NonNull Context context, @AnyRes int drawableId) {
    Uri imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
        "://" +
        context.getResources().getResourcePackageName(drawableId) +
        '/' +
        context.getResources().getResourceTypeName(drawableId) +
        '/' +
        context.getResources().getResourceEntryName(drawableId));
    return imageUri;
  }
}
