package com.tribe.app.data.network.entity;

import com.google.gson.annotations.SerializedName;
import com.tribe.app.data.realm.UserRealm;

/**
 * Created by tiago on 13/04/2017.
 */

public class LookupObject {

  @SerializedName("n") private String phone;
  @SerializedName("u") private String userId;
  @SerializedName("f") private int howManyFriends;
  private UserRealm userRealm;

  public String getPhone() {
    return phone;
  }

  public String getUserId() {
    return userId;
  }

  public int getHowManyFriends() {
    return howManyFriends;
  }

  public void setUserRealm(UserRealm userRealm) {
    this.userRealm = userRealm;
  }

  public UserRealm getUserRealm() {
    return userRealm;
  }
}
