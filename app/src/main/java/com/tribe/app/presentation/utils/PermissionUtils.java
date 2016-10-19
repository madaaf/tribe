package com.tribe.app.presentation.utils;

import android.Manifest;
import android.content.Context;

import com.tbruyelle.rxpermissions.RxPermissions;

/**
 * Created by tiago on 06/10/2016.
 */
public class PermissionUtils {

    public static final String PERMISSION_COARSE = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final String PERMISSION_FINE = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String[] PERMISSIONS_LOCATION = new String[]{ PERMISSION_COARSE, PERMISSION_FINE };
    public static final String PERMISSION_READ_WRITE_EXTERNAL = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final String PERMISSION_CONTACTS = Manifest.permission.READ_CONTACTS;

    public static boolean hasPermissionsLocation(Context context) {
       return (!RxPermissions.getInstance(context).isGranted(PermissionUtils.PERMISSION_COARSE)
                || !RxPermissions.getInstance(context).isGranted(PERMISSION_FINE));
    }

    public static boolean hasPermissionContact(Context context) {
        return RxPermissions.getInstance(context).isGranted(PermissionUtils.PERMISSION_CONTACTS);
    }
}
