package com.tribe.app.domain.entity;

import com.tribe.tribelivesdk.game.Game;
import io.realm.RealmObject;

/**
 * Created by tiago on 12/07/2017.
 */
public class Score extends RealmObject {

  private String id;
  private int ranking;
  private int value;
  private User user;
  private Game game;

  public Score(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public int getRanking() {
    return ranking;
  }

  public void setRanking(int ranking) {
    this.ranking = ranking;
  }

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Game getGame() {
    return game;
  }

  public void setGame(Game game) {
    this.game = game;
  }
}
