package com.tribe.app.presentation.view.notification;

import java.io.Serializable;

/**
 * Created by madaaflak on 06/12/2017.
 */

public class PayloadMissedCallAction implements Serializable {

  private String user_id;
  private String user_display_name;
  private String user_picture;
  private String click_action;
  private String body;
  private long time;
  private String title;
  private String session_id;

  public PayloadMissedCallAction(String user_id, String user_display_name, String user_picture,
      String click_action, String body, long time, String title, String session_id) {
    this.user_id = user_id;
    this.user_display_name = user_display_name;
    this.user_picture = user_picture;
    this.click_action = click_action;
    this.body = body;
    this.time = time;
    this.title = title;
    this.session_id = session_id;
  }

  public String getSessionId() {
    return session_id;
  }

  public void setSessionId(String session_id) {
    this.session_id = session_id;
  }

  public String getUserId() {
    return user_id;
  }

  public void setUserId(String user_id) {
    this.user_id = user_id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getUserDisplayName() {
    return user_display_name;
  }

  public void setUserDisplayName(String user_display_name) {
    this.user_display_name = user_display_name;
  }

  public String getUserPicture() {
    return user_picture;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public void setUserPicture(String user_picture) {
    this.user_picture = user_picture;
  }

  public void setClickAction(String clickAction) {
    this.click_action = clickAction;
  }

  public String getClickAction() {
    return click_action;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public String getBody() {
    return body;
  }
}
