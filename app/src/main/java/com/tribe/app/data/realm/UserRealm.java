package com.tribe.app.data.realm;

import android.support.annotation.StringDef;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
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
    public static final String PUSH_NOTIF = "push_notif";

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
    private RealmList<MembershipRealm> memberships;
    private boolean invisible_mode;
    private boolean push_notif = true;
    private boolean is_online = false;
    private Date last_online;

    @Ignore
    private RealmList<GroupRealm> groups;

    public UserRealm() {
        memberships = new RealmList<>();
        groups = new RealmList<>();
    }

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

    public RealmList<MembershipRealm> getMemberships() {
        return memberships;
    }

    public void setMemberships(RealmList<MembershipRealm> memberships) {
        this.memberships = memberships;
    }

    public RealmList<GroupRealm> getGroups() {
        return groups;
    }

    public void setGroups(RealmList<GroupRealm> groups) {
        this.groups = groups;
    }

    public void setPushNotif(boolean pushNotif) {
        this.push_notif = pushNotif;
    }

    public boolean isPushNotif() {
        return push_notif;
    }

    public boolean isOnline() {
        return is_online;
    }

    public void setIsOnline(boolean isOnline) {
        this.is_online = isOnline;
    }

    public Date getLastOnline() {
        return last_online;
    }

    public void setLastOnline(Date lastOnline) {
        this.last_online = lastOnline;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof UserRealm)) return false;

        UserRealm that = (UserRealm) o;

        return id != null ? id.equals(that.getId()) : that.getId() == null;
    }
}
