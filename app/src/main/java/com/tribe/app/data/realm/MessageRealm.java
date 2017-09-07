package com.tribe.app.data.realm;

import io.realm.RealmObject;

/**
 * Created by madaaflak on 06/09/2017.
 */

public class MessageRealm extends RealmObject {

  private String id;

  public MessageRealm() {
  }

  public MessageRealm(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
