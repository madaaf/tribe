package com.tribe.app.data.network.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by tiago on 13/04/2017.
 */

public class LookupEntity {

  @SerializedName("n") private String phone;
  @SerializedName("fb") private String fbId;
  @SerializedName("fn") private String firstName;
  @SerializedName("ln") private String lastName;
  @SerializedName("e") private String email;

  public LookupEntity(String phone, String firstName, String lastName, String email) {
    this.phone = phone;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
  }

  public LookupEntity(String fbId) {
    this.fbId = fbId;
  }

  public String getFbId() {
    return fbId;
  }

  public void setFbId(String fbId) {
    this.fbId = fbId;
  }

  public String getPhone() {
    return phone;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getEmail() {
    return email;
  }
}
