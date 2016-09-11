package com.tribe.app.data.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by tiago on 04/05/2016.
 */
public class SearchResultRealm extends RealmObject {

    @PrimaryKey
    private String key;

    private String id;
    private String display_name;
    private String username;
    private String picture;
    private FriendshipRealm friendshipRealm;
    private boolean searchDone = false;

    public SearchResultRealm() {
        this.key = "search";
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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

    public FriendshipRealm getFriendshipRealm() {
        return friendshipRealm;
    }

    public void setFriendshipRealm(FriendshipRealm friendshipRealm) {
        this.friendshipRealm = friendshipRealm;
    }

    public boolean isSearchDone() {
        return searchDone;
    }

    public void setSearchDone(boolean searchDone) {
        this.searchDone = searchDone;
    }
}
