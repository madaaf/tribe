package com.tribe.app.data.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by tiago on 05/09/2016.
 */
public class PhoneRealm extends RealmObject {

  @PrimaryKey private String phone;
  private boolean isInternational = false;

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getPhone() {
    return phone;
  }

  public void setInternational(boolean international) {
    isInternational = international;
  }

  public boolean isInternational() {
    return isInternational;
  }
}
