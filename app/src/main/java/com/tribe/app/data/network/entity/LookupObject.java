package com.tribe.app.data.network.entity;

import com.google.gson.annotations.SerializedName;
import com.tribe.app.data.realm.UserRealm;

/**
 * Created by tiago on 13/04/2017.
 */

public class LookupObject {

  @SerializedName("n") private String phone;
  @SerializedName("u") private String userId;
  @SerializedName("f") private int howManyFriends = 0;
  @SerializedName("ln") private String lastName;
  @SerializedName("fn") private String firstName;
  @SerializedName("e") private String email;

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

  public String getEmail() {
    return email;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setUserRealm(UserRealm userRealm) {
    this.userRealm = userRealm;
  }

  public UserRealm getUserRealm() {
    return userRealm;
  }
}
