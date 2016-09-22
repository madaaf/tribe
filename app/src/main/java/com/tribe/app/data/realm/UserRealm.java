package com.tribe.app.data.realm;

import android.support.annotation.StringDef;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by tiago on 04/05/2016.
 */
public class UserRealm extends RealmObject {

    @StringDef({UPDATED_AT})
    public @interface UserRealmAttributes {}

    public static final String DISPLAY_NAME = "display_name";
    public static final String USERNAME = "username";
    public static final String PROFILE_PICTURE = "picture";
    public static final String FBID = "fbid";
    public static final String INVISIBLE_MODE = "invisible_mode";
    public static final String TRIBE_SAVE = "tribe_save";
    public static final String UPDATED_AT = "updated_at";

    @PrimaryKey
    private String id;

    private Date created_at;
    private Date updated_at;
    private String display_name;
    private String username;
    private String phone;
    private String fbid;
    private String picture;
    private int score;
    private LocationRealm location;
    private boolean tribe_save = false;
    private RealmList<FriendshipRealm> friendships;
    private RealmList<UserRealm> reported;
    private RealmList<GroupRealm> groups;
    private boolean invisible_mode;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(Date createdAt) {
        this.created_at = createdAt;
    }

    public Date getUpdatedAt() {
        return updated_at;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updated_at = updatedAt;
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

    public LocationRealm getLocation() {
        return location;
    }

    public void setLocation(LocationRealm location) {
        this.location = location;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public RealmList<FriendshipRealm> getFriendships() {
        return friendships;
    }

    public void setFriendships(RealmList<FriendshipRealm> friendships) {
        this.friendships = friendships;
    }

    public RealmList<UserRealm> getReported() {
        return reported;
    }

    public void setReported(RealmList<UserRealm> reported) {
        this.reported = reported;
    }

    public RealmList<GroupRealm> getGroups() {
        return groups;
    }

    public void setGroups(RealmList<GroupRealm> groups) {
        this.groups = groups;
    }

    public String getFbid() {
        return fbid;
    }

    public void setFbid(String fbid) {
        this.fbid = fbid;
    }

    public boolean isInvisibleMode() {
        return invisible_mode;
    }

    public void setInvisibleMode(boolean invisibleMode) {
        this.invisible_mode = invisibleMode;
    }

    public boolean isTribeSave() {
        return tribe_save;
    }

    public void setTribeSave(boolean tribeSave) {
        this.tribe_save = tribeSave;
    }


}
