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
      SET_AS_ADMIN, REMOVE_FROM_GROUP, REMOVE_FROM_ADMIN, BLOCK, BLOCK_GROUP, CANCEL, OPEN_CAMERA,
      OPEN_PHOTOS, CLEAR_MESSAGES, HIDE, BLOCK_HIDE, TRIBE_SAVE, TRIBE_INCREASE_SPEED,
      TRIBE_DECREASE_SPEED, UNHIDE
  }) @Retention(RetentionPolicy.SOURCE) public @interface GenericTypeDef {
  }

  public static final String SET_AS_ADMIN = "setAsAdmin";
  public static final String REMOVE_FROM_GROUP = "removeFromGroup";
  public static final String REMOVE_FROM_ADMIN = "removeFromAdmin";

  public static final String BLOCK = "block";
  public static final String BLOCK_GROUP = "blockGroup";
  public static final String CANCELBTN = "cancelbtn";

  public static final String OPEN_CAMERA = "openCamera";
  public static final String OPEN_PHOTOS = "openPhotos";

  public static final String REPORT = "report";
  public static final String CANCEL = "cancel";

  public static final String CLEAR_MESSAGES = "clearMessages";

  public static final String MESSAGE_OPTION_UNSEND = "MESSAGE_OPTION_UNSEND";
  public static final String MESSAGE_OPTION_COPY = "MESSAGE_OPTION_COPY";

  public static final String CHANGE_NAME = "changeName";
  public static final String CHANGE_PICTURE = "changePicture";
  public static final String CUSTOMIZE = "customize";
  public static final String MARK_AS_READ = "markAsRead";
  public static final String HIDE = "hide";
  public static final String MUTE = "mute";
  public static final String UNMUTE = "unmute";
  public static final String UNHIDE = "unhide";
  public static final String DECLINE = "decline";
  public static final String BLOCK_HIDE = "blockHide";
  public static final String TRIBE_SAVE = "tribeSave";
  public static final String TRIBE_INCREASE_SPEED = "tribeIncreaseSpeed";
  public static final String TRIBE_DECREASE_SPEED = "tribeDecreaseSpeed";
  public static final String SEND_CALL_LINK_SMS = "sendCallLinkSms";
  public static final String SEND_CALL_LINK_MESSENGER = "sendCallLinkMessenger";
  public static final String MATCH_RANDOM_PLAYERS = "matchRandomPlayers";
  public static final String SEARCH_FRIENDS = "searchFriends";
  public static final String SCORES = "scores";

  public static final String INSTAGRAM = "instagram";
  public static final String TWITTER = "twitter";
  public static final String SNAPCHAT = "snapchat";
  public static final String TRIBE = "tribe";
  public static final String EMAIL = "email";

  public static final String LOGIN = "login";
  public static final String LOGIN_ALTERNATIVE = "loginAlternative";
  public static final String LOGIN_CALL = "loginCall";
  public static final String FORCE_LOGOUT = "forceLogout";

  public static final String INVITE_SMS = "inviteSMS";
  public static final String INVITE_WHATSAPP = "inviteWhatsapp";
  public static final String INVITE_MESSENGER = "inviteMessenger";
  public static final String SEARCH = "search";

  public static final String GAME_RESTART = "gameRestart";
  public static final String GAME_PLAY_ANOTHER = "gamePlayAnother";
  public static final String GAME_RESET_SCORES = "gameResetScores";
  public static final String GAME_STOP = "gameStop";
  public static final String GAME_LEADERBOARD = "gameLeaderboard";

  public static final String LEAVE_ROOM = "leaveRoom";
  public static final String STOP_GAME_SOLO = "stopGameSolo";

  private @GenericTypeDef String typeDef;
  private String label;
  private int color = -1;
  private int typeface = -1;

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

  public void setColor(int color) {
    this.color = color;
  }

  public void setTypeface(int typeface) {
    this.typeface = typeface;
  }

  public int getTypeface() {
    return typeface;
  }

  public int getColor() {
    return color;
  }

  @Override public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (label != null ? label.hashCode() : 0);
    return result;
  }
}
