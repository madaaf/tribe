package com.tribe.app.presentation.view.utils;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * Created by madaaflak on 30/07/2017.
 */
public class ViewPagerScroller extends Scroller {

  private int mScrollDuration = 1000;

  public ViewPagerScroller(Context context) {
    super(context);
  }

  public ViewPagerScroller(Context context, Interpolator interpolator, int mScrollDuration) {
    super(context, interpolator);
    this.mScrollDuration = mScrollDuration;
  }

  @Override public void startScroll(int startX, int startY, int dx, int dy, int duration) {
    super.startScroll(startX, startY, dx, dy, mScrollDuration);
  }

  @Override public void startScroll(int startX, int startY, int dx, int dy) {
    super.startScroll(startX, startY, dx, dy, mScrollDuration);
  }
}
