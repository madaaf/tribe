package com.tribe.app.data.realm;

import io.realm.RealmObject;

/**
 * Created by tiago on 24/01/2018.
 */

public class AnimationIconRealm extends RealmObject {

  private String url;

  public AnimationIconRealm() {
  }

  public AnimationIconRealm(String url) {
    this.url = url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getUrl() {
    return url;
  }
}
