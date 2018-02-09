package com.tribe.app.presentation.view.widget.chat.model;

/**
 * Created by madaaflak on 07/09/2017.
 */

public class MessageEmoji extends Message {

  public static String[] hearts = new String[] {
      "\u2764", // "default"
      "\uD83D\uDC9C", // purple
      "\uD83D\uDC99", // blue
      "\uD83D\uDC9A", //green
      "\uD83D\uDC9B", // yellow
  };


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
