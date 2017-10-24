package com.tribe.app.domain;

import java.util.Date;

/**
 * Created by madaaflak on 23/10/2017.
 */

public class ShortcutLastSeen {

  private String userId;
  private Date date;

  public ShortcutLastSeen() {
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }
}
