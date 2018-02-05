package com.tribe.app.presentation.view.component.live.game.birdrush;

/**
 * Created by madaaflak on 19/01/2018.
 */

public class PlayerTap {
  public static final String Y = "y";

  private double y;

  public PlayerTap(double y) {
    this.y = y;
  }

  public double getY() {
    return y;
  }

  public void setY(double y) {
    this.y = y;
  }

  @Override public String toString() {
    return "PlayerTap{" + ", y=" + y + '}';
  }
}
