package com.tribe.app.domain.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by tiago on 04/05/2016.
 */
public class User extends Friendship {

    public User(String id) {
        super(id);
    }

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
    private List<User> friendList;
    private List<User> reportedList;
    private List<Group> groupList;
    private List<Friendship> friendshipList;

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

    public List<User> getFriendList() {
        return friendList;
    }

    public void setFriendList(List<User> friendList) {
        this.friendList = friendList;
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

    public List<Friendship> getFriendshipList() {
        friendshipList = new ArrayList<>();
        if (friendList != null) friendshipList.addAll(friendList);
        if (groupList != null) friendshipList.addAll(groupList);

        Collections.sort(friendshipList, (lhs, rhs) -> {
            int res = Tribe.nullSafeComparator(lhs.getMostRecentTribe(), rhs.getMostRecentTribe());
            if (res != 0) {
                return res;
            }

            return Friendship.nullSafeComparator(lhs, rhs);
        });

        return friendshipList;
    }
}
