package com.tribe.app.data.network.entity;

/**
 * Created by tiago on 19/05/2016.
 */
public class LoginEntity {

    private String to;
    private String grant_type;
    private String username;
    private String password;
    private String scope;

    public LoginEntity(String to) {
        this.to = to;
    }

    public LoginEntity(String phoneNumber, String code, String scope, String grant_type) {
        this.username = phoneNumber;
        this.password = code;
        this.scope = scope;
        this.grant_type = grant_type;
    }
}
