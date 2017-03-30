package com.tribe.app.domain.entity;

import com.tribe.app.presentation.view.adapter.interfaces.BaseListInterface;
import com.tribe.app.presentation.view.adapter.model.AvatarModel;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by tiago on 04/05/2016.
 */
public class User implements Serializable, BaseListInterface {

  private static final int FIFTEEN_MINUTES = 15 * 60 * 1000;
  public static final String ID_EMPTY = "EMPTY";

  private String id;
  private String picture;
  private String display_name;
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
  private List<Invite> inviteList;
  private String fbid;
  private boolean invisible_mode;
  private boolean push_notif;
  private boolean is_online = false;
  private Date last_seen_at;

  private boolean isNewFriend = false;
  private boolean isFriend = false;
  private boolean animateAdd = false;

  private AvatarModel avatarModel = null;

  public User(String id) {
    this.id = id;
    this.inviteList = new ArrayList<>();
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
    return picture;
  }

  public void setProfilePicture(String profilePicture) {
    this.picture = profilePicture;
  }

  public String getDisplayName() {
    return display_name;
  }

  public void setDisplayName(String displayName) {
    this.display_name = displayName;
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
    if (membershipList == null) return new ArrayList<>();

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
    if (inviteList != null) friendshipList.addAll(inviteList);

    Collections.sort(friendshipList, (lhs, rhs) -> Recipient.nullSafeComparator(lhs, rhs));

    return friendshipList;
  }

  public boolean isOnline() {
    if (is_online) return is_online;
    if (last_seen_at == null) return false;

    // We consider that somebody that was online less than fifteen minutes ago is still online
    long fifteenMinutesAgo = System.currentTimeMillis() - FIFTEEN_MINUTES;
    return last_seen_at.getTime() > fifteenMinutesAgo;
  }

  public void setIsOnline(boolean isOnline) {
    this.is_online = isOnline;
  }

  public Date getLastSeenAt() {
    return last_seen_at;
  }

  public void setLastSeenAt(Date lastSeenAt) {
    this.last_seen_at = lastSeenAt;
  }

  @Override public boolean isFriend() {
    return isFriend;
  }

  @Override public AvatarModel getAvatar() {
    if (avatarModel != null) return avatarModel;
    avatarModel = new AvatarModel(picture, isOnline() ? AvatarView.ONLINE : AvatarView.REGULAR);
    return avatarModel;
  }

  @Override public boolean isReverse() {
    return false;
  }

  public void setFriend(boolean friend) {
    isFriend = friend;
  }

  @Override public boolean isAnimateAdd() {
    return animateAdd;
  }

  @Override public boolean isActionAvailable(User currentUser) {
    return !currentUser.equals(this);
  }

  @Override public boolean isInvisible() {
    return invisible_mode;
  }

  @Override public void setAnimateAdd(boolean animateAdd) {
    this.animateAdd = animateAdd;
  }

  public boolean isNewFriend() {
    return isNewFriend;
  }

  public void setNewFriend(boolean newFriend) {
    isNewFriend = newFriend;
  }

  public void setInviteList(Collection<Invite> inviteList) {
    this.inviteList.clear();
    this.inviteList.addAll(inviteList);
  }

  public List<Invite> getInviteList() {
    return inviteList;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || !(o instanceof User)) return false;

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
      if (user.getMembershipList() != null && user.getMembershipList().size() > 0) {
        setMembershipList(user.getMembershipList());
      }
      if (user.getFriendships() != null && user.getFriendshipList().size() > 0) {
        setFriendships(user.getFriendships());
      }
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

  public List<GroupMember> getUserList() {
    List<GroupMember> userList = new ArrayList<>();

    if (friendships == null) return userList;

    Collections.sort(friendships, (lhs, rhs) -> Recipient.nullSafeComparator(lhs, rhs));

    for (Friendship friendship : friendships) {
      if (!friendship.isFake() && !friendship.getSubId().equals(this.id)) {
        GroupMember groupMember = new GroupMember(friendship.getFriend());
        groupMember.setFriend(true);
        userList.add(groupMember);
      }
    }

    return userList;
  }

  public void computeMemberFriends(List<GroupMember> groupMemberList) {
    for (GroupMember groupMember : groupMemberList) {
      for (Friendship friendship : friendships) {
        if (friendship.getFriend().equals(groupMember.getUser())) {
          groupMember.setFriend(true);
          groupMember.setFriendship(friendship);
        }
      }
    }
  }

  public int computeUserFriends(List<User> userList) {
    int count = 0;

    if (friendships != null) {
      for (User user : userList) {
        for (Friendship friendship : friendships) {
          if (friendship.getFriend().equals(user)) {
            user.setFriend(true);
            count++;
          }
        }
      }
    }

    return count;
  }

  public User getFromFriendships(String userId) {
    for (Friendship friendship : friendships) {
      if (friendship.getSubId().equals(userId)) {
        return friendship.getFriend();
      }
    }

    return null;
  }

  @Override public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (getId() != null ? getId().hashCode() : 0);
    return result;
  }
}
