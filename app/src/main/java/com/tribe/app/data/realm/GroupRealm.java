package com.tribe.app.data.realm;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by tiago on 30/05/2016.
 */
public class GroupRealm extends RealmObject {

    @PrimaryKey
    private String id;

    private String name;
    private String picture;
    private boolean privateGroup;
    private Date created_at;
    private Date updated_at;
    private RealmList<UserRealm> members;
    private RealmList<UserRealm> admins;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public boolean isPrivateGroup() {
        return privateGroup;
    }

    public void setPrivateGroup(boolean privateGroup) {
        this.privateGroup = privateGroup;
    }

    public Date getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(Date created_at) {
        this.created_at = created_at;
    }

    public Date getUpdatedAt() {
        return updated_at;
    }

    public void setUpdatedAt(Date updated_at) {
        this.updated_at = updated_at;
    }

    public RealmList<UserRealm> getMembers() {
        return members;
    }

    public void setMembers(RealmList<UserRealm> members) {
        this.members = members;
    }

    public RealmList<UserRealm> getAdmins() {
        return admins;
    }

    public void setAdmins(RealmList<UserRealm> admins) {
        this.admins = admins;
    }
}
