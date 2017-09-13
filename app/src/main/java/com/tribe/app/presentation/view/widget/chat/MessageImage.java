package com.tribe.app.presentation.view.widget.chat;

import java.util.List;

/**
 * Created by madaaflak on 07/09/2017.
 */

public class MessageImage extends Message {

  private Image original;
  private List<Image> ressources;

  public MessageImage() {
    super();
  }

  public List<Image> getRessources() {
    return ressources;
  }

  public void setRessources(List<Image> ressources) {
    this.ressources = ressources;
  }

  public Image getOriginal() {
    return original;
  }

  public void setOriginal(Image original) {
    this.original = original;
  }
}
