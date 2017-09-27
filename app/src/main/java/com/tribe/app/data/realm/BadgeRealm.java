package com.tribe.app.data.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by tiago on 09/27/2017.
 */

public class BadgeRealm extends RealmObject {

  @PrimaryKey String id;
  private int value;

  public BadgeRealm() {
    id = "BADGE";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }
}
