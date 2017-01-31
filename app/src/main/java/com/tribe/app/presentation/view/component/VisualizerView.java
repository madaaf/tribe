package com.tribe.app.presentation.view.component;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.BadParcelableException;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.transformer.CropCircleTransformation;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.CircleView;

import java.util.Random;

import javax.inject.Inject;

/**
 * Created by horatiothomas on 10/17/16.
 */
public class VisualizerView extends FrameLayout {

  private byte[] bytes;
  private float[] points;
  private Rect rect = new Rect();
  private Paint smallPaint = new Paint();
  private Paint largePaint = new Paint();

  private float radiusLarge = 0;
  private float radiusSmall = 1000;

  private CircleView circleViewOutside;
  private CircleView circleViewInside;
  private ImageView imageAvatar;
  private FrameLayout.LayoutParams circleParamsLarge;
  private FrameLayout.LayoutParams circleParamsSmall;

  private int size;

  private ValueAnimator vaAlpha;
  private ValueAnimator vaCircleInside;
  private ValueAnimator vaCircleOutside;

  @Inject ScreenUtils screenUtils;

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

  private void init() {
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);
    bytes = null;
    smallPaint.setStrokeWidth(screenUtils.dpToPx(1f));
    smallPaint.setAntiAlias(true);
    smallPaint.setColor(ContextCompat.getColor(getContext(), R.color.black_opacity_20));

    largePaint.setStrokeWidth(screenUtils.dpToPx(1f));
    largePaint.setAntiAlias(true);
    largePaint.setColor(ContextCompat.getColor(getContext(), R.color.black_opacity_5));

    size = getContext().getResources().getDimensionPixelSize(R.dimen.voice_message_avatar_size);

    circleParamsLarge = new FrameLayout.LayoutParams(size, size);
    circleParamsSmall = new FrameLayout.LayoutParams(size, size);
    FrameLayout.LayoutParams imageAvatarParams =
        new FrameLayout.LayoutParams(size + screenUtils.dpToPx(30f),
            size + screenUtils.dpToPx(30f));
    circleParamsLarge.gravity = Gravity.CENTER;
    circleParamsSmall.gravity = Gravity.CENTER;
    imageAvatarParams.gravity = Gravity.CENTER;

    circleViewInside = new CircleView(getContext());
    circleViewOutside = new CircleView(getContext());
    imageAvatar = new ImageView(getContext());

    addView(circleViewOutside, circleParamsLarge);
    addView(circleViewInside, circleParamsSmall);
    addView(imageAvatar, imageAvatarParams);

