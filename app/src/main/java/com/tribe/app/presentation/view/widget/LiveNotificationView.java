package com.tribe.app.presentation.view.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.app.presentation.view.widget.avatar.AvatarLiveView;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class LiveNotificationView extends LinearLayout {

  @IntDef({ LIVE, ERROR }) public @interface LiveNotificationType {
  }

  public static final int LIVE = 0;
  public static final int ERROR = 1;

  private static final int TIME_TO_DISMISS = 5000;
  private static final int DURATION = 1000;
  private static final float OVERSHOOT = 0.25f;
  private static final int INVALID_POINTER = -1;
  private static final SpringConfig ORIGAMI_SPRING_CONFIG =
      SpringConfig.fromBouncinessAndSpeed(5f, 20f);

  @Inject SoundManager soundManager;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.txtTitle) TextViewFont txtTitle;

  @Nullable @BindView(R.id.avatar) AvatarLiveView avatarLiveView;

  @Nullable @BindView(R.id.imgIcon) ImageView imgIcon;

  // SPRINGS
  private SpringSystem springSystem = null;
  private Spring springHeight;
  private HeightSpringListener springHeightListener;

  // RESOURCES
  private int minHeight = 0, maxHeight = 0, thresholdOpen = 0, margin = 0;

  // VARIABLES
  private String id;
  private @LiveNotificationType int type;
  private Unbinder unbinder;
  private boolean expandable;
  private FrameLayout.LayoutParams params;
  private int currentHeight;
  private float startX, startY;
  private int activePointerId;
  private VelocityTracker velocityTracker;
  private int touchSlop;
  private boolean expanded = false;
  private int sound = -1;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<LiveNotificationView> onAnimationDone = PublishSubject.create();
  private PublishSubject<Float> onExpandChange = PublishSubject.create();
  private PublishSubject<Void> onExpanded = PublishSubject.create();
  private PublishSubject<Void> onDismissed = PublishSubject.create();
  private PublishSubject<LiveNotificationActionView.Action> onClickAction = PublishSubject.create();
  private Subscription timerToDismiss;

  private LiveNotificationView(Context context, @LiveNotificationType int type) {
    super(context);
    this.type = type;
    init(context, null);
  }

  private LiveNotificationView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    setTimerToDismiss();
    springHeight.addListener(springHeightListener);
  }

  @Override protected void onDetachedFromWindow() {
    springHeight.removeAllListeners();
    if (subscriptions.hasSubscriptions()) subscriptions.clear();
    clearTimerToDismiss();
    super.onDetachedFromWindow();
  }

  private void initResources() {
    minHeight = getResources().getDimensionPixelSize(R.dimen.live_notification_min_height);
    maxHeight = getResources().getDimensionPixelSize(R.dimen.live_notification_max_height);
    thresholdOpen = getResources().getDimensionPixelSize(R.dimen.threshold_open_notification);
    margin = getContext().getResources().getDimensionPixelSize(R.dimen.horizontal_margin_xsmall);
  }

  public static class Builder {

    private final Context context;
    private String imgUrl;
    private String title;
    private boolean expandable = true;
    private List<LiveNotificationActionView.Action> actionList;
    private @LiveNotificationType int type;
    private int sound;

    public Builder(Context context, @LiveNotificationType int type) {
      this.context = context;
      this.type = type;
      this.actionList = new ArrayList<>();
    }

    public Builder imgUrl(String imgUrl) {
      this.imgUrl = imgUrl;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder expandable(boolean expandable) {
      this.expandable = expandable;
      return this;
    }

    public Builder addAction(String id, String title, Intent intent) {
      this.actionList.add(new LiveNotificationActionView.Action(id, title, intent));
      return this;
    }

    public Builder sound(int sound) {
      this.sound = sound;
      return this;
    }

    public LiveNotificationView build() {
      LiveNotificationView view = new LiveNotificationView(context, type);
      view.setImgUrl(imgUrl);
      view.setTitle(title);
      view.setSound(sound);

      if (type != ERROR) {
        view.setExpandable(expandable);

        int count = 0;

        for (LiveNotificationActionView.Action action : actionList) {
          view.addAction(action, (count == (actionList.size() - 1)));

          count++;
        }
      } else {
        view.setExpandable(false);
      }

      return view;
    }
  }

  private void init(Context context, AttributeSet attrs) {
    id = UUID.randomUUID().toString();

    initResources();
    initUI();

    LayoutInflater inflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(
        type == LIVE ? R.layout.view_live_notification : R.layout.view_live_notification_error,
        this, true);

    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);

    unbinder = ButterKnife.bind(this);

    setOrientation(VERTICAL);
    setClickable(false);
    setMinimumHeight(minHeight);
    setBackgroundResource(
        type == LIVE ? R.drawable.bg_notifications_live : R.drawable.bg_notifications_live_error);
  }

  private void initUI() {
    springSystem = SpringSystem.create();
    springHeight = springSystem.createSpring();
    springHeight.setSpringConfig(ORIGAMI_SPRING_CONFIG);
    springHeightListener = new HeightSpringListener();
    touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
  }

  public String getViewId() {
    return id;
  }

  public void setTitle(String title) {
    txtTitle.setText(title);
  }

  public void setSound(int sound) {
    this.sound = sound;
  }

  public void setImgUrl(String url) {
    if (type == ERROR) {
      imgIcon.setImageResource(R.drawable.picto_lock);
    } else {
      avatarLiveView.load(url);
    }
  }

  public void setExpandable(boolean expandable) {
    this.expandable = expandable;
  }

  @SuppressLint("NewApi")
  public void addAction(LiveNotificationActionView.Action action, boolean isLast) {
    int sizeActionItem =
        getResources().getDimensionPixelSize(R.dimen.live_notification_item_height);

    LiveNotificationActionView actionView =
        new LiveNotificationActionView.Builder(getContext(), action).isLast(isLast).build();

    ViewGroup.LayoutParams params =
        new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, sizeActionItem);

    addView(actionView, params);

    maxHeight = minHeight + (getChildCount() - 1) * sizeActionItem;

    subscriptions.add(actionView.onClick().subscribe(onClickAction));
  }

  public void show(Activity activity, LiveNotificationContainer container) {
    if (getParent() == null) {
      params = (FrameLayout.LayoutParams) getLayoutParams();

      if (params == null) {
        params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, minHeight);
        params.setMargins(margin, margin, margin, margin);
      }

      setLayoutParams(params);

      if (activity == null || activity.isFinishing()) {
        return;
      }

      container.addView(this, params);
      container.addSubscriptionNotification(onExpandChange, this);
      container.addSubscriptionExpanded(onExpanded);
      container.addSubscriptionDismissed(onDismissed);
      container.addSubscriptionAnimationDone(onAnimationDone);
    }

    setTranslationY(-screenUtils.getHeightPx());
    requestLayout();

    getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override public void onGlobalLayout() {
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
        // TODO specify custom animation when there's more time ^^

        animate().translationY(0)
            .setInterpolator(new OvershootInterpolator(OVERSHOOT))
            .setDuration(DURATION)
            .setListener(new AnimatorListenerAdapter() {
              @Override public void onAnimationEnd(Animator animation) {
                if (sound != -1) soundManager.playSound(sound, SoundManager.SOUND_MAX);
                onAnimationDone.onNext(LiveNotificationView.this);
                onAnimationDone.onCompleted();
                animate().setListener(null).start();
              }
            })
            .start();
      }
    });
  }

  public void cancelTimer() {
    clearTimerToDismiss();
  }

  public void dismiss() {
    animate().translationY(-screenUtils.getHeightPx() >> 1)
        .setDuration(DURATION)
        .setListener(new AnimatorListenerAdapter() {
          @Override public void onAnimationEnd(Animator animation) {
            onDismissed.onNext(null);
            onDismissed.onCompleted();
          }
        })
        .start();
  }

  private void setTimerToDismiss() {
    timerToDismiss = Observable.timer(TIME_TO_DISMISS, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          if (!expanded) dismiss();
        });
  }

  private void clearTimerToDismiss() {
    if (timerToDismiss != null) {
      timerToDismiss.unsubscribe();
      timerToDismiss = null;
    }
  }

  /////////////
  //  TOUCH  //
  /////////////

  @Override public boolean onTouchEvent(MotionEvent event) {
    int action = event.getAction();

    if (expanded || !expandable) return false;

    final int location[] = { 0, 0 };
    getLocationOnScreen(location);

    switch (action & MotionEvent.ACTION_MASK) {
      case MotionEvent.ACTION_DOWN: {
        activePointerId = event.getPointerId(0);

        startY = event.getRawY();

        velocityTracker = VelocityTracker.obtain();
        velocityTracker.addMovement(event);

        clearTimerToDismiss();

        break;
      }

      case MotionEvent.ACTION_MOVE: {
        final int pointerIndex = event.findPointerIndex(activePointerId);

        if (pointerIndex != INVALID_POINTER) {
          float y = event.getY(pointerIndex) + location[1];
          float offsetY = y - startY;
          currentHeight = Math.max(Math.min(minHeight + (int) offsetY, maxHeight), minHeight);
          changeHeight(currentHeight);
          velocityTracker.addMovement(event);
        }

        break;
      }

      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL: {
        final int pointerIndex = event.findPointerIndex(activePointerId);

        if (pointerIndex != INVALID_POINTER && velocityTracker != null) {
          velocityTracker.addMovement(event);
          velocityTracker.computeCurrentVelocity(1000);

          float y = event.getY(pointerIndex) + location[1];
          float offsetY = y - startY;

          if (offsetY >= 0) {
            springHeight.setCurrentValue(currentHeight).setAtRest();

            if (offsetY > thresholdOpen) {
              openNotification();
            } else {
              setTimerToDismiss();
              springHeight.setVelocity(velocityTracker.getYVelocity()).setEndValue(minHeight);
            }
          }
        }

        break;
      }
    }

    return true;
  }

  private class HeightSpringListener extends SimpleSpringListener {

    @Override public void onSpringUpdate(Spring spring) {
      if (ViewCompat.isAttachedToWindow(LiveNotificationView.this)) {
        int value = (int) spring.getCurrentValue();
        changeHeight(value);
      }
    }
  }

  private void openNotification() {
    onExpanded.onNext(null);
    onExpanded.onCompleted();
    expanded = true;
    springHeight.setEndValue(maxHeight);
  }

  private void changeHeight(int value) {
    onExpandChange.onNext((float) (value - minHeight) / (maxHeight - minHeight));
    UIUtils.changeHeightOfView(this, value);
  }

  ///////////////////////
  //    OBSERVABLES    //
  ///////////////////////

  public Observable<LiveNotificationActionView.Action> onClickAction() {
    return onClickAction;
  }
}
