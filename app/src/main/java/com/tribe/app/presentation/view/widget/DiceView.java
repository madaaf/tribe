package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.tribe.app.R;
import java.util.ArrayList;
import java.util.List;
import timber.log.Timber;

/**
 * Created by madaaflak on 16/06/2017.
 */

public class DiceView extends FrameLayout {

  private final static int DURATON = 1000;
  private final static int NB_VIEWS = 6;

  @BindView(R.id.diceView) FrameLayout dice;
  @BindView(R.id.dicebg) FrameLayout dicebg;

  // VARIABLES
  private LayoutInflater inflater;
  private Unbinder unbinder;
  private int sizeDot;
  private List<View> viewDots = new ArrayList<>();
  private int unit;

  Drawable[] drawablesDots = new Drawable[] {
      ContextCompat.getDrawable(getContext(), R.drawable.dice_dot1),
      ContextCompat.getDrawable(getContext(), R.drawable.dice_dot2),
      ContextCompat.getDrawable(getContext(), R.drawable.dice_dot3),
      ContextCompat.getDrawable(getContext(), R.drawable.dice_dot4),
      ContextCompat.getDrawable(getContext(), R.drawable.dice_dot5),
      ContextCompat.getDrawable(getContext(), R.drawable.dice_dot6)
  };

  public DiceView(@NonNull Context context) {
    super(context);
    initView(context);
  }

