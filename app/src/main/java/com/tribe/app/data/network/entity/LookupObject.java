package com.tribe.app.data.network.entity;

import com.google.gson.annotations.SerializedName;
import com.tribe.app.data.realm.UserRealm;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tiago on 13/04/2017.
 */

public class LookupObject {

  @SerializedName("n") private String phone;
  @SerializedName("fb") private String fbId;
  @SerializedName("u") private String userId;
  @SerializedName("f") private int howManyFriends = 0;
  @SerializedName("ln") private String lastName;
  @SerializedName("fn") private String firstName;
  @SerializedName("e") private String email;
  @SerializedName("ff") private List<String> commonFriends;

  private List<String> commonFriendsNameList;

  private UserRealm userRealm;

  public List<String> getCommonFriends() {
    return commonFriends;
  }

  public String getFbId() {
    return fbId;
  }

  public void setFbId(String fbId) {
    this.fbId = fbId;
  }

  public void setCommonFriends(List<String> commonFriends) {
    this.commonFriends = commonFriends;
  }

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

  public void addFriendsDisplayName(String displayName) {
    if(commonFriendsNameList==null){
      commonFriendsNameList = new ArrayList<>();
    }
    commonFriendsNameList.add(displayName);
  }

  public List<String> getcommonFriendsNameList() {
    return commonFriendsNameList;
  }

  @Override public String toString() {
    return "LookupObject{"
        + "phone='"
        + phone
        + '\''
        + ", userId='"
        + userId
        + '\''
        + ", howManyFriends="
        + howManyFriends
        + ", lastName='"
        + lastName
        + '\''
        + ", firstName='"
        + firstName
        + '\''
        + '}';
  }
}
