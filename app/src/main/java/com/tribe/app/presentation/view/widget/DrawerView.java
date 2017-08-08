package com.tribe.app.presentation.view.widget;

/**
 * Created by madaaflak on 08/08/2017.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import com.tribe.app.R;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.subjects.PublishSubject;

public class DrawerView extends View {

  private PublishSubject<Void> onClearDraw = PublishSubject.create();

  private Paint paint;
  private TrackablePath path;
  private List<TrackablePath> paths;
  private ImageView hand;
  private TextViewFont clearBtn;
  private OnDrawerListener onDrawerListener;

  public DrawerView(Context context, ImageView hand, TextViewFont clearBtn) {
    super(context);
    this.hand = hand;
    this.clearBtn = clearBtn;
    paint = new Paint();
    paths = new ArrayList<>();

    newPath();

    paint.setAntiAlias(true);
    paint.setColor(ContextCompat.getColor(context, R.color.yellow_draw));
    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeJoin(Paint.Join.ROUND);
    paint.setStrokeCap(Paint.Cap.ROUND);
    paint.setStrokeWidth(12);

    clearBtn.setOnClickListener(v -> {
      clear();
      onClearDraw.onNext(null);
    });
  }

  public void clear() {
    paths = new ArrayList<>();
    path = new TrackablePath();
    invalidate();
  }

  public void draw(TrackablePath path) {
    paths.add(path);
    invalidate();
  }

  public void setOnDrawerListener(OnDrawerListener onDrawerListener) {
    this.onDrawerListener = onDrawerListener;
  }

  private void callListener() {
    if (onDrawerListener != null) {
      onDrawerListener.onTrackablePath(path);
    }
  }

  private void newPath() {
    path = new TrackablePath();
    paths.add(path);
  }

  @Override protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    for (TrackablePath path : paths) {
      canvas.drawPath(path, paint);
    }
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    float eventX = event.getX();
    float eventY = event.getY();

    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        hand.setVisibility(GONE);
        path.moveTo(eventX, eventY);
        return true;
      case MotionEvent.ACTION_MOVE:
        path.lineTo(eventX, eventY);
        break;
      case MotionEvent.ACTION_UP:
        callListener();
        newPath();
        break;
      default:
        return false;
    }

    invalidate();
    return true;
  }

  public interface OnDrawerListener {
    void onTrackablePath(TrackablePath path);
  }

  public Observable<Void> onClearDraw() {
    return onClearDraw;
  }
}