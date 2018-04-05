package com.tribe.app.domain.entity;

import java.io.Serializable;

/**
 * Created by madaaflak on 05/04/2018.
 */

public class FacebookPayload implements Serializable {

  private String text;
  private int uid;

  public String getText() {
    return text;
  }

  public int getUid() {
    return uid;
  }

  public void setUid(int uid) {
    this.uid = uid;
  }

  public void setText(String text) {
    this.text = text;
  }
}
