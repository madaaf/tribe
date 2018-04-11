package com.tribe.app.presentation.view.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import javax.inject.Inject;

/**
 * Created by madaaflak on 02/04/2018.
 */

public class LoadingGameView extends FrameLayout {

  // VARIABLES
  private LayoutInflater inflater;
  private Unbinder unbinder;
  private ValueAnimator animator;
  private OnFinishEventListener listener;

  @BindView(R.id.loading_game_back) ImageView background;
  @BindView(R.id.loading_game_btn) ImageView btns;
  @BindView(R.id.container) RelativeLayout container;

  @Inject ScreenUtils screenUtils;

  public LoadingGameView(@NonNull Context context) {
    super(context);
    initView(context);
  }

  public LoadingGameView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(context);
  }

  private void initView(Context context) {
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_loading_game, this, true);
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);
    unbinder = ButterKnife.bind(this);
    setAlpha(1);
  }

  public void setAnim(float a, float b) {
    if (animator != null) {
      return;
    }
    setAlpha(1f);
    animator = ValueAnimator.ofFloat(a, b);
    animator.setInterpolator(new OvershootInterpolator(1.2f));
    animator.setStartDelay(500);
    animator.setRepeatCount(ValueAnimator.INFINITE);
    animator.addUpdateListener(animation -> {
      float value = (float) animation.getAnimatedValue();
      background.setRotation(value);
      btns.setRotation(-value);
    });
    animator.setDuration(1000);
    animator.start();
  }

  boolean isFinish = false;

  public void finish() {
    if (isFinish) {
      isFinish = false;
      container.animate()
          .translationX(screenUtils.getWidthPx())
          .setDuration(3000)
          .withEndAction(new Runnable() {
            @Override public void run() {
              if (listener != null) listener.onFinishAnim();
            }
          })
          .setInterpolator(new OvershootInterpolator(1.5f))
          .start();
    }
  }

  public void start() {
    isFinish = true;
    container.setTranslationX(-screenUtils.getWidthPx());
    container.animate()
        .setStartDelay(500)
        .translationX(0)
        .setDuration(1000)
        .setInterpolator(new OvershootInterpolator(1.2f))
        .start();
  }

  public void setNotifEventListener(OnFinishEventListener eventListener) {
    listener = eventListener;
  }

  public interface OnFinishEventListener {
    void onFinishAnim();
  }
}
