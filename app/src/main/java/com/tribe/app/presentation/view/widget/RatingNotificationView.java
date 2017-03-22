package com.tribe.app.presentation.view.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.analytics.TagManager;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.view.listener.AnimationListenerAdapter;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by madaaflak on 14/03/2017.
 */

public class RatingNotificationView extends FrameLayout implements View.OnClickListener {

  private final static int DURATION = 300;
  private final static int DURATION_COLOR = 1000;
  private final static int DELAY = 0;
  private final static float OVERSHOOT = 0.5f;
  private final static float SCALE = 1.10f;

  @Inject ScreenUtils screenUtils;
  @Inject TagManager tagManager;

  @BindViews({ R.id.btnStar1, R.id.btnStar2, R.id.btnStar3, R.id.btnStar4, R.id.btnStar5 })
  List<ImageView> btnStars;
  @BindView(R.id.viewContainer) LinearLayout viewContainer;
  @BindView(R.id.txtAction) TextViewFont txtAction;
  @BindView(R.id.txtTitle) TextViewFont txtTitle;
  @BindView(R.id.starsContainer) LinearLayout starsContainer;

  // VARIABLES
  private LayoutInflater inflater;
  private Unbinder unbinder;
  private CountDownTimer countDownTimer;
  private int[] colorFilters;
  private int indexStar, lastIndexStar = 0, lastIndexStarUp = 0;
  private String roomId;
  private long timeout;

  public RatingNotificationView(Context context) {
    super(context);
    initView(context, null);
  }

