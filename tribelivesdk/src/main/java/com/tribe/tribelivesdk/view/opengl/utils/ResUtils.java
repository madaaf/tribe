package com.tribe.tribelivesdk.view.opengl.utils;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;

public class ResUtils {
  static String packageName = "";

  public static int getResourceId(Context paramContext, ResType paramResType, String paramString) {
    Resources localResources = paramContext.getResources();

    if (TextUtils.isEmpty(packageName)) {
      packageName = paramContext.getPackageName();
    }

    int i = localResources.getIdentifier(paramString, paramResType.toString(), packageName);

    if (i <= 0) {
      throw new RuntimeException(
          "packageName=" + packageName + " type=" + paramResType + " name=" + paramString);
    }

    return i;
  }

  public enum ResType {drawable, layout, mipmap, raw}
}
