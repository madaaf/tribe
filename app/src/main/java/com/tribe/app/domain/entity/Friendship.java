package com.tribe.app.domain.entity;

import java.util.Date;

/**
 * Created by tiago on 04/05/2016.
 */
public class Friendship {

    public static final String ID_EMPTY = "EMPTY";

    private String id;

    public Friendship(String id) {
        this.id = id;
    }

    private Date createdAt;
    private Date updatedAt;

    private int position;
    private Tribe tribe;

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

    public void setTribe(Tribe tribe) {
        this.tribe = tribe;
    }

    public Tribe getTribe() {
        return tribe;
    }
}
