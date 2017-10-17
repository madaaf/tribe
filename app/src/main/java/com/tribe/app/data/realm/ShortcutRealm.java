package com.tribe.app.data.realm;

import android.support.annotation.StringDef;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by tiago on 09/10/2017.
 */
public class ShortcutRealm extends RealmObject {

  @StringDef({ DEFAULT, HIDDEN, BLOCKED }) public @interface ShortcutStatus {
  }

  public static final String DEFAULT = "DEFAULT";
  public static final String HIDDEN = "HIDDEN";
  public static final String BLOCKED = "BLOCKED";

  public static final String ID = "id";
  public static final String NAME = "name";
  public static final String PICTURE = "picture";
  public static final String STATUS = "status";
  public static final String PINNED = "pinned";
  public static final String READ = "read";
  public static final String MUTE = "mute";
  public static final String SINGLE = "single";
  public static final String LEAVE_ONLINE_UNTIL = "leaveOnlineUntil";

  @PrimaryKey @Index private String id;
  private String name;
  private String picture;
  private boolean pinned;
  private boolean read;
  private boolean mute;
  @Index private boolean single;
  @Index private @ShortcutRealm.ShortcutStatus String status;
  private Date created_at;
  private Date last_activity_at;
  private RealmList<UserRealm> members;
  private String lastMessage;
  private Date leaveOnlineUntil;

  @Ignore private boolean online;
  @Ignore private boolean live;

  public ShortcutRealm() {
    members = new RealmList<>();
  }

  public String getId() {
    return id;
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

  public String getPicture() {
    return picture;
  }

  public void setPicture(String picture) {
    this.picture = picture;
  }

  public boolean isPinned() {
    return pinned;
  }

  public void setPinned(boolean pinned) {
    this.pinned = pinned;
  }

  public void setStatus(@ShortcutStatus String status) {
    this.status = status;
  }

  public @ShortcutStatus String getStatus() {
    return status;
  }

  public boolean isRead() {
    return read;
  }

  public void setRead(boolean read) {
    this.read = read;
  }

  public Date getCreatedAt() {
    return created_at;
  }

  public void setCreatedAt(Date created_at) {
    this.created_at = created_at;
  }

  public Date getLastActivityAt() {
    return last_activity_at;
  }

  public void setLastActivityAt(Date last_activity_at) {
    this.last_activity_at = last_activity_at;
  }

  public RealmList<UserRealm> getMembers() {
    return members;
  }

  public void setMembers(RealmList<UserRealm> members) {
    this.members = members;
  }

  public void setOnline(boolean online) {
    this.online = online;
  }

  public boolean isOnline() {
    return online;
  }

  public void setSingle(boolean single) {
    this.single = single;
  }

  public boolean isSingle() {
    return single;
  }

  public boolean isMute() {
    return mute;
  }

  public void setMute(boolean mute) {
    this.mute = mute;
  }

  public void setLeaveOnlineUntil(Date leaveOnlineUntil) {
    this.leaveOnlineUntil = leaveOnlineUntil;
  }

  public Date getLeaveOnlineUntil() {
    return leaveOnlineUntil;
  }

  public void computeMembersOnline(Map<String, Boolean> onlineMap) {
    for (UserRealm user : members) {
      user.setIsOnline(onlineMap.containsKey(user.getId()));
    }
  }

  public boolean isUniqueMemberOnline() {
    if (!isSingle()) return false;

    return members.get(0).isOnline();
  }

  public boolean isHidden() {
    return status.equalsIgnoreCase(HIDDEN);
  }

  public void setLive(boolean live) {
    this.live = live;
  }

  public boolean isLive() {
    return live;
  }

  public List<String> getMembersIds() {
    List<String> memberIds = new ArrayList<>();

    for (UserRealm userRealm : members) {
      memberIds.add(userRealm.getId());
    }

    return memberIds;
  }

  public UserRealm getSingleFriend() {
    if (members == null || members.size() == 0) return null;

    return members.get(0);
  }

  public String getLastMessage() {
    return lastMessage;
  }

  public void setLastMessage(String lastMessage) {
    this.lastMessage = lastMessage;
  }

  public static boolean isKeyABool(String key) {
    return key.equals(PINNED) || key.equals(READ) || key.equals(MUTE);
  }

  public boolean isSameShortcut(String... userIds) {
    List<String> memberIds = getMembersIds();
    List<String> userIdsList = Arrays.asList(userIds);
    if (userIds.length == members.size()) {
      memberIds.removeAll(userIdsList);
    }

    if (memberIds.size() == 0) return true;

    return false;
  }

  public static boolean isKeyEnum(String key) {
    return key.equals(STATUS);
  }
}
