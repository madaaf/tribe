package com.tribe.app.data.realm;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by tiago on 04/05/2016.
 */
public class UserRealm extends RealmObject {

    @PrimaryKey
    private String id;

    private Date created_at;
    private Date updated_at;
    private String displayName;
    private String username;
    private String phone;
    private String picture;
    private int score;
    private String email;
    private boolean emailVerified;
    private boolean isReal;
    private boolean isInvited;
    private LocationRealm location;
    private boolean disableSaveTribe;
    private boolean hideUsername;
    private RealmList<FriendshipRealm> friendships;
    private RealmList<UserRealm> reported;
    private RealmList<GroupRealm> groups;


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
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public boolean isReal() {
        return isReal;
    }

    public void setReal(boolean real) {
        isReal = real;
    }

    public LocationRealm getLocation() {
        return location;
    }

    public void setLocation(LocationRealm location) {
        this.location = location;
    }

    public boolean isInvited() {
        return isInvited;
    }

    public void setInvited(boolean invited) {
        isInvited = invited;
    }

    public boolean isDisableSaveTribe() {
        return disableSaveTribe;
    }

    public void setDisableSaveTribe(boolean disableSaveTribe) {
        this.disableSaveTribe = disableSaveTribe;
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

    public boolean isHideUsername() {
        return hideUsername;
    }

    public void setHideUsername(boolean hideUsername) {
        this.hideUsername = hideUsername;
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
}
