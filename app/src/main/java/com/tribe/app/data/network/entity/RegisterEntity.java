package com.tribe.app.data.network.entity;

/**
 * Created by tiago on 19/05/2016.
 */
public class RegisterEntity {

  private String displayName;
  private String username;
  private String countryCode;
  private String phoneNumber;
  private String password;
  private String pinId;

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getPinId() {
    return pinId;
  }

  public void setPinId(String pinId) {
    this.pinId = pinId;
  }
}
