package com.tribe.app.data.realm;

import java.io.Serializable;

import io.realm.RealmObject;

/**
 * Created by tiago on 11/22/16.
 */
public class GroupMemberRealm extends RealmObject implements Serializable {

    private String id;
    private String groupId;

    public GroupMemberRealm(String id, String groupId) {
        this.id = id;
        this.groupId = groupId;
    }

    public GroupMemberRealm() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupId() {
        return groupId;
    }
}