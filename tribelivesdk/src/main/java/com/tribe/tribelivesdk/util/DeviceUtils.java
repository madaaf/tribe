package com.tribe.tribelivesdk.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

/**
 * Created by tiago on 21/06/2017.
 */

public class DeviceUtils {

  public static String getDeviceName() {
    String manufacturer = Build.MANUFACTURER;
    String model = Build.MODEL;
    if (model.startsWith(manufacturer)) {
      return capitalize(model);
    } else {
      return capitalize(manufacturer) + " " + model;
    }
  }

  private static String capitalize(String s) {
    if (s == null || s.length() == 0) {
      return "";
    }
    char first = s.charAt(0);
    if (Character.isUpperCase(first)) {
      return s;
    } else {
      return Character.toUpperCase(first) + s.substring(1);
    }
  }

  public static String getVersionName(Context context) {
    PackageManager manager = context.getPackageManager();
    PackageInfo info = null;
    String versionName = "UNKNOWN";

    try {
      info = manager.getPackageInfo(context.getPackageName(), 0);
      if (info != null) versionName = info.versionName;
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }

    return versionName;
  }

  public static int getVersionCode(Context context) {
    PackageManager manager = context.getPackageManager();
    PackageInfo info = null;
    int versionCode = 0;

    try {
      info = manager.getPackageInfo(context.getPackageName(), 0);
      if (info != null) versionCode = info.versionCode;
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }

    return versionCode;
  }

  public static String getNetworkType(Context context) {
    String network_type = "mobile";
    NetworkInfo active_network = ((ConnectivityManager) context.getSystemService(
        Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

    if (active_network != null && active_network.isConnectedOrConnecting()) {
      if (active_network.getType() == ConnectivityManager.TYPE_WIFI) {
        network_type = "wifi";
      }
    }

    return network_type;
  }
}

