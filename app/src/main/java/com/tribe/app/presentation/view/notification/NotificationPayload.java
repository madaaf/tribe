package com.tribe.app.presentation.view.notification;

import android.support.annotation.StringDef;
import com.tribe.app.presentation.utils.StringUtils;
import java.io.Serializable;

/**
 * Created by tiago on 29/01/2017.
 */

public class NotificationPayload implements Serializable {

  @StringDef({ CLICK_ACTION_ONLINE, CLICK_ACTION_FRIENDSHIP }) public @interface ClickActionType {
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
  private String session_id;
  private String group_id;
  private String group_name;
  private String user_picture;
  private String group_picture;
  private boolean shouldDisplayDrag = true;
  private String thread;
  private int live_users_length;

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

  public void setGroupPicture(String groupPicture) {
    this.group_picture = groupPicture;
  }

  public String getGroupPicture() {
    return group_picture;
  }

  public void setUserPicture(String userPicture) {
    this.user_picture = userPicture;
  }

  public String getUserPicture() {
    return user_picture;
  }

  public boolean isShouldDisplayDrag() {
    return shouldDisplayDrag;
  }

  public void setShouldDisplayDrag(boolean shouldDisplayDrag) {
    this.shouldDisplayDrag = shouldDisplayDrag;
  }

  public void setThread(String thread) {
    this.thread = thread;
  }

  public String getThread() {
    return thread;
  }

  public int getLiveUsersLength() {
    return live_users_length;
  }

  public void setLiveUsersLength(int live_users_length) {
    this.live_users_length = live_users_length;
  }

  public boolean isLive() {
    return click_action.equals(CLICK_ACTION_BUZZ) || click_action.equals(CLICK_ACTION_LIVE);
  }

  public boolean isUserCall() {
    return StringUtils.isEmpty(getGroupId()) && !StringUtils.isEmpty(getUserId());
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || !(o instanceof NotificationPayload)) return false;

    NotificationPayload that = (NotificationPayload) o;

    return group_id != null ? group_id.equals(that.group_id)
        : (that.user_id != null ? user_id.equals(that.user_id)
            : (session_id != null ? session_id.equals(that.session_id) : session_id == null));
  }
}
