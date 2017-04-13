package com.tribe.app.data.network.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by tiago on 13/04/2017.
 */

public class LookupObject {

  @SerializedName("n") private String phone;
  @SerializedName("u") private String userId;
  @SerializedName("f") private int howManyFriends;

  public String getPhone() {
    return phone;
  }

  public String getUserId() {
    return userId;
  }

  public int getHowManyFriends() {
    return howManyFriends;
  }
}
