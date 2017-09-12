package com.tribe.app.domain.entity;

import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.adapter.model.AvatarModel;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by tiago on 09/10/2017.
 */
public class Shortcut extends Recipient implements Serializable {

  private static final int NB_MAX_USERS_STRING = 3;

  private String id;
  private String name;
  private String picture;
  private boolean online;
  private boolean pinned;
  private boolean read;
  private boolean mute;
  private boolean single;
  private @ShortcutRealm.ShortcutStatus String status;
  private Date last_activity_at;
  private List<User> members;

  public Shortcut(String id) {
    this.id = id;
  }

  @Override public String getId() {
    return id;
  }

  @Override public boolean isLive() {
    return false;
  }

  @Override public boolean isActionAvailable(User currentUser) {
    return false;
  }

  @Override public boolean isInvisible() {
    return false;
  }

  @Override public boolean isFriend() {
    return false;
  }

  @Override public AvatarModel getAvatar() {
    return null;
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
    return online;
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

  public boolean isRead() {
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

  @Override public String getDisplayName() {
    return StringUtils.isEmpty(name) ? getUserNames() : name;
  }

  @Override public String getUsername() {
    return null;
  }

  @Override public String getProfilePicture() {
    return picture;
  }

  @Override public String getSubId() {
    return id;
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

  private String getUserNames() {
    if (members == null || members.size() == 0) return "";

    StringBuffer buffer = new StringBuffer();
    int min = Math.min(NB_MAX_USERS_STRING, members.size());
    for (int i = 0; i < min; i++) {
      User user = members.get(i);
      buffer.append(user.getDisplayName());

      if (i < min - 1) buffer.append(", ");
    }

    if (members.size() > NB_MAX_USERS_STRING) {
      buffer.append(", " + (members.size() - NB_MAX_USERS_STRING) + " persons");
    }

    return buffer.toString();
  }

  private boolean isMemberOnline() {
    if (members == null || members.size() == 0) return false;

    for (User user : members) {
      if (user.isOnline()) return true;
    }

    return false;
  }
}
