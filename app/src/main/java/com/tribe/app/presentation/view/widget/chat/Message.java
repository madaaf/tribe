package com.tribe.app.presentation.view.widget.chat;

import com.tribe.app.domain.entity.User;

/**
 * Created by madaaflak on 05/09/2017.
 */

public class Message {

  private User author;
  private String id;

  public Message(String id) {
    this.id = id;
  }

  public User getAuthor() {
    return author;
  }

  public void setAuthor(User author) {
    this.author = author;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  /*
  private String message;


  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  */
}
