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

  public TribeGuest(String id) {
    this.id = id;
  }

  public TribeGuest(String id, String displayName, String picture) {
    this.id = id;
    this.displayName = displayName;
    this.picture = picture;
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
}
