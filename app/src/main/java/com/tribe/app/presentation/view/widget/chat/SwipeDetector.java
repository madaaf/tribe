package com.tribe.app.presentation.view.widget.chat;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import timber.log.Timber;

/**
 * Created by madaaflak on 11/10/2017.
 */

public class SwipeDetector implements View.OnTouchListener {

  private static final int SWIPE_MIN_DISTANCE = 1;

  private GestureDetector mGestureDetector;
  private View mView;
  private View recordingView;
  private ChatView context;
  private float initialPosition, initPos, ratio;
  private ScreenUtils screenUtils;

  public SwipeDetector(ChatView context, View view, View recordingView, float initPos,
      ScreenUtils screenUtils) {
    mGestureDetector = new GestureDetector(view.getContext(), mGestureListener);
    this.mView = view;
    this.context = context;
    this.initPos = initPos;
    this.recordingView = recordingView;
    this.initialPosition = view.getX();
    this.screenUtils = screenUtils;
  }

  @Override public boolean onTouch(View v, MotionEvent event) {
    if (event.getAction() == MotionEvent.ACTION_UP) {
      context.onActionUp(mView, getRatio());
    } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
      context.onActionDown(mView);
    }
    return mGestureDetector.onTouchEvent(event);
  }

  private float getRatio() {
    float dist = initialPosition - initPos;
    float r = mView.getX() - initPos;
    return (r / dist) + 1;
  }

  private GestureDetector.OnGestureListener mGestureListener =
      new GestureDetector.SimpleOnGestureListener() {
        private float mMotionDownX, mMotionDownY;

        @Override public boolean onDown(MotionEvent e) {
          mMotionDownX = e.getRawX() - mView.getTranslationX();
          mMotionDownY = e.getRawY() - mView.getTranslationY();

          return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
          float x = recordingView.getX() + (recordingView.getWidth() / 2);
          float x2 = mView.getX() + (mView.getWidth() / 2);
          ratio = getRatio();

          try {

            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && mView.getX() > initPos) {
              Timber.e("SOEF 1");
              mView.setTranslationX(e2.getRawX() - mMotionDownX);
            } else if (mView.getX() < initPos) { // TRASH POSTITION
              Timber.e("SOEF 2");
              mView.setX(initPos);
            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                && mView.getX() < initialPosition) {
              Timber.e("SOEF 3");
              mView.setTranslationX(e2.getRawX() - mMotionDownX);
            } else if (mView.getX() > initialPosition) {
              Timber.e("SOEF 4");
              mView.setX(initialPosition);
            }

            if (x2 < (screenUtils.getWidthPx() / 2) || x2 > initialPosition) {
              recordingView.setX(screenUtils.getWidthPx() / 2 - (recordingView.getWidth() / 2));
            } else {
              recordingView.setTranslationX(
                  e2.getRawX() - mMotionDownX - (screenUtils.getWidthPx() / 2));
            }

            context.right2left(mView, ratio);
          } catch (Exception e) {
            // nothing
          }
          return true;
        }
      };
}