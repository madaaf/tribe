package com.tribe.tribelivesdk.model;

import java.util.List;
import org.webrtc.IceCandidate;

/**
 * Created by tiago on 16/01/2017.
 */

public class TribeCandidate {

  private IceCandidate iceCandidate;
  private List<IceCandidate> iceCandidateList;
  private TribeSession session;

  public TribeCandidate(TribeSession session, IceCandidate iceCandidate) {
    this.session = session;
    this.iceCandidate = iceCandidate;
  }

  public TribeCandidate(TribeSession tribeSession, List<IceCandidate> iceCandidateList) {
    this.session = tribeSession;
    this.iceCandidateList = iceCandidateList;
  }

  public IceCandidate getIceCandidate() {
    return iceCandidate;
  }

  public List<IceCandidate> getIceCandidateList() {
    return iceCandidateList;
  }

  public void setIceCandidateList(List<IceCandidate> iceCandidateList) {
    this.iceCandidateList = iceCandidateList;
  }

  public TribeSession getSession() {
    return session;
  }

  public void setSession(TribeSession session) {
    this.session = session;
  }
}
