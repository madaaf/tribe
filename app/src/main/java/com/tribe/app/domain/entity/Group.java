package com.tribe.app.domain.entity;

import com.tribe.app.presentation.utils.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
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
    private String groupLink;
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

    public String getGroupLink() {
        return groupLink;
    }

    public void setGroupLink(String groupLink) {
        this.groupLink = groupLink;
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

    public List<String> getMembersPics() {
        List<String> pics = new ArrayList<>();

        if (members != null) {
            List<User> subMembers = members.subList(Math.max(members.size() - 4, 0), members.size());

            if (subMembers != null) {
                for (User user : subMembers) {
                    String url = user.getProfilePicture();
                    if (!StringUtils.isEmpty(url))
                        pics.add(url);
                }
            }
        }

        return pics;
    }
}
