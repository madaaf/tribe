package com.tribe.app.presentation.view.widget.game;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by madaaflak on 09/08/2017.
 */

public class GameViewPager extends ViewPager {

  public GameViewPager(Context context) {
    super(context);
  }

  public GameViewPager(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override public boolean onInterceptTouchEvent(MotionEvent event) {
    return false;
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    return false;
  }
}
