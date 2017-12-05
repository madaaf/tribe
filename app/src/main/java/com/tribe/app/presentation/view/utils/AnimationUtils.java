package com.tribe.app.presentation.view.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.tribe.app.R;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by tiago on 26/05/2016.
 */
public class AnimationUtils {

  private static final float OVERSHOOT_REPLACE = 1f;
  private static final int DURATION_REPLACE = 300;

  private static double TENSION = 400;
  private static double DAMPER = 10;
  public static final float SCALE_RESET = 1f;
  public static final float SCALE_INVISIBLE = 0f;
  public static final int TRANSLATION_RESET = 0;
  public static final int NO_START_DELAY = 0;
  public static final float ALPHA_FULL = 1;
  public static final float ALPHA_NONE = 0;
  public static final int ANIMATION_DURATION_EXTRA_SHORT = 100;
  public static final int ANIMATION_DURATION_SHORT = 300;
  public static final int ANIMATION_DURATION_MID = 500;

  public static void scaleUp(final View view, long duration, long delay,
      Interpolator interpolator) {
    view.setScaleX(0);
    view.setScaleY(0);
    view.setVisibility(View.INVISIBLE);
    view.animate()
        .scaleX(1)
        .scaleY(1)
        .setInterpolator(interpolator)
        .setDuration(duration)
        .setStartDelay(delay)
        .setListener(new AnimatorListenerAdapter() {
          @Override public void onAnimationStart(Animator animation) {
            view.setVisibility(View.VISIBLE);
          }

          @Override public void onAnimationCancel(Animator animation) {
            view.animate().setListener(null).start();
            view.setScaleX(1);
            view.setScaleY(1);
          }

          @Override public void onAnimationEnd(Animator animation) {
            view.animate().setListener(null).start();
            view.setScaleX(1);
            view.setScaleY(1);
          }
        })
        .start();
  }

  public static void scaleUp(final View view, long duration, Interpolator interpolator) {
    scaleUp(view, duration, 0, interpolator);
  }

  public static void scaleUp(final View view, long duration) {
    scaleUp(view, duration, 0, new LinearInterpolator());
  }

  public static void scaleDown(final View view, long duration, long delay,
      Interpolator interpolator) {
    view.setScaleX(1);
    view.setScaleY(1);
    view.setVisibility(View.VISIBLE);
    view.animate()
        .scaleX(0)
        .scaleY(0)
        .setInterpolator(interpolator)
        .setDuration(duration)
        .setStartDelay(delay)
        .setListener(new AnimatorListenerAdapter() {
          @Override public void onAnimationCancel(Animator animation) {
            view.animate().setListener(null).start();
            view.setScaleX(0);
            view.setScaleY(0);
            view.setVisibility(View.INVISIBLE);
          }

          @Override public void onAnimationEnd(Animator animation) {
            view.animate().setListener(null).start();
            view.setScaleX(0);
            view.setScaleY(0);
            view.setVisibility(View.INVISIBLE);
          }
        })
        .start();
  }

  public static void scaleDown(final View view, long duration, Interpolator interpolator) {
    scaleDown(view, duration, 0, interpolator);
  }

  public static void scaleDown(final View view, long duration) {
    scaleDown(view, duration, 0, new LinearInterpolator());
  }

  public static void fadeOut(View v, long duration) {
    v.animate()
        .alpha(0)
        .setStartDelay(NO_START_DELAY)
        .setInterpolator(new DecelerateInterpolator())
        .setDuration(duration)
        .start();
  }

  public static void fadeOut(View v, long duration, AnimatorListenerAdapter listenerAdapter) {
    v.animate()
        .alpha(0)
        .setStartDelay(NO_START_DELAY)
        .setInterpolator(new DecelerateInterpolator())
        .setDuration(duration)
        .setListener(listenerAdapter)
        .start();
  }

