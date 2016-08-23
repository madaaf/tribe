package com.tribe.app.domain.entity;

import java.io.Serializable;

/**
 * Created by tiago on 04/08/2016.
 */
public class MessageRecipient implements Serializable {

    private String id;
    private String to;
    private boolean isSeen;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setIsSeen(boolean isSeen) {
        this.isSeen = isSeen;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
