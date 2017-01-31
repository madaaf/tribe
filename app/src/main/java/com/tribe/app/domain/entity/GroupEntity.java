package com.tribe.app.domain.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by tiago on 21/11/2016.
 */

public class GroupEntity implements Serializable {

    private String name;
    private String imgPath;
    private List<String> membersId;
    private boolean custom = false;

    public GroupEntity(String name, String imgPath) {
        this.name = name;
        this.imgPath = imgPath;
    }

    public GroupEntity() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public void setMembersId(List<String> membersId) {
        this.membersId = membersId;
    }

    public List<String> getMembersId() {
        return membersId;
    }

    public boolean isCustom() {
        return custom;
    }

    public void setCustom(boolean custom) {
        this.custom = custom;
    }
}
