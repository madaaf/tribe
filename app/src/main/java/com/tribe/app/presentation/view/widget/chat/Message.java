package com.tribe.app.presentation.view.widget.chat;

import android.support.annotation.StringDef;
import com.tribe.app.domain.entity.User;

/**
 * Created by madaaflak on 05/09/2017.
 */

public class Message {

  @StringDef({ MESSAGE_TEXT, MESSAGE_EMOJI, MESSAGE_IMAGE }) public @interface Type {
  }

  public static final String MESSAGE_TEXT = "TextMessage";
  public static final String MESSAGE_EMOJI = "EmojiMessage";
  public static final String MESSAGE_IMAGE = "ImageMessage";

  private User author;
  private String id;
  private @Type String type;

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
}
