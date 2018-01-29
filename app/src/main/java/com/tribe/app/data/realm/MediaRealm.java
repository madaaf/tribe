package com.tribe.app.data.realm;

import io.realm.RealmObject;

/**
 * Created by madaaflak on 07/09/2017.
 */

public class MediaRealm extends RealmObject {

  private String url;
  private Integer filesize;
  private String width;
  private String height;
  private float duration;

  public MediaRealm() {
  }

  public float getDuration() {
    return duration;
  }

  public void setDuration(float duration) {
    this.duration = duration;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Integer getFilesize() {
    return filesize;
  }

  public void setFilesize(Integer filesize) {
    this.filesize = filesize;
  }

  public String getWidth() {
    return width;
  }

  public void setWidth(String width) {
    this.width = width;
  }

  public String getHeight() {
    return height;
  }

  public void setHeight(String height) {
    this.height = height;
  }
}
