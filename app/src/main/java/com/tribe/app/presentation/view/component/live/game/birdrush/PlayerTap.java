package com.tribe.app.presentation.view.component.live.game.birdrush;

/**
 * Created by madaaflak on 19/01/2018.
 */

public class PlayerTap {
  public static final String Y = "y";
  public static final String Y_RATIO = "yRatio";

  private Double y;
  private Double yRatio;

  public PlayerTap(Double y, Double yRatio) {
    this.y = y;
    this.yRatio = yRatio;
  }

  public Double getyRatio() {
    return yRatio;
  }

  public void setyRatio(Double yRatio) {
    this.yRatio = yRatio;
  }

  public Double getY() {
    return y;
  }

  public void setY(double y) {
    this.y = y;
  }

  @Override public String toString() {
    return "PlayerTap{" + ", y=" + y + '}';
  }
}
