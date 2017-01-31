package com.tribe.tribelivesdk.model;

import org.webrtc.SessionDescription;

/**
 * Created by tiago on 16/01/2017.
 */

public class TribeOffer {

  private SessionDescription sessionDescription;
  private TribeSession session;

  public TribeOffer(TribeSession session, SessionDescription sessionDescription) {
    this.session = session;
    this.sessionDescription = sessionDescription;
  }

  public void setSessionDescription(SessionDescription sessionDescription) {
    this.sessionDescription = sessionDescription;
  }

  public SessionDescription getSessionDescription() {
    return sessionDescription;
  }

  public TribeSession getSession() {
    return session;
  }

  public void setSession(TribeSession session) {
    this.session = session;
  }
}
