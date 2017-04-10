package com.tribe.app.presentation.view.widget.notifications;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.activity.LiveActivity;
import com.tribe.app.presentation.view.listener.AnimationListenerAdapter;
import com.tribe.app.presentation.view.widget.TextViewFont;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by madaaflak on 06/04/2017.
 */

public class NotificationContainerView extends FrameLayout {

  public static final String DISPLAY_RATING_NOTIFICATON = "DISPLAY_RATING_NOTIFICATON";
  public static final String DISPLAY_CREATE_GRP_NOTIF = "DISPLAY_CREATE_FRP_NOTIF";

  private final static int START_OFFSET_DURATION = 500;
  private final static int BACKGROUND_ANIM_DURATION = 1500;
  private final static int NOTIF_DURATION = 800;

  @BindView(R.id.ratingNotificationView) RatingNotificationView ratingNotificationView;
  @BindView(R.id.notificationView) NotificationView notificationView;
  @BindView(R.id.bgView) View bgView;
  @BindView(R.id.container) FrameLayout container;
  @BindView(R.id.txtDismiss) TextViewFont textDismiss;

  // VARIABLES
  private LayoutInflater inflater;
  private Unbinder unbinder;
  private GestureDetectorCompat gestureScanner;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public NotificationContainerView(@NonNull Context context) {
    super(context);
    initView(context);
  }

  public NotificationContainerView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(context);
  }

  private void initView(Context context) {
    initDependencyInjector();
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_notification_container, this, true);

    unbinder = ButterKnife.bind(this);

    subscriptions.add(notificationView.onHideNotification().subscribe(aVoid -> {
      hideView();
    }));

    container.setOnTouchListener(new OnTouchListener() {
      @Override public boolean onTouch(View v, MotionEvent event) {
        return gestureScanner.onTouchEvent(event);
      }
    });
    gestureScanner = new GestureDetectorCompat(getContext(), new TapGestureListener());
  }

  public void displayNotification(Intent data) {
    boolean notifIsCreated = notificationView.createNotif(data);
    setVisibility(VISIBLE);
    if (notifIsCreated) {
      animateView();
    } else if (data.getBooleanExtra(DISPLAY_RATING_NOTIFICATON, false)) {
      textDismiss.setVisibility(INVISIBLE);
      long timeout = data.getLongExtra(LiveActivity.TIMEOUT_RATING_NOTIFICATON, 0);
      String roomId = data.getStringExtra(LiveActivity.ROOM_ID);
      ratingNotificationView.displayView(timeout, roomId);
    }
  }

  public void animateView() {
    bgView.animate().setDuration(BACKGROUND_ANIM_DURATION).alpha(1f).start();
    notificationView.setVisibility(VISIBLE);
    Animation slideInAnimation =
        AnimationUtils.loadAnimation(getContext(), R.anim.alerter_slide_in_from_top);
    slideInAnimation.setFillAfter(false);
    slideInAnimation.setStartOffset(START_OFFSET_DURATION);
    slideInAnimation.setDuration(NOTIF_DURATION);
    slideInAnimation.setAnimationListener(new AnimationListenerAdapter() {
      @Override public void onAnimationEnd(Animation animation) {
        super.onAnimationEnd(animation);
        textDismiss.setVisibility(VISIBLE);
      }
    });
    notificationView.startAnimation(slideInAnimation);
  }

  ///////////////////
  //     PRIVATE   //
  ///////////////////

  protected void hideView() {
    textDismiss.setVisibility(INVISIBLE);
    Animation slideInAnimation =
        AnimationUtils.loadAnimation(getContext(), R.anim.alerter_slide_in_to_down);
    setAnimation(slideInAnimation);
    slideInAnimation.setFillAfter(false);
    slideInAnimation.setDuration(NOTIF_DURATION);
    slideInAnimation.setAnimationListener(new AnimationListenerAdapter() {

      @Override public void onAnimationEnd(Animation animation) {
        super.onAnimationEnd(animation);
        clearAnimation();
        bgView.animate().setDuration(NOTIF_DURATION).alpha(0f).start();
        setVisibility(GONE);
      }
    });
    notificationView.startAnimation(slideInAnimation);
  }

  protected void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) getContext()));
  }

  ///////////////////
  //  GESTURE  IMP //
  ///////////////////

  private class TapGestureListener implements GestureDetector.OnGestureListener {

    @Override public boolean onDown(MotionEvent e) {
      hideView();
      return true;
    }

    @Override public void onShowPress(MotionEvent e) {

    }

    @Override public boolean onSingleTapUp(MotionEvent e) {
      return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
      return false;
    }

    @Override public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      return false;
    }
  }
}
