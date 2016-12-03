package com.tribe.app.data.realm;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by tiago on 11/30/2016.
 */
public class GroupMemberRealm extends RealmObject {

    @PrimaryKey
    private String id;

    private Date created_at;
    private Date updated_at;

    private String display_name;
    private String username;
    private String picture;
    private int score;
    private boolean invisible_mode;

    public GroupMemberRealm() {

    }

    public GroupMemberRealm(UserRealm userRealm) {
        this.id = userRealm.getId();
        this.display_name = userRealm.getDisplayName();
        this.username = userRealm.getUsername();
        this.picture = userRealm.getProfilePicture();
        this.score = userRealm.getScore();
        this.invisible_mode = userRealm.isInvisibleMode();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return display_name;
    }

    public void setDisplayName(String displayName) {
        this.display_name = displayName;
    }

    public String getProfilePicture() {
        return picture;
    }

    public void setProfilePicture(String profilePicture) {
        this.picture = profilePicture;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isInvisibleMode() {
        return invisible_mode;
    }

    public void setInvisibleMode(boolean invisibleMode) {
        this.invisible_mode = invisibleMode;
    }

    public Date getCreatedAt() {
        return created_at;
    }

    public Date getUpdatedAt() {
        return updated_at;
    }

    public void setCreatedAt(Date createdAt) {
        this.created_at = createdAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updated_at = updatedAt;
    }
}
