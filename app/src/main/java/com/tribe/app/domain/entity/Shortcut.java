package com.tribe.app.domain.entity;

import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.domain.ShortcutLastSeen;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.adapter.decorator.BaseSectionItemDecoration;
import com.tribe.app.presentation.view.adapter.interfaces.LiveInviteAdapterSectionInterface;
import com.tribe.app.presentation.view.adapter.model.AvatarModel;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by tiago on 09/10/2017.
 */
public class Shortcut extends Recipient implements Serializable, LiveInviteAdapterSectionInterface {

  private static final int ONE_MINUTE = 60 * 1000;

  private String id;
  private String name;
  private String picture;
  private boolean online;
  private boolean live;
  private boolean pinned;
  private boolean read = true;
  private boolean mute;
  private boolean single;
  private @ShortcutRealm.ShortcutStatus String status;
  private Date last_activity_at;
  private List<ShortcutLastSeen> shortcutLastSeen;
  private List<User> members;
  private String lastMessage;
  private Date leaveOnlineUntil;
  private String membersHash;

  private boolean selected;

  public Shortcut(String id) {
    this.id = id;
  }

  public List<ShortcutLastSeen> getShortcutLastSeen() {
    return shortcutLastSeen;
  }

  public void setShortcutLastSeen(List<ShortcutLastSeen> shortcutLastSeen) {
    this.shortcutLastSeen = shortcutLastSeen;
  }

  @Override public String getId() {
    return id;
  }

  @Override public boolean isLive() {
    return live;
  }

  @Override public boolean isActionAvailable(User currentUser) {
    return true;
  }

  @Override public boolean isInvisible() {
    return single ? getSingleFriend().isInvisibleMode() : false;
  }

  @Override public boolean isFriend() {
    return single;
  }

  @Override public AvatarModel getAvatar() {
    return new AvatarModel(picture, isOnline() ? AvatarView.ONLINE : AvatarView.REGULAR);
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setPicture(String picture) {
    this.picture = picture;
  }

  public boolean isOnline() {
    return online || (leaveOnlineUntil != null
        && !single
        && System.currentTimeMillis() - leaveOnlineUntil.getTime() <= ONE_MINUTE);
  }

  @Override public boolean isRinging() {
    return false;
  }

  @Override public String getCurrentRoomId() {
    return null;
  }

  @Override public int getLiveInviteSectionType() {
    return BaseSectionItemDecoration.LIVE_ADD_FRIENDS_IN_CALL;
  }

  @Override public Date getLastSeenAt() {
    return last_activity_at;
  }

  public void setOnline(boolean online) {
    this.online = online;
  }

  public boolean isPinned() {
    return pinned;
  }

  public void setPinned(boolean pinned) {
    this.pinned = pinned;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  @Override public boolean isRead() {
    return read;
  }

  public void setRead(boolean read) {
    this.read = read;
  }

  public boolean isMute() {
    return mute;
  }

  public void setMute(boolean mute) {
    this.mute = mute;
  }

  public boolean isSingle() {
    return single;
  }

  public void setSingle(boolean single) {
    this.single = single;
  }

  public Date getCreatedAt() {
    return created_at;
  }

  public void setCreatedAt(Date created_at) {
    this.created_at = created_at;
  }

  public void setLive(boolean live) {
    this.live = live;
  }

  @Override public String getDisplayName() {
    return StringUtils.isEmpty(name) || isSingle() ? getUserDisplayNames() : name;
  }

  @Override public String getUsername() {
    return StringUtils.isEmpty(name) ? getUserNames() : name;
  }

  @Override public String getProfilePicture() {
    return isSingle() ? getSingleFriend().getProfilePicture() : picture;
  }

  public Date getLastActivityAt() {
    return last_activity_at;
  }

  public void setLastActivityAt(Date last_activity_at) {
    this.last_activity_at = last_activity_at;
  }

  public List<User> getMembers() {
    return members;
  }

  public void setMembers(List<User> members) {
    this.members = members;
  }

  public String getLastMessage() {
    return lastMessage;
  }

  public void setLastMessage(String lastMessage) {
    this.lastMessage = lastMessage;
  }

  public void setLeaveOnlineUntil(Date leaveOnlineUntil) {
    this.leaveOnlineUntil = leaveOnlineUntil;
  }

  public Date getLeaveOnlineUntil() {
    return leaveOnlineUntil;
  }

  public void setMembersHash(String membersHash) {
    this.membersHash = membersHash;
  }

  public String getMembersHash() {
    return membersHash;
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  public boolean isSelected() {
    return selected;
  }

  public void addMember(User user) {
    if (members == null) members = new ArrayList<>();
    members.add(user);
  }

  public boolean isFriend(User friend) {
    if (members == null || members.size() == 0) return false;

    for (User user : members) {
      if (user.equals(friend)) return true;
    }

    return false;
  }

  public List<String> getMembersIds() {
    List<String> memberIds = new ArrayList<>();

    if (members != null) {
      for (User user : members) {
        memberIds.add(user.getId());
      }
    }

    return memberIds;
  }

  public User getSingleFriend() {
    if (members == null || members.size() == 0) return null;

    return members.get(0);
  }

  public boolean isBlockedOrHidden() {
    return status.equalsIgnoreCase(ShortcutRealm.BLOCKED) || status.equalsIgnoreCase(
        ShortcutRealm.HIDDEN);
  }

  public boolean isBlocked() {
    return status.equalsIgnoreCase(ShortcutRealm.BLOCKED);
  }

  public boolean isHidden() {
    return status.equals(ShortcutRealm.HIDDEN);
  }

  public String getUserDisplayNames() {
    if (members == null || members.size() == 0) return "";

    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < members.size(); i++) {
      User user = members.get(i);
      String label = user.getDisplayName();
      buffer.append(label);
      if (i < members.size() - 1) buffer.append(", ");
    }

    return buffer.toString();
  }

  private String getUserNames() {
    if (members == null || members.size() == 0) return "";

    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < members.size(); i++) {
      User user = members.get(i);
      String label = user.getUsername();
      buffer.append(label);
      if (i < members.size() - 1) buffer.append(", ");
    }

    return buffer.toString();
  }

  public boolean isUniqueMemberOnline(Map<String, Boolean> onlineMap) {
    if (!isSingle()) return false;
    User user = getSingleFriend();

    return onlineMap.containsKey(user.getId()) || user.isOnline();
  }

  @Override public String toString() {
    return "Shortcut{"
        + "id='"
        + id
        + '\''
        + ", name='"
        + name
        + ", online="
        + online
        + ", live="
        + live
        + ", pinned="
        + ", shortcutLastSeen="
        + shortcutLastSeen
        + ", members="
        + members;
  }

}
