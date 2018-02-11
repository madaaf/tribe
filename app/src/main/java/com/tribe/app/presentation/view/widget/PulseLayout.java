package com.tribe.app.presentation.view.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;
import com.tribe.app.R;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.repeatCount;

/**
 * Created by tiago on 01/06/2017
 * Taken from https://github.com/booncol/Pulsator4Droid/
 */
public class PulseLayout extends RelativeLayout {

  private static final int DEFAULT_COUNT = 2;
  private static final int DEFAULT_COLOR = Color.BLACK;
  private static final int DEFAULT_DURATION = 1000;

  private int count;
  private int duration;
  private int color;

  private final List<View> views = new ArrayList<>();
  private AnimatorSet animatorSet;
  private Paint paint;
  private float radius;
  private float centerX;
  private float centerY;
  private boolean started;
  private boolean startFromScratch;

  /**
   * Simple constructor to use when creating a view from code.
   *
   * @param context The Context the view is running in, through which it can access the current
   * theme, resources, etc.
   */
  public PulseLayout(Context context) {
    this(context, null, 0);
  }

  /**
   * Constructor that is called when inflating a view from XML.
   *
   * @param context The Context the view is running in, through which it can access the current
   * theme, resources, etc.
   * @param attrs The attributes of the XML tag that is inflating the view.
   */
  public PulseLayout(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  /**
   * Perform inflation from XML and apply a class-specific base style from a theme attribute.
   *
   * @param context The Context the view is running in, through which it can access the current
   * theme, resources, etc.
   * @param attrs The attributes of the XML tag that is inflating the view.
   * @param defStyleAttr An attribute in the current theme that contains a reference to a style
   * resource that supplies default values for the view. Can be 0 to not look
   * for defaults.
   */
  public PulseLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    TypedArray attr =
        context.getTheme().obtainStyledAttributes(attrs, R.styleable.PulseLayout, 0, 0);

    count = DEFAULT_COUNT;
    duration = DEFAULT_DURATION;
    color = DEFAULT_COLOR;

    try {
      count = attr.getInteger(R.styleable.PulseLayout_pulseCount, DEFAULT_COUNT);
      duration = attr.getInteger(R.styleable.PulseLayout_pulseDuration, DEFAULT_DURATION);
      color = attr.getColor(R.styleable.PulseLayout_pulseColor, DEFAULT_COLOR);
    } finally {
      attr.recycle();
    }

    paint = new Paint();
    paint.setAntiAlias(true);
    paint.setStyle(Paint.Style.FILL);
    paint.setColor(color);

    build();
  }

  /**
   * Start pulse animation.
   */
  public synchronized void start() {
    if (animatorSet == null || started) {
      return;
    }

    if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
      animatorSet.start();
    }
  }

  /**
   * Stop pulse animation.
   */
  public synchronized void stop() {
    if (animatorSet == null || !started) {
      return;
    }

    animatorSet.end();
  }

  public synchronized boolean isStarted() {
    return (animatorSet != null && started);
  }

  /**
   * Get number of pulses.
   *
   * @return Number of pulses
   */
  public int getCount() {
    return count;
  }

  /**
   * Get pulse duration.
   *
   * @return Duration of single pulse in milliseconds
   */
  public int getDuration() {
    return duration;
  }

  /**
   * Set number of pulses.
   *
   * @param count Number of pulses
   */
  public void setCount(int count) {
    if (count < 0) {
      throw new IllegalArgumentException("Count cannot be negative");
    }

    if (this.count != count) {
      this.count = count;
      reset();
      invalidate();
    }
  }

  /**
   * Set single pulse duration.
   *
   * @param millis Pulse duration in milliseconds
   */
  public void setDuration(int millis) {
    if (millis < 0) {
      throw new IllegalArgumentException("Duration cannot be negative");
    }

    if (millis != duration) {
      duration = millis;
      reset();
      invalidate();
    }
  }

  /**
   * Gets the current color of the pulse effect in integer
   * Defaults to Color.rgb(0, 116, 193);
   *
   * @return an integer representation of color
   */
  public int getColor() {
    return color;
  }

  /**
   * Sets the current color of the pulse effect in integer
   * Takes effect immediately
   * Usage: Color.parseColor("<hex-value>") or getResources().getColor(R.color.colorAccent)
   *
   * @param color : an integer representation of color
   */
  public void setColor(int color) {
    if (this.color != color) {
      this.color = color;

      if (paint != null) {
        paint.setColor(color);
        reset();
        invalidate();
      }
    }
  }

  @Override public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
    int height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();

    centerX = width * 0.5f;
    centerY = height * 0.5f;
    radius = Math.min(width, height) * 0.5f;

    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }

  /**
   * Remove all views and animators.
   */
  private void clear() {
    stop();

    for (View view : views) {
      removeView(view);
    }

    views.clear();
  }

  /**
   * Build pulse views and animators.
   */
  private void build() {
    LayoutParams layoutParams =
        new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

    List<Animator> animators = new ArrayList<>();

    for (int index = 0; index < count; index++) {
      PulseView pulseView = new PulseView(getContext());
      pulseView.setScaleX(0);
      pulseView.setScaleY(0);
      pulseView.setAlpha(0.3f);

      addView(pulseView, index, layoutParams);
      views.add(pulseView);

      long delay = index * duration / count;

      ObjectAnimator scaleXAnimator =
          ObjectAnimator.ofFloat(pulseView, "scaleX", pulseView.getScaleX(), 1f);
      scaleXAnimator.setRepeatCount(repeatCount);
      scaleXAnimator.setRepeatMode(ObjectAnimator.RESTART);
      scaleXAnimator.setStartDelay(delay);
      animators.add(scaleXAnimator);

      ObjectAnimator scaleYAnimator =
          ObjectAnimator.ofFloat(pulseView, "scaleY", pulseView.getScaleY(), 1f);
      scaleYAnimator.setRepeatCount(repeatCount);
      scaleYAnimator.setRepeatMode(ObjectAnimator.RESTART);
      scaleYAnimator.setStartDelay(delay);
      animators.add(scaleYAnimator);

      ObjectAnimator alphaAnimator =
          ObjectAnimator.ofFloat(pulseView, "alpha", pulseView.getAlpha(), 0f);
      alphaAnimator.setRepeatCount(repeatCount);
      alphaAnimator.setRepeatMode(ObjectAnimator.RESTART);
      alphaAnimator.setStartDelay(delay);
      animators.add(alphaAnimator);
    }

    prepareAnimations(animators);
  }

  private void createAnimationForView(int index, View pulseView, List<Animator> animators) {

  }

  private void prepareAnimations(List<Animator> animators) {
    animatorSet = new AnimatorSet();
    animatorSet.playTogether(animators);
    animatorSet.setInterpolator(new DecelerateInterpolator());
    animatorSet.setDuration(duration);
    animatorSet.addListener(new AnimatorListenerAdapter() {

      @Override public void onAnimationCancel(Animator animation) {
        started = false;
      }

      @Override public void onAnimationEnd(Animator animation) {
        started = false;
      }

      @Override public void onAnimationStart(Animator animation) {
        started = true;
      }
    });
  }

  /**
   * Reset views and animations.
   */
  private void reset() {
    boolean isStarted = isStarted();

    clear();
    build();

    if (isStarted) {
      start();
    }
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();

    if (animatorSet != null) {
      animatorSet.cancel();
      animatorSet = null;
    }
  }

  private class PulseView extends View {

    public PulseView(Context context) {
      super(context);
    }

    @Override protected void onDraw(Canvas canvas) {
      canvas.drawCircle(centerX, centerY, radius, paint);
    }
  }
}