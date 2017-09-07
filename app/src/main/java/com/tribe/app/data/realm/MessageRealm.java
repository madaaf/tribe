package com.tribe.app.data.realm;

import com.tribe.app.domain.entity.User;
import io.realm.RealmObject;

/**
 * Created by madaaflak on 06/09/2017.
 */

public class MessageRealm extends RealmObject {

  private String id;
  private UserRealm author;


  public MessageRealm() {
  }

  public MessageRealm(String id) {
    this.id = id;
  }

  public UserRealm getAuthor() {
    return author;
  }

  public void setAuthor(UserRealm author) {
    this.author = author;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
