package com.tribe.app.data.network.authorizer;

public class TribeAuthorizer {

    private String mApiClient;
    private String mApiSecret;

    public TribeAuthorizer(String apiClient, String apiSecret) {
        mApiClient = apiClient;
        mApiSecret = apiSecret;
    }

    public void setApiClient(String mApiClient) {
        this.mApiClient = mApiClient;
    }

    public void setApiSecret(String mApiSecret) {
        this.mApiSecret = mApiSecret;
    }

    public String getApiClient() {
        return mApiClient;
    }

    public String getApiSecret() {
        return mApiSecret;
    }
}
