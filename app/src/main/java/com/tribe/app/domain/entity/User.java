package com.tribe.app.domain.entity;

import com.tribe.app.domain.entity.helpers.Changeable;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.adapter.decorator.BaseSectionItemDecoration;
import com.tribe.app.presentation.view.adapter.interfaces.BaseListInterface;
import com.tribe.app.presentation.view.adapter.interfaces.HomeAdapterInterface;
import com.tribe.app.presentation.view.adapter.interfaces.LiveInviteAdapterSectionInterface;
import com.tribe.app.presentation.view.adapter.model.AvatarModel;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import com.tribe.tribelivesdk.model.TribeGuest;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by tiago on 04/05/2016.
 */

public class User
    implements Serializable, BaseListInterface, Changeable, LiveInviteAdapterSectionInterface,
    HomeAdapterInterface {

  public static final String ID = "id";
  public static final String FBID = "fbid";
  public static final String USERNAME = "username";
  public static final String DISPLAY_NAME = "display_name";
  public static final String PICTURE = "picture";

  private static final int FIFTEEN_MINUTES = 15 * 60 * 1000;
  public static final String ID_EMPTY = "EMPTY";

  private String id;
  private String picture;
  private String display_name;
  private Date created_at;
  private Date updated_at;
  private String username;
  private String phone;
  private List<Recipient> recipientList;
  private List<Shortcut> shortcutList;
  private List<Message> messageList;
  private List<Invite> inviteList;
  private transient List<Score> scoreList;
  private String fbid;
  private boolean invisible_mode;
  private boolean push_notif;
  private boolean mute_online_notif;
  private boolean is_online = false;
  private UserPlaying is_playing;
  private boolean is_live = false;
  private boolean isActive = false;
  private boolean isTyping = false;
  private long time_in_call = 0;
  private Date last_seen_at;
  private boolean random_banned_permanently;
  private Date random_banned_until;

  private boolean isNewFriend = false;
  private boolean isFriend = false;
  private boolean animateAdd = false;
  private boolean isSelected = false;
  private List<String> emojiLeaderGameList = new ArrayList<>();

  private AvatarModel avatarModel = null;

  private boolean isNew = false;

  private String currentRoomId;
  private boolean ringing = false;

  public User(String id) {
    this.id = id;
    this.inviteList = new ArrayList<>();
    this.shortcutList = new ArrayList<>();
  }

  public boolean isRandom_banned_permanently() {
    return random_banned_permanently;
  }

  public void setRandom_banned_permanently(boolean random_banned_permanently) {
    this.random_banned_permanently = random_banned_permanently;
  }

  public Date getRandom_banned_until() {
    return random_banned_until;
  }

  public void setRandom_banned_until(Date random_banned_until) {
    this.random_banned_until = random_banned_until;
  }

  public boolean isActive() {
    return isActive;
  }

  public boolean isTyping() {
    return isTyping;
  }

  public void setTyping(boolean typing) {
    isTyping = typing;
  }

  public void setActive(boolean active) {
    isActive = active;
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

  @Override public boolean isRead() {
    return false;
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

  public long getTimeInCall() {
    return time_in_call;
  }

  public void setTimeInCall(long time_in_call) {
    this.time_in_call = time_in_call;
  }

  public List<Message> getMessages() {
    if (messageList == null) return new ArrayList<>();

    return messageList;
  }

  public List<Message> getMessageList() {
    return messageList;
  }

  public void setMessageList(List<Message> messageList) {
    this.messageList = messageList;
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

  public void setPushNotif(boolean pushNotif) {
    this.push_notif = pushNotif;
  }

  public boolean isPushNotif() {
    return push_notif;
  }

  public void setMute_online_notif(boolean mute_online_notif) {
    this.mute_online_notif = mute_online_notif;
  }

  public boolean isMute_online_notif() {
    return mute_online_notif;
  }

  @Override public boolean isOnline() {
    if (is_online) return is_online;
    if (last_seen_at == null) return false;

    //Timber.d("last_seen_at : " + display_name + " / " + last_seen_at);
    //Timber.d("System.currentTimeMillis() - last_seen_at.getTime() <= FIFTEEN_MINUTES : " +
    //    (System.currentTimeMillis() - last_seen_at.getTime() <= FIFTEEN_MINUTES));
    // We consider that somebody that was online less than fifteen minutes ago is still online
    return System.currentTimeMillis() - last_seen_at.getTime() <= FIFTEEN_MINUTES;
  }

  public UserPlaying isPlaying() {
    return is_playing;
  }

  public void setPlaying(UserPlaying userPlaying) {
    this.is_playing = userPlaying;
  }

  @Override public boolean isLive() {
    return is_live;
  }

  public void setIsLive(boolean is_live) {
    this.is_live = is_live;
  }

  @Override public boolean isRinging() {
    return ringing;
  }

  public void setIsOnline(boolean isOnline) {
    this.is_online = isOnline;
  }

  public Date getLastSeenAt() {
    return last_seen_at;
  }

  @Override public int getHomeSectionType() {
    return BaseSectionItemDecoration.SEARCH_SUGGESTED_CONTACTS;
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

  public boolean isSelected() {
    return isSelected;
  }

  public void setSelected(boolean selected) {
    isSelected = selected;
  }

  public void setInviteList(Collection<Invite> inviteList) {
    this.inviteList.clear();
    this.inviteList.addAll(inviteList);
  }

  public List<Invite> getInviteList() {
    return inviteList;
  }

  public void setShortcutList(List<Shortcut> shortcutList) {
    this.shortcutList = shortcutList;
  }

  public List<Shortcut> getShortcutList() {
    return shortcutList;
  }

  public List<Recipient> getRecipientList() {
    recipientList = new ArrayList<>();
    if (shortcutList != null) recipientList.addAll(shortcutList);
    if (inviteList != null) recipientList.addAll(inviteList);

    Collections.sort(recipientList, (lhs, rhs) -> Recipient.nullSafeComparator(lhs, rhs));

    return recipientList;
  }

  public void setNew(boolean aNew) {
    isNew = aNew;
  }

  public boolean isNew() {
    return isNew;
  }

  public void setCurrentRoomId(String currentRoomId) {
    this.currentRoomId = currentRoomId;
  }

  public void setRinging(boolean ringing) {
    this.ringing = ringing;
  }

  @Override public String getCurrentRoomId() {
    return currentRoomId;
  }

  public void setScoreList(List<Score> scoreList) {
    this.scoreList = scoreList;
  }

  public List<Score> getScoreList() {
    return scoreList;
  }

  public Score getScoreForGame(String gameId) {
    Score result = null;

    if (scoreList != null) {
      for (Score score : scoreList) {
        if (score.getGame().getId().equals(gameId)) result = score;
      }
    }

    return result;
  }

  public void setEmojiLeaderGameList(List<String> emojiLeaderGameList) {
    this.emojiLeaderGameList = emojiLeaderGameList;
  }

  public List<String> getEmojiLeaderGameList() {
    return emojiLeaderGameList;
  }

  public boolean isUserInCall() {
    return !StringUtils.isEmpty(currentRoomId) || ringing;
  }

  public TribeGuest asTribeGuest() {
    TribeGuest guest = new TribeGuest(getId());
    guest.setDisplayName(getDisplayName());
    guest.setPicture(getProfilePicture());
    guest.setUserName(getUsername());
    return guest;
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
      setPhone(user.getPhone());
      setFbid(user.getFbid());
      setInvisibleMode(user.isInvisibleMode());
      setPushNotif(user.isPushNotif());
      setTimeInCall(user.getTimeInCall());
      setLastSeenAt(user.getLastSeenAt());
      setRandom_banned_until(user.getRandom_banned_until());
      setMute_online_notif(user.isMute_online_notif());
      setScoreList(user.getScoreList());
      setEmojiLeaderGameList(user.getEmojiLeaderGameList());
    }
  }

  public void clear() {
    setId(null);
    setCreatedAt(null);
    setUpdatedAt(null);
    setDisplayName(null);
    setUsername(null);
    setProfilePicture(null);
    setPhone(null);
    setFbid(null);
    setInvisibleMode(false);
    setPushNotif(false);
    setTimeInCall(0);
    setLastSeenAt(null);
    setRandom_banned_until(null);
    setMute_online_notif(false);
  }

  public boolean isEmpty() {
    return StringUtils.isEmpty(username);
  }

  @Override public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (getId() != null ? getId().hashCode() : 0);
    return result;
  }

  @Override public int getChangeHashCode() {
    return id.hashCode();
  }

  @Override public int getLiveInviteSectionType() {
    if (isUserInCall()) {
      return BaseSectionItemDecoration.LIVE_CHAT_MEMBERS;
    } else {
      return BaseSectionItemDecoration.LIVE_ADD_FRIENDS_IN_CALL;
    }
  }

  @Override public String toString() {
    return "User{" +
        "id='" +
        id +
        '\'' +
        ", display_name='" +
        display_name +
        '\'' +
        ", is_online=" +
        isOnline() +
        ", isActive=" +
        isActive() +
        '}';
  }
}
