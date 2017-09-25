package com.tribe.app.presentation.view.widget.chat.model;

/**
 * Created by madaaflak on 07/09/2017.
 */

public class MessageEmoji extends Message {

  private String emoji;

  public MessageEmoji() {
    super();
  }

  public MessageEmoji(String id) {
    super(id);
  }

  public String getEmoji() {
    return emoji;
  }

  public void setEmoji(String emoji) {
    this.emoji = emoji;
  }

  @Override public String toString() {
    return "MessageEmoji{" + "emoji='" + emoji + '\'' + '}';
  }
}
