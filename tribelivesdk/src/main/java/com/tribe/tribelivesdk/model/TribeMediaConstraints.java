package com.tribe.tribelivesdk.model;

/**
 * Created by tiago on 02/20/2017.
 */

public class TribeMediaConstraints {

  private int maxWidth, maxHeight, minFps, maxFps;
  private boolean shouldCreateOffer = false;

  public TribeMediaConstraints() {
  }

  public int getMaxHeight() {
    return maxHeight;
  }

  public void setMaxHeight(int maxHeight) {
    this.maxHeight = maxHeight;
  }

  public int getMaxWidth() {
    return maxWidth;
  }

  public void setMaxWidth(int maxWidth) {
    this.maxWidth = maxWidth;
  }

  public int getMaxFps() {
    return maxFps;
  }

  public void setMaxFps(int maxFps) {
    this.maxFps = maxFps;
  }

  public int getMinFps() {
    return minFps;
  }

  public void setMinFps(int minFps) {
    this.minFps = minFps;
  }

  public boolean isShouldCreateOffer() {
    return shouldCreateOffer;
  }

  public void setShouldCreateOffer(boolean shouldCreateOffer) {
    this.shouldCreateOffer = shouldCreateOffer;
  }

  @Override public String toString() {
    StringBuilder stringBuilder = new StringBuilder();

    stringBuilder.append("***** Media Constraints *****\n");
    stringBuilder.append("maxWidth = " + maxWidth + "\n");
    stringBuilder.append("maxHeight = " + maxHeight + "\n");
    stringBuilder.append("minFps = " + minFps + "\n");
    stringBuilder.append("maxFps = " + maxFps + "\n");
    stringBuilder.append("*******************************\n");

    return stringBuilder.toString();
  }
}
