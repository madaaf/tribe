package com.tribe.app.presentation.view.component.live.game.birdrush;

/**
 * Created by madaaflak on 19/01/2018.
 */

public class PlayerTap {

  private double x;
  private double y;

  public PlayerTap(double x, double y) {
    this.x = x;
    this.y = y;
  }

  public double getX() {
    return x;
  }

  public void setX(double x) {
    this.x = x;
  }

  public double getY() {
    return y;
  }

  public void setY(double y) {
    this.y = y;
  }

  @Override public String toString() {
    return "PlayerTap{" + "x=" + x + ", y=" + y + '}';
  }
}
