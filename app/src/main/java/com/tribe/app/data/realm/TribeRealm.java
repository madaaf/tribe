package com.tribe.app.data.realm;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by tiago on 29/06/2016.
 */
public class TribeRealm extends RealmObject {


    @PrimaryKey
    private String localId;
    private String id;
    private UserRealm from;
    private String type;
    private UserRealm toUser;
    private GroupRealm toGroup;
    private Date recordedAt;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    public UserRealm getFrom() {
        return from;
    }

    public void setFrom(UserRealm from) {
        this.from = from;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UserRealm getToUser() {
        return toUser;
    }

    public void setToUser(UserRealm toUser) {
        this.toUser = toUser;
    }

    public GroupRealm getToGroup() {
        return toGroup;
    }

    public void setToGroup(GroupRealm toGroup) {
        this.toGroup = toGroup;
    }

    public Date getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Date recordedAt) {
        this.recordedAt = recordedAt;
    }
}
