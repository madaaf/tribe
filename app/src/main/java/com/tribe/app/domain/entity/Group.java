package com.tribe.app.domain.entity;

import java.util.Date;

/**
 * Created by tiago on 30/05/2016.
 */
public class Group extends Recipient {

    public Group(String id) {
        this.id = id;
    }

    private String id;
    private String picture;
    private String name;

    @Override
    public String getProfilePicture() {
        return picture;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getFriendshipId() {
        return id;
    }

    @Override
    public String getUsername() {
        return "";
    }

    @Override
    public String getUsernameDisplay() {
        return "";
    }

    @Override
    public Date getUpdatedAt() {
        return updated_at;
    }
}
