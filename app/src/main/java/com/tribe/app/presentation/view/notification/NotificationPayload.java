package com.tribe.app.presentation.view.notification;

import android.support.annotation.StringDef;
import java.io.Serializable;

/**
 * Created by tiago on 29/01/2017.
 */

public class NotificationPayload implements Serializable {

  @StringDef({ CLICK_ACTION_ONLINE, CLICK_ACTION_FRIENDSHIP })
  public @interface ClickActionType {
  }

  public static final String CLICK_ACTION_ONLINE = "online";
  public static final String CLICK_ACTION_FRIENDSHIP = "friendship";
  public static final String CLICK_ACTION_LIVE = "live";
  public static final String CLICK_ACTION_BUZZ = "buzz";

  private String user_id;
  private String body;
  private String click_action;
  private String user_display_name;
  private String sound;
  private String user_display_names;
  private String session_id;
  private String group_id;
  private String group_name;
  private String other_user_display_names;
  private String profile_picture;

  public void setUserId(String userId) {
    this.user_id = userId;
  }

  public String getUserId() {
    return user_id;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public String getBody() {
    return body;
  }

  public void setClickAction(String clickAction) {
    this.click_action = clickAction;
  }

  public String getClickAction() {
    return click_action;
  }

  public String getSound() {
    return sound;
  }

  public void setSound(String sound) {
    this.sound = sound;
  }

  public String getUserDisplayName() {
    return user_display_name;
  }

  public void setUserDisplayName(String userDisplayName) {
    this.user_display_name = userDisplayName;
  }

  public String getUserDisplayNames() {
    return user_display_names;
  }

  public void setUser_display_names(String userDisplayNames) {
    this.user_display_names = userDisplayNames;
  }

  public String getSessionId() {
    return session_id;
  }

  public void setSessionId(String sessionId) {
    this.session_id = sessionId;
  }

  public String getGroupId() {
    return group_id;
  }

  public void setGroupId(String groupId) {
    this.group_id = groupId;
  }

  public String getGroupName() {
    return group_name;
  }

  public void setGroupName(String group_name) {
    this.group_name = group_name;
  }

  public String getOtherUserDisplayNames() {
    return other_user_display_names;
  }

  public void setOtherUserDisplayNames(String otherUserDisplayNames) {
    this.other_user_display_names = otherUserDisplayNames;
  }

  public void setProfilePicture(String profilePicture) {
    this.profile_picture = profilePicture;
  }

  public String getProfilePicture() {
    return profile_picture;
  }
}
