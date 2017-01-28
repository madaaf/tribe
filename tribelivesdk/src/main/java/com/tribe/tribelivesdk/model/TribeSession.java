package com.tribe.tribelivesdk.model;

/**
 * Created by tiago on 26/01/2017.
 */

public class TribeSession {

    private String peerId;
    private String userId;

    public TribeSession(String peerId, String userId) {
        this.peerId = peerId;
        this.userId = userId;
    }

    public String getPeerId() {
        return peerId;
    }

    public String getUserId() {
        return userId;
    }
}