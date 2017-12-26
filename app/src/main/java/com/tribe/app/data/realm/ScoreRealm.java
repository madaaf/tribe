package com.tribe.app.data.realm;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import java.util.UUID;

/**
 * Created by tiago on 12/07/2017.
 */
public class ScoreRealm extends RealmObject {

  @PrimaryKey String id = UUID.randomUUID().toString();
  private int ranking;
  private int value;
  private ScoreUserRealm user;
  private String game_id;

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
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

  public void setGame_id(String game_id) {
    this.game_id = game_id;
  }

  public String getGame_id() {
    return game_id;
  }

  public ScoreUserRealm getUser() {
    return user;
  }

  public void setUser(ScoreUserRealm user) {
    this.user = user;
  }

  public static ScoreRealm deserialize(JsonObject jobject, Gson gson) {
    ScoreRealm scoreRealm = new ScoreRealm();
    if (jobject.has("value")) scoreRealm.setValue(jobject.get("value").getAsInt());
    if (jobject.has("ranking")) scoreRealm.setRanking(jobject.get("ranking").getAsInt());
    if (jobject.has("user")) {
      scoreRealm.setUser(gson.fromJson(jobject.get("user"), ScoreUserRealm.class));
    }
    if (jobject.has("game")) {
      scoreRealm.setGame_id(jobject.get("game").getAsJsonObject().get("id").getAsString());
    }
    return scoreRealm;
  }
}
