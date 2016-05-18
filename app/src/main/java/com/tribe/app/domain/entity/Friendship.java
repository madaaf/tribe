package com.tribe.app.domain.entity;

import java.util.Date;

/**
 * Created by tiago on 04/05/2016.
 */
public class Friendship {

    private int id;

    public Friendship(int id) {
        this.id = id;
    }

    private User friend;
    private Date createdAt;
    private Date updatedAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getFriend() {
        return friend;
    }

    public void setFriend(User friend) {
        this.friend = friend;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("***** Friendship Details *****\n");
        stringBuilder.append("id = " + id);
        stringBuilder.append("friend = " + friend);
        stringBuilder.append("createdAt = " + createdAt);
        stringBuilder.append("updatedAt = " + updatedAt);
        stringBuilder.append("*******************************");

        return stringBuilder.toString();
    }
}
