package com.tribe.app.presentation.utils;

import android.Manifest;
import com.tbruyelle.rxpermissions.RxPermissions;

/**
 * Created by tiago on 06/10/2016.
 */
public class PermissionUtils {

  public static final String PERMISSION_COARSE = Manifest.permission.ACCESS_COARSE_LOCATION;
  public static final String PERMISSION_FINE = Manifest.permission.ACCESS_FINE_LOCATION;
  public static final String[] PERMISSIONS_LOCATION =
      new String[] { PERMISSION_COARSE, PERMISSION_FINE };
  public static final String PERMISSION_READ_WRITE_EXTERNAL =
      Manifest.permission.WRITE_EXTERNAL_STORAGE;
  public static final String PERMISSIONS_CONTACTS = Manifest.permission.READ_CONTACTS;
  public static final String[] PERMISSIONS_CAMERA = new String[] {
      Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,
      Manifest.permission.WRITE_EXTERNAL_STORAGE
  };
  public static final String[] PERMISSIONS_HOME = new String[] {
      Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,
      Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_COARSE, PERMISSION_FINE
  };
  public static final String[] PERMISSIONS_LIVE = new String[] {
      Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
  };

  public static boolean hasPermissionsContact(RxPermissions rxPermissions) {
    return rxPermissions.isGranted(PermissionUtils.PERMISSIONS_CONTACTS);
  }

  public static boolean hasPermissionsCamera(RxPermissions rxPermissions) {
    boolean hasAllPermissions = true;

    for (String permission : PERMISSIONS_CAMERA) {
      if (!rxPermissions.isGranted(permission)) hasAllPermissions = false;
    }

    return hasAllPermissions;
  }
}
