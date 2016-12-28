package com.tribe.app.domain.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by tiago on 05/08/2016.
 */
public abstract class Recipient implements Serializable {

    public static final String ID_EMPTY = "EMPTY";
    public static final String ID_HEADER = "HEADER";

    protected Date created_at;
    protected Date updated_at;

    protected int position;

    public Date getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(Date createdAt) {
        this.created_at = createdAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updated_at = updatedAt;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public static int nullSafeComparator(final Recipient one, final Recipient two) {
        if (one.getUpdatedAt() == null ^ two.getUpdatedAt() == null) {
            return (one.getUpdatedAt() == null) ? 1 : -1;
        }

        if (one.getUpdatedAt() == null && two.getUpdatedAt() == null) {
            return 0;
        }

        return two.getUpdatedAt().compareTo(one.getUpdatedAt());
    }

    public abstract String getDisplayName();
    public abstract String getUsername();
    public abstract String getUsernameDisplay();
    public abstract String getProfilePicture();
    public abstract String getSubId();
    public abstract String getId();
    public abstract Date getUpdatedAt();
    public abstract void setScore(int score);

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getId() != null ? getId().hashCode() : 0);
        return result;
    }
}
