package com.tribe.app.domain.entity;

import java.util.Date;

/**
 * Created by tiago on 22/05/2016.
 *
 * Class that represents a Message in the domain layer.
 */
public class Message {

    private final int id;

    public Message(int id) {
        this.id = id;
    }

    private String localId;
    private String text;
    private String senderId;
    private String receiverId;
    private String topic;
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

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        Message that = (Message) o;

        return localId != null ? localId.equals(that.localId) : that.localId == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (localId != null ? localId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("***** User Details *****\n");
        stringBuilder.append("id = " + id);
        stringBuilder.append("localId = " + localId);
        stringBuilder.append("topic = " + topic);
        stringBuilder.append("senderId = " + senderId);
        stringBuilder.append("receiverId = " + receiverId);
        stringBuilder.append("text = " + text);
        stringBuilder.append("createdAt = " + createdAt);
        stringBuilder.append("updatedAt = " + updatedAt);
        stringBuilder.append("*******************************");

        return stringBuilder.toString();
    }
}
