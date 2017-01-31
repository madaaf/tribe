package com.tribe.app.domain.entity;

import android.support.annotation.StringDef;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by tiago on 04/08/2016.
 */
public class LabelType implements Serializable {

  @StringDef({
      SET_AS_ADMIN, REMOVE_FROM_GROUP, REMOVE_FROM_ADMIN, BLOCK, CANCEL, OPEN_CAMERA, OPEN_PHOTOS,
      CLEAR_MESSAGES, HIDE, BLOCK_HIDE, GROUP_INFO, GROUP_LEAVE, TRIBE_SAVE, TRIBE_INCREASE_SPEED,
      TRIBE_DECREASE_SPEED, UNHIDE
  }) @Retention(RetentionPolicy.SOURCE) public @interface GenericTypeDef {
  }

  public static final String SET_AS_ADMIN = "setAsAdmin";
  public static final String REMOVE_FROM_GROUP = "removeFromGroup";
  public static final String REMOVE_FROM_ADMIN = "removeFromAdmin";

  public static final String BLOCK = "block";
  public static final String CANCEL = "cancel";

  public static final String OPEN_CAMERA = "openCamera";
  public static final String OPEN_PHOTOS = "openPhotos";

  public static final String CLEAR_MESSAGES = "clearMessages";
  public static final String HIDE = "hide";
  public static final String UNHIDE = "unhide";
  public static final String BLOCK_HIDE = "blockHide";
  public static final String GROUP_INFO = "groupInfo";
  public static final String GROUP_LEAVE = "groupLeave";
  public static final String TRIBE_SAVE = "tribeSave";
  public static final String TRIBE_INCREASE_SPEED = "tribeIncreaseSpeed";
  public static final String TRIBE_DECREASE_SPEED = "tribeDecreaseSpeed";

  public static final String INSTAGRAM = "instagram";
  public static final String TWITTER = "twitter";
  public static final String SNAPCHAT = "snapchat";
  public static final String TRIBE = "tribe";
  public static final String EMAIL = "email";

  public static final String INVITE_SMS = "inviteSMS";
  public static final String INVITE_WHATSAPP = "inviteWhatsapp";
  public static final String INVITE_MESSENGER = "inviteMessenger";
  public static final String SEARCH = "search";

  private @GenericTypeDef String typeDef;
  private String label;

  public LabelType(String label, String typeDef) {
    this.label = label;
    this.typeDef = typeDef;
  }

  public LabelType(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public String getTypeDef() {
    return typeDef;
  }

  @Override public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (label != null ? label.hashCode() : 0);
    return result;
  }
}
