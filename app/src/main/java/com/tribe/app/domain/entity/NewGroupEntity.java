package com.tribe.app.domain.entity;

import java.io.Serializable;

/**
 * Created by tiago on 21/11/2016.
 */

public class NewGroupEntity implements Serializable {

    private String name;
    private String imgPath;

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
}
