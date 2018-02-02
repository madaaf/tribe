package com.tribe.app.domain.entity;

import java.io.Serializable;

/**
 * Created by tiago on 01/02/2018.
 */

public class UserPlaying implements Serializable {

  private String room_id;
  private String game_id;
  private String emoji;
  private String title;

  public UserPlaying(String room_id, String game_id) {
    this.room_id = room_id;
    this.game_id = game_id;
  }

  public UserPlaying() {

  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public void setEmoji(String emoji) {
    this.emoji = emoji;
  }

  public String getEmoji() {
    return emoji;
  }

  public String getRoom_id() {
    return room_id;
  }

  public void setRoom_id(String room_id) {
    this.room_id = room_id;
  }

  public String getGame_id() {
    return game_id;
  }

  public void setGame_id(String game_id) {
    this.game_id = game_id;
  }
}
