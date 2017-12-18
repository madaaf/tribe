package com.tribe.app.data.realm;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by tiago on 12/07/2017.
 */
public class ScoreUserRealm extends RealmObject {

  @PrimaryKey private String id;

  private String display_name;
  private String username;
  private String picture;

  @Ignore private boolean is_online = false;
  @Ignore private boolean is_live = false;

  public ScoreUserRealm() {

  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDisplay_name() {
    return display_name;
  }

  public void setDisplay_name(String display_name) {
    this.display_name = display_name;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPicture() {
    return picture;
  }

  public void setPicture(String picture) {
    this.picture = picture;
  }

  public boolean is_online() {
    return is_online;
  }

  public void setIs_online(boolean is_online) {
    this.is_online = is_online;
  }

  public boolean is_live() {
    return is_live;
  }

  public void setIs_live(boolean is_live) {
    this.is_live = is_live;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || !(o instanceof ScoreUserRealm)) return false;

    ScoreUserRealm that = (ScoreUserRealm) o;

    return id != null ? id.equals(that.getId()) : that.getId() == null;
  }
}
