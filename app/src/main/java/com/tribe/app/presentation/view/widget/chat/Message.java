package com.tribe.app.presentation.view.widget.chat;

import com.tribe.app.domain.entity.User;

/**
 * Created by madaaflak on 05/09/2017.
 */

public class Message {

  private String message;
  private User auther;

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public User getAuther() {
    return auther;
  }

  public void setAuther(User auther) {
    this.auther = auther;
  }
}
