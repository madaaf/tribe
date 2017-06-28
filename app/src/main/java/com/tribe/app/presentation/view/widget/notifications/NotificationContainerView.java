package com.tribe.app.presentation.view.widget.notifications;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.f2prateek.rx.preferences.BuildConfig;
import com.f2prateek.rx.preferences.Preference;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
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
import com.tribe.app.presentation.view.utils.Constants;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.StateManager;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.tribelivesdk.model.TribeGuest;
import java.util.ArrayList;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by madaaflak on 06/04/2017.
 */

public class NotificationContainerView extends FrameLayout {

  @StringDef({
      DISPLAY_CREATE_GRP_NOTIF, DISPLAY_PERMISSION_NOTIF, DISPLAY_ENJOYING_NOTIF,
      DISPLAY_INVITE_NOTIF, DISPLAY_SHARING_NOTIF, DISPLAY_FB_CALL_ROULETTE
  }) public @interface NotifType {
  }

  public static final String DISPLAY_CREATE_GRP_NOTIF = "DISPLAY_CREATE_FRP_NOTIF";
  public static final String DISPLAY_PERMISSION_NOTIF = "DISPLAY_PERMISSION_NOTIF";
  public static final String DISPLAY_ENJOYING_NOTIF = "DISPLAY_ENJOYING_NOTIF";
  public static final String DISPLAY_INVITE_NOTIF = "DISPLAY_INVITE_NOTIF";
  public static final String DISPLAY_SHARING_NOTIF = "DISPLAY_SHARING_NOTIF";
  public static final String DISPLAY_FB_CALL_ROULETTE = "DISPLAY_FB_CALL_ROULETTE";

  private final static int BACKGROUND_ANIM_DURATION_ENTER = 1500;
  private final static int NOTIF_ANIM_DURATION_ENTER = 500;
  private final static int BACKGROUND_ANIM_DURATION_EXIT = 500;

  @Inject TagManager tagManager;
  @Inject StateManager stateManager;
  @Inject ScreenUtils screenUtils;
  @Inject @NumberOfCalls Preference<Integer> numberOfCalls;
  @Inject @MinutesOfCalls Preference<Float> minutesOfCalls;

  @BindView(R.id.notificationView) FrameLayout notificationView;
  @BindView(R.id.bgView) View bgView;
  @BindView(R.id.container) FrameLayout container;
  @BindView(R.id.txtDismiss) TextViewFont textDismiss;

  // VARIABLES
  private LayoutInflater inflater;
  private LifeNotification viewToDisplay;
  private Unbinder unbinder;
  private Context context;
  private GestureDetectorCompat gestureScanner;
  private FirebaseRemoteConfig firebaseRemoteConfig;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Boolean> onAcceptedPermission = PublishSubject.create();
  private PublishSubject<Void> onSendInvitations = PublishSubject.create();
  private PublishSubject<Void> onFacebookSuccess = PublishSubject.create();

  public NotificationContainerView(@NonNull Context context) {
    super(context);
    initView(context);
  }

