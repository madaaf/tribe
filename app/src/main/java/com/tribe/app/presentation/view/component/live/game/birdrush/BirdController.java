package com.tribe.app.presentation.view.component.live.game.birdrush;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.io.Serializable;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by madaaflak on 15/01/2018.
 */

public class BirdController extends View implements TouchHandler {

  private PublishSubject<Void> onTap = PublishSubject.create();

  public BirdController(Context context) {
    super(context);
  }

  public BirdController(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  @Override public boolean onTouch(View v, MotionEvent event) {
    Point p = null;
    p = new Point(event.getX(), event.getY());

    switch (event.getAction()) {

      case MotionEvent.ACTION_DOWN:
        onTap.onNext(null);
        break;

      case MotionEvent.ACTION_MOVE:
        break;
      case MotionEvent.ACTION_UP:
        break;
      default:
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
