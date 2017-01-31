package com.tribe.app.presentation.view.tutorial;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.IntDef;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;

import com.tribe.app.presentation.view.utils.ScreenUtils;

public class ToolTip {

  @IntDef({ RIGHT_UPWARD, RIGHT_DOWNWARD, CENTER_DOWNWARD, NO_IND }) public @interface Style {
  }

  public static final int RIGHT_UPWARD = 0;
  public static final int RIGHT_DOWNWARD = 1;
  public static final int CENTER_DOWNWARD = 2;
  public static final int NO_IND = 3;

  public String title;
  public int backgroundRes;
  public View.OnClickListener onClickListener;
  public Animation enterAnimation, exitAnimation;
  public int gravity;
  public int textColor;
  private ScreenUtils screenUtils;
  public int offsetX = 0;
  public int offsetY = 0;

  public ToolTip(Context context, ScreenUtils screenUtils) {
    title = "";
    backgroundRes = Color.WHITE;
    textColor = Color.BLACK;
    gravity = Gravity.CENTER;
    this.screenUtils = screenUtils;
  }

  public ToolTip setTitle(String title) {
    this.title = title;
    return this;
  }

  public ToolTip setBackgroundRes(int backgroundRes) {
    this.backgroundRes = backgroundRes;
    return this;
  }

  public ToolTip setOnClickListener(View.OnClickListener onClickListener) {
    this.onClickListener = onClickListener;
    return this;
  }

  public ToolTip setTextColor(int textColor) {
    this.textColor = textColor;
    return this;
  }

  public ToolTip setOffsetX(int offsetX) {
    this.offsetX = offsetX;
    return this;
  }

  public ToolTip setOffsetY(int offsetY) {
    this.offsetY = offsetY;
    return this;
  }

  public ToolTip setEnterAnimation(Animation enterAnimation) {
    this.enterAnimation = enterAnimation;
    return this;
  }

  public ToolTip setExitAnimation(Animation exitAnimation) {
    this.exitAnimation = exitAnimation;
    return this;
  }

  public ToolTip setGravity(int gravity) {
    this.gravity = gravity;
    computeDefaultAnimations();
    return this;
  }

  private void computeDefaultAnimations() {
    Animation tooltipEnAlphaAnimation = new AlphaAnimation(0f, 1f);
    Animation tooltipEnTranslateAnimation = new TranslateAnimation(0f, 0f,
        screenUtils.dpToPx((gravity & Gravity.BOTTOM) == Gravity.BOTTOM ? 100 : -100), 0f);
    tooltipEnTranslateAnimation.setInterpolator(new OvershootInterpolator(2f));

    AnimationSet tooltipEnterSet = new AnimationSet(false);
    tooltipEnterSet.setDuration(600);
    tooltipEnterSet.setFillAfter(true);
    tooltipEnterSet.setStartOffset(300);
    tooltipEnterSet.addAnimation(tooltipEnAlphaAnimation);
    tooltipEnterSet.addAnimation(tooltipEnTranslateAnimation);
    enterAnimation = tooltipEnterSet;

    Animation tooltipExAlphaAnimation = new AlphaAnimation(1f, 0f);
    tooltipExAlphaAnimation.setFillAfter(true);

    Animation tooltipExTranslateAnimation = new TranslateAnimation(0f, 0f, 0f,
        screenUtils.dpToPx((gravity & Gravity.BOTTOM) == Gravity.BOTTOM ? 100 : -100));
    tooltipExTranslateAnimation.setInterpolator(new OvershootInterpolator(1.2f));

    AnimationSet tooltipExitSet = new AnimationSet(false);
    tooltipExitSet.setDuration(300);
    tooltipExitSet.setFillAfter(true);
    tooltipExitSet.addAnimation(tooltipExAlphaAnimation);
    tooltipExitSet.addAnimation(tooltipExTranslateAnimation);
    exitAnimation = tooltipExitSet;
  }
}
