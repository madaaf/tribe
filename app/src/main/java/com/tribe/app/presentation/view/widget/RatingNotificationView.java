package com.tribe.app.presentation.view.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
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
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.analytics.TagManager;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.utils.preferences.CallTagsMap;
import com.tribe.app.presentation.utils.preferences.PreferencesUtils;
import com.tribe.app.presentation.view.listener.AnimationListenerAdapter;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.ViewUtils;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by madaaflak on 14/03/2017.
 */

public class RatingNotificationView extends FrameLayout implements View.OnClickListener {

  private final static int DURATION = 300;
  private final static int DURATION_COLOR = 300;
  private final static int DELAY = 0;
  private final static float OVERSHOOT = 0.5f;
  private final static float SCALE = 1.10f;

  @Inject ScreenUtils screenUtils;
  @Inject TagManager tagManager;
  @Inject @CallTagsMap Preference<String> callTagsMap;

  @BindViews({ R.id.btnStar1, R.id.btnStar2, R.id.btnStar3, R.id.btnStar4, R.id.btnStar5 })
  List<ImageView> btnStars;
  @BindView(R.id.viewContainer) LinearLayout viewContainer;
  @BindView(R.id.txtAction) TextViewFont txtAction;
  @BindView(R.id.txtTitle) TextViewFont txtTitle;
  @BindView(R.id.starsContainer) LinearLayout starsContainer;

  // VARIABLES
  private LayoutInflater inflater;
  private Unbinder unbinder;
  private int[] colorFilters;
  private int indexStar, lastIndexStar = 0, lastIndexStarUp = 0;
  private String roomId;
  private long timeout;

  // OBSERVABLES
  Subscription timerSubscription;

  public RatingNotificationView(Context context) {
    super(context);
    initView(context, null);
  }

  public RatingNotificationView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initView(context, attrs);
  }

  @Override protected void onDetachedFromWindow() {
    if (timerSubscription != null) {
      timerSubscription.unsubscribe();
      timerSubscription = null;
    }
    super.onDetachedFromWindow();
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

    starsContainer.setOnTouchListener(new SeekBarTouchListener());
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
    slideInAnimation.setStartOffset(1000);
    slideInAnimation.setDuration(800);
    setAnimation(slideInAnimation);
    startAnimation(slideInAnimation);
  }

  ///////////////////
  //    PRIVATE    //
  ///////////////////

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  private void hideView() {
    if (timerSubscription != null) timerSubscription.unsubscribe();

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
      }
    });
    startAnimation(slideInAnimation);
  }

  private void setTimer() {
    timerSubscription = Observable.timer(timeout, TimeUnit.SECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          manageTags(PreferencesUtils.getMapFromJson(callTagsMap));
          hideView();
        });
  }

  private void resetTimer() {
    if (timerSubscription != null) timerSubscription.unsubscribe();
    setTimer();
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

  private void manageTags(Map<String, Object> map) {
    if (map != null && map.size() > 0) {
      TagManagerUtils.manageTags(tagManager, map);
      callTagsMap.set("");
    }
  }

  ///////////////////
  //    CLICKS     //
  ///////////////////

  @OnClick(R.id.txtAction) void onClickTextAction() {
    Map<String, Object> map = PreferencesUtils.getMapFromJson(callTagsMap);

    if (indexStar > 0) {
      map.put(TagManagerUtils.RATE, String.valueOf(indexStar));
      map.put(TagManagerUtils.ROOM_ID, roomId);
    }

    manageTags(map);
    hideView();
  }

  @Override public void onClick(View v) {
    manageTags(PreferencesUtils.getMapFromJson(callTagsMap));
    hideView();
  }

  private class SeekBarTouchListener implements OnTouchListener {

    private final int MAX_CLICK_DURATION = 150;
    private final int MAX_CLICK_DISTANCE_X = 15;
    private final int MAX_CLICK_DISTANCE_Y = 30;
    private long startClickTime, maxClickDistanceX, maxClickDistanceY;
    private float x1, y1, x2, y2, dx, dy;
    private int lengthProgress;

    public SeekBarTouchListener() {
      lengthProgress = screenUtils.dpToPx(100);
    }

    @Override public boolean onTouch(View view, MotionEvent event) {
      resetTimer();

      switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN: {
          maxClickDistanceX = screenUtils.dpToPx(MAX_CLICK_DISTANCE_X);
          maxClickDistanceY = screenUtils.dpToPx(MAX_CLICK_DISTANCE_Y);
          startClickTime = System.currentTimeMillis();
          x1 = event.getX();
          y1 = event.getY();

          indexStar = -1;

          for (int i = 0; i < btnStars.size(); i++) {
            if (ViewUtils.isIn(btnStars.get(i), (int) event.getRawX(), (int) event.getRawY())) {
              indexStar = i + 1;
              return true;
            }
          }

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
              && Math.abs(dx) < maxClickDistanceX
              && Math.abs(dy) < maxClickDistanceY
              && indexStar != -1) {
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
          for (int i = 0; i < btnStars.size(); i++) {
            if (isIn(btnStars.get(i), (int) event.getRawX(), (int) event.getRawY())) {
              indexStar = i + 1;
            }
          }

          fillStarsColors(indexStar);
        }
      }

      return false;
    }
  }

  public boolean isIn(View child, int x, int y) {
    int marginX = screenUtils.dpToPx(5);
    int marginY = screenUtils.dpToPx(15);
    int[] location = new int[2];
    child.getLocationOnScreen(location);
    Rect rect = new Rect(location[0] - marginX, location[1] - marginY,
        location[0] + child.getWidth() + marginX, location[1] + child.getHeight() + marginY);
    return rect.contains(x, y);
  }
}
