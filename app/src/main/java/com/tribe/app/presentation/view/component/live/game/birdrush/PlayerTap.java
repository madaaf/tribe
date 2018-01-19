package com.tribe.app.presentation.view.component.live.game.birdrush;

/**
 * Created by madaaflak on 19/01/2018.
 */

public class PlayerTap {

  private Double x;
  private Double y;

  public PlayerTap(Double x, Double y) {
    this.x = x;
    this.y = y;
  }

  public Double getX() {
    return x;
  }

  public void setX(Double x) {
    this.x = x;
  }

  public Double getY() {
    return y;
  }

  public void setY(Double y) {
    this.y = y;
  }

  @Override public String toString() {
    return "PlayerTap{" + "x=" + x + ", y=" + y + '}';
  }
}
