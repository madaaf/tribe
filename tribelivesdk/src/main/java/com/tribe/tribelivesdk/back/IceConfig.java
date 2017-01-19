package com.tribe.tribelivesdk.back;

/**
 * Created by tiago on 13/01/2017.
 */

public class IceConfig {

    private String url;
    private String username;
    private String credentials;

    public IceConfig(String url, String username, String credentials) {
        this.url = url;
        this.username = username;
        this.credentials = credentials;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getCredentials() {
        return credentials;
    }
}
