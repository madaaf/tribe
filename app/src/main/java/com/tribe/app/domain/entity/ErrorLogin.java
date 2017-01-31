package com.tribe.app.domain.entity;

import java.io.Serializable;

/**
 * Created by tiago on 12/09/2016.
 */
public class ErrorLogin implements Serializable {

  private String pinId;
  private String msisdn;
  private boolean verified;
  private int attempsRemaining = 0;

  public String getPinId() {
    return pinId;
  }

  public void setPinId(String pinId) {
    this.pinId = pinId;
  }

  public String getMsisdn() {
    return msisdn;
  }

  public void setMsisdn(String msisdn) {
    this.msisdn = msisdn;
  }

  public boolean isVerified() {
    return verified;
  }

  public void setVerified(boolean verified) {
    this.verified = verified;
  }

  public int getAttempsRemaining() {
    return attempsRemaining;
  }

  public void setAttempsRemaining(int attempsRemaining) {
    this.attempsRemaining = attempsRemaining;
  }
}
