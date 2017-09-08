package com.tribe.app.presentation.view.widget.chat;

/**
 * Created by madaaflak on 07/09/2017.
 */

public class MessageImage extends Message {

  private Original original;

  public MessageImage(String id) {
    super(id);
  }

  public Original getOriginal() {
    return original;
  }

  public void setOriginal(Original original) {
    this.original = original;
  }
}
