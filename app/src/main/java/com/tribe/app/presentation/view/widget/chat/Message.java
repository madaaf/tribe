package com.tribe.app.presentation.view.widget.chat;

import com.tribe.app.domain.entity.User;

/**
 * Created by madaaflak on 05/09/2017.
 */

public class Message {

  private String id;

  public Message(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  /*
  private String message;
  private User author;

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public User getAuthor() {
    return author;
  }

  public void setAuthor(User author) {
    this.author = author;
  }*/
}
