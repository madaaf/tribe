package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
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
import butterknife.Unbinder;
import com.tribe.app.R;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by madaaflak on 16/06/2017.
 */

public class DiceView extends FrameLayout {

  private final static int DURATON = 900;
  private final static int DURATON_ROTATE = 300;
  private final static int STEP6_DURATION = 0;
  private final static int NB_VIEWS = 6;

  @BindView(R.id.diceView) FrameLayout dice;
  @BindView(R.id.dicebg) FrameLayout dicebg;

  // VARIABLES
  private LayoutInflater inflater;
  private Unbinder unbinder;
  private int sizeDot;
  private int dotsMargin;
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
  }

  private void initView(Context context) {
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_dice, this, true);
    unbinder = ButterKnife.bind(this);
    getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

      @Override public void onGlobalLayout() {
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
        sizeDot = (int) (0.15 * getWidth());
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

  private void step6Anim() {
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

  private int getRotationValue() {
    int rotationDelay = ((int) dice.getRotation()) % 90;
    int fixed = ((int) dice.getRotation() - rotationDelay + 90);
    return fixed;
  }

  private void animateDots() {
    /**  STEP 1 [0, 0] **/
    dice.animate()
        .rotation(getRotationValue())
        .setDuration(DURATON_ROTATE)
        .setStartDelay(DURATON)
        .setInterpolator(new OvershootInterpolator())
        .withStartAction(() -> {
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
          dice.animate()
              .rotation(getRotationValue())
              .setDuration(DURATON_ROTATE)
              .setInterpolator(new OvershootInterpolator())
              .withStartAction(() -> {
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
                dice.animate()
                    .rotation(getRotationValue())
                    .setInterpolator(new OvershootInterpolator())
                    .setDuration(DURATON_ROTATE)
                    .withStartAction(() -> {

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
                            dice.animate()
                                .rotation(getRotationValue())
                                .setInterpolator(new OvershootInterpolator())
                                .setDuration(DURATON_ROTATE)
                                .withStartAction(() -> {

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
                                        /**  STEP 6
                                         /*   0 : [+0, +0]
                                         *    1 : [+0, +0]
                                         *    2 : [+0, +0]
                                         *    3 : [+0, +0]
                                         *    4 : [+0, +0]
                                         */

                                        resetDotsStates();
                                        new Handler().postDelayed(this::step6Anim, 1000);
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

  public void setBackgroundDiceView(Drawable bg) {
    dice.setBackground(bg);
    animateDots();
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
