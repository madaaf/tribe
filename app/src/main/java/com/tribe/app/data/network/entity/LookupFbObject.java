package com.tribe.app.data.network.entity;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tiago on 13/04/2017.
 */

public class LookupFbObject {

  @SerializedName("fb") private String n;
  @SerializedName("u") private String userId;
  @SerializedName("f") private int howManyFriends = 0;
  @SerializedName("ln") private String lastName;
  @SerializedName("fn") private String firstName;
  @SerializedName("e") private String email;
  @SerializedName("ff") private List<String> commonFriends;


  private List<String> commonFriendsNameList;

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public int getHowManyFriends() {
    return howManyFriends;
  }

  public void setHowManyFriends(int howManyFriends) {
    this.howManyFriends = howManyFriends;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public List<String> getCommonFriends() {
    return commonFriends;
  }

  public void setCommonFriends(List<String> commonFriends) {
    this.commonFriends = commonFriends;
  }

  public String getN() {
    return n;
  }

  public void setN(String n) {
    this.n = n;
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
}
