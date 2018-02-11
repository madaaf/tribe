package com.tribe.app.data.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by tiago on 12/07/2017.
 */
public class ScoreUserRealm extends RealmObject {

  @PrimaryKey private String id;

  private String display_name;
  private String username;
  private String picture;
  private int value, ranking;

  public ScoreUserRealm() {

  }

  public ScoreUserRealm(String id, String display_name, String username, String picture) {
    this.id = id;
    this.display_name = display_name;
    this.username = username;
    this.picture = picture;
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

  public void setValue(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  public void setRanking(int ranking) {
    this.ranking = ranking;
  }

  public int getRanking() {
    return ranking;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || !(o instanceof ScoreUserRealm)) return false;

    ScoreUserRealm that = (ScoreUserRealm) o;

    return id != null ? id.equals(that.getId()) : that.getId() == null;
  }
}
