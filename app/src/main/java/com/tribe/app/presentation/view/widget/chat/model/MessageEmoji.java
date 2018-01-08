package com.tribe.app.presentation.view.widget.chat.model;

/**
 * Created by madaaflak on 07/09/2017.
 */

public class MessageEmoji extends Message {

  private String emoji;
  private boolean isUpdating;

  public MessageEmoji() {
    super();
  }

  public MessageEmoji(String id) {
    super(id);
  }

  public String getEmoji() {
    return emoji;
  }

  public boolean isUpdating() {
    return isUpdating;
  }

  public void setUpdating(boolean updating) {
    isUpdating = updating;
  }

  public void setEmoji(String emoji) {
    this.emoji = emoji;
  }

  @Override public String toString() {
    return "MessageEmoji{" + "emoji='  id " + getId() + " " + emoji + '\'' + '}';
  }
}
