package com.tribe.app.presentation.view.utils;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import rx.Observable;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Created by madaaflak on 30/07/2017.
 */

public class MoveViewTouchListener implements View.OnTouchListener {
  private GestureDetector mGestureDetector;
  private View mView;

  public PublishSubject<Boolean> onBlockOpenInviteView = PublishSubject.create();

  public MoveViewTouchListener(View view) {
    mGestureDetector = new GestureDetector(view.getContext(), mGestureListener);
    mView = view;
  }

  @Override public boolean onTouch(View v, MotionEvent event) {
    if (event.getAction() == MotionEvent.ACTION_UP) {
      Timber.e("SOEF ON ACTION UP");
      v.animate()
          .translationX(0)
          .translationY(0)
          .setDuration(300)
          .setInterpolator(new OvershootInterpolator(1.3f))
          .start();
      onBlockOpenInviteView.onNext(false);
    }
    return mGestureDetector.onTouchEvent(event);
  }

  private GestureDetector.OnGestureListener mGestureListener =
      new GestureDetector.SimpleOnGestureListener() {
        private float mMotionDownX, mMotionDownY;

        @Override public boolean onDown(MotionEvent e) {
          mMotionDownX = e.getRawX() - mView.getTranslationX();
          mMotionDownY = e.getRawY() - mView.getTranslationY();
          Timber.d("SOEF MOVE " + mMotionDownX + " " + mMotionDownY);
          onBlockOpenInviteView.onNext(true);
          return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
          mView.setTranslationX(e2.getRawX() - mMotionDownX);
          mView.setTranslationY(e2.getRawY() - mMotionDownY);
          Timber.d("SOEF ON SCROLL " + (e2.getRawX() - mMotionDownX) + " " + (e2.getRawY()
              - mMotionDownY));
          return true;
        }
      };

  public Observable<Boolean> onBlockOpenInviteView() {
    return onBlockOpenInviteView;
  }
}