  public DiceView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(context);
    initDots();
    initAnimation();
  }

  private void initView(Context context) {
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_dice, this, true);
    unbinder = ButterKnife.bind(this);
    sizeDot = getResources().getDimensionPixelSize(R.dimen.view_dice_dot_size);
    unit = (getResources().getDimensionPixelSize(R.dimen.dice_size) / 2) - 80;
  }

  private void initAnimation() {
    // finally i use this code to execute the animation
/*    Animation anim1 = AnimationUtils.loadAnimation(getContext(), R.anim.dice_anim1);
    anim1.setRepeatCount(ObjectAnimator.INFINITE);
    anim1.setRepeatMode(ObjectAnimator.RESTART);
    anim1.setAnimationListener(new AnimationListenerAdapter() {
      @Override public void onAnimationEnd(Animation animation) {
        super.onAnimationEnd(animation);
        anim1.setAnimationListener(this);
        dice.startAnimation(anim1);
      }
    });
    dice.startAnimation(anim1);*/
    animateDots();
  }

  private void resetDotsPlace() {
    for (int i = 0; i < NB_VIEWS; i++) {
      int finalI = i;
      viewDots.get(i)
          .animate()
          .translationX(0)
          .translationY(0)
          .setDuration(1000)
          .setListener(null)
          .withEndAction(() -> {
            Timber.e("SOEF RESET");
            if (finalI != 0) viewDots.get(finalI).setVisibility(INVISIBLE);
          })
          .start();
    }
  }

  private void animateDots() {
    viewDots.get(0).setVisibility(VISIBLE);
    dice.animate().rotation(90).setDuration(DURATON).withStartAction(() -> {
      viewDots.get(1).setVisibility(VISIBLE);
      viewDots.get(1)
          .animate()
          .translationX(unit)
          .translationY(unit)
          .setDuration(DURATON)
          .setListener(null);

      viewDots.get(0)
          .animate()
          .translationX(-unit)
          .translationY(-unit)
          .setDuration(DURATON)
          .setListener(null)
          .withEndAction(() -> {
            Timber.e("SOEF 1");
          });
    }).withEndAction(() -> {

      dice.animate().rotation(180).setDuration(DURATON).withStartAction(() -> {
        viewDots.get(2).setX(viewDots.get(2).getX() - unit);
        viewDots.get(2).setY(viewDots.get(2).getY() - unit);
        viewDots.get(2).setAlpha(0);
        viewDots.get(2).setVisibility(VISIBLE);

        viewDots.get(2)
            .animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setStartDelay(300)
            .setDuration(DURATON - 300)
            .setListener(null)
            .start();

        viewDots.get(0)
            .animate()
            .translationX(0)
            .translationY(0)
            .setDuration(DURATON)
            .setListener(null);
      }).withEndAction(() -> {

        dice.animate().rotation(270).setDuration(DURATON).withStartAction(() -> {
          viewDots.get(3).setX(viewDots.get(3).getX() + unit);
          viewDots.get(3).setY(viewDots.get(3).getY() - unit);
          viewDots.get(3).setAlpha(0);
          viewDots.get(3).setVisibility(VISIBLE);

          viewDots.get(3)
              .animate()
              .alpha(1)
              .scaleX(1)
              .scaleY(1)
              .setStartDelay(300)
              .setDuration(DURATON - 300)
              .setListener(null)
              .start();

          viewDots.get(0)
              .animate()
              .translationX(-unit)
              .translationY(unit)
              .setDuration(DURATON)
              .withStartAction(() -> {

              })
              .withEndAction(() -> {
                dice.animate().rotation(360).setDuration(DURATON).withStartAction(() -> {
                  Timber.e("SOEF 3");
                  viewDots.get(4).setAlpha(0);
                  viewDots.get(4).setVisibility(VISIBLE);
                  viewDots.get(4)
                      .animate()
                      .alpha(1)
                      .scaleX(1f)
                      .scaleY(1f)
                      .setDuration(DURATON)
                      .setListener(null)
                      .start();
                }).setListener(null).start();
              })
              .setListener(null)
              .start();
        }).withEndAction(() -> {
        }).setListener(null).start();
      }).setListener(null).start();
    }).setListener(null).start();
  }

  private void ok() {
    viewDots.get(1).setVisibility(VISIBLE);
    viewDots.get(1)
        .animate()
        .translationX(unit)
        .translationY(unit)
        .setDuration(DURATON)
        .setListener(null)
        .withEndAction(() -> {

        });

    viewDots.get(0).setVisibility(VISIBLE);
    viewDots.get(0)
        .animate()
        .translationX(-unit)
        .translationY(-unit)
        .setDuration(DURATON)
        .setListener(null)
        .withEndAction(() -> {
          Timber.e("SOEF 1");

          viewDots.get(2).setX(viewDots.get(2).getX() - unit);
          viewDots.get(2).setY(viewDots.get(2).getY() - unit);
          viewDots.get(2).setAlpha(0);
          viewDots.get(2).setVisibility(VISIBLE);

          viewDots.get(2)
              .animate()
              .alpha(1f)
              .scaleX(1f)
              .scaleY(1f)
              .setStartDelay(300)
              .setDuration(DURATON - 300)
              .setListener(null)
              .start();

          viewDots.get(0)
              .animate()
              .translationX(0)
              .translationY(0)
              .setDuration(DURATON)
              .setListener(null)
              .withEndAction(() -> {
                Timber.e("SOEF 2");

                viewDots.get(3).setX(viewDots.get(3).getX() + unit);
                viewDots.get(3).setY(viewDots.get(3).getY() - unit);
                viewDots.get(3).setAlpha(0);
                viewDots.get(3).setVisibility(VISIBLE);

                viewDots.get(3)
                    .animate()
                    .alpha(1)
                    .scaleX(1)
                    .scaleY(1)
                    .setStartDelay(300)
                    .setDuration(DURATON - 300)
                    .setListener(null)
                    .start();

                viewDots.get(0)
                    .animate()
                    .translationX(-unit)
                    .translationY(unit)
                    .setDuration(DURATON)
                    .setListener(null)
                    .withEndAction(() -> {

                      Timber.e("SOEF 3");
                      viewDots.get(4).setAlpha(0);
                      viewDots.get(4).setVisibility(VISIBLE);
                      viewDots.get(4)
                          .animate()
                          .alpha(1)
                          .scaleX(1f)
                          .scaleY(1f)
                          .setDuration(DURATON)
                          .setListener(null)
                          .start();
                    })
                    .start();
              })
              .start();
        })
        .start();
  }

  @OnClick(R.id.ok) public void repeat() {
    animateDots();
  }

  @OnClick(R.id.ok1) public void reset() {
    resetDotsPlace();
    dice.setRotation(0);
    dice.clearAnimation();
  }

  private void initDots() {
    for (int i = 0; i < NB_VIEWS; i++) {
      View v = new View(getContext());
      v.setBackground(drawablesDots[i]);
      FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(sizeDot, sizeDot);
      lp.gravity = Gravity.CENTER;
      v.setLayoutParams(lp);
      viewDots.add(v);
      if (i != 0) v.setVisibility(INVISIBLE);
      dicebg.addView(v);
    }
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
