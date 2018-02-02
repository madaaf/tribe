package com.tribe.tribelivesdk.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by tiago on 07/02/2017.
 */

public class TribeGuest extends GroupJoiner implements Serializable {

  public static final String ID = "id";
  public static final String DISPLAY_NAME = "display_name";
  public static final String PICTURE = "picture";
  public static final String USERNAME = "username";

  private String id;
  private String displayName;
  private String userName;
  private String picture;
  private boolean isInvite;
  private List<String> memberPics;
  private boolean canRemove;
  private boolean isAnonymous = false;
  private boolean isExternal = false;
  private boolean isFriend = true;
  private int scoreValue;

  public TribeGuest(String id) {
    this.id = id;
  }

  public TribeGuest(String id, String displayName, String picture, boolean isInvite,
      boolean canRemove, String userName) {
    this.id = id;
    this.displayName = displayName;
    this.picture = picture;
    this.isInvite = isInvite;
    this.canRemove = canRemove;
    this.userName = userName;
  }

  public boolean isAnonymous() {
    return isAnonymous;
  }

  public void setAnonymous(boolean anonymous) {
    isAnonymous = anonymous;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getPicture() {
    return picture;
  }

  public void setPicture(String picture) {
    this.picture = picture;
  }

  public String getId() {
    return id;
  }

  public void setMemberPics(List<String> memberPics) {
    this.memberPics = memberPics;
  }

  public List<String> getMemberPics() {
    return memberPics;
  }

  public boolean isInvite() {
    return isInvite;
  }

  public void setInvite(boolean invite) {
    isInvite = invite;
  }

  public boolean canRemove() {
    return canRemove;
  }

  public boolean isFriend() {
    return isFriend;
  }

  public void setFriend(boolean friend) {
    isFriend = friend;
  }

  public void setCanRemove(boolean canRemove) {
    this.canRemove = canRemove;
  }

  public void setExternal(boolean external) {
    isExternal = external;
  }

  public boolean isExternal() {
    return isExternal;
  }

  public boolean canPlayGames(String gameId) {
    return true;
  }

  public void setScoreValue(int scoreValue) {
    this.scoreValue = scoreValue;
  }

  public int getScoreValue() {
    return scoreValue;
  }
}
