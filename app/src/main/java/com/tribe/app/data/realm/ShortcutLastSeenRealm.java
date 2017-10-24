package com.tribe.app.data.realm;

import io.realm.RealmObject;
import java.util.Date;

/**
 * Created by madaaflak on 23/10/2017.
 */

public class ShortcutLastSeenRealm extends RealmObject {

  private String user_id;
  private Date date;

  public ShortcutLastSeenRealm() {
  }

  public String getUserId() {
    return user_id;
  }

  public void setUserId(String user_id) {
    this.user_id = user_id;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }
}
