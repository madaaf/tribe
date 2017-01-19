package com.tribe.tribelivesdk.model;

import org.webrtc.MediaStream;

/**
 * Created by tiago on 16/01/2017.
 */

public class TribeMediaStream {

    private MediaStream mediaStream;
    private String id;

    public TribeMediaStream(String id, MediaStream mediaStream) {
        this.id = id;
        this.mediaStream = mediaStream;
    }

    public MediaStream getMediaStream() {
        return mediaStream;
    }

    public void setMediaStream(MediaStream mediaStream) {
        this.mediaStream = mediaStream;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