  public RatingNotificationView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initView(context, attrs);
  }

  private void initView(Context context, AttributeSet attrs) {
    initDependencyInjector();

    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_rating_notification, this, true);

    unbinder = ButterKnife.bind(this);

    txtTitle.setText(EmojiParser.demojizedText(getContext().getString(R.string.live_rating_title)));

    colorFilters = new int[btnStars.size()];
    initColorStars();

    viewContainer.setOnTouchListener((v, event) -> true);

    starsContainer.setOnTouchListener(new seekBarTouchListener());
  }

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) getContext()));
  }

  public void displayView(long timeoutInSeconde, String roomid) {
    roomId = roomid;
    timeout = timeoutInSeconde;
    fillStarsColors(0);
    setTimer();
    setVisibility(VISIBLE);
    setOnClickListener(this);
    Animation slideInAnimation =
        AnimationUtils.loadAnimation(getContext(), R.anim.alerter_slide_in_from_top);
    slideInAnimation.setStartTime(1000);
    setAnimation(slideInAnimation);
    startAnimation(slideInAnimation);
  }

  ///////////////////
  //    PRIVATE     //
  ///////////////////
  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  private void hideView() {
    Animation slideInAnimation =
        AnimationUtils.loadAnimation(getContext(), R.anim.alerter_slide_out_to_top);
    setAnimation(slideInAnimation);
    slideInAnimation.setAnimationListener(new AnimationListenerAdapter() {
      @Override public void onAnimationStart(Animation animation) {
        super.onAnimationStart(animation);
        setClickable(false);
        setOnClickListener(null);
      }

      @Override public void onAnimationEnd(Animation animation) {
        super.onAnimationEnd(animation);
        clearAnimation();
        setVisibility(GONE);
        countDownTimer.cancel();
      }
    });
    startAnimation(slideInAnimation);
  }

  private void setTimer() {
    countDownTimer = new CountDownTimer(timeout * 1000, 1000) {
      @Override public void onTick(long millisUntilFinished) {
      }

      @Override public void onFinish() {
        hideView();
      }
    }.start();
  }

  private void resetTimer() {
    countDownTimer.cancel();
    countDownTimer.start();
  }

  private void fillStarsColors(int index) {
    if (lastIndexStar == index) return;

    if (index == 0) {
      txtAction.setText(getResources().getString(R.string.live_rating_dismiss));
    } else {
      txtAction.setText(getResources().getString(R.string.live_rating_send));
    }

    int color;

    switch (index) {
      case 0:
        for (int i = 0; i < btnStars.size(); i++) {
          color = Color.TRANSPARENT;
          ImageView btnStar = btnStars.get(i);
          btnStar.clearAnimation();
          com.tribe.app.presentation.view.utils.AnimationUtils.animateColorFilter(btnStar,
              colorFilters[i], color, DURATION_COLOR);
          colorFilters[i] = color;
        }

        break;
      case 1:
        color = ContextCompat.getColor(getContext(), R.color.star_red);
        actionForIndexWithColor(index, color);

        break;
      case 2:
        color = ContextCompat.getColor(getContext(), R.color.star_orange);
        actionForIndexWithColor(index, color);

        break;
      case 3:
        color = ContextCompat.getColor(getContext(), R.color.star_yellow);
        actionForIndexWithColor(index, color);

        break;
      case 4:
        color = ContextCompat.getColor(getContext(), R.color.star_yellow_strong);
        actionForIndexWithColor(index, color);

        break;
      case 5:
        color = ContextCompat.getColor(getContext(), R.color.star_green);
        actionForIndexWithColor(index, color);

        break;
    }

    lastIndexStar = index;
  }

  private void actionForIndexWithColor(int index, int color) {
    for (int i = 0; i < index; i++) {
      ImageView btnStar = btnStars.get(i);
      com.tribe.app.presentation.view.utils.AnimationUtils.animateColorFilter(btnStar,
              colorFilters[i], color, DURATION_COLOR);
      colorFilters[i] = color;
    }

    for (int i = index; i < btnStars.size(); i++) {
      ImageView btnStar = btnStars.get(i);
      com.tribe.app.presentation.view.utils.AnimationUtils.animateColorFilter(btnStar,
              colorFilters[i], Color.TRANSPARENT, DURATION_COLOR);
      colorFilters[i] = Color.TRANSPARENT;
    }

    scaleAnim(btnStars.get(index - 1));
  }

  private void scaleAnim(View view) {
    view.clearAnimation();
    view.animate()
        .scaleX(SCALE)
        .scaleY(SCALE)
        .setDuration(DURATION)
        .setInterpolator(new DecelerateInterpolator())
        .setListener(new AnimatorListenerAdapter() {
          @Override public void onAnimationCancel(Animator animation) {
            view.animate().setListener(null).start();
          }

          @Override public void onAnimationEnd(Animator animation) {
            view.animate()
                .scaleX(1)
                .scaleY(1)
                .setDuration(DURATION)
                .setStartDelay(DELAY)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                  @Override public void onAnimationCancel(Animator animation) {
                    view.animate().setListener(null).start();
                  }

                  @Override public void onAnimationEnd(Animator animation) {
                    view.animate().setListener(null).start();
                  }
                })
                .start();
          }
        })
        .start();
  }

  private void initColorStars() {
    for (int i = 0; i < colorFilters.length; i++) {
      colorFilters[i] = Color.TRANSPARENT;
    }
  }

  ///////////////////
  //    CLICKS     //
  ///////////////////

  @OnClick(R.id.txtAction) void onClickTextAction() {
    hideView();
    Bundle properties = new Bundle();
    properties.putString(TagManagerUtils.RATING, String.valueOf(indexStar));
    properties.putString(TagManagerUtils.ROOM_ID, roomId);
    tagManager.trackEvent(TagManagerUtils.CALLS_RATINGS, properties);
  }

  @Override public void onClick(View v) {
    hideView();
  }

  private class seekBarTouchListener implements OnTouchListener {

    private final int MAX_CLICK_DURATION = 150;
    private final int MAX_CLICK_DISTANCE = 15;
    private long startClickTime, maxClickDistance;
    private float x1, y1, x2, y2, dx, dy;

    @Override public boolean onTouch(View view, MotionEvent event) {
      resetTimer();

      switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN: {
          maxClickDistance = screenUtils.dpToPx(MAX_CLICK_DISTANCE);
          startClickTime = System.currentTimeMillis();
          x1 = event.getX();
          y1 = event.getY();
          break;
        }

        case MotionEvent.ACTION_UP: {
          long clickDuration = System.currentTimeMillis() - startClickTime;
          x2 = event.getX();
          y2 = event.getY();
          dx = x2 - x1;
          dy = y2 - y1;
          /**
           *  ON CLICK TOUCH EVENT
           */
          if (clickDuration < MAX_CLICK_DURATION
              && Math.abs(dx) < maxClickDistance
              && Math.abs(dy) < maxClickDistance) {

            if (lastIndexStarUp == indexStar) {
              fillStarsColors(0);
              lastIndexStarUp = 0;
            } else {
              fillStarsColors(indexStar);
              lastIndexStarUp = indexStar;
            }

            break;
          }

          lastIndexStarUp = indexStar;

          break;
        }

        case MotionEvent.ACTION_MOVE: {
          int progress = Math.round(event.getX() / starsContainer.getWidth() * 100);
          int unity = 100 / 5;
          if (progress < unity) {
            indexStar = 1;
          } else if (progress >= unity && progress < 2 * unity) {
            indexStar = 2;
          } else if (progress >= 2 * unity && progress < 3 * unity) {
            indexStar = 3;
          } else if (progress >= 3 * unity && progress < 4 * unity) {
            indexStar = 4;
          } else {
            indexStar = 5;
          }

          fillStarsColors(indexStar);
        }
      }

      return false;
    }
  }
}
