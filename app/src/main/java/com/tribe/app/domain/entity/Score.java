package com.tribe.app.domain.entity;

import android.widget.TextView;
import com.tribe.tribelivesdk.game.Game;

/**
 * Created by tiago on 12/07/2017.
 */
public class Score {

  public static final String ID_PROGRESS = "ID_PROGRESS";

  private String id;
  private String emoticon;
  private int ranking = 0;
  private int value = 0;
  private User user;
  private Game game;
  private TextView textView;
  private boolean isAbove = false;
  private boolean isWaiting = false;


  public Score() {

  }

  public Score(String id) {
    this.id = id;
  }

  public TextView getTextView() {
    return textView;
  }

  public void setTextView(TextView textView) {
    this.textView = textView;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
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

  @Override public int hashCode() {
    int result = super.hashCode();
    if (game == null) return result;
    result = 31 * result + (game.getId() != null ? game.getId().hashCode() : 0);
    return result;
  }

  public void setEmoticon(String emoticon) {
    this.emoticon = emoticon;
  }

  public String getEmoticon() {
    return emoticon;
  }

  public void setAbove(boolean isAbove) {
    this.isAbove = isAbove;
  }

  public boolean isAbove() {
    return isAbove;
  }

  public void setWaiting(boolean isWaiting) {
    this.isWaiting = isWaiting;
  }

  public boolean isWaiting() {
    return isWaiting;
  }

  @Override public String toString() {
    return "Score{"
        + "id='"
        + id
        + '\''
        + ", emoticon='"
        + emoticon
        + '\''
        + ", isWaiting="
        + isWaiting
        + '}';
  }
}
