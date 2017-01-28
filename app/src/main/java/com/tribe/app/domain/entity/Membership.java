package com.tribe.app.domain.entity;

import java.util.Date;
import java.util.List;

public class Membership extends Recipient {

    private String id;
    private Group group;
    private boolean mute;
    private Date created_at;
    private Date updated_at;

    public Membership(String id) {
        this.id = id;
    }

    @Override
    public String getDisplayName() {
        return group.getName();
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public String getUsernameDisplay() {
        return null;
    }

    @Override
    public String getProfilePicture() {
        return group.getPicture();
    }

    @Override
    public String getSubId() {
        return group.getId();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Date getUpdatedAt() {
        return updated_at;
    }

    @Override
    public boolean isLive() {
        return false;
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public Date getLastOnline() {
        return null;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public boolean isMute() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public Date getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(Date createdAt) {
        this.created_at = createdAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updated_at = updatedAt;
    }

    public List<String> getMembersPic() {
        return group.getMembersPics();
    }
}