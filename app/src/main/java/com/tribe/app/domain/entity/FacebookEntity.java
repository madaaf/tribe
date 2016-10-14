package com.tribe.app.domain.entity;

import java.io.Serializable;

/**
 * Created by tiago on 12/09/2016.
 */
public class FacebookEntity implements Serializable {

    private String id;
    private String profilePicture;
    private String username;
    private String name;

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
