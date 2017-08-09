package com.tribe.app.presentation.view.widget.game;

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
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.TrackablePath;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.subjects.PublishSubject;

public class DrawerView extends View {

  private Paint paint;
  private TrackablePath path, tmpPath;
  private List<TrackablePath> paths;
  private ImageView hand;
  private TextViewFont clearBtn;
  private OnDrawerListener onDrawerListener;
  private int maxDataSetSize, counter;
  private int width, height;

  private PublishSubject<Void> onClearDraw = PublishSubject.create();

  public DrawerView(Context context, ImageView hand, TextViewFont clearBtn) {
    super(context);
    this.hand = hand;
    this.clearBtn = clearBtn;
    post(new Runnable() {
      @Override public void run() {
        width = getWidth();
        height = getHeight();
      }
    });

    paint = new Paint();
    paths = new ArrayList<>();

    tmpPath = new TrackablePath();
    path = new TrackablePath();
    paths.add(path);
    counter = 0;

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

  public void setOnDrawerListener(OnDrawerListener onDrawerListener, int maxDataSetSize) {
    this.onDrawerListener = onDrawerListener;
    this.maxDataSetSize = maxDataSetSize;
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
        tmpPath.moveTo(eventX / width, eventY / height);
        return true;
      case MotionEvent.ACTION_MOVE:
        path.lineTo(eventX, eventY);
        tmpPath.lineTo(eventX / width, eventY / height);

        if (onDrawerListener != null) {
          counter++;
          if (counter == maxDataSetSize) {
            onDrawerListener.onTrackablePath(tmpPath);
            tmpPath = new TrackablePath();
            counter = 0;
            tmpPath.lineTo(eventX / width, eventY / height);
          }
        }
        break;
      case MotionEvent.ACTION_UP:
        if (onDrawerListener != null) {
          onDrawerListener.onTrackablePath(tmpPath);
          tmpPath = new TrackablePath();
          counter = 0;
        }

        path = new TrackablePath();
        paths.add(path);
        break;
      default:
        return false;
    }

    invalidate();
    return true;
  }

  public Observable<Void> onClearDraw() {
    return onClearDraw;
  }

  public interface OnDrawerListener {
    void onTrackablePath(TrackablePath path);
  }
}