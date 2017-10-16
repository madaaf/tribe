package com.tribe.app.data.realm;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by madaaflak on 06/09/2017.
 */

public class MessageRealm extends RealmObject {

  public static final String TEXT = "TEXT";
  public static final String EMOJI = "EMOJI";
  public static final String IMAGE = "IMAGE";
  public static final String EVENT = "EVENT";
  public static final String AUDIO = "AUDIO";

  private String localId;
  @PrimaryKey private String id;
  private UserRealm author;
  private UserRealm user;
  private String data;
  private String __typename;
  private ImageRealm original;
  private RealmList<ImageRealm> alts;
  private String action;
  private String created_at;
  private String threadId;

  public MessageRealm() {
  }

  public String getThreadId() {
    return threadId;
  }

  public void setThreadId(String threadId) {
    this.threadId = threadId;
  }

  public String getLocalId() {
    return localId;
  }

  public void setLocalId(String localId) {
    this.localId = localId;
  }

  public String getData() {
    return data;
  }

  public RealmList<ImageRealm> getAlts() {
    return alts;
  }

  public void setAlts(RealmList<ImageRealm> alts) {
    this.alts = alts;
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

  public String getAction() {
    if (action != null) {
      return action.toUpperCase();
    } else {
      return action;
    }
  }

  public UserRealm getUser() {
    return user;
  }

  public void setUser(UserRealm user) {
    this.user = user;
  }

  public void setAction(String action) {
    if (action != null) {
      this.action = action.toUpperCase();
    } else {
      this.action = action;
    }
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

  public String getCreated_at() {
    return created_at;
  }

  public void setCreated_at(String created_at) {
    this.created_at = created_at;
  }
}
