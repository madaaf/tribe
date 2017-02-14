package com.tribe.tribelivesdk.model;

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

  public TribeGuest(String id) {
    this.id = id;
  }

  public TribeGuest(String id, String displayName, String picture, boolean isGroup) {
    this.id = id;
    this.displayName = displayName;
    this.picture = picture;
    this.isGroup = isGroup;
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
}
