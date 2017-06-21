package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by madaaflak on 16/06/2017.
 */

public class DiceView extends FrameLayout {

  private final static int DURATON = 900;
  private final static int DURATON_ROTATE = 300;
  private final static int STEP6_DURATION = 0;
  private final static int NB_VIEWS = 6;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.diceView) FrameLayout dice;
  @BindView(R.id.dicebg) FrameLayout dicebg;
  @BindView(R.id.txtNext) TextViewFont txtNext;
  @BindView(R.id.txtLabel) TextViewFont label;

  // VARIABLES
  private LayoutInflater inflater;
  private Unbinder unbinder;
  private int sizeDot;
  private int dotsMargin;
  private List<View> viewDots = new ArrayList<>();
  private int unit;
  private int rotationUnit = 90;

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
  }

  //////////////
  //  PUBLIC  //
  //////////////

  public void setNextAnimation() {
    stopRotation();
    resetDotsStates();

    new Handler().postDelayed(() -> {
      if ((dice.getRotation() % 180) == 0) {
        dice.animate()
            .scaleX((float) 2)
            .scaleY(1)
            .setDuration(500)
            .withStartAction(this::animateStepNextLayout)
            .setListener(null)
            .start();
      } else {
        dice.animate()
            .scaleY((float) 2)
            .scaleX(1)
            .setDuration(500)
            .withStartAction(this::animateStepNextLayout)
            .setListener(null)
            .start();
      }
    }, 100);
  }

  @OnClick(R.id.diceView) public void onNextClick() {
    dice.setEnabled(false);
    dice.animate().scaleX((float) 1).scaleY(1).setDuration(300).withStartAction(() -> {
      resetDotsStates();
      restartRotation();
      dicebg.animate().translationX(0).setListener(null).start();
      txtNext.animate().alpha(0).translationX(0).setListener(null).start();
      new Handler().postDelayed(this::animateDots, 1000);
    });
  }

  public void setBackgroundDiceView(Drawable bg) {
    dice.setBackground(bg);
    animateDots();
  }

  public void setSize(int size) {
    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
    params.gravity = Gravity.CENTER;
    dice.setLayoutParams(params);
  }

  public void showLabel(boolean showLabel) {
    if (showLabel) {
      label.setAlpha(0);
      label.setVisibility(VISIBLE);
      label.animate().alpha(1).setDuration(500).setListener(null).start();
    } else {
      label.animate()
          .alpha(0)
          .setDuration(500)
          .setListener(null)
          .withEndAction(() -> label.setVisibility(GONE))
          .start();
    }
  }

  //////////////
  //  PRIVATE //
  //////////////

  private void initView(Context context) {
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_dice, this, true);
    unbinder = ButterKnife.bind(this);
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);

    dice.setEnabled(false);
    getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

      @Override public void onGlobalLayout() {
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
        sizeDot = (int) (0.12 * dice.getWidth());
        dotsMargin = (int) (sizeDot / 9.75);
        unit = (int) (sizeDot * 1.18);

        initDots();
        animateDots();
      }
    });
  }

  private void resetDotsStates() {
    for (int i = 0; i < NB_VIEWS; i++) {
      if (i != 0) {
        viewDots.get(i)
            .animate()
            .translationX(0)
            .translationY(0)
            .scaleX(0)
            .scaleY(0)
            .alpha(0)
            .setDuration(100)
            .setListener(null)
            .start();
      } else {
        viewDots.get(i)
            .animate()
            .translationX(0)
            .translationY(0)
            .setDuration(300)
            .setListener(null)
            .start();
      }
    }
  }

  private void step6Anim(boolean isRecursive) {
    viewDots.get(0)
        .animate()
        .translationX(-unit)
        .translationY(-unit)
        .setStartDelay(STEP6_DURATION)
        .alpha(1)
        .scaleX(1)
        .scaleY(1)
        .setInterpolator(new BounceInterpolator())
        .setDuration(DURATON / 2)
        .setListener(null)
        .start();

    viewDots.get(1)
        .animate()
        .translationX(-unit)
        .translationY(0)
        .setStartDelay(STEP6_DURATION)
        .alpha(1)
        .scaleX(1)
        .scaleY(1)
        .setInterpolator(new BounceInterpolator())
        .setDuration(DURATON / 2)
        .setListener(null)
        .start();

    viewDots.get(2)
        .animate()
        .translationX(-unit)
        .translationY(+unit)
        .alpha(1)
        .scaleX(1)
        .scaleY(1)
        .setInterpolator(new BounceInterpolator())
        .setStartDelay(STEP6_DURATION)
        .setDuration(DURATON / 2)
        .setListener(null)
        .start();

    viewDots.get(3)
        .animate()
        .scaleX(1)
        .scaleY(1)
        .translationX(0)
        .translationY(-(sizeDot / 2) - dotsMargin)
        .alpha(1)
        .setInterpolator(new BounceInterpolator())
        .setStartDelay(STEP6_DURATION)
        .setDuration(DURATON / 2)
        .setListener(null)
        .start();

    viewDots.get(4)
        .animate()
        .scaleX(1)
        .scaleY(1)
        .translationX(0)
        .translationY(+(sizeDot / 2) + dotsMargin)
        .alpha(1)
        .setInterpolator(new BounceInterpolator())
        .setStartDelay(STEP6_DURATION)
        .setDuration(DURATON / 2)
        .setListener(null)
        .start();

    viewDots.get(5)
        .animate()
        .translationX(unit)
        .translationY(0)
        .alpha(1)
        .scaleX(1)
        .scaleY(1)
        .setInterpolator(new BounceInterpolator())
        .setStartDelay(STEP6_DURATION)
        .setDuration(DURATON / 2)
        .setListener(null)
        .start();

    if (isRecursive) {
      new Handler().postDelayed(() -> {
        dice.animate()
            .rotation(getRotationValue())
            .setDuration(DURATON_ROTATE)
            .setInterpolator(new OvershootInterpolator())
            .withStartAction(this::resetDotsStates)
            .withEndAction(this::animateDots)
            .setListener(null)
            .start();
      }, 1000);
    }
  }

  private int getRotationValue() {
    int rotationDelay = ((int) dice.getRotation()) % 90;
    return ((int) dice.getRotation() - rotationDelay + rotationUnit);
  }

  private void stopRotation() {
    rotationUnit = 0;
  }

  private void restartRotation() {
    rotationUnit = 90;
  }

  private void animateDots() {
    /**  STEP 1 [0, 0] **/
    dice.animate()
        .rotation(getRotationValue())
        .setDuration(DURATON_ROTATE)
        .setStartDelay(DURATON)
        .setInterpolator(new OvershootInterpolator())
        .withStartAction(() -> {
          if (rotationUnit == 0) return;
          /**  STEP 2
           /*  0 : [-1, -1]
           *   1 : [+1, +1]
           */
          viewDots.get(1)
              .animate()
              .scaleX(1)
              .scaleY(1)
              .alpha(1)
              .setInterpolator(new BounceInterpolator())
              .translationX(unit)
              .translationY(unit)
              .setDuration(DURATON)
              .setListener(null)
              .start();

          viewDots.get(0)
              .animate()
              .alpha(1)
              .translationX(-unit)
              .translationY(-unit)
              .setDuration(DURATON)
              .setInterpolator(new BounceInterpolator())
              .setListener(null)
              .start();
        })
        .withEndAction(() -> {
          if (rotationUnit == 0) return;
          dice.animate()
              .rotation(getRotationValue())
              .setDuration(DURATON_ROTATE)
              .setInterpolator(new OvershootInterpolator())
              .withStartAction(() -> {
                if (rotationUnit == 0) return;
                /**  STEP 3
                 /*   0 : [+0, +0]
                 *    1 : [+1, +1]
                 *    2 : [-1, -1]
                 */
                viewDots.get(2).setX(viewDots.get(2).getX() - unit);
                viewDots.get(2).setY(viewDots.get(2).getY() - unit);
                viewDots.get(2).setAlpha(0);

                viewDots.get(2)
                    .animate()
                    .alpha(1)
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setStartDelay(300)
                    .setInterpolator(new BounceInterpolator())
                    .setDuration(DURATON - 300)
                    .setListener(null)
                    .start();

                viewDots.get(0)
                    .animate()
                    .translationX(0)
                    .translationY(0)
                    .setInterpolator(new BounceInterpolator())
                    .setDuration(DURATON)
                    .setListener(null)
                    .start();
              })
              .withEndAction(() -> {
                if (rotationUnit == 0) return;
                dice.animate()
                    .rotation(getRotationValue())
                    .setInterpolator(new OvershootInterpolator())
                    .setDuration(DURATON_ROTATE)
                    .withStartAction(() -> {
                      if (rotationUnit == 0) return;
                      /**  STEP 4
                       /*   0 : [-1, +1]
                       *    1 : [+1, +1]
                       *    2 : [-1, -1]
                       *    3 : [+1, -1]
                       */

                      viewDots.get(3).setX(viewDots.get(3).getX() + unit);
                      viewDots.get(3).setY(viewDots.get(3).getY() - unit);
                      viewDots.get(3).setAlpha(0);
                      viewDots.get(3).setVisibility(VISIBLE);

                      viewDots.get(3)
                          .animate()
                          .alpha(1)
                          .scaleX(1)
                          .scaleY(1)
                          .setInterpolator(new BounceInterpolator())
                          .setStartDelay(300)
                          .setDuration(DURATON - 300)
                          .setListener(null)
                          .start();

                      viewDots.get(0)
                          .animate()
                          .translationX(-unit)
                          .translationY(unit)
                          .setDuration(DURATON)
                          .setInterpolator(new BounceInterpolator())
                          .withEndAction(() -> {
                            if (rotationUnit == 0) return;
                            dice.animate()
                                .rotation(getRotationValue())
                                .setInterpolator(new OvershootInterpolator())
                                .setDuration(DURATON_ROTATE)
                                .withStartAction(() -> {
                                  if (rotationUnit == 0) return;
                                  /**  STEP 5
                                   /*   0 : [-1, +1]
                                   *    1 : [+1, +1]
                                   *    2 : [-1, -1]
                                   *    3 : [+1, -1]
                                   *    4 : [+0, +0]
                                   */

                                  viewDots.get(4).setAlpha(0);
                                  viewDots.get(4)
                                      .animate()
                                      .alpha(1)
                                      .alpha(1)
                                      .scaleX(1f)
                                      .scaleY(1f)
                                      .setInterpolator(new BounceInterpolator())
                                      .setDuration(DURATON)
                                      .withEndAction(() -> {
                                        if (rotationUnit == 0) return;
                                        /**  STEP 6
                                         /*   0 : [+0, +0]
                                         *    1 : [+0, +0]
                                         *    2 : [+0, +0]
                                         *    3 : [+0, +0]
                                         *    4 : [+0, +0]
                                         */

                                        resetDotsStates();
                                     /*   if (pauseAnim) {

                                        } else {

                                        }*/
                                        new Handler().postDelayed(this::animateDots, 1000);
                                        //new Handler().postDelayed(() -> step6Anim(true), 1000);
                                      })
                                      .setListener(null)
                                      .start();
                                })
                                .setListener(null)
                                .start();
                          })
                          .setListener(null)
                          .start();
                    })
                    .setListener(null)
                    .start();
              })
              .setListener(null)
              .start();
        });
  }

  private void animateStepNextLayout() {
    dicebg.animate().translationX(-screenUtils.dpToPx(30)).setListener(null).start();
    txtNext.animate().alpha(1).translationX(screenUtils.dpToPx(50)).setListener(null).start();
    new Handler().postDelayed(() -> {
      step6Anim(false);
      showLabel(false);
    }, 500);
    GradientDrawable drawable = (GradientDrawable) dice.getBackground();
    drawable.setCornerRadius(screenUtils.dpToPx(15));
    dice.setEnabled(true);
  }

  private void initDots() {
    for (int i = 0; i < NB_VIEWS; i++) {
      View v = new View(getContext());
      v.setBackground(drawablesDots[i]);
      if (i != 0) {
        v.setScaleX(0);
        v.setScaleY(0);
      }
      FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(sizeDot, sizeDot);
      lp.gravity = Gravity.CENTER;
      v.setLayoutParams(lp);
      viewDots.add(v);
    }

    for (int i = 0; i < NB_VIEWS; i++) {
      dicebg.addView(viewDots.get(NB_VIEWS - i - 1));
    }
  }
}
