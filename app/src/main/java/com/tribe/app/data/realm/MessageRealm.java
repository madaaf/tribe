package com.tribe.app.data.realm;

import io.realm.RealmObject;

/**
 * Created by madaaflak on 06/09/2017.
 */

public class MessageRealm extends RealmObject {

  public static final String MESSAGE_TEXT = "TextMessage";
  public static final String EMOJI = "EmojiMessage";
  public static final String IMAGE = "ImageMessage";

  private String id;
  private UserRealm author;
  private String data;
  private String __typename;
  private OriginalRealm original;

  public MessageRealm() {
  }

  public String getData() {
    return data;
  }

  public String get__typename() {
    return __typename;
  }

  public OriginalRealm getOriginal() {
    return original;
  }

  public void setOriginal(OriginalRealm original) {
    this.original = original;
  }

  public void set__typename(String __typename) {
    this.__typename = __typename;
  }

  public void setData(String data) {
    this.data = data;
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
