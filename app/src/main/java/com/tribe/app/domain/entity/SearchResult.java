package com.tribe.app.domain.entity;

import java.io.Serializable;

/**
 * Created by tiago on 08/09/2016.
 */
public class SearchResult implements Serializable {

    private String id;
    private String display_name;
    private String username;
    private String picture;
    private boolean invisible_mode = false;
    private Friendship friendship;
    private boolean searchDone = false;
    private boolean shouldAnimateAdd = false;
    private boolean isMyself = false;

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public Friendship getFriendship() {
        return friendship;
    }

    public void setFriendship(Friendship friendship) {
        this.friendship = friendship;
    }

    public boolean isSearchDone() {
        return searchDone;
    }

    public void setSearchDone(boolean searchDone) {
        this.searchDone = searchDone;
    }

    public boolean isShouldAnimateAdd() {
        return shouldAnimateAdd;
    }

    public void setShouldAnimateAdd(boolean shouldAnimateAdd) {
        this.shouldAnimateAdd = shouldAnimateAdd;
    }

    public void setInvisibleMode(boolean invisibleMode) {
        this.invisible_mode = invisibleMode;
    }

    public boolean isInvisibleMode() {
        return invisible_mode;
    }

    public boolean isMyself() {
        return isMyself;
    }

    public void setMyself(boolean myself) {
        isMyself = myself;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + "search".hashCode();
        return result;
    }
}