  public NotificationContainerView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(context);
  }

  ///////////////////
  //     PUBLIC   //
  ///////////////////

  public boolean showNotification(Intent data, @NotifType String type) {
    boolean notifIsDisplayed = false;

    if (type != null) {
      switch (type) {
        case DISPLAY_PERMISSION_NOTIF:
          notifIsDisplayed = displayPermissionNotification();
          break;
        case DISPLAY_INVITE_NOTIF:
          notifIsDisplayed = displayInviteNotification();
          break;
        case DISPLAY_FB_CALL_ROULETTE:
          initRemoteConfig();
          Animation shake = AnimationUtils.loadAnimation(getContext(), R.anim.shake_with_duration);
          container.setOnTouchListener((v, event) -> {
            textDismiss.startAnimation(shake);
            return false;
          });
          textDismiss.setOnTouchListener((v, event) -> {
            hideView();
            return false;
          });
          notifIsDisplayed = displayFbCallRouletteNotification();
      }
    } else if (data != null) {
      notifIsDisplayed = displayNotifFromIntent(data);
    }

    initSubscription();
    return notifIsDisplayed;
  }

  private void initRemoteConfig() {
    firebaseRemoteConfig = firebaseRemoteConfig.getInstance();
    FirebaseRemoteConfigSettings configSettings =
        new FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(BuildConfig.DEBUG)
            .build();
    firebaseRemoteConfig.setConfigSettings(configSettings);
    firebaseRemoteConfig.fetch().addOnCompleteListener(task -> {
      if (task.isSuccessful()) {
        firebaseRemoteConfig.activateFetched();
        textDismiss.setText(
            firebaseRemoteConfig.getString(Constants.wording_unlock_roll_the_dice_decline_action));
      }
    });
  }

  ///////////////////
  //     PRIVATE   //
  ///////////////////

  private boolean displayNotifFromIntent(Intent data) {
    boolean notifIsCreated = createNotifFromIntent(data);
    if (notifIsCreated) animateView();
    return notifIsCreated;
  }

  private boolean displayPermissionNotification() {
    RxPermissions rxPermissions = new RxPermissions((Activity) getContext());
    if (!PermissionUtils.hasPermissionsCameraOnly(rxPermissions)
        || !PermissionUtils.hasPermissionsMicroOnly(rxPermissions)) {
      viewToDisplay = new PermissionNotificationView(context);
      addViewInContainer(viewToDisplay);
      animateView();
      return true;
    }
    return false;
  }

  private boolean displayInviteNotification() {
    viewToDisplay = new InviteNotificationView(context);
    addViewInContainer(viewToDisplay);
    animateView();
    return true;
  }

  private boolean displayFbCallRouletteNotification() {
    viewToDisplay = new FBCallRouletteNotificationView(context);
    addViewInContainer(viewToDisplay);
    animateView();
    return true;
  }

  private void initView(Context context) {
    this.context = context;
    initDependencyInjector();
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_notification_container, this, true);
    unbinder = ButterKnife.bind(this);
    container.setOnTouchListener((v, event) -> gestureScanner.onTouchEvent(event));
    gestureScanner = new GestureDetectorCompat(getContext(), new TapGestureListener());
  }

  private void initSubscription() {
    if (viewToDisplay == null) return;
    subscriptions.add(viewToDisplay.onHideNotification().subscribe(aVoid -> {
      hideView();
    }));

    subscriptions.add(viewToDisplay.onFacebookSuccess().subscribe(aVoid -> {
      onFacebookSuccess.onNext(null);
    }));

    subscriptions.add(viewToDisplay.onAcceptedPermission().subscribe(onAcceptedPermission));

    subscriptions.add(viewToDisplay.onSendInvitations().subscribe(onSendInvitations));
  }

  @Override protected void onDetachedFromWindow() {
    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.clear();
    clearAnimation();
    super.onDetachedFromWindow();
  }

  private boolean createNotifFromIntent(Intent data) {
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

  private void animateView() {
    notificationView.setTranslationY(-screenUtils.getHeightPx());

    setVisibility(VISIBLE);
    bgView.animate().setDuration(BACKGROUND_ANIM_DURATION_ENTER).alpha(1f).start();
    notificationView.animate()
        .setStartDelay(NOTIF_ANIM_DURATION_ENTER)
        .translationY(0f)
        .setDuration(NOTIF_ANIM_DURATION_ENTER)
        .setInterpolator(new OvershootInterpolator(1.15f))
        .setListener(new AnimatorListenerAdapter() {
          @Override public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            textDismiss.setVisibility(VISIBLE);
          }
        })
        .start();
  }

  protected void hideView() {
    textDismiss.setVisibility(INVISIBLE);
    Animation slideOutAnimation =
        AnimationUtils.loadAnimation(getContext(), R.anim.notif_container_exit_animation);
    setAnimation(slideOutAnimation);
    slideOutAnimation.setFillAfter(false);
    slideOutAnimation.setAnimationListener(new AnimationListenerAdapter() {

      @Override public void onAnimationStart(Animation animation) {
        super.onAnimationStart(animation);
        bgView.animate()
            .setDuration(BACKGROUND_ANIM_DURATION_EXIT)
            .alpha(0f)
            .withEndAction(() -> setVisibility(GONE))
            .start();
      }

      @Override public void onAnimationEnd(Animation animation) {
        super.onAnimationEnd(animation);
        clearAnimation();
        notificationView.removeAllViews();
        viewToDisplay = null;
      }
    });
    notificationView.startAnimation(slideOutAnimation);
  }

  private LifeNotification getViewFromIntent(Intent data) {
    Bundle extra = data.getExtras();
    boolean displayEnjoyingTribeView = false;

    if (numberOfCalls.get() >= EnjoyingTribeNotificationView.MIN_USER_CALL_COUNT
        && minutesOfCalls.get() >= EnjoyingTribeNotificationView.MIN_USER_CALL_MINUTES) {
      displayEnjoyingTribeView = true;
      numberOfCalls.set(0);
      minutesOfCalls.set(0f);
    }

    if (data.getBooleanExtra(DISPLAY_CREATE_GRP_NOTIF, false) && extra != null) {
      ArrayList<TribeGuest> members = (ArrayList<TribeGuest>) extra.getSerializable(
          CreateGroupNotificationView.PREFILLED_GRP_MEMBERS);
      viewToDisplay = new CreateGroupNotificationView(context, members);
    } else if (data.getBooleanExtra(DISPLAY_SHARING_NOTIF, false) && extra != null) {
      bgView.setVisibility(INVISIBLE);
      textDismiss.setAlpha(0);
      ArrayList<TribeGuest> members = (ArrayList<TribeGuest>) extra.getSerializable(
          SharingCardNotificationView.CALL_GRP_MEMBERS);
      double durationCall = extra.getDouble(SharingCardNotificationView.DURATION_CALL);
      viewToDisplay = new SharingCardNotificationView(context, members, durationCall);
    } else if (displayEnjoyingTribeView) {
      viewToDisplay = new EnjoyingTribeNotificationView(context);
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

  public Observable<Boolean> onAcceptedPermission() {
    return onAcceptedPermission;
  }

  public Observable<Void> onSendInvitations() {
    return onSendInvitations;
  }

  public Observable<Void> onFacebookSuccess() {
    return onFacebookSuccess;
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
