package com.tribe.app.domain.entity;

import java.io.Serializable;

/**
 * Created by tiago on 23/11/2016.
 */
public class GroupMember implements Serializable {

    private boolean ogMember = false;
    private boolean member = false;
    private boolean admin = false;
    private boolean friend = false;
    private boolean animateAdd = false;
    private User user;

    public GroupMember(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        GroupMember that = (GroupMember) o;

        return user.getId() != null ? user.getId().equals(that.user.getId()) : that.user.getId() == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (user.getId() != null ? user.getId().hashCode() : 0);
        return result;
    }

    public boolean isMember() {
        return member;
    }

    public boolean isOgMember() {
        return ogMember;
    }

    public void setOgMember(boolean ogMember) {
        this.ogMember = ogMember;
    }

    public void setMember(boolean member) {
        this.member = member;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isAnimateAdd() {
        return animateAdd;
    }

    public void setAnimateAdd(boolean animateAdd) {
        this.animateAdd = animateAdd;
    }

    public boolean isFriend() {
        return friend;
    }

    public void setFriend(boolean friend) {
        this.friend = friend;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
