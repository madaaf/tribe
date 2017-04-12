package com.tribe.app.presentation.view.widget.notifications;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.utils.preferences.MinutesOfCalls;
import com.tribe.app.presentation.utils.preferences.NumberOfCalls;
import com.tribe.tribelivesdk.model.TribeGuest;
import java.util.ArrayList;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

import static com.tribe.app.presentation.view.widget.notifications.NotificationContainerView.DISPLAY_CREATE_GRP_NOTIF;

/**
 * Created by madaaflak on 10/04/2017.
 */

public class NotificationView extends FrameLayout {
  public final static int TYPE_NORMAL = 0;

  // VARIABLES
  private LayoutInflater inflater;
  private View viewToDisplay = null;
  private Unbinder unbinder;
  private Context context;
  private int type;

  @Inject @NumberOfCalls Preference<Integer> numberOfCalls;

  @Inject @MinutesOfCalls Preference<Float> minutesOfCalls;

  @BindView(R.id.containerNotif) FrameLayout containerNotif;

  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Void> onHideNotification = PublishSubject.create();

  private EnjoyingTribeNotificationView enjoyingTribeView;
  private CreateGroupNotificationView createGrpNotifView;

  public NotificationView(@NonNull Context context) {
    super(context);
    initView(context, null);
  }

  public NotificationView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(context, attrs);
  }

  private void initView(Context context, AttributeSet attrs) {
    this.context = context;
    initDependencyInjector();
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_bg_notification, this, true);
    unbinder = ButterKnife.bind(this);

    LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    containerNotif.setLayoutParams(params);
    initViews();
    initViewSubscription();

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NotificationView);
    type = a.getInt(R.styleable.NotificationView_notifType, TYPE_NORMAL);
  }

  private void initViewSubscription() {
    subscriptions.add(enjoyingTribeView.onHideNotification().subscribe(aVoid -> {
      onHideNotification.onNext(null);
    }));

    subscriptions.add(createGrpNotifView.onHideNotification().subscribe(aVoid -> {
      onHideNotification.onNext(null);
    }));
  }

  private void initViews() {
    enjoyingTribeView = new EnjoyingTribeNotificationView(context);
    createGrpNotifView = new CreateGroupNotificationView(context);
  }

  public boolean createNotif(Intent data) {
    viewToDisplay = getViewFromIntent(data);
    if (viewToDisplay != null) addViewInContainer(viewToDisplay);
    return (viewToDisplay != null);
  }

  private void addViewInContainer(View v) {
    containerNotif.removeAllViews();
    if (v.getParent() != null) {
      ((ViewGroup) v.getParent()).removeView(v);
    }
    containerNotif.addView(v);
  }

  /////////////////
  //   PRIVATE   //
  /////////////////

  private View getViewFromIntent(Intent data) {
    Bundle extra = data.getExtras();
    boolean displayCreateGrpNotifView = data.getBooleanExtra(DISPLAY_CREATE_GRP_NOTIF, false);
    boolean displayEnjoyingTribeView = false;
    if (numberOfCalls.get() > EnjoyingTribeNotificationView.MIN_USER_CALL_COUNT
        && minutesOfCalls.get() > EnjoyingTribeNotificationView.MIN_USER_CALL_MINUTES) {
      displayEnjoyingTribeView = true;
      numberOfCalls.set(0);
      minutesOfCalls.set(0f);
    }

    if (displayCreateGrpNotifView && extra != null) {
      ArrayList<TribeGuest> members = (ArrayList<TribeGuest>) extra.getSerializable(
          CreateGroupNotificationView.PREFILLED_GRP_MEMBERS);
      viewToDisplay = (CreateGroupNotificationView) createGrpNotifView;
      createGrpNotifView.setMembers(members);
    } else if (displayEnjoyingTribeView) {
      viewToDisplay = (EnjoyingTribeNotificationView) enjoyingTribeView;
    }
    return viewToDisplay;
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<Void> onHideNotification() {
    return onHideNotification;
  }

  private void initDependencyInjector() {
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
}
