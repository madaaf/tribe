package com.tribe.app.presentation.view.activity;

import android.content.IntentFilter;
import android.os.Bundle;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.TribeBroadcastReceiver;
import com.tribe.app.presentation.mvp.presenter.common.RoomPresenter;
import com.tribe.app.presentation.mvp.view.adapter.RoomMVPViewAdapter;
import com.tribe.app.presentation.service.BroadcastUtils;
import com.tribe.app.presentation.view.notification.NotificationPayload;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

/**
 * Base {@link android.app.Activity} class for every Activity with in-app notifications in this
 * application.
 */
public abstract class BaseBroadcastReceiverActivity extends BaseActivity {

  @Inject RoomPresenter baseRoomPresenter;

  // VARIABLES
  protected boolean receiverRegistered = false;
  protected TribeBroadcastReceiver notificationReceiver;
  protected RoomMVPViewAdapter roomMVPViewAdapter;
  private NotificationPayload notificationPayload;

  // OBSERVABLES
  protected CompositeSubscription subscriptionsBroadcastReceiver = new CompositeSubscription();

  @Override protected void onStart() {
    super.onStart();
    baseRoomPresenter.onViewAttached(roomMVPViewAdapter);
  }

  @Override protected void onStop() {
    baseRoomPresenter.onViewDetached();
    super.onStop();
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    notificationReceiver = new TribeBroadcastReceiver(this);

    roomMVPViewAdapter = new RoomMVPViewAdapter() {
      @Override public void onRoomInfos(Room room) {
        if (notificationReceiver != null) {
          List<User> userList = room.getAllUsers();
          List<User> endUser = new ArrayList<>();

          for (User user : userList) {
            if (!user.getId().equals(getCurrentUser().getId()) &&
                !user.getId().equals(notificationPayload.getUserId())) {
              endUser.add(user);
            }
          }

          notificationReceiver.displayJoinPopup(BaseBroadcastReceiverActivity.this,
              notificationPayload, endUser);
        }
      }
    };
  }

  @Override protected void onPause() {
    if (receiverRegistered) {
      unregisterReceiver(notificationReceiver);
      subscriptionsBroadcastReceiver.clear();
      receiverRegistered = false;
    }

    super.onPause();
  }

  @Override protected void onDestroy() {
    subscriptionsBroadcastReceiver.clear();
    notificationReceiver.dispose();
    super.onDestroy();
  }

  @Override protected void onResume() {
    super.onResume();

    if (!receiverRegistered) {
      if (subscriptionsBroadcastReceiver != null) subscriptionsBroadcastReceiver.clear();

      subscriptionsBroadcastReceiver.add(notificationReceiver.onDeclineInvitation()
          .subscribe(roomId -> baseRoomPresenter.declineInvite(roomId)));

      subscriptionsBroadcastReceiver.add(
          notificationReceiver.onGetRoomMembersLive().subscribe(notificationPayload -> {
            this.notificationPayload = notificationPayload;
            baseRoomPresenter.getRoomMembers(notificationPayload.getSessionId());
          }));

      registerReceiver(notificationReceiver,
          new IntentFilter(BroadcastUtils.BROADCAST_NOTIFICATIONS));

      receiverRegistered = true;
    }
  }

  public void setWaitingToJoin(String userIdAsk) {
    this.notificationReceiver.setWaitingToJoin(userIdAsk);
  }

  public TribeBroadcastReceiver getBroadcastReceiver() {
    return notificationReceiver;
  }
}
