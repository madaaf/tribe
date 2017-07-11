package com.tribe.tribelivesdk.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by tiago on 11/07/2017.
 */

public class FileUtils {

  public static void copyFolderFromAssets(Context context, String fromPath, String toPath) {
    AssetManager assetManager = context.getResources().getAssets();
    String[] files = null;
    try {
      files = assetManager.list(fromPath);
    } catch (Exception e) {
      Log.e("read ulsdata ERROR", "" + fromPath + " : " + e.toString());
      e.printStackTrace();
    }
    if (files != null) {
      for (String file : files) {
        InputStream in;
        OutputStream out;
        try {
          File targetFile = new File(toPath + file);
          if (!targetFile.exists()) {
            in = assetManager.open(fromPath + "/" + file);
            out = new FileOutputStream(toPath + file);
            copyFile(in, out);
            in.close();
            out.flush();
            out.close();
          }
        } catch (Exception e) {
          Log.e("copy ulsdata ERROR", e.toString());
          e.printStackTrace();
        }
        Log.d("copy ", "" + fromPath + "/" + file);
      }
    }
  }

  public static void copyFile(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[1024];
    int read;
    while ((read = in.read(buffer)) != -1) {
      out.write(buffer, 0, read);
    }
  }
}
