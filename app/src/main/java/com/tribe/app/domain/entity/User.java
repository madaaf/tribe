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
    private Location location;
    private boolean tribe_save;
    private List<Friendship> friendships;
    private List<User> reportedList;
    private List<Group> groupList;
    private List<Recipient> friendshipList;
    private String fbid;
    private boolean invisible_mode;

    public int getScore() {
        return score;
    }

    public String getScoreStr() {
        return "" + score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getUsername() {
        return username;
    }

    public String getUsernameDisplay() {
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

    public String getFbid() {
        return fbid;
    }

    public void setFbid(String fbid) {
        this.fbid = fbid;
    }

    public boolean isTribeSave() {
        return tribe_save;
    }

    public void setTribeSave(boolean tribeSave) {
        this.tribe_save = tribeSave;
    }

    public boolean isInvisibleMode() {
        return invisible_mode;
    }

    public void setInvisibleMode(boolean invisibleMode) {
        this.invisible_mode = invisibleMode;
    }

    public List<Recipient> getFriendshipList() {
        friendshipList = new ArrayList<>();
        if (friendships != null) friendshipList.addAll(friendships);
        if (groupList != null) friendshipList.addAll(groupList);

        Collections.sort(friendshipList, (lhs, rhs) -> Recipient.nullSafeComparator(lhs, rhs));

        return friendshipList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        User that = (User) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    public void copy(User user) {
        if (user != null) {
            setId(user.getId());
            setCreatedAt(user.getCreatedAt());
            setUpdatedAt(user.getUpdatedAt());
            setDisplayName(user.getDisplayName());
            setUsername(user.getUsername());
            setProfilePicture(user.getProfilePicture());
            setScore(user.getScore());
            setPhone(user.getPhone());
            setFbid(user.getFbid());
            setInvisibleMode(user.isInvisibleMode());
            setTribeSave(user.isTribeSave());
            if (user.getLocation() != null) setLocation(user.getLocation());
            if (user.getGroupList() != null) setGroupList(user.getGroupList());
            if (user.getFriendships() != null) setFriendships(user.getFriendships());
            if (user.getReportedList() != null) setReportedList(user.getReportedList());
        }
    }

    public void clear() {
        setId(null);
        setCreatedAt(null);
        setUpdatedAt(null);
        setDisplayName(null);
        setUsername(null);
        setProfilePicture(null);
        setScore(0);
        setPhone(null);
        setFbid(null);
        setInvisibleMode(false);
        setTribeSave(false);
        setLocation(null);
        setGroupList(null);
        setFriendships(null);
        setReportedList(null);
    }
}
