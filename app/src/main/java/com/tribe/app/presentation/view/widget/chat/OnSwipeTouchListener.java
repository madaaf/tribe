package com.tribe.app.presentation.view.widget.chat;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import rx.Observable;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Created by madaaflak on 11/10/2017.
 */

public class OnSwipeTouchListener implements View.OnTouchListener {

  private final GestureDetector gestureDetector;

  // OBSERVABLES
  private PublishSubject<Void> onSwipeUp = PublishSubject.create();

  public OnSwipeTouchListener(Context ctx) {
    gestureDetector = new GestureDetector(ctx, new GestureListener());
  }

  @Override public boolean onTouch(View v, MotionEvent event) {
    gestureDetector.onTouchEvent(event);
    return true;
  }

  private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    @Override public boolean onDown(MotionEvent e) {
      return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      boolean result = false;
      try {
        float diffY = e2.getY() - e1.getY();
        float diffX = e2.getX() - e1.getX();
        if (Math.abs(diffX) > Math.abs(diffY)) {
          if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
            if (diffX > 0) {
              swipeRight();
            } else {
              swipeLeft();
            }
            result = true;
          }
        } else if (Math.abs(diffY) > SWIPE_THRESHOLD &&
            Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
          if (diffY > 0) {
            swipeBottom();
          } else {
            swipeUp();
          }
          result = true;
        }
      } catch (Exception exception) {
        exception.printStackTrace();
      }
      return result;
    }
  }

  public void swipeRight() {
    Timber.d("onSwipeRight");
  }

  public void swipeLeft() {
    Timber.d("onSwipeLeft");
  }

  public void swipeUp() {
    onSwipeUp.onNext(null);
  }

  public void swipeBottom() {
    Timber.d("onSwipeBottom");
  }

  // OBSERVABLES

  public Observable<Void> onSwipeUp() {
    return onSwipeUp;
  }
}

