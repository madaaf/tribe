package com.tribe.tribelivesdk.model;

import org.webrtc.SessionDescription;

/**
 * Created by tiago on 16/01/2017.
 */

public class TribeOffer {

    private SessionDescription sessionDescription;
    private String id;

    public TribeOffer(String id, SessionDescription sessionDescription) {
        this.id = id;
        this.sessionDescription = sessionDescription;
    }

    public void setSessionDescription(SessionDescription sessionDescription) {
        this.sessionDescription = sessionDescription;
    }

    public SessionDescription getSessionDescription() {
        return sessionDescription;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
