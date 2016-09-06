package com.tribe.app.data.network.entity;

/**
 * Created by tiago on 19/05/2016.
 */
public class RefreshEntity {

    private String refresh_token;

    public RefreshEntity(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public String getRefreshToken() {
        return refresh_token;
    }

    public void setRefreshToken(String refreshToken) {
        this.refresh_token = refreshToken;
    }
}
