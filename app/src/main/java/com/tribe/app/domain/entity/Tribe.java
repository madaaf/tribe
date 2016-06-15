package com.tribe.app.domain.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by tiago on 22/05/2016.
 *
 * Class that represents a Tribe in the domain layer.
 */
public class Tribe implements Serializable {

    private final String id;

    public Tribe(String id) {
        this.id = id;
    }

    private String localId;
    private Date createdAt;
    private Date updatedAt;

    public String getId() {
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

    public String getLocalId() {
        return localId;
    }

    public void setLocalId(String localId) {
        this.localId = localId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        Tribe that = (Tribe) o;

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
        stringBuilder.append("createdAt = " + createdAt);
        stringBuilder.append("updatedAt = " + updatedAt);
        stringBuilder.append("*******************************");

        return stringBuilder.toString();
    }
}
