package com.tribe.tribelivesdk.model;

import android.support.annotation.StringDef;

/**
 * Created by tiago on 16/01/2017.
 */

public class TribePeerMediaConfiguration {

  @StringDef({ USER_UPDATE, LOW_BANDWIDTH, IN_CALL, APP_IN_BACKGROUND, FPS_DROP, NONE })
  public @interface MediaConfigurationType {
  }

  public static final String USER_UPDATE = "user_update";
  public static final String LOW_BANDWIDTH = "low_bandwidth";
  public static final String IN_CALL = "in_call";
  public static final String APP_IN_BACKGROUND = "app_in_background";
  public static final String FPS_DROP = "fps_drop";
  public static final String NONE = "none";

  private TribeSession session;
  private boolean audioEnabled = true, videoEnabled = true;

  private @MediaConfigurationType String mediaConfigurationType;

  public TribePeerMediaConfiguration(TribeSession tribeSession) {
    session = tribeSession;
  }

  public TribePeerMediaConfiguration(TribePeerMediaConfiguration mediaConfiguration) {
    session = mediaConfiguration.getSession();
    update(mediaConfiguration);
  }

  public void update(TribePeerMediaConfiguration mediaConfiguration) {
    audioEnabled = mediaConfiguration.audioEnabled;
    videoEnabled = mediaConfiguration.videoEnabled;
    mediaConfigurationType = mediaConfiguration.getType();
  }

  public void setAudioEnabled(boolean audioEnabled) {
    this.audioEnabled = audioEnabled;
  }

  public void setVideoEnabled(boolean videoEnabled) {
    this.videoEnabled = videoEnabled;
  }

  public void setSession(TribeSession session) {
    this.session = session;
  }

  public TribeSession getSession() {
    return session;
  }

  public boolean isAudioEnabled() {
    return audioEnabled;
  }

  public boolean isVideoEnabled() {
    return videoEnabled;
  }

  public boolean isLowConnection() {
    return mediaConfigurationType != null && mediaConfigurationType.equals(FPS_DROP);
  }

  public @MediaConfigurationType String getType() {
    return mediaConfigurationType;
  }

  public void setMediaConfigurationType(@MediaConfigurationType String mediaConfigurationType) {
    this.mediaConfigurationType = mediaConfigurationType;
  }

  @MediaConfigurationType public static String computeType(String str) {
    if (str == null) return NONE;

    if (str.equals(USER_UPDATE)) {
      return USER_UPDATE;
    } else if (str.equals(APP_IN_BACKGROUND)) {
      return APP_IN_BACKGROUND;
    } else if (str.equals(FPS_DROP)) {
      return FPS_DROP;
    } else if (str.equals(IN_CALL)) {
      return IN_CALL;
    } else {
      return LOW_BANDWIDTH;
    }
  }

  @Override public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (session.getUserId() != null ? session.hashCode() : 0);
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || !(o instanceof TribePeerMediaConfiguration)) return false;

    TribePeerMediaConfiguration that = (TribePeerMediaConfiguration) o;

    if (session == null) {
      if (that.session != null) return false;
    } else if (!session.getUserId().equals(session.getUserId())) {
      return false;
    }

    if (!mediaConfigurationType.equals(that.mediaConfigurationType)) {
      return false;
    }

    if (audioEnabled != that.audioEnabled) {
      return false;
    }

    if (videoEnabled != that.videoEnabled) {
      return false;
    }

    return true;
  }
}
