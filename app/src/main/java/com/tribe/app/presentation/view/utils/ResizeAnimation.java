package com.tribe.app.presentation.view.utils;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by madaaflak on 22/06/2017.
 */

public class ResizeAnimation extends Animation {

  private int startHeight;
  private int deltaHeight;
  private int startWidth;
  private int deltaWidth;
  private View view;

  public ResizeAnimation(View v) {
    this.view = v;
  }

  @Override protected void applyTransformation(float interpolatedTime, Transformation t) {

    view.getLayoutParams().height = (int) (startHeight + deltaHeight * interpolatedTime);
    view.getLayoutParams().width = (int) (startWidth + deltaWidth * interpolatedTime);
    view.requestLayout();
  }

  public void setParams(int startWidth, int endWidth) {
    this.startWidth = startWidth;
    deltaWidth = endWidth - startWidth;
  }

  public void setParams(int startWidth, int endWidth, int startHeight, int endHeight) {
    this.startHeight = startHeight;
    deltaHeight = endHeight - startHeight;

    this.startWidth = startWidth;
    deltaWidth = endWidth - startWidth;
  }

  /**
   * set the duration for the hideshowanimation
   */
  @Override public void setDuration(long durationMillis) {
    super.setDuration(durationMillis);
  }

  @Override public boolean willChangeBounds() {
    return true;
  }
}
