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
  public static final String POKE = "POKE";

  private String localId;
  @PrimaryKey private String id;
  private UserRealm author;
  private UserRealm user;
  private String data;
  private String __typename;
  private MediaRealm original;
  private RealmList<MediaRealm> alts;
  private String action;
  private String created_at;
  private String threadId;
  private String supportAuthorId;
  private String client_message_id;
  private String intent;
  private String game_id;



  public MessageRealm() {
  }

  public String getClient_message_id() {
    return client_message_id;
  }

  public void setClient_message_id(String client_message_id) {
    this.client_message_id = client_message_id;
  }

  public String getIntent() {
    return intent;
  }

  public void setIntent(String intent) {
    this.intent = intent;
  }

  public String getGame_id() {
    return game_id;
  }

  public void setGame_id(String game_id) {
    this.game_id = game_id;
  }

  public String getSupportAuthorId() {
    return supportAuthorId;
  }

  public void setSupportAuthorId(String supportAuthorId) {
    this.supportAuthorId = supportAuthorId;
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

  public RealmList<MediaRealm> getAlts() {
    return alts;
  }

  public void setAlts(RealmList<MediaRealm> alts) {
    this.alts = alts;
  }

  public String get__typename() {
    return __typename;
  }

  public MediaRealm getOriginal() {
    return original;
  }

  public void setOriginal(MediaRealm original) {
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
    setAuthor(user);
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

  @Override public String toString() {
    return "MessageRealm{"
        + "localId='"
        + localId
        + '\''
        + ", id='"
        + id
        + '\''
        + ", author="
        + author
        + ", user="
        + user
        + ", data='"
        + data
        + '\''
        + ", __typename='"
        + __typename
        + '\''
        + ", original="
        + '\''
        + ", created_at='"
        + created_at
        + '\''
        + ", threadId='"
        + threadId
        + '\''
        + '}';
  }
}
