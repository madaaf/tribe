package com.tribe.tribelivesdk.model;

/**
 * Created by tiago on 16/01/2017.
 */

public class TribePeerMediaConfiguration {

  private TribeSession session;
  private boolean audioEnabled = true, videoEnabled = true;

  public TribePeerMediaConfiguration(TribeSession tribeSession) {
    session = tribeSession;
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
}
