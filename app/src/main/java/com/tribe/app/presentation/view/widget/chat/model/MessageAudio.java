package com.tribe.app.presentation.view.widget.chat.model;

import java.util.List;

/**
 * Created by madaaflak on 07/09/2017.
 */

public class MessageAudio extends Message {

  private Media original;
  private List<Media> ressources;
  private String time;
  private boolean pause = true;

  public boolean isPause() {
    return pause;
  }

  public void setPause(boolean pause) {
    this.pause = pause;
  }

  public Media getOriginal() {
    return original;
  }

  public void setOriginal(Media original) {
    this.original = original;
  }

  public List<Media> getRessources() {
    return ressources;
  }

  public void setRessources(List<Media> ressources) {
    this.ressources = ressources;
  }

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
