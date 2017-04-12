package com.tribe.app.presentation.view.widget.notifications;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.f2prateek.rx.preferences.Preference;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.utils.PermissionUtils;
import com.tribe.app.presentation.utils.analytics.TagManager;
import com.tribe.app.presentation.utils.preferences.MinutesOfCalls;
import com.tribe.app.presentation.utils.preferences.NumberOfCalls;
import com.tribe.app.presentation.view.listener.AnimationListenerAdapter;
import com.tribe.app.presentation.view.utils.StateManager;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.tribelivesdk.model.TribeGuest;
import java.util.ArrayList;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by madaaflak on 06/04/2017.
 */

public class NotificationContainerView extends FrameLayout {
  public static final String DISPLAY_CREATE_GRP_NOTIF = "DISPLAY_CREATE_FRP_NOTIF";
  public static final String DISPLAY_PERMISSION_NOTIF = "DISPLAY_PERMISSION_NOTIF";

  private final static int START_OFFSET_DURATION = 500;
  private final static int BACKGROUND_ANIM_DURATION = 1500;
  private final static int NOTIF_DURATION = 800;

  @Inject TagManager tagManager;
  @Inject StateManager stateManager;
  @Inject @NumberOfCalls Preference<Integer> numberOfCalls;
  @Inject @MinutesOfCalls Preference<Float> minutesOfCalls;

  @BindView(R.id.notificationView) FrameLayout notificationView;
  @BindView(R.id.bgView) View bgView;
  @BindView(R.id.container) FrameLayout container;
  @BindView(R.id.txtDismiss) TextViewFont textDismiss;

  // VARIABLES
  private LayoutInflater inflater;
  private View viewToDisplay = null;
  private Unbinder unbinder;
  private Context context;
  private GestureDetectorCompat gestureScanner;
  private EnjoyingTribeNotificationView enjoyingTribeView;
  private CreateGroupNotificationView createGrpNotifView;
  private PermissionNotificationView permissionNotifView;

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
    this.context = context;
    initDependencyInjector();
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_notification_container, this, true);
    unbinder = ButterKnife.bind(this);
    initViews();

    container.setOnTouchListener(new OnTouchListener() {
      @Override public boolean onTouch(View v, MotionEvent event) {
        return gestureScanner.onTouchEvent(event);
      }
    });
    gestureScanner = new GestureDetectorCompat(getContext(), new TapGestureListener());
  }

  private void initViews() {
    enjoyingTribeView = new EnjoyingTribeNotificationView(context);
    createGrpNotifView = new CreateGroupNotificationView(context);
    permissionNotifView = new PermissionNotificationView(context);

    subscriptions.add(enjoyingTribeView.onHideNotification().subscribe(aVoid -> {
      hideView();
    }));

    subscriptions.add(createGrpNotifView.onHideNotification().subscribe(aVoid -> {
      hideView();
    }));

    subscriptions.add(permissionNotifView.onHideNotification().subscribe(aVoid -> {
      hideView();
    }));
  }

  public boolean displayNotifFromIntent(Intent data) {
    boolean notifIsCreated = createNotifFromIntent(data);
    if (notifIsCreated) animateView();
    return notifIsCreated;
  }

  @Override protected void onDetachedFromWindow() {
    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.clear();
    clearAnimation();
    super.onDetachedFromWindow();
  }

  public boolean displayPermissionNotification() {
    RxPermissions rxPermissions = new RxPermissions((Activity) getContext());
    if (!PermissionUtils.hasPermissionsCameraOnly(rxPermissions)
        || !PermissionUtils.hasPermissionsMicroOnly(rxPermissions)) {
      viewToDisplay = permissionNotifView;
      addViewInContainer(viewToDisplay);
      animateView();
      return true;
    }
    return false;
  }

  public boolean createNotifFromIntent(Intent data) {
    viewToDisplay = getViewFromIntent(data);
    if (viewToDisplay != null) addViewInContainer(viewToDisplay);
    return (viewToDisplay != null);
  }

  private void addViewInContainer(View v) {
    notificationView.removeAllViews();
    if (v.getParent() != null) {
      ((ViewGroup) v.getParent()).removeView(v);
    }
    notificationView.addView(v);
  }

  ///////////////////
  //     PRIVATE   //
  ///////////////////

  private void animateView() {
    setVisibility(VISIBLE);
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
        notificationView.removeAllViews();
        viewToDisplay = null;
      }
    });
    notificationView.startAnimation(slideInAnimation);
  }

  private View getViewFromIntent(Intent data) {
    Bundle extra = data.getExtras();
    boolean displayEnjoyingTribeView = false;

    if (numberOfCalls.get() > EnjoyingTribeNotificationView.MIN_USER_CALL_COUNT
        && minutesOfCalls.get() > EnjoyingTribeNotificationView.MIN_USER_CALL_MINUTES) {
      displayEnjoyingTribeView = true;
      numberOfCalls.set(0);
      minutesOfCalls.set(0f);
    }

    if (data.getBooleanExtra(DISPLAY_CREATE_GRP_NOTIF, false) && extra != null) {
      ArrayList<TribeGuest> members = (ArrayList<TribeGuest>) extra.getSerializable(
          CreateGroupNotificationView.PREFILLED_GRP_MEMBERS);
      viewToDisplay = createGrpNotifView;
      createGrpNotifView.setMembers(members);
    } else if (displayEnjoyingTribeView) {
      viewToDisplay = enjoyingTribeView;
    }
    return viewToDisplay;
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
