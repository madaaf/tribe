package com.tribe.app.presentation.view.component.live.game.birdrush;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.io.Serializable;
import java.util.List;
import rx.Observable;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Created by madaaflak on 15/01/2018.
 */

public class BirdController extends View implements TouchHandler {

  private PublishSubject<Void> onTap = PublishSubject.create();
  private PublishSubject<Void> ok = PublishSubject.create();

  public BirdController(Context context) {
    super(context);
  }

  public BirdController(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  @Override public boolean isTouchDown(int pointer) {
    return false;
  }

  @Override public int getTouchX(int pointer) {
    return 0;
  }

  @Override public int getTouchY(int pointer) {
    return 0;
  }

  @Override public List<Integer> getTouchEvents() {
    return null;
  }

  @Override public boolean onTouch(View v, MotionEvent event) {
    Point p = null;
    p = new Point(event.getX(), event.getY());

    switch (event.getAction()) {

      case MotionEvent.ACTION_DOWN:
        Timber.e("SOEF TOUCH 1 " + p.toString());
        onTap.onNext(null);
        break;

      case MotionEvent.ACTION_MOVE:
        Timber.e("SOEF TOUCH 2 " + p.toString());
        break;
      case MotionEvent.ACTION_UP:
        Timber.e("SOEF TOUCH 3 " + p.toString());
        break;
      default:
        Timber.e("SOEF TOUCH 3 " + p.toString() + " " + event.getAction());
    }
    return false;
  }

  public static class Point implements Serializable {
    public float x;
    public float y;

    public Point(float x, float y) {
      this.x = x;
      this.y = y;
    }

    @Override public String toString() {
      return "Point{" + "x=" + x + ", y=" + y + '}';
    }
  }

  public Observable<Void> onTap() {
    return onTap;
  }

}
