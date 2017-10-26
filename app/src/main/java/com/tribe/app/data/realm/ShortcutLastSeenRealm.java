package com.tribe.app.data.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import java.util.UUID;

/**
 * Created by madaaflak on 23/10/2017.
 */

public class ShortcutLastSeenRealm extends RealmObject {


  @PrimaryKey private String id = UUID.randomUUID().toString();
  private String user_id;
  private String date;

  public ShortcutLastSeenRealm() {
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUserId() {
    return user_id;
  }

  public void setUserId(String user_id) {
    this.user_id = user_id;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }
}
