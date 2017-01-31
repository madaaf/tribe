package com.tribe.app.presentation.view.camera.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.tribe.app.R;
import com.tribe.app.presentation.view.camera.interfaces.AudioVisualizerCallback;
import com.tribe.app.presentation.view.utils.ScreenUtils;

import javax.inject.Inject;

/**
 * Created by tiago on 2016/06/13.
 */
public class PictoVisualizerView extends LinearLayout implements AudioVisualizerCallback {

  private static final int THRESHOLD = 1;
  private static final int DURATION_ULTRA_SHORT = 100;
  private static final int DURATION_SHORT = 200;
  private static final int DURATION_LONG = 250;
  private static final int SAMPLING_DURATION = 100;

  @Inject ScreenUtils screenUtils;

  // Dimens
  private int startSize, endSize, iconsPadding;

  // Variables
  private ImageView imageView;
  private int lastValue = Integer.MAX_VALUE;
  private ValueAnimator valueAnimator;
  private int vaLastAnimationValue;
  private Handler handler;
  private long lastSamplingTimestamp;

  public PictoVisualizerView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    startSize = getContext().getResources().getDimensionPixelSize(R.dimen.icons_start);
    endSize = getContext().getResources().getDimensionPixelSize(R.dimen.icons_end);
    iconsPadding = getContext().getResources().getDimensionPixelSize(R.dimen.icons_padding);

    imageView = new ImageView(getContext());
    //imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    LinearLayout.LayoutParams vp = new LinearLayout.LayoutParams(startSize, startSize);
    vp.gravity = Gravity.CENTER;
    imageView.setPadding(iconsPadding, iconsPadding, iconsPadding, iconsPadding);
    imageView.setLayoutParams(vp);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PictoVisualizerView);
    imageView.setImageResource(
        a.getResourceId(R.styleable.PictoVisualizerView_picto, R.drawable.picto_sound_big));
    a.recycle();
    addView(imageView);

    handler = new Handler(Looper.getMainLooper());

    setWillNotDraw(false);
  }

  @Override public void receive(final double[]... toTransform) {
    if (System.currentTimeMillis() - lastSamplingTimestamp > SAMPLING_DURATION) {
      handler.post(() -> {
        lastSamplingTimestamp = System.currentTimeMillis();
        int max = Integer.MIN_VALUE;

        for (int i = 0; i < toTransform[0].length; i++) {
          int value = (int) (toTransform[0][i] * 15);
          if (value > max) max = value;
        }

        if (Math.abs(lastValue - max) >= THRESHOLD) {
          layoutImage(endSize + max * 30, true);
          lastValue = max;
        }
      });
    }
  }

  private void layoutImage(int size, boolean hasReturn) {
    if (valueAnimator != null && valueAnimator.isRunning()) valueAnimator.cancel();

    if (hasReturn) {
      valueAnimator = ValueAnimator.ofInt(vaLastAnimationValue, size, endSize);
    } else {
      valueAnimator = ValueAnimator.ofInt(vaLastAnimationValue, size);
    }

    valueAnimator.setDuration(DURATION_LONG);
    valueAnimator.setInterpolator(new LinearInterpolator());
    valueAnimator.addUpdateListener(animation -> {
      Integer value = (Integer) animation.getAnimatedValue();
      vaLastAnimationValue = value.intValue();
      imageView.getLayoutParams().height = value.intValue();
      imageView.getLayoutParams().width = value.intValue();
      imageView.requestLayout();
    });

    valueAnimator.start();
  }

  @Override public void activate() {
    setVisibility(View.VISIBLE);
  }

  @Override public void startRecording() {
    vaLastAnimationValue = startSize;
    layoutImage(endSize, false);
  }

  @Override public void stopRecording() {
    layoutImage(startSize, false);
  }

  @Override public void deactivate() {
    setVisibility(View.GONE);
  }
}
