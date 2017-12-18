package com.tribe.app.presentation.view.notification;

import android.support.annotation.StringDef;
import com.google.gson.JsonArray;
import com.tribe.app.presentation.utils.StringUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tiago on 29/01/2017.
 */

public class NotificationPayload implements Serializable {

  @StringDef({
      CLICK_ACTION_ONLINE, CLICK_ACTION_FRIENDSHIP, CLICK_ACTION_BUZZ, CLICK_ACTION_LIVE,
      CLICK_ACTION_END_LIVE, CLICK_ACTION_DECLINE, CLICK_ACTION_JOIN_CALL, CLICK_ACTION_GAME_LEADER
  }) public @interface ClickActionType {
  }

  public static final String CLICK_ACTION_ONLINE = "online";
  public static final String CLICK_ACTION_FRIENDSHIP = "friendship";
  public static final String CLICK_ACTION_LIVE = "live";
  public static final String CLICK_ACTION_BUZZ = "buzz";
  public static final String CLICK_ACTION_END_LIVE = "end_live";
  public static final String CLICK_ACTION_DECLINE = "decline_live";
  public static final String CLICK_ACTION_JOIN_CALL = "join_live";
  public static final String CLICK_ACTION_USER_REGISTERED = "user_registered";
  public static final String CLICK_ACTION_MESSAGE = "message";
  public static final String ACTION_JOINED = "joined";
  public static final String ACTION_LEFT = "left";
  public static final String CLICK_ACTION_GAME_LEADER = "game_friend_leader";

  private String user_id;
  private String body;
  private int badge;
  private String title;
  private String click_action;
  private String action;
  private String user_display_name;
  private String sound;
  private String session_id;
  private String user_picture;
  private String room_link;
  private boolean shouldDisplayDrag = true;
  private String thread;
  private int live_users_length;
  private List<MissedCallAction> missedCallList = new ArrayList<>();
  private long time;
  private String message_picture;
  private String users_ids;
  private JsonArray thread_id;
  private String game_id;

  public void setTime(long time) {
    this.time = time;
  }

  public String getUsers_ids() {
    return users_ids;
  }

  public void setUsers_ids(String users_ids) {
    this.users_ids = users_ids;
  }

  public void setThread_id(JsonArray thread_id) {
    this.thread_id = thread_id;
  }

  public long getTime() {
    return time;
  }

  public void setUserId(String userId) {
    this.user_id = userId;
  }

  public String getUserId() {
    return user_id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
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

  public List<MissedCallAction> getMissedCallList() {
    return missedCallList;
  }

  public void setMissedCallActionList(List<MissedCallAction> missedCallList) {
    this.missedCallList = missedCallList;
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

  public String getRoomLink() {
    return room_link;
  }

  public void setRoomLink(String room_link) {
    this.room_link = room_link;
  }

  public int getBadge() {
    return badge;
  }

  public void setBadge(int badge) {
    this.badge = badge;
  }

  public void setMessagePicture(String messagePicture) {
    this.message_picture = messagePicture;
  }

  public String getMessagePicture() {
    return message_picture;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getAction() {
    return action;
  }

  public boolean isLive() {
    return click_action == null ||
        click_action.equals(CLICK_ACTION_BUZZ) ||
        click_action.equals(CLICK_ACTION_LIVE);
  }

  public boolean isUserCall() {
    return !StringUtils.isEmpty(getUserId());
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || !(o instanceof NotificationPayload)) return false;

    NotificationPayload that = (NotificationPayload) o;

    return (that.user_id != null ? user_id.equals(that.user_id)
        : (session_id != null ? session_id.equals(that.session_id) : session_id == null));
  }
}
