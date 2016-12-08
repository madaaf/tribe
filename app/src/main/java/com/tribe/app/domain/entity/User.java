package com.tribe.app.domain.entity;

import com.tribe.app.presentation.view.utils.Constants;

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
    private List<Membership> membershipList;
    private List<Recipient> friendshipList;
    private String fbid;
    private boolean invisible_mode;
    private boolean push_notif;

    public User(String id) {
        this.id = id;
    }

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
        if (friendships == null) return new ArrayList<>();

        return friendships;
    }

    public void setMembershipList(List<Membership> membershipList) {
        this.membershipList = membershipList;
    }

    public List<Membership> getMembershipList() {
        if (friendships == null) return new ArrayList<>();

        return membershipList;
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

    public void setPushNotif(boolean pushNotif) {
        this.push_notif = pushNotif;
    }

    public boolean isPushNotif() {
        return push_notif;
    }

    public List<Recipient> getFriendshipList() {
        friendshipList = new ArrayList<>();

        List<Friendship> friendshipWithoutMe = new ArrayList<>();

        if (friendships != null) {
            for (Friendship fr : friendships) {
                if (!id.equals(fr.getSubId())) {
                    friendshipWithoutMe.add(fr);
                }
            }

            friendshipList.addAll(friendshipWithoutMe);
        }

        if (membershipList != null) friendshipList.addAll(membershipList);

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
            setPushNotif(user.isPushNotif());
            if (user.getLocation() != null) setLocation(user.getLocation());
            if (user.getMembershipList() != null && user.getMembershipList().size() > 0) setMembershipList(user.getMembershipList());
            if (user.getFriendships() != null && user.getFriendshipList().size() > 0) setFriendships(user.getFriendships());
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
        setPushNotif(false);
        setTribeSave(false);
        setLocation(null);
        setMembershipList(null);
        setFriendships(null);
    }

    public boolean hasOnlySupport() {
        boolean hasOnlySupport = false;

        List<Recipient> result = getFriendshipList();
        if (result != null && result.size() == 1) {
            for (Recipient fr : result) {
                if (fr.getSubId() != null && fr.getSubId().equals(Constants.SUPPORT_ID)) {
                    hasOnlySupport = true;
                }
            }
        }

        return hasOnlySupport;
    }

    public List<GroupMember> getUserList() {
        List<GroupMember> userList = new ArrayList<>();

        Collections.sort(friendships, (lhs, rhs) -> Recipient.nullSafeComparator(lhs, rhs));

        for (Friendship friendship : friendships) {
            if (!friendship.getSubId().equals(Constants.SUPPORT_ID)
                    && !friendship.getSubId().equals(Recipient.ID_EMPTY)
                    && !friendship.getSubId().equals(this.id)) {
                userList.add(new GroupMember(friendship.getFriend()));
            }
        }

        return userList;
    }

    public void computeFriends(List<GroupMember> groupMemberList) {
        for (GroupMember groupMember : groupMemberList) {
            for (Friendship friendship : friendships) {
                if (friendship.getFriend().equals(groupMember.getUser())) {
                    groupMember.setFriend(true);
                }
            }
        }

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getId() != null ? getId().hashCode() : 0);
        return result;
    }
}
