package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by madaaflak on 09/08/2017.
 */

public class GameDrawViewPager extends ViewPager {
  public GameDrawViewPager(Context context) {
    super(context);
  }

  public GameDrawViewPager(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override public boolean onInterceptTouchEvent(MotionEvent event) {
    // Never allow swiping to switch between pages
    return false;
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    // Never allow swiping to switch between pages
    return false;
  }
}
