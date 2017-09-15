package com.tribe.app.data.realm;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by madaaflak on 06/09/2017.
 */

public class MessageRealm extends RealmObject {

  public static final String TEXT = "TEXT";
  public static final String EMOJI = "EMOJI";
  public static final String IMAGE = "IMAGE";
  public static final String EVENT = "EVENT";

  private String id;
  private UserRealm author;
  private String data;
  private String __typename;
  private ImageRealm original;
  private RealmList<ImageRealm> resources;

  public MessageRealm() {
  }

  public String getData() {
    return data;
  }

  public RealmList<ImageRealm> getResources() {
    return resources;
  }

  public void setResources(RealmList<ImageRealm> resources) {
    this.resources = resources;
  }

  public String get__typename() {
    return __typename;
  }

  public ImageRealm getOriginal() {
    return original;
  }

  public void setOriginal(ImageRealm original) {
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
