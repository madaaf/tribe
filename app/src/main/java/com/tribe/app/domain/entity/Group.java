package com.tribe.app.domain.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by tiago on 30/05/2016.
 */
public class Group implements Serializable {

    public Group(String id) {
        this.id = id;
    }

    private String id;
    private String picture;
    private String name;
    private boolean privateGroup;
    private List<User> members;
    private List<User> admins;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPrivateGroup() {
        return privateGroup;
    }

    public void setPrivateGroup(boolean privateGroup) {
        this.privateGroup = privateGroup;
    }

    public List<User> getMembers() {
        return members;
    }

    public void setMembers(List<User> members) {
        this.members = members;
    }

    public List<User> getAdmins() {
        return admins;
    }

    public void setAdmins(List<User> admins) {
        this.admins = admins;
    }
}
