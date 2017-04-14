package com.tribe.app.presentation.view.utils;

import android.content.Context;
import com.tribe.app.R;
import com.tribe.tribelivesdk.model.TribePeerMediaConfiguration;

/**
 * Created by tiago on 03/11/2017.
 */

public class MediaConfigurationUtils {

  public static String getStateLabel(Context context, TribePeerMediaConfiguration mediaConfiguration) {
    if (mediaConfiguration == null
        || mediaConfiguration.getType() == null
        || mediaConfiguration.getType().equals(TribePeerMediaConfiguration.NONE)) {
      return "";
    }

    if (mediaConfiguration.getType().equals(TribePeerMediaConfiguration.USER_UPDATE)) {
      return context.getString(R.string.live_placeholder_camera_paused);
    } else if (mediaConfiguration.getType().equals(TribePeerMediaConfiguration.APP_IN_BACKGROUND)) {
      return context.getString(R.string.live_placeholder_app_in_background);
    } else if (mediaConfiguration.getType().equals(TribePeerMediaConfiguration.FPS_DROP)) {
      return context.getString(R.string.live_placeholder_fps_drops);
    } else if (mediaConfiguration.getType().equals(TribePeerMediaConfiguration.IN_CALL)) {
      return context.getString(R.string.live_placeholder_in_call);
    } else {
      return context.getString(R.string.live_placeholder_low_bandwidth);
    }
  }

  public static int getStateResource(TribePeerMediaConfiguration mediaConfiguration) {
    if (mediaConfiguration == null
        || mediaConfiguration.getType() == null
        || mediaConfiguration.getType().equals(TribePeerMediaConfiguration.NONE)
        || mediaConfiguration.getType().equals(TribePeerMediaConfiguration.FPS_DROP)) {
      return -1;
    }

    if (mediaConfiguration.getType().equals(TribePeerMediaConfiguration.USER_UPDATE)) {
      if (!mediaConfiguration.isAudioEnabled()) {
        return R.drawable.picto_in_call;
      } else {
        return -1;
      }
    } else if (mediaConfiguration.getType().equals(TribePeerMediaConfiguration.APP_IN_BACKGROUND)
        || mediaConfiguration.getType().equals(TribePeerMediaConfiguration.FPS_DROP)) {
      return -1;
    } else if (mediaConfiguration.getType().equals(TribePeerMediaConfiguration.IN_CALL)) {
      return R.drawable.picto_in_call;
    } else {
      return R.drawable.picto_poor_connection;
    }
  }
}
