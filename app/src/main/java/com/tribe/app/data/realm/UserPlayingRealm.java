package com.tribe.app.data.realm;

import io.realm.RealmObject;

/**
 * Created by tiago on 01/02/2018.
 */

public class UserPlayingRealm extends RealmObject {

  private String game_id;
  private String room_id;

  public String getGame_id() {
    return game_id;
  }

  public void setGame_id(String game_id) {
    this.game_id = game_id;
  }

  public String getRoom_id() {
    return room_id;
  }

  public void setRoom_id(String room_id) {
    this.room_id = room_id;
  }
}
