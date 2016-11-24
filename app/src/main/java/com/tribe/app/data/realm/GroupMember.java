package com.tribe.app.data.realm;

import java.io.Serializable;

import io.realm.RealmObject;

/**
 * Created by tiago on 11/22/16.
 */
public class GroupMember extends RealmObject implements Serializable {

    private String id;

    public GroupMember(String id) {
        this.id = id;
    }

    public GroupMember() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}