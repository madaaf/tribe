package com.tribe.app.presentation.view.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import com.tribe.app.R;

/**
 * Created by tiago on 14/11/2016.
 */

public class UIUtils {

  public static final int DURATION_REVEAL = 300;

  public static void setBackgroundGrid(ScreenUtils screenUtils, View v, int position,
      boolean hasCorners) {
    Drawable background = v.getBackground();
    int color = PaletteGrid.get(position - 1);
    int radiusTopLeft = position == 1 ? screenUtils.dpToPx(5) : 0;
    int radiusTopRight =
        position == screenUtils.getContext().getResources().getInteger(R.integer.columnNumber)
            ? screenUtils.dpToPx(5) : 0;
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

  public static void changeWidthHeightOfView(View v, int width, int height) {
    ViewGroup.LayoutParams params = v.getLayoutParams();
    params.width = width;
    params.height = height;
    v.setLayoutParams(params);
  }

  public static void showReveal(View v, boolean animate, AnimatorListenerAdapter listenerAdapter) {
    if (!ViewCompat.isAttachedToWindow(v) || v.getVisibility() == View.VISIBLE) return;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && animate) {
      v.post(() -> {
        int initialRadius = 0;
        Animator anim =
            ViewAnimationUtils.createCircularReveal(v, (int) (v.getX() + v.getWidth() / 2),
                (int) (v.getY() + v.getHeight() / 2), initialRadius,
                Math.max(v.getWidth(), v.getHeight()));
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        if (listenerAdapter != null) anim.addListener(listenerAdapter);
        anim.setDuration(DURATION_REVEAL);
        anim.start();
      });
    } else {
      v.setVisibility(View.VISIBLE);
    }
  }

  public static void hideReveal(View v, boolean animate, AnimatorListenerAdapter listenerAdapter) {
    if (!ViewCompat.isAttachedToWindow(v) || v.getVisibility() == View.GONE) return;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && animate) {
      v.post(() -> {
        int initialRadius = Math.max(v.getWidth(), v.getHeight());
        Animator anim =
            ViewAnimationUtils.createCircularReveal(v, (int) (v.getX() + v.getWidth() / 2),
                (int) (v.getY() + v.getHeight() / 2), initialRadius, 0);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.setDuration(DURATION_REVEAL);
        if (listenerAdapter != null) anim.addListener(listenerAdapter);
        anim.start();
      });
    } else {
      v.setVisibility(View.GONE);
    }
  }

  public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int color, int cornerDips,
      int borderDips, Context context) {
    Bitmap output =
        Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(output);

    final int borderSizePx =
        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) borderDips,
            context.getResources().getDisplayMetrics());
    final int cornerSizePx =
        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) cornerDips,
            context.getResources().getDisplayMetrics());
    final Paint paint = new Paint();
    final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
    final RectF rectF = new RectF(rect);

    // prepare canvas for transfer
    paint.setAntiAlias(true);
    paint.setColor(0xFFFFFFFF);
    paint.setStyle(Paint.Style.FILL);
    canvas.drawARGB(0, 0, 0, 0);
    canvas.drawRoundRect(rectF, cornerSizePx, cornerSizePx, paint);

    // draw bitmap
    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
    canvas.drawBitmap(bitmap, rect, rect, paint);

    // draw border
    paint.setColor(color);
    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeWidth((float) borderSizePx);
    canvas.drawRoundRect(rectF, cornerSizePx, cornerSizePx, paint);

    return output;
  }
}
