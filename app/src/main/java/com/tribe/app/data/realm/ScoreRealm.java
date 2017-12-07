package com.tribe.app.data.realm;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;
import java.util.UUID;

/**
 * Created by tiago on 12/07/2017.
 */
public class ScoreRealm extends RealmObject {

  @PrimaryKey private String id = UUID.randomUUID().toString();
  private int ranking;
  private int value;
  private String game_id;
  private String user_id;

  @Ignore private GameRealm game;
  @Ignore private UserRealm user;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getGame_id() {
    return game_id;
  }

  public void setGame_id(String game_id) {
    this.game_id = game_id;
  }

  public String getUser_id() {
    return user_id;
  }

  public void setUser_id(String user_id) {
    this.user_id = user_id;
  }

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }

  public int getRanking() {
    return ranking;
  }

  public void setRanking(int ranking) {
    this.ranking = ranking;
  }

  public GameRealm getGame() {
    return game;
  }

  public void setGame(GameRealm game) {
    this.game = game;
  }

  public UserRealm getUser() {
    return user;
  }

  public void setUser(UserRealm user) {
    this.user = user;
  }
}
