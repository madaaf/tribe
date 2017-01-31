package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

public class DiagonalLayout extends FrameLayout {

  private int height = 0;
  private int width = 0;
  private float angle = 5;

  private Path path;
  private Paint paint;

  public DiagonalLayout(Context context) {
    super(context);
    init(context, null);
  }

  public DiagonalLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public void init(Context context, AttributeSet attrs) {
    path = new Path();
    paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    paint.setColor(Color.BLUE);

    getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override public void onGlobalLayout() {
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
        height = getMeasuredHeight();
        width = getMeasuredWidth();
        calculateLayout();
      }
    });

    setWillNotDraw(false);
  }

  private void calculateLayout() {
    if (width > 0 && height > 0) {
      float perpendicularHeight = (float) (width * Math.tan(Math.toRadians(angle)));
      path.moveTo(0, 0);
      path.lineTo(width, 0);
      path.lineTo(width, height - perpendicularHeight);
      path.lineTo(0, height);
      path.lineTo(0, 0);
    }
  }

  @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    if (changed) {
      calculateLayout();
    }
  }

  @Override protected void onDraw(Canvas canvas) {
    canvas.clipPath(path);
    super.onDraw(canvas);
  }

  public void setAngle(float angle) {
    path.reset();
    this.angle = angle;
    invalidate();
  }
}