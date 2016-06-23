package com.tribe.app.domain.entity;

import java.util.Date;

/**
 * Created by tiago on 04/05/2016.
 */
public class Friendship {

    private String id;

    public Friendship(String id) {
        this.id = id;
    }

    private Date createdAt;
    private Date updatedAt;

    private int position;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
