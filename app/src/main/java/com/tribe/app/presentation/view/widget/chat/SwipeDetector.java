package com.tribe.app.presentation.view.widget.chat;

import android.view.MotionEvent;
import android.view.View;
import timber.log.Timber;

/**
 * Created by madaaflak on 11/10/2017.
 */

public class SwipeDetector implements View.OnTouchListener {

  private ChatView activity;
  static final int MIN_DISTANCE = 100;
  private float downX, downY, upX, upY;

  public SwipeDetector(ChatView activity) {
    this.activity = activity;
  }

  public void onRightToLeftSwipe(View v) {
    activity.right2left(v);
  }

  public void onLeftToRightSwipe(View v) {
    activity.left2right(v);
  }

  public void onTopToBottomSwipe(View v) {
    activity.top2bottom(v);
  }

  public void onBottomToTopSwipe(View v) {
    activity.bottom2top(v);
  }

  public void onActionUp(View v) {
    activity.onActionUp(v);
  }

  public void onActionDown(View v) {
    activity.onActionDown(v);
  }

  public boolean onTouch(View v, MotionEvent event) {
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN: {

        this.onActionDown(v);
        downX = event.getX();
        downY = event.getY();
        Timber.e("onActionDown");
        return true;
      }
      case MotionEvent.ACTION_UP: {
        this.onActionUp(v);
        Timber.e("onActionUp");
        upX = event.getX();
        upY = event.getY();

        float deltaX = downX - upX;
        float deltaY = downY - upY;

        // swipe horizontal?
        if (Math.abs(deltaX) > MIN_DISTANCE) {
          // left or right
          if (deltaX < 0) {
            this.onLeftToRightSwipe(v);
            return true;
          }
          if (deltaX > 0) {
            this.onRightToLeftSwipe(v);
            return true;
          }
        } else {
          Timber.i("Swipe was only " + Math.abs(deltaX) + " long, need at least " + MIN_DISTANCE);
        }

        // swipe vertical?
        if (Math.abs(deltaY) > MIN_DISTANCE) {
          // top or down
          if (deltaY < 0) {
            this.onTopToBottomSwipe(v);
            return true;
          }
          if (deltaY > 0) {
            this.onBottomToTopSwipe(v);
            return true;
          }
        } else {
          Timber.i("Swipe was only " + Math.abs(deltaX) + " long, need at least " + MIN_DISTANCE);
          v.performClick();
        }
      }
    }
    return false;
  }
}
