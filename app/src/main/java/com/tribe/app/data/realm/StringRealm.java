package com.tribe.app.data.realm;

import io.realm.RealmObject;

/**
 * Created by madaaflak on 12/03/2018.
 */

public class StringRealm extends RealmObject {

  private String content;

  public StringRealm() {
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }


}
