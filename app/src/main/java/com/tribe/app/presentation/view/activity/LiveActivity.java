package com.tribe.app.presentation.view.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.RoomConfiguration;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.LivePresenter;
import com.tribe.app.presentation.mvp.view.LiveMVPView;
import com.tribe.app.presentation.service.BroadcastUtils;
import com.tribe.app.presentation.utils.PermissionUtils;
import com.tribe.app.presentation.view.component.TileView;
import com.tribe.app.presentation.view.component.live.LiveContainer;
import com.tribe.app.presentation.view.component.live.LiveInviteView;
import com.tribe.app.presentation.view.component.live.LiveView;
import com.tribe.app.presentation.view.notification.NotificationPayload;
import com.tribe.app.presentation.view.notification.NotificationUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.LiveNotificationContainer;
import com.tribe.app.presentation.view.widget.LiveNotificationView;
import java.util.List;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class LiveActivity extends BaseActivity implements LiveMVPView {

  private static final String EXTRA_RECIPIENT = "EXTRA_RECIPIENT";
  private static final String EXTRA_COLOR = "EXTRA_COLOR";

  public static Intent getCallingIntent(Context context, Recipient recipient, int color) {
    Intent intent = new Intent(context, LiveActivity.class);
    intent.putExtra(EXTRA_RECIPIENT, recipient);
    intent.putExtra(EXTRA_COLOR, color);
    return intent;
  }

  @Inject ScreenUtils screenUtils;

  @Inject LivePresenter livePresenter;

  @BindView(R.id.viewLive) LiveView viewLive;

  @BindView(R.id.viewInviteLive) LiveInviteView viewInviteLive;

  @BindView(R.id.viewLiveContainer) LiveContainer viewLiveContainer;

  @BindView(R.id.layoutNotifications) LiveNotificationContainer layoutNotifications;

  // VARIABLES
  private Unbinder unbinder;
  private Recipient recipient;
  private int color;
  private NotificationReceiver notificationReceiver;
  private boolean receiverRegistered = false;

  // RESOURCES
  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_live);

    unbinder = ButterKnife.bind(this);

    initDependencyInjector();
    if (getIntent().hasExtra(EXTRA_RECIPIENT)) initParams();
    init();
    initResources();
    initPermissions();
    initSubscriptions();
  }

  @Override protected void onStart() {
    super.onStart();
    livePresenter.onViewAttached(this);
  }

  @Override protected void onStop() {
    livePresenter.onViewDetached();
    super.onStop();
  }

  @Override protected void onResume() {
    super.onResume();

    if (!receiverRegistered) {
      if (notificationReceiver == null) notificationReceiver = new NotificationReceiver();

      registerReceiver(notificationReceiver,
          new IntentFilter(BroadcastUtils.BROADCAST_NOTIFICATIONS));
      receiverRegistered = true;
    }
  }

  @Override protected void onPause() {
    if (receiverRegistered) {
      unregisterReceiver(notificationReceiver);
      receiverRegistered = false;
    }

    super.onPause();
  }

  @Override protected void onDestroy() {
    if (unbinder != null) unbinder.unbind();
    if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    super.onDestroy();
  }

  private void initParams() {
    this.recipient = (Recipient) getIntent().getSerializableExtra(EXTRA_RECIPIENT);
    this.color = getIntent().getIntExtra(EXTRA_COLOR, Color.BLACK);
  }

  private void init() {
    ViewGroup.LayoutParams params = viewInviteLive.getLayoutParams();
    params.width = screenUtils.getWidthPx() / 3;
    viewInviteLive.setLayoutParams(params);
    viewInviteLive.requestLayout();

    viewLive.setRecipient(recipient, color);
  }

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .applicationComponent(getApplicationComponent())
        .activityModule(getActivityModule())
        .build()
        .inject(this);
  }

  private void initResources() {
    int result = 0;
    int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");

    if (resourceId > 0) {
      result = getResources().getDimensionPixelSize(resourceId);
    }

    viewLiveContainer.setStatusBarHeight(result);
  }

  private void initPermissions() {
    subscriptions.add(RxPermissions.getInstance(LiveActivity.this)
        .request(PermissionUtils.PERMISSIONS_LIVE)
        .subscribe(granted -> {

        }));
  }

  private void initSubscriptions() {
    subscriptions.add(viewLive.onShouldJoinRoom().subscribe(shouldJoin -> {
      if (shouldJoin) {
        livePresenter.joinRoom(recipient);
      } else {
        finish();
      }
    }));

    subscriptions.add(viewLive.onNotify().subscribe(aVoid -> {
      livePresenter.buzzRoom(viewLive.getRoom().getOptions().getRoomId());
    }));

    subscriptions.add(
        viewLiveContainer.onDropped().map(TileView::getRecipient).subscribe(recipient -> {
          livePresenter.inviteUserToRoom(viewLive.getRoom().getOptions().getRoomId(),
              recipient.getSubId());
        }));
  }

  @Override public void finish() {
    super.finish();
    overridePendingTransition(R.anim.activity_in_scale, R.anim.activity_out_to_right);
  }

  ////////////////
  //   PUBLIC   //
  ////////////////

  @Override public void renderFriendshipList(List<Friendship> friendshipList) {
    viewInviteLive.renderFriendshipList(friendshipList);
  }

  @Override public void onJoinedRoom(RoomConfiguration roomConfiguration) {
    Timber.d("Room configuration : " + roomConfiguration);
    viewLive.joinRoom(roomConfiguration);
    livePresenter.inviteUserToRoom(roomConfiguration.getRoomId(), recipient.getSubId());
  }

  /////////////////
  //  BROADCAST  //
  /////////////////

  class NotificationReceiver extends BroadcastReceiver {

    @Override public void onReceive(Context context, Intent intent) {
      if (!layoutNotifications.isExpanded()) { // TODO CHANGE THIS WITH A QUEUE
        NotificationPayload notificationPayload =
            (NotificationPayload) intent.getSerializableExtra(BroadcastUtils.NOTIFICATION_PAYLOAD);

        LiveNotificationView notificationView =
            NotificationUtils.getNotificationViewFromPayload(context, getCurrentUser(), notificationPayload);

        if (notificationView != null) {
          subscriptions.add(notificationView.onClickAction().subscribe(s -> {
            layoutNotifications.dismissNotification(notificationView);
          }));

          notificationView.show(LiveActivity.this, layoutNotifications);
        }
      }
    }
  }
}