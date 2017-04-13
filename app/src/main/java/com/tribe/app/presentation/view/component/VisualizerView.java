package com.tribe.app.presentation.view.component;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.CircleView;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 04/11/17.
 */
public class VisualizerView extends FrameLayout {

  private static final int DURATION = 150;
  private static final int RADIUS_MIN = 50;
  private static final int RADIUS_MAX = 100;

  @Inject ScreenUtils screenUtils;

  // VARIABLES
  private int radiusMin, radiusMax;
  private Rect rect = new Rect();
  private Paint paint = new Paint();
  private CircleView viewCircle;
  private FrameLayout.LayoutParams paramsCircle;
  private int size;
  private ValueAnimator vaCircle;
  private boolean isActive = false;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public VisualizerView(Context context) {
    super(context);
    init();
  }

  public VisualizerView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public VisualizerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    subscriptions.add(Observable.interval(0, DURATION, TimeUnit.MILLISECONDS)
        .onBackpressureDrop()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          if (isActive) {
            int radius = randomRadius(radiusMin, radiusMax);
            animateToRadius(radius);
          }
        }));
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    subscriptions.clear();
  }

  @Override protected void dispatchDraw(Canvas canvas) {
    super.dispatchDraw(canvas);

    if (vaCircle == null || viewCircle.getRect() == null) {
      rect.set(0, 0, getMeasuredWidth(), getMeasuredHeight());

      viewCircle.setRect(rect);
      viewCircle.setPaint(paint);
      viewCircle.setRadius(0);
    }
  }

  private void init() {
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);
    initResources();

    paint.setStrokeWidth(screenUtils.dpToPx(1f));
    paint.setAntiAlias(true);
    paint.setColor(ContextCompat.getColor(getContext(), R.color.black_opacity_15));

    size = screenUtils.getWidthPx();

    paramsCircle = new FrameLayout.LayoutParams(size, size);
    paramsCircle.gravity = Gravity.CENTER;

    viewCircle = new CircleView(getContext());

    addView(viewCircle, paramsCircle);

    viewCircle.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.scale_sound_small));
  }

  private void initResources() {
    radiusMin = screenUtils.dpToPx(RADIUS_MIN);
    radiusMax = screenUtils.dpToPx(RADIUS_MAX);
  }

  private void animateToRadius(float radius) {
    if (vaCircle != null) vaCircle.cancel();

    vaCircle = ValueAnimator.ofFloat(viewCircle.getRadius(), radius);
    vaCircle.setDuration(DURATION);
    vaCircle.setInterpolator(new DecelerateInterpolator());
    vaCircle.setStartDelay(0);
    vaCircle.addUpdateListener(animation -> {
      Float value = (Float) animation.getAnimatedValue();
      viewCircle.setRadius(value);
    });

    vaCircle.start();
  }

  private static int randomRadius(int x, int y) {
    Random rand = new Random();
    return rand.nextInt(y - x) + x;
  }

  public void show() {
    isActive = true;
  }

  public void hide(boolean animate) {
    isActive = false;
    if (animate) {
      animateToRadius(0);
    } else if (viewCircle != null) viewCircle.setRadius(0);
  }

  public void release() {
    if (viewCircle != null) viewCircle.clearAnimation();
    if (vaCircle != null) {
      vaCircle.removeAllUpdateListeners();
      vaCircle.cancel();
    }
  }
}
