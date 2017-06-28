package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
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
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.listener.AnimationListenerAdapter;
import com.tribe.app.presentation.view.utils.ResizeAnimation;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Created by madaaflak on 16/06/2017.
 */

public class DiceView extends FrameLayout {

  public final static int TYPE_FROM_GRID = 0;
  public final static int TYPE_FROM_TILE = 1;
  public final static int TYPE_FROM_ROOM = 2;
  public final static int TYPE_FROM_FB_NOTIF = 3;

  private final static int DURATON = 900;
  private final static float SCALE_DOTS_STEP6 = 0.7f;
  private final static int DURATON_ROTATE = 300;
  private final static int STEP6_DURATION = 0;
  private final static int NB_VIEWS = 6;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.diceView) FrameLayout dice;
  @BindView(R.id.dotsContainer) FrameLayout dotsContainer;
  @BindView(R.id.txtNext) TextViewFont txtNext;
  @BindView(R.id.txtLabel) TextViewFont label;
  @BindView(R.id.diceBgView) FrameLayout bgView;

  // VARIABLES
  private LayoutInflater inflater;
  private Unbinder unbinder;
  private int sizeDot, dotsMargin, type, unit, rotationUnit = 90, sizeDice;
  private List<View> viewDots = new ArrayList<>();
  private Drawable[] drawablesDots = new Drawable[] {
      ContextCompat.getDrawable(getContext(), R.drawable.dice_dot1),
      ContextCompat.getDrawable(getContext(), R.drawable.dice_dot2),
      ContextCompat.getDrawable(getContext(), R.drawable.dice_dot3),
      ContextCompat.getDrawable(getContext(), R.drawable.dice_dot4),
      ContextCompat.getDrawable(getContext(), R.drawable.dice_dot5),
      ContextCompat.getDrawable(getContext(), R.drawable.dice_dot6)
  };

  private PublishSubject<Void> onNextDiceClick = PublishSubject.create();

  public DiceView(@NonNull Context context) {
    super(context);
    initView(context);
  }

  public DiceView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DiceView);
    type = a.getInt(R.styleable.DiceView_diceType, TYPE_FROM_GRID);
    a.recycle();

    initView(context);
  }

  //////////////
  //  PUBLIC  //
  //////////////

  public void setNextAnimation() {
    if (dice.getWidth() > (2 * sizeDice) || dice.getHeight() > (2 * sizeDice)) return;
    stopRotation();
    resetDotsStates();
    setAlphaBackground(0f);
    new Handler().postDelayed(() -> {
      ResizeAnimation a = new ResizeAnimation(dice);
      a.setDuration(500);
      a.setInterpolator(new OvershootInterpolator());
      a.setAnimationListener(new AnimationListenerAdapter() {
        @Override public void onAnimationStart(Animation animation) {
          super.onAnimationStart(animation);
          animateStepNextLayout();
          GradientDrawable drawable = (GradientDrawable) dice.getBackground();
          drawable.setCornerRadius(screenUtils.dpToPx(400));
        }
      });
      if ((dice.getRotation() % 180) == 0) {
        a.setParams(sizeDice, (int) (sizeDice * 2.5) + 1, sizeDice, sizeDice);
      } else {
        a.setParams(sizeDice, sizeDice + 1, sizeDice, (int) (sizeDice * 2.5));
      }
      dice.startAnimation(a);
    }, 1000);
  }

  @OnClick(R.id.diceView) public void onNextClick() {
    startDiceAnimation();
    onNextDiceClick.onNext(null);
  }

  public void startDiceAnimation() {
    dice.setEnabled(false);
    setAlphaBackground(1f);
    if (type == TYPE_FROM_ROOM) {
      reduceDice();
    } else {
      dice.animate().scaleX(1).scaleY(1).setDuration(1000).withStartAction(() -> {
        resetDotsStates();
        restartRotation();
        showLabel(true);
        dotsContainer.animate().translationX(0).setListener(null).start();
        txtNext.animate().alpha(0).translationX(0).setListener(null).start();
        new Handler().postDelayed(this::animateDots, 1000);
      });
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

    switch (type) {
      case TYPE_FROM_GRID:
        bgView.setVisibility(GONE);
        label.setVisibility(GONE);
        dice.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.dice_top_bar_bg));
        Timber.d("dice from grid");
        break;
      case TYPE_FROM_TILE:
        bgView.setVisibility(GONE);
        label.setVisibility(GONE);
        dice.setScaleY(1);
        dice.setScaleX(1);
        dice.setBackground(
            ContextCompat.getDrawable(getContext(), R.drawable.shape_rect_white_corner));
        Timber.d("dice from tile view");
        break;
      case TYPE_FROM_ROOM:
        bgView.setVisibility(VISIBLE);
        label.setVisibility(VISIBLE);
        sizeDice = screenUtils.dpToPx(60);
        setDiceSize(sizeDice);
        showLabel(true);
        Timber.d("dice from room");
        break;
      case TYPE_FROM_FB_NOTIF:
        bgView.setVisibility(GONE);
        label.setVisibility(GONE);
        sizeDice = screenUtils.dpToPx(70);
        setDiceSize(sizeDice);
        showLabel(false);
        dice.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_dice_fb));
        Timber.d("dice from fb notif");
    }

    getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

      @Override public void onGlobalLayout() {
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
        sizeDot = (int) (0.13 * dice.getWidth());
        dotsMargin = (int) (sizeDot / 9.75);
        unit = (int) (sizeDot * 1.18);

        initDots();
        startDiceAnimation();
      }
    });
  }

  private void reduceDice() {
    ResizeAnimation a = new ResizeAnimation(dice);
    a.setDuration(500);
    a.setInterpolator(new OvershootInterpolator());
    a.setAnimationListener(new AnimationListenerAdapter() {
      @Override public void onAnimationStart(Animation animation) {
        super.onAnimationStart(animation);
        resetDotsStates();
        restartRotation();
        showLabel(true);
        dotsContainer.animate().translationX(0).setListener(null).start();
        txtNext.animate().alpha(0).translationX(0).setListener(null).start();
        if (dice.getBackground() instanceof GradientDrawable) {
          GradientDrawable drawable = (GradientDrawable) dice.getBackground();
          drawable.setCornerRadius(screenUtils.dpToPx(13));
        }
        new Handler().postDelayed(() -> animateDots(), 1000);
      }

      @Override public void onAnimationEnd(Animation animation) {
        super.onAnimationEnd(animation);
      }
    });
    a.setParams(dice.getWidth(), sizeDice + 1, dice.getHeight(), sizeDice);
    dice.startAnimation(a);
  }

  private void setDiceSize(int size) {
    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size + 1, size);
    params.gravity = Gravity.CENTER;
    dice.setLayoutParams(params);
  }

  private void showLabel(boolean showLabel) {
    if (showLabel) {
      label.setAlpha(0);
      label.animate().alpha(1).setDuration(500).setListener(null).start();
    } else {
      label.animate().alpha(0).setDuration(500).setListener(null).start();
    }
  }

  private void setAlphaBackground(float alpha) {
    bgView.animate().alpha(alpha).setDuration(1000).setListener(null).start();
  }

  private void resetDotsStates() {
    dotsContainer.animate().scaleX(1).scaleY(1).setListener(null).start();

    for (int i = 0; i < viewDots.size(); i++) {
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
            .scaleX(1)
            .scaleY(1)
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
        .setInterpolator(new OvershootInterpolator())
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
        .setInterpolator(new OvershootInterpolator())
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
        .setInterpolator(new OvershootInterpolator())
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
        .setInterpolator(new OvershootInterpolator())
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
        .setInterpolator(new OvershootInterpolator())
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
        .setInterpolator(new OvershootInterpolator())
        .setStartDelay(STEP6_DURATION)
        .setDuration(DURATON / 2)
        .setListener(null)
        .start();

    dotsContainer.animate()
        .scaleX(SCALE_DOTS_STEP6)
        .scaleY(SCALE_DOTS_STEP6)
        .setListener(null)
        .start();

    if (rotationUnit == 0) return;
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
      }, 100);
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
    if (rotationUnit == 0) return;
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
              .setInterpolator(new OvershootInterpolator())
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
              .setInterpolator(new OvershootInterpolator())
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
                    .setInterpolator(new OvershootInterpolator())
                    .setDuration(DURATON - 300)
                    .setListener(null)
                    .start();

                viewDots.get(0)
                    .animate()
                    .translationX(0)
                    .translationY(0)
                    .setInterpolator(new OvershootInterpolator())
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
                          .setInterpolator(new OvershootInterpolator())
                          .setStartDelay(300)
                          .setDuration(DURATON - 300)
                          .setListener(null)
                          .start();

                      viewDots.get(0)
                          .animate()
                          .translationX(-unit)
                          .translationY(unit)
                          .setDuration(DURATON)
                          .setInterpolator(new OvershootInterpolator())
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
                                      .setInterpolator(new OvershootInterpolator())
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
                                        new Handler().postDelayed(this::animateDots, 1000);
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
    int layoutSize = (sizeDot * 5) + txtNext.getWidth();
    int dotsContainerOffset = -(layoutSize / 2) + ((sizeDot * 5) / 2);
    int txtNextOffset = -(layoutSize / 2) + (sizeDot * 5) + (txtNext.getWidth() / 2);
    dotsContainer.animate()
        .translationX(dotsContainerOffset - screenUtils.dpToPx(5))
        .setListener(null)
        .start();
    txtNext.animate()
        .alpha(1)
        .translationX(txtNextOffset + screenUtils.dpToPx(3))
        .setListener(null)
        .start();
    new Handler().postDelayed(() -> {
      step6Anim(false);
      showLabel(false);
    }, 500);
    GradientDrawable drawable = (GradientDrawable) dice.getBackground();
    drawable.setCornerRadius(screenUtils.dpToPx(10));
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
      dotsContainer.addView(viewDots.get(NB_VIEWS - i - 1));
    }
  }

  public Observable<Void> onNextDiceClick() {
    return onNextDiceClick;
  }
}
