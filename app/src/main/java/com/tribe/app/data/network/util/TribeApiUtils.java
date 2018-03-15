package com.tribe.app.data.network.util;

import android.content.Context;
import android.os.Build;
import com.tribe.app.presentation.view.utils.Constants;
import com.tribe.app.presentation.view.utils.DeviceUtils;
import com.tribe.app.presentation.view.utils.RemoteConfigManager;
import okhttp3.Request;

public class TribeApiUtils {

  public static String getUnixTimeStamp() {
    return String.valueOf(System.currentTimeMillis() / 1000L);
  }

  public static String getUserAgent(Context context) {
    String agent =
        RemoteConfigManager.getInstance(context).getString(Constants.FIREBASE_AGENT_VERSION, "");
    return context.getPackageName() +
        "/" +
        DeviceUtils.getVersionCode(context) +
        " android/" +
        Build.VERSION.RELEASE +
        " okhttp/3.9.1" +
        " Agent/" +
        agent;
  }

  public static void appendTribeHeaders(Context context, String userId,
      Request.Builder requestBuilder) {

    // User-Agent
    requestBuilder.header("User-Agent", TribeApiUtils.getUserAgent(context));

    // Platform
    requestBuilder.header("X-Tribe-Platform", "Android");

    // AppVersion
    requestBuilder.header("X-Tribe-AppVersion", "" + DeviceUtils.getVersionCode(context));

    // UserId
    if (userId != null) {
      requestBuilder.header("X-Tribe-UserId", userId);
    }
  }
}
