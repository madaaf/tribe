package com.tribe.tribelivesdk.model;

import org.webrtc.MediaStream;

/**
 * Created by tiago on 16/01/2017.
 */

public class TribeMediaStream {

  private MediaStream mediaStream;
  private TribeSession session;

  public TribeMediaStream(TribeSession session, MediaStream mediaStream) {
    this.session = session;
    this.mediaStream = mediaStream;
  }

  public MediaStream getMediaStream() {
    return mediaStream;
  }

  public void setMediaStream(MediaStream mediaStream) {
    this.mediaStream = mediaStream;
  }

  public TribeSession getSession() {
    return session;
  }

  public void setSession(TribeSession session) {
    this.session = session;
  }
}
