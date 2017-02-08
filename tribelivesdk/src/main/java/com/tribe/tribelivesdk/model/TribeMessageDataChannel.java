package com.tribe.tribelivesdk.model;

import org.webrtc.MediaStream;

/**
 * Created by tiago on 16/01/2017.
 */

public class TribeMessageDataChannel {

  private TribeSession session;
  private String message;

  public TribeMessageDataChannel(TribeSession session, String message) {
    this.session = session;
    this.message = message;
  }

  public TribeSession getSession() {
    return session;
  }

  public void setSession(TribeSession session) {
    this.session = session;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
