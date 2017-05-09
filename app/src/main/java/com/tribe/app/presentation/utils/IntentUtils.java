package com.tribe.app.presentation.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.tribe.app.R;
import com.tribe.app.presentation.view.activity.LiveActivity;

/**
 * Created by tiago on 15/03/2017.
 */

public class IntentUtils {

  public static final String FINISH = "finish";
  public static final String USER_REGISTERED = "USER_REGISTERED";

  public static Intent getLiveIntentFromURI(Context context, Uri uri, String source) {
    String path = uri.getPath();
    String host = uri.getHost();
    String scheme = uri.getScheme();
    String roomId = uri.getQueryParameter("roomId");
    String linkId, url, deepLinkScheme = context.getString(R.string.deeplink_host);

    if (!StringUtils.isEmpty(roomId)) {
      linkId = roomId;
      url = uri.getScheme() + "://" + host + "/" + linkId;
    } else {
      linkId = path.substring(1, path.length());
      if (deepLinkScheme.equals(scheme)) {
        url = StringUtils.getUrlFromLinkId(context, linkId);
      } else {
        url = uri.toString();
      }
    }

    if (host.startsWith(context.getString(R.string.web_host)) || deepLinkScheme.equals(scheme)) {
      return LiveActivity.getCallingIntent(context, linkId, url, source);
    }

    return null;
  }
}
