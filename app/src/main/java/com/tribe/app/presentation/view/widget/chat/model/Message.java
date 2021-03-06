package com.tribe.app.presentation.view.widget.chat.model;

import android.support.annotation.StringDef;
import com.tribe.app.domain.entity.User;
import java.io.Serializable;

/**
 * Created by madaaflak on 05/09/2017.
 */

public class Message implements Serializable {

  @StringDef({
      MESSAGE_TEXT, MESSAGE_EMOJI, MESSAGE_IMAGE, MESSAGE_EVENT, MESSAGE_AUDIO, MESSAGE_POKE
  }) public @interface Type {
  }

  public static final String PENDING = "PENDING";

  public static final String MESSAGE_TEXT = "TextMessage";
  public static final String MESSAGE_EMOJI = "EmojiMessage";
  public static final String MESSAGE_IMAGE = "ImageMessage";
  public static final String MESSAGE_EVENT = "EventMessage";
  public static final String MESSAGE_AUDIO = "AudioMessage";
  public static final String MESSAGE_POKE = "PokeMessage";

  private User author;
  private String id;
  private @Type String type;
  private String creationDate;
  private boolean isPending = false;
  private String content;
  private String supportAuthorId;

  public Message() {
  }

  public String getSupportAuthorId() {
    return supportAuthorId;
  }

  public void setSupportAuthorId(String supportAuthorId) {
    this.supportAuthorId = supportAuthorId;
  }

  public boolean isPending() {
    return isPending;
  }

  public void setPending(boolean pending) {
    isPending = pending;
  }

  public Message(String id) {
    this.id = id;
  }

  public User getAuthor() {
    return author;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getContent() {
    return content;
  }

  public String getMessageContent() {
    switch (type) {
      case MESSAGE_TEXT:
        return ((MessageText) this).getMessage();
      case MESSAGE_EVENT:
        ((MessageEvent) this).getAction();
        return ((MessageEvent) this).getAction();
      case MESSAGE_EMOJI:
        return ((MessageEmoji) this).getEmoji();
      case MESSAGE_IMAGE:
        return ((MessageImage) this).getOriginal().getUrl();
    }
    return null;
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

  @Override public boolean equals(Object obj) {
    if (obj == null) return false;

    if (!(obj instanceof Message)) return false;
    Message other = (Message) obj;

    return (this.id.equals(other.id) && this.id.equals(other.id));
  }

  @Override public int hashCode() {
    return this.id.length();
  }
}
