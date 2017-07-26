package com.tribe.app.data.network.entity;

import com.facebook.AccessToken;

import java.io.Serializable;

/**
 * Created by tiago on 19/05/2016.
 */
public class LoginEntity implements Serializable {

  private String to;
  private String username;
  private String phoneNumber;
  private String password;
  private String pinId;
  private String countryCode;
  private String nationalNumber;
  private String call;
  private transient AccessToken fbAccessToken;

  public LoginEntity(String to, boolean shouldCall) {
    this.to = to;
    this.call = shouldCall ? "1" : "0";
  }

  public LoginEntity(String phoneNumber, String code, String pinId, AccessToken fbAccessToken) {
    this.username = phoneNumber;
    this.phoneNumber = phoneNumber;
    this.password = code;
    this.pinId = pinId;
    this.fbAccessToken = fbAccessToken;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getPinId() {
    return pinId;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setPinId(String pinId) {
    this.pinId = pinId;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public String getNationalNumber() {
    return nationalNumber;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public void setNationalNumber(String nationalNumber) {
    this.nationalNumber = nationalNumber;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public AccessToken getFbAccessToken() {
    return fbAccessToken;
  }

  public void setFbAccessToken(AccessToken fbAccessToken) {
    this.fbAccessToken = fbAccessToken;
  }
}
