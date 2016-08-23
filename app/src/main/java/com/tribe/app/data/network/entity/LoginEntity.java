package com.tribe.app.data.network.entity;

/**
 * Created by tiago on 19/05/2016.
 */
public class LoginEntity {

    private String to;
    private String username;
    private String password;
    private String pinId;

    public LoginEntity(String to) {
        this.to = to;
    }

    public LoginEntity(String phoneNumber, String code, String pinId) {
        this.username = phoneNumber;
        this.password = code;
        this.pinId = pinId;
    }
}
