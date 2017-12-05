package com.tribe.app.presentation.view.widget.chat.model;

import android.net.Uri;
import java.util.List;

/**
 * Created by madaaflak on 07/09/2017.
 */

public class MessageImage extends Message {

  private Image original;
  private List<Image> ressources;
  private Uri uri;

  public MessageImage(String id) {
    super(id);
  }

  public MessageImage() {
    super();
  }

  public Uri getUri() {
    return uri;
  }

  public void setUri(Uri uri) {
    this.uri = uri;
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

  @Override public String toString() {
    return "MessageImage{"
        + "id = "
        + getId()
        + "original="
        + original
        + ", ressources="
        + ressources
        + ", uri="
        + uri
        + '}';
  }
}
