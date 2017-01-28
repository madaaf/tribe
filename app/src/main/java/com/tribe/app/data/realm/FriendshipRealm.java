package com.tribe.app.data.realm;

import android.support.annotation.StringDef;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by tiago on 05/08/2016.
 */
public class FriendshipRealm extends RealmObject implements RecipientRealmInterface {

    @StringDef({DEFAULT, HIDDEN, BLOCKED})
    public @interface FriendshipStatus {}

    public static final String DEFAULT = "DEFAULT";
    public static final String HIDDEN = "HIDDEN";
    public static final String BLOCKED = "BLOCKED";

    @PrimaryKey
    private String id;

    private @FriendshipStatus String status;
    private String tag;
    private boolean blocked;
    private String category;
    private UserRealm friend;
    private Date created_at;
    private Date updated_at;
    private boolean is_live;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public UserRealm getFriend() {
        return friend;
    }

    public void setFriend(UserRealm friend) {
        this.friend = friend;
    }

    public Date getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(Date created_at) {
        this.created_at = created_at;
    }

    public Date getUpdatedAt() {
        return updated_at;
    }

    public void setUpdatedAt(Date updated_at) {
        this.updated_at = updated_at;
    }

    @Override
    public String getSubId() {
        return friend.getId();
    }

    public @FriendshipStatus String getStatus() {
        return status;
    }

    public void setStatus(@FriendshipStatus String status) {
        this.status = status;
    }

    public boolean isBlockedOrHidden() {
        return status != null && !status.equals(FriendshipRealm.DEFAULT);
    }

    public boolean isHidden() {
        return status != null && status.equals(FriendshipRealm.HIDDEN);
    }

    public boolean isLive() {
        return is_live;
    }

    public void setLive(boolean isLive) {
        this.is_live = isLive;
    }
}
