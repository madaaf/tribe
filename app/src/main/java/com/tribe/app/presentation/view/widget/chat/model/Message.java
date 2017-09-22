package com.tribe.app.presentation.view.widget.chat.model;

import android.support.annotation.StringDef;
import com.tribe.app.domain.entity.User;

/**
 * Created by madaaflak on 05/09/2017.
 */

public class Message {

  @StringDef({ MESSAGE_TEXT, MESSAGE_EMOJI, MESSAGE_IMAGE, MESSAGE_EVENT }) public @interface Type {
  }

  public static final String MESSAGE_TEXT = "TextMessage";
  public static final String MESSAGE_EMOJI = "EmojiMessage";
  public static final String MESSAGE_IMAGE = "ImageMessage";
  public static final String MESSAGE_EVENT = "EventMessage";

  private User author;
  private String id;
  private @Type String type;
  private String creationDate;

  public Message() {
  }

  public Message(String id) {
    this.id = id;
  }

  public User getAuthor() {
    return author;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
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

  public String getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(String creationDate) {
    this.creationDate = creationDate;
  }
}
