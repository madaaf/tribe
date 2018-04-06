package com.tribe.app.presentation.view.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;

/**
 * Created by madaaflak on 02/04/2018.
 */

public class LoadingGameView extends FrameLayout {

  // VARIABLES
  private LayoutInflater inflater;
  private Unbinder unbinder;
  private ValueAnimator animator;

  @BindView(R.id.loading_game_back) ImageView background;
  @BindView(R.id.loading_game_btn) ImageView btns;

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
    unbinder = ButterKnife.bind(this);
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);
    setAlpha(0);
  }

  public void setAnim(float a, float b) {
    setAlpha(1f);
    if (animator == null) {
      animator = ValueAnimator.ofFloat(a, b);
      animator.setInterpolator(new OvershootInterpolator(1.5f));
      animator.setStartDelay(500);
      animator.addUpdateListener(animation -> {
        float value = (float) animation.getAnimatedValue();
        background.setRotation(value);
        btns.setRotation(-value);
      });
      animator.setDuration(500);
      animator.addListener(new AnimatorListenerAdapter() {
        @Override public void onAnimationEnd(Animator animation) {
          super.onAnimationEnd(animation);
          setAnim(a + 90f, b + 90f);
        }
      });
      animator.start();
    }
  }
}
