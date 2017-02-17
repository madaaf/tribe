package com.tribe.app.presentation.view.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by tiago on 14/11/2016.
 */

public class UIUtils {

  private static final int DURATION_REVEAL = 450;

  public static void setBackgroundGrid(ScreenUtils screenUtils, View v, int position,
      boolean hasCorners) {
    Drawable background = v.getBackground();
    int color = PaletteGrid.get(position - 1);
    int radiusTopLeft = position == 1 ? screenUtils.dpToPx(5) : 0;
    int radiusTopRight = position == 2 ? screenUtils.dpToPx(5) : 0;
    float[] radiusMatrix =
        new float[] { radiusTopLeft, radiusTopLeft, radiusTopRight, radiusTopRight, 0, 0, 0, 0 };

    if (background == null) {
      GradientDrawable gradientDrawable = new GradientDrawable();
      gradientDrawable.setShape(GradientDrawable.RECTANGLE);
      gradientDrawable.setColor(color);
      if (hasCorners) {
        gradientDrawable.setCornerRadii(radiusMatrix);
      } else {
        gradientDrawable.setCornerRadius(0);
      }
      v.setBackground(gradientDrawable);
    } else if (background instanceof GradientDrawable) {
      GradientDrawable gradientDrawable = (GradientDrawable) background;
      gradientDrawable.setColor(color);
      if (hasCorners) {
        gradientDrawable.setCornerRadii(radiusMatrix);
      } else {
        gradientDrawable.setCornerRadius(0);
      }
    }
  }

  public static void setBackgroundMultiple(ScreenUtils screenUtils, View v, int position) {
    Drawable background = v.getBackground();
    int color = PaletteGrid.get(position - 1);
    int radius = screenUtils.dpToPx(5);
    float[] radiusMatrix =
        new float[] { radius, radius, radius, radius, radius, radius, radius, radius };

    if (background == null) {
      GradientDrawable gradientDrawable = new GradientDrawable();
      gradientDrawable.setShape(GradientDrawable.RECTANGLE);
      gradientDrawable.setColor(color);
      gradientDrawable.setCornerRadii(radiusMatrix);
      v.setBackground(gradientDrawable);
    } else if (background instanceof GradientDrawable) {
      GradientDrawable gradientDrawable = (GradientDrawable) background;
      gradientDrawable.setColor(color);
      gradientDrawable.setCornerRadii(radiusMatrix);
    }
  }

  public static void setBackgroundCard(CardView v, int position) {
    int color = PaletteGrid.get(position - 1);
    v.setCardBackgroundColor(color);
  }

  public static void setBackgroundInd(View v, int position) {
    int color = PaletteGrid.get(position - 1);
    GradientDrawable gradientDrawable = new GradientDrawable();
    gradientDrawable.setShape(GradientDrawable.OVAL);
    gradientDrawable.setColor(color);
    v.setBackground(gradientDrawable);
  }

  public static void changeSizeOfView(View v, int size) {
    ViewGroup.LayoutParams params = v.getLayoutParams();
    params.width = params.height = size;
    v.setLayoutParams(params);
  }

  public static void changeHeightOfView(View v, int height) {
    ViewGroup.LayoutParams params = v.getLayoutParams();
    params.height = height;
    v.setLayoutParams(params);
  }

  public static void changeWidthOfView(View v, int width) {
    ViewGroup.LayoutParams params = v.getLayoutParams();
    params.width = width;
    v.setLayoutParams(params);
  }

  public static void showReveal(View v, AnimatorListenerAdapter listenerAdapter) {
    if (v.getVisibility() == View.VISIBLE) return;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      int initialRadius = 0;
      Animator anim =
          ViewAnimationUtils.createCircularReveal(v, (int) (v.getX() + v.getWidth() / 2),
              (int) (v.getY() + v.getHeight() / 2), initialRadius,
              Math.max(v.getWidth(), v.getHeight()));
      anim.setInterpolator(new AccelerateDecelerateInterpolator());
      anim.addListener(listenerAdapter);
      anim.setDuration(DURATION_REVEAL);
      anim.setStartDelay(500);
      anim.start();
    } else {
      v.setVisibility(View.VISIBLE);
    }
  }

  public static void hideReveal(View v, AnimatorListenerAdapter listenerAdapter) {
    if (v.getVisibility() == View.GONE) return;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      int initialRadius = Math.max(v.getWidth(), v.getHeight());
      Animator anim =
          ViewAnimationUtils.createCircularReveal(v, (int) (v.getX() + v.getWidth() / 2),
              (int) (v.getY() + v.getHeight() / 2), initialRadius, 0);
      anim.setInterpolator(new AccelerateDecelerateInterpolator());
      anim.setDuration(DURATION_REVEAL);
      anim.addListener(listenerAdapter);
      anim.start();
    } else {
      v.setVisibility(View.GONE);
    }
  }
}
