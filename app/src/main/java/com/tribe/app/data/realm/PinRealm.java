package com.tribe.app.data.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by tiago on 04/05/2016.
 */
public class PinRealm extends RealmObject {

  @PrimaryKey private String pinId;

  private String to;
  private String ncStatus;
  private String smsStatus;

  public String getPinId() {
    return pinId;
  }

  public void setPinId(String pinId) {
    this.pinId = pinId;
  }

  public String getTo() {
    return to;
  }

  public void setTo(String to) {
    this.to = to;
  }

  public String getNcStatus() {
    return ncStatus;
  }

  public void setNcStatus(String ncStatus) {
    this.ncStatus = ncStatus;
  }

  public String getSmsStatus() {
    return smsStatus;
  }

  public void setSmsStatus(String smsStatus) {
    this.smsStatus = smsStatus;
  }
}
