package com.tribe.app.domain.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by tiago on 04/05/2016.
 */
public class User implements Serializable {

    private String id;

    public User(String id) {
        this.id = id;
    }

    private String profilePicture;
    private String displayName;
    private Date created_at;
    private Date updated_at;
    private String username;
    private String phone;
    private int score = 0;
    private String email;
    private boolean emailVerified;
    private boolean isReal;
    private boolean isInvited;
    private Location location;
    private boolean disableSaveTribe;
    private boolean hideUsername;
    private List<Friendship> friendships;
    private List<User> reportedList;
    private List<Group> groupList;
    private List<Recipient> friendshipList;

    public int getScore() {
        return score;
    }

    public String getScoreStr() {
        return "" + score;
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

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
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
        return "@" + username;
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

    public List<User> getReportedList() {
        return reportedList;
    }

    public void setReportedList(List<User> reportedList) {
        this.reportedList = reportedList;
    }

    public List<Group> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<Group> groupList) {
        this.groupList = groupList;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFriendships(List<Friendship> friendships) {
        this.friendships = friendships;
    }

    public List<Friendship> getFriendships() {
        return friendships;
    }

    public List<Recipient> getFriendshipList() {
        friendshipList = new ArrayList<>();
        if (friendships != null) friendshipList.addAll(friendships);
        if (groupList != null) friendshipList.addAll(groupList);

        Collections.sort(friendshipList, (lhs, rhs) -> {
            int res = TribeMessage.nullSafeComparator(lhs.getMostRecentTribe(), rhs.getMostRecentTribe());
            if (res != 0) {
                return res;
            }

            return Recipient.nullSafeComparator(lhs, rhs);
        });

        return friendshipList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        User that = (User) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }
}
