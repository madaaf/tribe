package com.tribe.app.presentation.view.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

public class RuntimePermissionUtil {

  public final static String[] requestWritePermission =
      { Manifest.permission.WRITE_EXTERNAL_STORAGE };

  private RuntimePermissionUtil() {

  }

  public static boolean checkPermission(Context context, Activity activity) {
    boolean hasWritePermission = RuntimePermissionUtil.checkPermissonGranted(context,
        Manifest.permission.WRITE_EXTERNAL_STORAGE);

    if (!hasWritePermission) {
      RuntimePermissionUtil.requestPermission(activity,
          RuntimePermissionUtil.requestWritePermission, 100);
    }

    return hasWritePermission;
  }

  public static void onRequestPermissionsResult(int[] grantResults,
      RPResultListener RPResultListener) {
    if (grantResults.length > 0) {
      for (int i = 0; i < grantResults.length; i++) {
        if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
          RPResultListener.onPermissionGranted();
        } else {
          RPResultListener.onPermissionDenied();
        }
      }
    }
  }

  public static void requestPermission(final Activity activity, final String[] permissions,
      final int REQUEST_CODE) {
    // No explanation needed, we can request the permission.
    ActivityCompat.requestPermissions(activity, permissions, REQUEST_CODE);
  }

  public static boolean checkPermissonGranted(Context context, String permission) {
    return (ActivityCompat.checkSelfPermission(context, permission)
        == PackageManager.PERMISSION_GRANTED);
  }

  public interface RPResultListener {

    void onPermissionGranted();

    void onPermissionDenied();
  }
}
