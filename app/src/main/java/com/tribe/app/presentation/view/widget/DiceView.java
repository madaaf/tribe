package com.tribe.app.presentation.view.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.view.listener.AnimationListenerAdapter;
import java.util.Random;

/**
 * Created by madaaflak on 16/06/2017.
 */

public class DiceView extends FrameLayout {

  @BindView(R.id.diceView) FrameLayout dice;

  // VARIABLES
  private LayoutInflater inflater;
  private Unbinder unbinder;

  int[] colors = new int[] {
      ContextCompat.getColor(getContext(), R.color.watermark_1),
      ContextCompat.getColor(getContext(), R.color.watermark_2),
      ContextCompat.getColor(getContext(), R.color.watermark_3),
      ContextCompat.getColor(getContext(), R.color.watermark_4),
      ContextCompat.getColor(getContext(), R.color.watermark_5),
      ContextCompat.getColor(getContext(), R.color.watermark_6)
  };

  public DiceView(@NonNull Context context) {
    super(context);
    initView(context);
  }

  public DiceView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(context);
    initAnimation();
  }

  private void initView(Context context) {
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_dice, this, true);
    unbinder = ButterKnife.bind(this);
  }

  private void initAnimation() {

 /*   final ValueAnimator animation = ValueAnimator.ofInt(0, 360);
    animation.setDuration(6000);
    animation.setStartDelay(1000);
    animation.setRepeatCount(ObjectAnimator.INFINITE);
    // animation.setRepeatMode(ObjectAnimator.RESTART);
    animation.addUpdateListener(animation1 -> {
      int rotation = (int) animation1.getAnimatedValue();
      dice.setRotation(rotation);
    });*/

    // finally i use this code to execute the animation
    Animation anim1 = AnimationUtils.loadAnimation(getContext(), R.anim.dice_anim1);
    anim1.setRepeatCount(ObjectAnimator.INFINITE);
    anim1.setRepeatMode(ObjectAnimator.RESTART);
    anim1.setAnimationListener(new AnimationListenerAdapter() {
      @Override public void onAnimationEnd(Animation animation) {
        super.onAnimationRepeat(animation);
        anim1.setAnimationListener(this);
        dice.startAnimation(anim1);
      }
    });
    dice.startAnimation(anim1);
  }

  private static int getRandom(int[] array) {
    int rnd = new Random().nextInt(array.length);
    return array[rnd];
  }

  /*

  <!--
<?xml version="1.0" encoding="utf-8"?>
<rotate xmlns:android="http://schemas.android.com/apk/res/android"
    android:duration="1000"
    android:fillAfter="true"
    android:fromDegrees="0"
    android:pivotX="50%"
    android:pivotY="50%"
    android:toDegrees="90" />-->

   */
}
