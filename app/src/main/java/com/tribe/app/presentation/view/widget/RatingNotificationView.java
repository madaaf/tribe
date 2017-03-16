package com.tribe.app.presentation.view.widget;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.BindView;
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
import java.util.Calendar;
import javax.inject.Inject;

/**
 * Created by madaaflak on 14/03/2017.
 */

public class RatingNotificationView extends FrameLayout implements View.OnClickListener {

  private static int DURATION = 300;
  private static int indexStar;
  private static String roomId;
  private static long timeout;

  @Inject ScreenUtils screenUtils;
  @Inject TagManager tagManager;

  @BindView(R.id.btnStar1) ImageView btnStart1;
  @BindView(R.id.btnStar2) ImageView btnStart2;
  @BindView(R.id.btnStar3) ImageView btnStart3;
  @BindView(R.id.btnStar4) ImageView btnStart4;
  @BindView(R.id.btnStar5) ImageView btnStart5;
  @BindView(R.id.viewContainer) LinearLayout viewContainer;
  @BindView(R.id.txtAction) TextViewFont txtAction;
  @BindView(R.id.txtTitle) TextViewFont txtTitle;
  @BindView(R.id.starsContainer) LinearLayout starsContainer;

  // VARIABLES
  private LayoutInflater inflater;
  private Unbinder unbinder;
  private CountDownTimer mCountDownTimer;
  private Boolean[] statesStars;

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
    statesStars = new Boolean[6];
    viewContainer.setOnTouchListener(new OnTouchListener() {
      @Override public boolean onTouch(View v, MotionEvent event) {
        return true;
      }
    });

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
    fillStartState(0);
    fillStartsColors(0);
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
        mCountDownTimer.cancel();
      }
    });
    startAnimation(slideInAnimation);
  }

  private void setTimer() {
    mCountDownTimer = new CountDownTimer(timeout * 1000, 1000) {
      @Override public void onTick(long millisUntilFinished) {
      }

      @Override public void onFinish() {
        hideView();
      }
    }.start();
  }

  private void resetTimer() {
    mCountDownTimer.cancel();
    mCountDownTimer.start();
  }

  private void fillStartsColors(int index) {
    if (index == 0) {
      txtAction.setText(getResources().getString(R.string.live_rating_dismiss));
    } else {
      txtAction.setText(getResources().getString(R.string.live_rating_send));
    }
    switch (index) {
      case 0:
        btnStart1.setImageResource(R.drawable.picto_rating_star);
        btnStart2.setImageResource(R.drawable.picto_rating_star);
        btnStart3.setImageResource(R.drawable.picto_rating_star);
        btnStart4.setImageResource(R.drawable.picto_rating_star);
        btnStart5.setImageResource(R.drawable.picto_rating_star);
        break;
      case 1:
        btnStart1.setImageResource(R.drawable.picto_rating_star_red);
        btnStart2.setImageResource(R.drawable.picto_rating_star);
        btnStart3.setImageResource(R.drawable.picto_rating_star);
        btnStart4.setImageResource(R.drawable.picto_rating_star);
        btnStart5.setImageResource(R.drawable.picto_rating_star);

        scaleAnim(btnStart1);

        break;
      case 2:
        btnStart1.setImageResource(R.drawable.picto_rating_star_orange);
        btnStart2.setImageResource(R.drawable.picto_rating_star_orange);

        btnStart3.setImageResource(R.drawable.picto_rating_star);
        btnStart4.setImageResource(R.drawable.picto_rating_star);
        btnStart5.setImageResource(R.drawable.picto_rating_star);

        scaleAnim(btnStart2);
        break;
      case 3:
        btnStart1.setImageResource(R.drawable.picto_rating_star_yellow);
        btnStart2.setImageResource(R.drawable.picto_rating_star_yellow);
        btnStart3.setImageResource(R.drawable.picto_rating_star_yellow);

        btnStart4.setImageResource(R.drawable.picto_rating_star);
        btnStart5.setImageResource(R.drawable.picto_rating_star);

        scaleAnim(btnStart3);
        break;
      case 4:
        btnStart1.setImageResource(R.drawable.picto_rating_star_yellow_light);
        btnStart2.setImageResource(R.drawable.picto_rating_star_yellow_light);
        btnStart3.setImageResource(R.drawable.picto_rating_star_yellow_light);
        btnStart4.setImageResource(R.drawable.picto_rating_star_yellow_light);

        btnStart5.setImageResource(R.drawable.picto_rating_star);

        scaleAnim(btnStart4);
        break;
      case 5:
        btnStart1.setImageResource(R.drawable.picto_rating_star_green);
        btnStart2.setImageResource(R.drawable.picto_rating_star_green);
        btnStart3.setImageResource(R.drawable.picto_rating_star_green);
        btnStart4.setImageResource(R.drawable.picto_rating_star_green);
        btnStart5.setImageResource(R.drawable.picto_rating_star_green);

        scaleAnim(btnStart5);
        break;
    }
  }

  private void scaleAnim(View view) {
    Animation scaleAnimation =
        android.view.animation.AnimationUtils.loadAnimation(getContext(), R.anim.scale_stars);
    view.startAnimation(scaleAnimation);
  }

  private void fillStartState(int index) {
    indexStar = index;
    for (int i = 0; i < statesStars.length; i++) {
      if (i <= index) {
        statesStars[i] = true;
      } else {
        statesStars[i] = false;
      }
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
    private final int MAX_CLICK_DURATION = 400;
    private final int MAX_CLICK_DISTANCE = 5;
    private long startClickTime;
    private float x1;
    private float y1;
    private float x2;
    private float y2;
    private float dx;
    private float dy;

    @Override public boolean onTouch(View view, MotionEvent event) {
      // TODO Auto-generated method stub
      resetTimer();

      switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN: {
          startClickTime = Calendar.getInstance().getTimeInMillis();
          x1 = event.getX();
          y1 = event.getY();
          break;
        }
        case MotionEvent.ACTION_UP: {
          long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
          x2 = event.getX();
          y2 = event.getY();
          dx = x2 - x1;
          dy = y2 - y1;
          /**
           *  ON CLICK TOUCH EVENT
           */
          if (clickDuration < MAX_CLICK_DURATION
              && dx < MAX_CLICK_DISTANCE
              && dy < MAX_CLICK_DISTANCE) {

            if (statesStars[indexStar]) {
              fillStartsColors(0);
              fillStartState(0);
            } else {
              fillStartsColors(indexStar);
              fillStartState(indexStar);
            }
            break;
          }
          fillStartState(indexStar);
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
          fillStartsColors(indexStar);
        }
      }

      return false;
    }
  }
}
