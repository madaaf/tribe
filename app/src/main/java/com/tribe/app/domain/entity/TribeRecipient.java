package com.tribe.app.domain.entity;

import java.io.Serializable;

/**
 * Created by tiago on 05/08/2016.
 */
public class TribeRecipient implements Serializable {

    private User to;
    private boolean isSeen;

    public User getTo() {
        return to;
    }

    public void setTo(User to) {
        this.to = to;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setIsSeen(boolean isSeen) {
        this.isSeen = isSeen;
    }
}
