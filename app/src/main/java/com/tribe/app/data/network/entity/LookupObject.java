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
  @SerializedName("u") private String userId;
  @SerializedName("f") private int howManyFriends = 0;
  @SerializedName("ln") private String lastName;
  @SerializedName("fn") private String firstName;
  @SerializedName("e") private String email;
  @SerializedName("ff") private List<String> friends;

  private List<String> friendsNameList;

  private UserRealm userRealm;

  public List<String> getFriends() {
    return friends;
  }

  public void setFriends(List<String> friends) {
    this.friends = friends;
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
    if(friendsNameList==null){
      friendsNameList = new ArrayList<>();
    }
    friendsNameList.add(displayName);
  }

  public List<String> getFriendsNameList() {
    return friendsNameList;
  }

}
