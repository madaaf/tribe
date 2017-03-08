package com.tribe.tribelivesdk.model;

import java.util.List;

/**
 * Created by tiago on 07/02/2017.
 */

public class TribeGuest {

  public static final String ID = "id";
  public static final String DISPLAY_NAME = "display_name";
  public static final String PICTURE = "picture";

  private String id;
  private String displayName;
  private String picture;
  private boolean isGroup;
  private boolean isInvite;
  private List<String> memberPics;

  public TribeGuest(String id) {
    this.id = id;
  }

  public TribeGuest(String id, String displayName, String picture, boolean isGroup,
      boolean isInvite, List<String> memberPics) {
    this.id = id;
    this.displayName = displayName;
    this.picture = picture;
    this.isGroup = isGroup;
    this.isInvite = isInvite;
    this.memberPics = memberPics;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getPicture() {
    return picture;
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
}
