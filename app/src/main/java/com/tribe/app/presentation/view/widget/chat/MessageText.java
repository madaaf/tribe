package com.tribe.app.presentation.view.widget.chat;

/**
 * Created by madaaflak on 07/09/2017.
 */

public class MessageText extends Message {

  private String message;

  public MessageText() {
    super();
  }

  public MessageText(String id) {
    super(id);
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @Override public String toString() {
    return "MessageText{" + "message='" + message + '\'' + '}';
  }
}
