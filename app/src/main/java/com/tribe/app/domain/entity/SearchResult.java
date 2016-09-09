package com.tribe.app.domain.entity;

import java.io.Serializable;

/**
 * Created by tiago on 08/09/2016.
 */
public class SearchResult implements Serializable {

    private String display_name;
    private String username;
    private String picture;
    private Friendship friendship;

    public String getDisplayName() {
        return display_name;
    }

    public void setDisplayName(String displayName) {
        this.display_name = displayName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public Friendship getFriendship() {
        return friendship;
    }

    public void setFriendship(Friendship friendship) {
        this.friendship = friendship;
    }
}
