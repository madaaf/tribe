package com.tribe.app.data.realm;

import javax.inject.Singleton;

import io.realm.RealmObject;

@Singleton
public class AccessToken extends RealmObject {

    private String access_token;
    private String token_type;
    private String refresh_token;
    private String user_id;

    public String getAccessToken() {
        return access_token;
    }

    public void setAccessToken(String access_token) {
        this.access_token = access_token;
    }

    public String getTokenType() {
        if (!Character.isUpperCase(token_type.charAt(0))) {
            token_type = Character.toString(token_type.charAt(0)).toUpperCase() + token_type.substring(1);
        }

        return token_type;
    }

    public void setTokenType(String token_type) {
        this.token_type = token_type;
    }

    public String getRefreshToken() {
        return refresh_token;
    }

    public void setRefreshToken(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public String getUserId() {
        return user_id;
    }

    public void setUserId(String user_id) {
        this.user_id = user_id;
    }

    public void clear() {
        access_token = null;
        token_type = null;
        refresh_token = null;
        user_id = null;
    }
}
