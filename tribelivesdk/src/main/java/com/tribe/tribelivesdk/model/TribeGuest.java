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
  private boolean isGroup;
  private boolean isInvite;
  private List<String> memberPics;
  private boolean canRemove;
  private boolean isAnonymous = false;
  private boolean isExternal = false;

  public TribeGuest(String id) {
    this.id = id;
  }

  public TribeGuest(String id, String displayName, String picture, boolean isGroup,
      boolean isInvite, List<String> memberPics, boolean canRemove, String userName) {
    this.id = id;
    this.displayName = displayName;
    this.picture = picture;
    this.isGroup = isGroup;
    this.isInvite = isInvite;
    this.memberPics = memberPics;
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

  public boolean isGroup() {
    return isGroup;
  }

  public void setGroup(boolean group) {
    isGroup = group;
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

  public void setCanRemove(boolean canRemove) {
    this.canRemove = canRemove;
  }

  public void setExternal(boolean external) {
    isExternal = external;
  }

  public boolean isExternal() {
    return isExternal;
  }
}
