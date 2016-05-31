package com.tribe.app.data.network.authorizer;

import com.tribe.app.data.realm.AccessToken;

public class TribeAuthorizer {

    private String apiClient;
    private String apiSecret;
    private AccessToken accessToken;

    public TribeAuthorizer(String apiClient, String apiSecret, AccessToken accessToken) {
        this.apiClient = apiClient;
        this.apiSecret = apiSecret;
        this.accessToken = accessToken;
    }

    public void setApiClient(String mApiClient) {
        this.apiClient = mApiClient;
    }

    public void setApiSecret(String mApiSecret) {
        this.apiSecret = mApiSecret;
    }

    public String getApiClient() {
        return apiClient;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public AccessToken getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(AccessToken accessToken) {
        this.accessToken = accessToken;
    }
}
