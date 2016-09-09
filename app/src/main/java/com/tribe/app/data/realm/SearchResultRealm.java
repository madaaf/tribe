package com.tribe.app.data.realm;

import io.realm.RealmObject;

/**
 * Created by tiago on 04/05/2016.
 */
public class SearchResultRealm extends RealmObject {

    private String display_name;
    private String username;
    private String picture;
    private FriendshipRealm friendshipRealm;

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
}
