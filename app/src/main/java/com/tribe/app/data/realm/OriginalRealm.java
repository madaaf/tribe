package com.tribe.app.data.realm;

import io.realm.RealmObject;

/**
 * Created by madaaflak on 07/09/2017.
 */

public class OriginalRealm extends RealmObject {

  private String url;
  private String filesize;
  private String width;
  private String height;

  public OriginalRealm() {
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getFilesize() {
    return filesize;
  }

  public void setFilesize(String filesize) {
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
