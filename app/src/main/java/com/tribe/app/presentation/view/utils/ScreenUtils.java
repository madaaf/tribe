package com.tribe.app.presentation.view.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton public class ScreenUtils {

  private Context context;

  @Inject public ScreenUtils(Context context) {
    this.context = context;
  }

  public int dpToPx(int dp) {
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
        context.getResources().getDisplayMetrics());
  }

  public int dpToPx(float dp) {
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
        context.getResources().getDisplayMetrics());
  }

  public float pxToDp(int px) {
    Resources resources = context.getResources();
    DisplayMetrics metrics = resources.getDisplayMetrics();
    float dp = px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    return dp;
  }

  public int getWidthPx() {
    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
    return metrics.widthPixels;
  }

  public int getHeightPx() {
    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
    return metrics.heightPixels;
  }

  public float getWidthDp() {
    return pxToDp(getWidthPx());
  }

  public float getHeightDp() {
    return pxToDp(getHeightPx());
  }

  public float getDensity() {
    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
    return metrics.densityDpi;
  }

  public int getOrientation() {
    return context.getResources().getConfiguration().orientation;
  }

  public void hideKeyboard(Activity activity) {
    View view = activity.getCurrentFocus();
    if (view != null) {
      InputMethodManager imm =
          (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
  }

  public void hideKeyboard(View view) {
    if (view != null) {
      InputMethodManager imm =
          (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
  }

  public void showKeyboard(View view, int delay) {
    if (view != null) {
      view.requestFocus();
      view.postDelayed(() -> {
        InputMethodManager keyboard =
            (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.showSoftInput(view, 0);
      }, delay);
    }
  }

  public void setTopMargin(View view, int topMargin) {
    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
    layoutParams.topMargin = topMargin;
    view.setLayoutParams(layoutParams);
  }
}
