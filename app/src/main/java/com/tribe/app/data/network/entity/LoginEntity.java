package com.tribe.app.data.network.entity;

/**
 * Created by tiago on 19/05/2016.
 */
public class LoginEntity {

    private String to;
    private String username;
    private String password;
    private String pinId;
    private String countryCode;
    private String nationalNumber;

    public LoginEntity(String to) {
        this.to = to;
    }

    public LoginEntity(String phoneNumber, String code, String pinId) {
        this.username = phoneNumber;
        this.password = code;
        this.pinId = pinId;
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
}
