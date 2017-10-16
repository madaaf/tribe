package com.tribe.app.data.realm;

import io.realm.RealmObject;

/**
 * Created by madaaflak on 13/10/2017.
 */

public class AudioResourceRealm extends RealmObject {

  private String url;
  private Float duration;
  private Integer filesize;

  public AudioResourceRealm() {
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Float getDuration() {
    return duration;
  }

  public void setDuration(Float duration) {
    this.duration = duration;
  }

  public Integer getFilesize() {
    return filesize;
  }

  public void setFilesize(Integer filesize) {
    this.filesize = filesize;
  }
}