  public static void fadeOutIntermediate(View v, long duration) {
    v.animate()
        .alpha(0.5f)
        .setStartDelay(NO_START_DELAY)
        .setInterpolator(new DecelerateInterpolator())
        .setDuration(duration)
        .start();
  }

  public static void addSpringAnim(View v) {
    SpringSystem springSystem = SpringSystem.create();
    Spring spring = springSystem.createSpring();
    SpringConfig config = new SpringConfig(TENSION, DAMPER);
    spring.setSpringConfig(config);
    spring.addListener(new SimpleSpringListener() {
      @Override public void onSpringUpdate(Spring spring) {
        float value = (float) spring.getCurrentValue();
        v.setScaleX(value);
        v.setScaleY(value);
      }
    });
    spring.setEndValue(1);
  }

  public static void fadeIn(View v, long duration) {
    v.animate()
        .alpha(1)
        .setStartDelay(NO_START_DELAY)
        .setInterpolator(new DecelerateInterpolator())
        .setDuration(duration)
        .start();
  }

  public static void fadeOutAccelerate(View v, long duration) {
    v.animate()
        .alpha(0)
        .setStartDelay(NO_START_DELAY)
        .setInterpolator(new AccelerateInterpolator(2))
        .setDuration(duration)
        .start();
  }

  public static void fadeInAccelerate(View v, long duration) {
    v.animate()
        .alpha(1)
        .setStartDelay(NO_START_DELAY)
        .setInterpolator(new AccelerateInterpolator(2))
        .setDuration(duration)
        .start();
  }

  public static void fadeViewDownOut(View view, Animator.AnimatorListener listener) {
    view.animate()
        .translationY(25)
        .alpha(0)
        .setDuration(DURATION_REPLACE)
        .setListener(listener)
        .start();
  }

  public static void fadeViewUpIn(View view) {
    view.animate().translationY(0).alpha(1).
        setDuration(DURATION_REPLACE).start();
  }

  public static void fadeOutFast(View view) {
    view.animate().setStartDelay(0).alpha(0).setDuration(50).start();
  }

