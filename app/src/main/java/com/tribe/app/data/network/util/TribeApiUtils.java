package com.tribe.app.data.network.util;

import android.content.Context;
import android.os.Build;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.tribe.app.presentation.view.utils.Constants;
import com.tribe.app.presentation.view.utils.DeviceUtils;

public class TribeApiUtils {

  public static String getUnixTimeStamp() {
    return String.valueOf(System.currentTimeMillis() / 1000L);
  }

  public static String getUserAgent(Context context) {
    String agent = FirebaseRemoteConfig.getInstance() != null ? FirebaseRemoteConfig.getInstance()
        .getString(Constants.FIREBASE_AGENT_VERSION) : "";
    return context.getPackageName()
        + "/"
        + DeviceUtils.getVersionCode(context)
        + " android/"
        + Build.VERSION.RELEASE
        + " okhttp/3.2"
        + " Agent/"
        + agent;
  }
}
