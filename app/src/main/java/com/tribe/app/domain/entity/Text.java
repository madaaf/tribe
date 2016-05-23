package com.tribe.app.domain.entity;

import java.util.Date;

/**
 * Created by tiago on 22/05/2016.
 *
 * Class that represents a Text in the domain layer.
 */
public class Text {

    private final int id;

    public Text(int id) {
        this.id = id;
    }

    private String text;
    private Date createdAt;
    private Date updatedAt;

    public int getId() {
        return id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("***** User Details *****\n");
        stringBuilder.append("id = " + id);
        stringBuilder.append("text = " + text);
        stringBuilder.append("createdAt = " + createdAt);
        stringBuilder.append("updatedAt = " + updatedAt);
        stringBuilder.append("*******************************");

        return stringBuilder.toString();
    }
}
