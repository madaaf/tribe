package com.tribe.tribelivesdk.model;

/**
 * Created by tiago on 26/01/2017.
 */

public class TribeSession {

  public static final String PUBLISHER_ID = "publisher";
  public static final String ANONYMOUS = "anonymous";
  public static final String WEB_ID = "anon__";

  private String peerId;
  private String userId;
  private boolean isExternal = false;

  public TribeSession(String peerId, String userId) {
    this.peerId = peerId;
    this.userId = userId;

    if (userId == null || userId.equals("") || userId.startsWith(WEB_ID)) {
      isExternal = true;
      this.userId = peerId;
    }
  }

  public String getPeerId() {
    return peerId;
  }

  public String getUserId() {
    return userId;
  }

  public boolean isExternal() {
    return isExternal;
  }
}