  public static void fadeViewInOut(View view, int distance) {
    view.animate()
        .alpha(0)
        .setDuration(DURATION_REPLACE)
        .translationY(distance)
        .setStartDelay(0)
        .setListener(new AnimatorListenerAdapter() {
          @Override public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            view.animate()
                .alpha(1)
                .setDuration(DURATION_REPLACE)
                .translationY(0)
                .setStartDelay(0)
                .setListener(null)
                .start();
          }
        })
        .start();
  }

  /**
   * @param v1 the view that will be animated out of the screen
   * @param v2 the view coming in to the screen
   */
  public static void replaceView(Context context, View v1, View v2,
      Animator.AnimatorListener listener) {
    int translateOut = context.getResources().getDimensionPixelSize(R.dimen.transition_replace);
    v1.animate()
        .alpha(0)
        .translationY(translateOut)
        .setDuration(DURATION_REPLACE)
        .setInterpolator(new OvershootInterpolator(OVERSHOOT_REPLACE))
        .start();
    v2.animate()
        .alpha(1)
        .translationY(0)
        .setDuration(DURATION_REPLACE)
        .setInterpolator(new OvershootInterpolator(OVERSHOOT_REPLACE))
        .setListener(listener)
        .start();
  }

  public static void scaleIn(View view, int duration) {
    view.animate()
        .scaleY((float) 1.2)
        .scaleX((float) 1.2)
        .setDuration(duration / 2)
        .setListener(new AnimatorListenerAdapter() {
          @Override public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            view.animate().scaleX(1).scaleY(1).setDuration(duration / 2).setListener(null).start();
          }
        })
        .start();
  }

  public static void collapseScale(View view, int duration) {
    ScaleAnimation scaleAnimation =
        new ScaleAnimation(1f, 1f, 1f, 0f, Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0f);
    scaleAnimation.setDuration(duration);
    scaleAnimation.setFillAfter(true);
    view.startAnimation(scaleAnimation);
  }

  public static void expandScale(View view, int duration) {
    ScaleAnimation scaleAnimation =
        new ScaleAnimation(1f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0f);
    scaleAnimation.setDuration(duration);
    scaleAnimation.setFillAfter(true);
    view.startAnimation(scaleAnimation);
  }

  public static void animateBottomMargin(View view, int margin, int duration) {
    ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
    ValueAnimator animator = ValueAnimator.ofInt(lp.bottomMargin, margin);
    animator.setDuration(duration);
    animator.addUpdateListener(animation -> {
      lp.bottomMargin = (Integer) animation.getAnimatedValue();
      view.setLayoutParams(lp);
    });
    animator.start();
  }

  public static void animateBottomPadding(View view, int padding, int duration) {
    ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
    ValueAnimator animator = ValueAnimator.ofInt(view.getPaddingBottom(), padding);
    animator.setDuration(duration);
    animator.addUpdateListener(animation -> {
      view.setPadding(0, 0, 0, (Integer) animation.getAnimatedValue());
      view.setLayoutParams(lp);
    });
    animator.start();
  }

  public static void animateLeftMargin(View view, int margin, int duration,
      Interpolator interpolator) {
    ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
    ValueAnimator animator = ValueAnimator.ofInt(lp.leftMargin, margin);
    animator.setDuration(duration);
    animator.setInterpolator(interpolator);
    animator.addUpdateListener(animation -> {
      lp.leftMargin = (Integer) animation.getAnimatedValue();
      view.setLayoutParams(lp);
    });
    animator.start();
  }

  public static void animateRightMargin(View view, int margin, int duration) {
    ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
    ValueAnimator animator = ValueAnimator.ofInt(lp.rightMargin, margin);
    animator.setDuration(duration);
    animator.addUpdateListener(animation -> {
      lp.rightMargin = (Integer) animation.getAnimatedValue();
      view.setLayoutParams(lp);
    });
    animator.start();
  }

  public static void animateTopMargin(View view, int margin, int duration,
      Interpolator interpolator) {
    ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
    ValueAnimator animator = ValueAnimator.ofInt(lp.topMargin, margin);
    animator.setDuration(duration);
    animator.setInterpolator(interpolator);
    animator.addUpdateListener(animation -> {
      lp.topMargin = (Integer) animation.getAnimatedValue();
      view.setLayoutParams(lp);
    });
    animator.start();
  }

  public static void animateSize(View view, int startSize, int endSize, int duration,
      Interpolator interpolator) {
    ValueAnimator animator = ValueAnimator.ofInt(startSize, endSize);
    animator.setDuration(duration);
    animator.setInterpolator(interpolator);
    animator.addUpdateListener(
        animation -> UIUtils.changeSizeOfView(view, (int) animation.getAnimatedValue()));
    animator.start();
  }

  public static void animateWidth(View view, int startSize, int endSize, int duration,
      Interpolator interpolator) {
    ValueAnimator animator = ValueAnimator.ofInt(startSize, endSize);
    animator.setDuration(duration);
    animator.setInterpolator(interpolator);
    animator.addUpdateListener(
        animation -> UIUtils.changeWidthOfView(view, (int) animation.getAnimatedValue()));
    animator.start();
  }

  public static void makeItBounce(View view, int duration, Interpolator interpolator) {
    ValueAnimator animator = ValueAnimator.ofFloat(1.0f, 1.15f, 1.0f);
    animator.setInterpolator(interpolator);
    animator.setDuration(duration);
    animator.addUpdateListener(animation -> {
      Float scale = (float) animation.getAnimatedValue();
      view.setScaleX(scale);
      view.setScaleY(scale);
    });
    animator.start();
  }

  public static Animator getSizeAnimator(View view, int size) {
    ValueAnimator animator = ValueAnimator.ofInt(view.getWidth(), size);
    animator.addUpdateListener(
        animation -> UIUtils.changeSizeOfView(view, (int) animation.getAnimatedValue()));
    return animator;
  }

  public static Animator getScaleAnimator(View view, float scale) {
    ValueAnimator animator = ValueAnimator.ofFloat(view.getScaleX(), scale);
    animator.addUpdateListener(animation -> {
      view.setScaleX((float) animation.getAnimatedValue());
      view.setScaleY((float) animation.getAnimatedValue());
    });
    return animator;
  }

  public static Animator getWidthAnimator(View view, int startWidth, int endWidth) {
    ValueAnimator animator = ValueAnimator.ofInt(startWidth, endWidth);
    animator.addUpdateListener(
        animation -> UIUtils.changeWidthOfView(view, (int) animation.getAnimatedValue()));
    return animator;
  }

  public static Animator getHeightAnimator(View view, int startHeight, int endHeight) {
    ValueAnimator animator = ValueAnimator.ofInt(startHeight, endHeight);
    animator.addUpdateListener(
        animation -> UIUtils.changeHeightOfView(view, (int) animation.getAnimatedValue()));
    return animator;
  }

  public static Animator getLeftMarginAnimator(View view, int margin) {
    ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
    ValueAnimator animator = ValueAnimator.ofInt(lp.leftMargin, margin);
    animator.addUpdateListener(animation -> {
      lp.leftMargin = (Integer) animation.getAnimatedValue();
      view.setLayoutParams(lp);
    });
    return animator;
  }

  public static Animator getTopMarginAnimator(View view, int margin) {
    ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
    ValueAnimator animator = ValueAnimator.ofInt(lp.topMargin, margin);
    animator.addUpdateListener(animation -> {
      lp.topMargin = (Integer) animation.getAnimatedValue();
      view.setLayoutParams(lp);
    });
    return animator;
  }

  public static Animator getRotationAnimator(View view, int rotation) {
    ValueAnimator animator = ValueAnimator.ofFloat(view.getRotation(), rotation);
    animator.addUpdateListener(animation -> view.setRotation((float) animation.getAnimatedValue()));
    return animator;
  }

  public static Animator getRadiusAnimator(CardView view, int radius) {
    ValueAnimator animator = ValueAnimator.ofFloat(view.getRadius(), radius);
    animator.addUpdateListener(animation -> view.setRadius((float) animation.getAnimatedValue()));
    return animator;
  }

  public static Animator getElevationAnimator(View view, float elevation) {
    ValueAnimator animator = ValueAnimator.ofFloat(ViewCompat.getElevation(view), elevation);
    animator.addUpdateListener(animation -> ViewCompat.setElevation(view, elevation));
    return animator;
  }

  public static ValueAnimator getColorAnimator(View v, int colorFrom, int colorTo) {
    ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
    colorAnimation.addUpdateListener(
        animator -> setColorToBG(v, (Integer) animator.getAnimatedValue()));
    colorAnimation.setInterpolator(new DecelerateInterpolator());
    return colorAnimation;
  }

  public static void scaleOldImageOutNewImageIn(ImageView imageView, Drawable oldDrawable,
      Drawable newDrawable) {
    imageView.setImageDrawable(oldDrawable);
    AnimationUtils.scaleDown(imageView, AnimationUtils.ANIMATION_DURATION_SHORT);
    Observable.timer(AnimationUtils.ANIMATION_DURATION_SHORT, TimeUnit.MILLISECONDS)
        .onBackpressureDrop()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(time -> {
          imageView.setImageDrawable(newDrawable);
          AnimationUtils.scaleUp(imageView, ANIMATION_DURATION_SHORT);
        });
  }

  public static void animateTextColor(TextView txtView, int colorFrom, int colorTo, int duration) {
    ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
    colorAnimation.addUpdateListener(
        animator -> txtView.setTextColor((Integer) animator.getAnimatedValue()));
    colorAnimation.setDuration(duration);
    colorAnimation.setInterpolator(new DecelerateInterpolator());
    colorAnimation.start();
  }

  public static void animateBGColor(View v, int colorFrom, int colorTo, int duration) {
    ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
    colorAnimation.addUpdateListener(
        animator -> setColorToBG(v, (Integer) animator.getAnimatedValue()));
    colorAnimation.setDuration(duration);
    colorAnimation.setInterpolator(new DecelerateInterpolator());
    colorAnimation.start();
  }

  public static void animateColorFilter(ImageView v, int colorFrom, int colorTo, int duration) {
    ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
    colorAnimation.addUpdateListener(
        animator -> v.setColorFilter((Integer) animator.getAnimatedValue()));
    colorAnimation.setDuration(duration);
    colorAnimation.setInterpolator(new DecelerateInterpolator());
    colorAnimation.start();
  }

  public static void animateBGColorLinear(View v, int colorFrom, int colorTo, float percent) {
    if (percent <= 1 && percent >= 0) {
      int color = interpolateColor(colorFrom, colorTo, percent);
      setColorToBG(v, color);
    }
  }

  private static float interpolate(float a, float b, float proportion) {
    return (a + ((b - a) * proportion));
  }

  /**
   * Returns an interpolated color, between <code>a</code> and <code>b</code>
   * proportion = 0, results in color a
   * proportion = 1, results in color b
   */
  private static int interpolateColor(int a, int b, float proportion) {

    if (proportion > 1 || proportion < 0) {
      throw new IllegalArgumentException("proportion must be [0 - 1]");
    }
    float[] hsva = new float[3];
    float[] hsvb = new float[3];
    float[] hsv_output = new float[3];

    Color.colorToHSV(a, hsva);
    Color.colorToHSV(b, hsvb);
    for (int i = 0; i < 3; i++) {
      hsv_output[i] = interpolate(hsva[i], hsvb[i], proportion);
    }

    int alpha_a = Color.alpha(a);
    int alpha_b = Color.alpha(b);
    float alpha_output = interpolate(alpha_a, alpha_b, proportion);

    return Color.HSVToColor((int) alpha_output, hsv_output);
  }

  private static void setColorToBG(View v, int color) {
    if (v instanceof CardView) {
      ((CardView) v).setCardBackgroundColor(color);
    } else {
      Drawable background = v.getBackground();

      if (background instanceof ShapeDrawable) {
        ShapeDrawable shapeDrawable = (ShapeDrawable) background;
        shapeDrawable.getPaint().setColor(color);
      } else if (background instanceof GradientDrawable) {
        GradientDrawable gradientDrawable = (GradientDrawable) background;
        gradientDrawable.setColor(color);
      } else if (background instanceof ColorDrawable) {
        ColorDrawable colorDrawable = (ColorDrawable) background;
        colorDrawable.setColor(color);
      }
    }
  }

  public static void crossFadeDrawable(LayerDrawable layer, int from, int to, int duration) {
    ValueAnimator alphaAnimationFrom = ValueAnimator.ofObject(new IntEvaluator(), 255, 0);
    alphaAnimationFrom.addUpdateListener(
        animator -> layer.getDrawable(from).setAlpha((Integer) animator.getAnimatedValue()));
    alphaAnimationFrom.setDuration(duration);
    alphaAnimationFrom.setInterpolator(new DecelerateInterpolator());
    alphaAnimationFrom.start();

    ValueAnimator alphaAnimationTo = ValueAnimator.ofObject(new IntEvaluator(), 0, 255);
    alphaAnimationTo.addUpdateListener(
        animator -> layer.getDrawable(to).setAlpha((Integer) animator.getAnimatedValue()));
    alphaAnimationTo.setDuration(duration);
    alphaAnimationTo.setInterpolator(new DecelerateInterpolator());
    alphaAnimationTo.start();
  }
}
