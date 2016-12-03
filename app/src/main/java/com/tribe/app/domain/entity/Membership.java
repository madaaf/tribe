package com.tribe.app.domain.entity;

import java.util.Date;
import java.util.List;

public class Membership extends Recipient {

    private String id;
    private Group group;
    private boolean mute;
    private boolean is_admin;
    private String link;
    private Date link_expires_at;
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
    public void setScore(int score) {

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

    public boolean isAdmin() {
        return is_admin;
    }

    public void setIsAdmin(boolean is_admin) {
        this.is_admin = is_admin;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Date getLink_expires_at() {
        return link_expires_at;
    }

    public void setLink_expires_at(Date link_expires_at) {
        this.link_expires_at = link_expires_at;
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