    circleViewInside.startAnimation(
        AnimationUtils.loadAnimation(getContext(), R.anim.scale_sound_small));
    circleViewOutside.startAnimation(
        AnimationUtils.loadAnimation(getContext(), R.anim.scale_sound));
  }

  public void updateVisualizer(byte[] bytes) {
    this.bytes = bytes;
    invalidate();
  }

  public void setAvatarPicture(String url) {
    Glide.with(getContext())
        .load(url)
        .fitCenter()
        .crossFade()
        .bitmapTransform(new CropCircleTransformation(getContext()))
        .into(imageAvatar);
  }

  @Override protected void dispatchDraw(Canvas canvas) {
    super.dispatchDraw(canvas);

    if (bytes == null) {
      return;
    }

    rect.set(0, 0, getWidth(), getHeight());

    if (points == null || points.length < bytes.length * 4) {
      points = new float[bytes.length * 4];
    }

    float radiusLarge = 0;
    float radiusSmall = 1000;
    float[] cartPoint = new float[2];

    for (int i = 0; i < bytes.length - 1; i++) {
      cartPoint[0] = (float) i / (bytes.length - 1);
      cartPoint[1] = rect.height() / 2 + ((byte) (bytes[i] + 128)) * (rect.height() / 2) / 128;

      float tempRadius = Math.abs(rect.height() - cartPoint[1]) / 2;
      radiusLarge = tempRadius;

      radiusSmall = tempRadius;
    }

    boolean shouldMultiply = true;

    if (Math.abs((screenUtils.getHeightPx() >> 1) - cartPoint[1]) < screenUtils.dpToPx(10f)) {
      radiusLarge = size + screenUtils.dpToPx(140f);
      radiusSmall = size + screenUtils.dpToPx(100f);
      shouldMultiply = false;
    } else {
      radiusLarge = radiusSmall + radiusLarge / 2;
      radiusSmall = radiusLarge - screenUtils.dpToPx(40);
    }

    if (Math.abs(radiusSmall - this.radiusSmall) >= screenUtils.dpToPx(2.5f)) {
      this.radiusSmall = radiusSmall;

      circleViewInside.setRect(rect);
      circleViewInside.setPaint(smallPaint);
      circleViewInside.setRadius(this.radiusSmall);
      circleViewInside.clearAnimation();
      circleViewInside.startAnimation(
          AnimationUtils.loadAnimation(getContext(), R.anim.scale_sound_small));

      int tempWidth = 0;

      if (shouldMultiply) {
        tempWidth = (int) (this.radiusSmall * randomFloat(1.2f, 1.5f));
      } else {
        tempWidth = (int) this.radiusSmall;
      }
      //            tempWidth  += screenUtils.dpToPx(10f);
      vaCircleInside = ValueAnimator.ofInt(circleParamsSmall.width, tempWidth);
      vaCircleInside.setDuration(300);
      vaCircleInside.setInterpolator(new DecelerateInterpolator());
      vaCircleInside.setStartDelay(0);
      vaCircleInside.addUpdateListener(animation -> {
        Integer value = (Integer) animation.getAnimatedValue();
        circleViewInside.getLayoutParams().height = value.intValue();
        circleViewInside.getLayoutParams().width = value.intValue();
        circleViewInside.requestLayout();
      });
      vaCircleInside.start();
    }

    if (Math.abs(radiusLarge - this.radiusLarge) >= screenUtils.dpToPx(2.5f)) {
      this.radiusLarge = radiusLarge;

      circleViewOutside.setRect(rect);
      circleViewOutside.setPaint(largePaint);
      circleViewOutside.setRadius(this.radiusLarge);
      circleViewOutside.clearAnimation();
      circleViewOutside.startAnimation(
          AnimationUtils.loadAnimation(getContext(), R.anim.scale_sound));

      int widthTarget = 0;

      if (shouldMultiply) {
        widthTarget = (int) (this.radiusLarge * randomFloat(1.5f, 2.0f));
      } else {
        widthTarget = (int) this.radiusLarge;
      }
      //            widthTarget += screenUtils.dpToPx(10f);

      vaCircleOutside = ValueAnimator.ofInt(circleParamsLarge.width, widthTarget);
      vaCircleOutside.setDuration(300);
      vaCircleOutside.setInterpolator(new DecelerateInterpolator());
      vaCircleOutside.addUpdateListener(animation -> {
        Integer value = (Integer) animation.getAnimatedValue();
        circleViewOutside.getLayoutParams().height = value.intValue();
        circleViewOutside.getLayoutParams().width = value.intValue();
        circleViewOutside.requestLayout();
      });
      vaCircleOutside.start();

      float alpha = 1;

      if (widthTarget > (((float) screenUtils.getWidthPx() / 5) * 4)) {
        alpha = 1 - widthTarget / (((float) screenUtils.getWidthPx() / 5) * 4) + 1;
      }

      if (vaAlpha != null) vaAlpha.cancel();

      vaAlpha = ValueAnimator.ofFloat(circleViewOutside.getAlpha(), alpha);
      vaAlpha.setDuration(300);
      vaAlpha.setInterpolator(new DecelerateInterpolator());
      vaAlpha.addUpdateListener(animation -> {
        Float value = (Float) animation.getAnimatedValue();
        try {
          if (circleViewOutside != null) circleViewOutside.setAlpha(value.floatValue());
        } catch (BadParcelableException ex) {
          // TODO BETTER :)
        }
      });
      vaAlpha.start();
    }
  }

  private static float randomFloat(float x, float y) {
    Random rand = new Random();
    return rand.nextFloat() * (x - y) + x;
  }

  public void release() {
    if (circleViewInside != null) circleViewInside.clearAnimation();
    if (circleViewOutside != null) circleViewOutside.clearAnimation();
    if (vaAlpha != null) {
      vaAlpha.removeAllUpdateListeners();
      vaAlpha.cancel();
    }
    if (vaCircleInside != null) {
      vaCircleInside.removeAllUpdateListeners();
      vaCircleInside.cancel();
    }
    if (vaCircleOutside != null) {
      vaCircleInside.removeAllUpdateListeners();
      vaCircleOutside.cancel();
    }
  }
}
