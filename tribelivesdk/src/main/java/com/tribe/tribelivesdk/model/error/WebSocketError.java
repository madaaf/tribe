package com.tribe.tribelivesdk.model.error;

/**
 * Created by tiago on 26/01/2017.
 */

public class WebSocketError {

    private String message;
    private String id;

    public WebSocketError(String id, String message) {
        this.id = id;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getId() {
        return id;
    }
}
