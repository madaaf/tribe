package com.tribe.app.domain.entity;

import android.widget.TextView;
import com.tribe.tribelivesdk.game.Game;

/**
 * Created by tiago on 12/07/2017.
 */
public class Score {

  public static final String ID_PROGRESS = "ID_PROGRESS";

  private String id;
  private int ranking = 0;
  private int value = 0;
  private User user;
  private Game game;
  private Long countDownTimer;
  private TextView textView;
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

  @Override public String toString() {
    return super.toString();
  }

  public boolean isWaiting() {
    return isWaiting;
  }

  public void setWaiting(boolean waiting) {
    isWaiting = waiting;
  }

  public Long getCountDownTimer() {
    return countDownTimer;
  }

  public void setCountDownTimer(Long countDownTimer) {
    this.countDownTimer = countDownTimer;
  }
}
