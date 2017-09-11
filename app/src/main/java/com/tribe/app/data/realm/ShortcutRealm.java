package com.tribe.app.data.realm;

import android.support.annotation.StringDef;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import java.util.Date;

/**
 * Created by tiago on 09/10/2017.
 */
public class ShortcutRealm extends RealmObject {

  @StringDef({ DEFAULT, HIDDEN, BLOCKED }) public @interface ShortcutStatus {
  }

  public static final String DEFAULT = "DEFAULT";
  public static final String HIDDEN = "HIDDEN";
  public static final String BLOCKED = "BLOCKED";

  @PrimaryKey private String id;
  private String name;
  private String picture;
  private boolean online;
  private boolean pinned;
  private @ShortcutRealm.ShortcutStatus String status;
  private boolean read;
  private Date created_at;
  private Date last_activity_at;
  private RealmList<UserRealm> members;

  public ShortcutRealm() {

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
}
