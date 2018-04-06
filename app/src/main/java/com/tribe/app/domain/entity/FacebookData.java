package com.tribe.app.domain.entity;

import java.util.List;

/**
 * Created by madaaflak on 05/04/2018.
 */

public class FacebookData {

  private int __ar;
  private List<FacebookPayload> payload;

  public int get__ar() {
    return __ar;
  }

  public void set__ar(int __ar) {
    this.__ar = __ar;
  }

  public List<FacebookPayload> getPayload() {
    return payload;
  }

  public void setPayload(List<FacebookPayload> payload) {
    this.payload = payload;
  }
}
