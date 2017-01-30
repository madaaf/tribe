package com.tribe.app.presentation.view.notification;

/**
 * Created by tiago on 29/01/2017.
 */

public class NotificationPayload {

  private String user_id;
  private String body;
  private String click_action;
  private String user_display;

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

  public void setUserDisplay(String userDisplay) {
    this.user_display = userDisplay;
  }

  public String getUserDisplay() {
    return user_display;
  }
}
