package com.tribe.app.domain.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by tiago on 21/11/2016.
 */

public class NewGroupEntity implements Serializable {

    private String name;
    private String imgPath;
    private List<String> membersId;

    public NewGroupEntity(String name, String imgPath) {
        this.name = name;
        this.imgPath = imgPath;
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
}
