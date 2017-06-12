package com.tribe.app.presentation.view.interpolator;

import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.animation.Interpolator;

public class BezInterpolator {

  private static BezInterpolator bezInterpolator;

  //Control points to create the cubic bezier curve.
  private float[] PRINCIPLE_DEFAULT_EASE = { (float) 0.25, (float) 0.1, (float) 0.25, (float) 1.0 };
  private float[] EASE_OUT = { (float) 0, (float) 0, (float) 0.58, (float) 1.0 };
  private float[] EASE_IN = { (float) 0.42, (float) 0, (float) 1.0, (float) 1.0 };

  public static BezInterpolator getInstance() {
    if (bezInterpolator == null) {
      bezInterpolator = new BezInterpolator();
    }
    return bezInterpolator;
  }

  public Interpolator getDefaultInterpolator() {
    return PathInterpolatorCompat.create(PRINCIPLE_DEFAULT_EASE[0], PRINCIPLE_DEFAULT_EASE[1],
        PRINCIPLE_DEFAULT_EASE[2], PRINCIPLE_DEFAULT_EASE[3]);
  }

  public Interpolator getEaseOutInterpolator() {
    return PathInterpolatorCompat.create(EASE_OUT[0], EASE_OUT[1], EASE_OUT[2], EASE_OUT[3]);
  }

  public Interpolator getEaseInInterpolator() {
    return PathInterpolatorCompat.create(EASE_IN[0], EASE_IN[1], EASE_IN[2], EASE_IN[3]);
  }
}