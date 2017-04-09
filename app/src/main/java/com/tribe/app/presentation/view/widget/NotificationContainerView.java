package com.tribe.app.presentation.view.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
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
import com.tribe.app.presentation.view.activity.LiveActivity;
import com.tribe.tribelivesdk.model.TribeGuest;
import java.util.ArrayList;
import javax.inject.Inject;

/**
 * Created by madaaflak on 06/04/2017.
 */

public class NotificationContainerView extends FrameLayout {
  public static final String DISPLAY_RATING_NOTIFICATON = "DISPLAY_RATING_NOTIFICATON";
  public static final String DISPLAY_CREATE_GROUPE_NOTIFICATION =
      "DISPLAY_CREATE_GROUPE_NOTIFICATION";

  @Inject @NumberOfCalls Preference<Integer> numberOfCalls;

  @Inject @MinutesOfCalls Preference<Float> minutesOfCalls;

  @BindView(R.id.ratingNotificationView) RatingNotificationView ratingNotificationView;

  @BindView(R.id.enjoyingTribeNotificationView) EnjoyingTribeNotificationView
      enjoyingTribeNotificationView;

  @BindView(R.id.createGroupNotificationView) CreateGroupNotificationView
      createGroupNotificationView;

  // VARIABLES
  private LayoutInflater inflater;
  private Unbinder unbinder;

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

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) getContext()));
  }

  public void displayNotification(Intent data) {

    boolean displayRatingNotifView = data.getBooleanExtra(DISPLAY_RATING_NOTIFICATON, false);
    boolean displayCreateGrpNotifView =
        data.getBooleanExtra(DISPLAY_CREATE_GROUPE_NOTIFICATION, false);

    boolean displayEnjoyingTribeView = false;

    if (numberOfCalls.get() > EnjoyingTribeNotificationView.MIN_USER_CALL_COUNT
        && minutesOfCalls.get() > EnjoyingTribeNotificationView.MIN_USER_CALL_MINUTES) {
      displayEnjoyingTribeView = true;
      numberOfCalls.set(0);
      minutesOfCalls.set(0f);
    }

    Bundle extra = data.getExtras();

    if (displayCreateGrpNotifView && extra != null) {
      ArrayList<TribeGuest> members = (ArrayList<TribeGuest>) extra.getSerializable(
          CreateGroupNotificationView.PREFILLED_GRP_MEMBERS);
      createGroupNotificationView.displayView(members);
    } else if (displayEnjoyingTribeView) {
      enjoyingTribeNotificationView.displayView();
    } else if (displayRatingNotifView) {
      long timeout = data.getLongExtra(LiveActivity.TIMEOUT_RATING_NOTIFICATON, 0);
      String roomId = data.getStringExtra(LiveActivity.ROOM_ID);
      ratingNotificationView.displayView(timeout, roomId);
    }
  }
}
