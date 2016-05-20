package com.tribe.app.data.network.entity;

/**
 * Created by tiago on 19/05/2016.
 */
public class LoginEntity {

    String username;
    String password;
    String grant_type;

    public LoginEntity(String username, String password, String grantType) {
        this.username = username;
        this.password = password;
        this.grant_type = grantType;
    }
}
