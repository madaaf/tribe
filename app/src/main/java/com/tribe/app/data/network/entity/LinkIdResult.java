package com.tribe.app.data.network.entity;

import java.io.Serializable;

/**
 * Created by remy on 07/08/2017.
 */

public class LinkIdResult implements Serializable {

  private boolean unlinked;

  private boolean linked;

  private boolean verified;

  private String error;

  public boolean isUnlinked() {
    return unlinked;
  }

  public void setUnlinked(boolean unlinked) {
    this.unlinked = unlinked;
  }

  public boolean isLinked() {
    return linked;
  }

  public void setLinked(boolean linked) {
    this.linked = linked;
  }

  public boolean isVerified() {
    return verified;
  }

  public void setVerified(boolean verified) {
    this.verified = verified;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }
}
