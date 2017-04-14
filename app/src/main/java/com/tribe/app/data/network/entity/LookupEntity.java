package com.tribe.app.data.network.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Created by tiago on 13/04/2017.
 */

public class LookupEntity {

  @SerializedName("n") private String phone;
  @SerializedName("fn") private String firstName;
  @SerializedName("ln") private String lastName;

  public LookupEntity(String phone, String firstName, String lastName) {
    this.phone = phone;
    this.firstName = firstName;
    this.lastName = lastName;
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
}
