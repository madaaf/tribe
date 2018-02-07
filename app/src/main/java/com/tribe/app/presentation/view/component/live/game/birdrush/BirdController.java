package com.tribe.app.presentation.view.component.live.game.birdrush;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by madaaflak on 15/01/2018.
 */

public class BirdController extends View implements TouchHandler {

  private PublishSubject<Boolean> onTap = PublishSubject.create();

  public BirdController(Context context) {
    super(context);
  }

  public BirdController(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  @Override public boolean onTouch(View v, MotionEvent event) {

    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        onTap.onNext(true);
        break;

      case MotionEvent.ACTION_MOVE:
        break;
      case MotionEvent.ACTION_UP:
        onTap.onNext(false);
        break;
      default:
    }
    return false;
  }

  public Observable<Boolean> onTap() {
    return onTap;
  }
}
