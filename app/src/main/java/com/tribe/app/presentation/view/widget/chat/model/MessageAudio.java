package com.tribe.app.presentation.view.widget.chat.model;

/**
 * Created by madaaflak on 07/09/2017.
 */

public class MessageAudio extends Message {

  String time;


  public MessageAudio() {
    super();
  }

  public String getTime() {
    return time;
  }

  public void setTime(String time) {
    this.time = time;
  }

  public MessageAudio(String id) {
    super(id);
  }
}
