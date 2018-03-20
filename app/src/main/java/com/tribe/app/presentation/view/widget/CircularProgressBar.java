package com.tribe.app.presentation.view.widget;

import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class CircularProgressBar extends View {

  private int viewWidth;
  private int viewHeight;

  private final float startAngle = -90;
  private float sweepAngle = 0;
  private float maxSweepAngle = 360;
  private int strokeWidth = 20;
  private int animationDuration = 400;
  private int maxProgress = 100;
  private boolean roundedCorners = true;
  private int progressColor = Color.BLACK;
  private ValueAnimator animator = null;
  private int progress = 0;

  private final Paint paint;

  public CircularProgressBar(Context context) {
    this(context, null);
  }

  public CircularProgressBar(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public CircularProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    paint = new Paint(Paint.ANTI_ALIAS_FLAG);
  }

  @Override protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    initMeasurements();
    drawOutlineArc(canvas);
  }

  private void initMeasurements() {
    viewWidth = getWidth();
    viewHeight = getHeight();
  }

  private void drawOutlineArc(Canvas canvas) {
    final int diameter = Math.min(viewWidth, viewHeight) - (strokeWidth >> 1);

    final RectF outerOval = new RectF(strokeWidth >> 1, strokeWidth >> 1, diameter, diameter);

    Bitmap bitmap =
        Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
    canvas.drawBitmap(bitmap, 0, 0, paint);

    paint.setColor(progressColor);
    paint.setStrokeWidth(strokeWidth);
    paint.setAntiAlias(true);
    //if (sweepAngle < 357) {
    paint.setStrokeCap(roundedCorners ? Paint.Cap.ROUND : Paint.Cap.BUTT);
    paint.setStrokeJoin(roundedCorners ? Paint.Join.ROUND : Paint.Join.MITER);
    //} else {
    //  paint.setStrokeCap(Paint.Cap.BUTT);
    //  paint.setStrokeJoin(Paint.Join.MITER);
    //}
    paint.setStyle(Paint.Style.STROKE);
    canvas.drawArc(outerOval, startAngle, sweepAngle, false, paint);
  }

  private float calcSweepAngleFromProgress(int progress) {
    return (maxSweepAngle / maxProgress) * progress;
  }

  public void setProgress(int progress) {
    this.progress = progress;
    sweepAngle = calcSweepAngleFromProgress(progress);
    invalidate();
  }

  public int getProgress() {
    return progress;
  }

  /**
   * Set progress of the circular progress bar.
   *
   * @param progress progress between 0 and 100.
   */
  public void setProgress(int progress, int duration, int delay,
      AnimatorListenerAdapter listenerAdapter,
      ValueAnimator.AnimatorUpdateListener animatorUpdateListener) {
    animator = ValueAnimator.ofFloat(sweepAngle, calcSweepAngleFromProgress(progress));
    animator.setInterpolator(new DecelerateInterpolator());
    animator.setDuration(duration);
    animator.setStartDelay(delay);
    animator.addUpdateListener(valueAnimator -> {
      if (animatorUpdateListener != null) animatorUpdateListener.onAnimationUpdate(valueAnimator);
      sweepAngle = (float) valueAnimator.getAnimatedValue();
      invalidate();
    });
    if (listenerAdapter != null) animator.addListener(listenerAdapter);
    animator.start();
  }

  public void setProgressColor(int color) {
    progressColor = color;
    invalidate();
  }

  public void setAnimationDuration(int animationDuration) {
    this.animationDuration = animationDuration;
  }

  public void setProgressWidth(int width) {
    strokeWidth = width;
    invalidate();
  }

  public void stop() {
    if (animator == null) return;
    animator.cancel();
    animator = null;
  }

  /**
   * Toggle this if you don't want rounded corners on progress bar.
   * Default is true.
   *
   * @param roundedCorners true if you want rounded corners of false otherwise.
   */
  public void useRoundedCorners(boolean roundedCorners) {
    this.roundedCorners = roundedCorners;
    invalidate();
  }
}