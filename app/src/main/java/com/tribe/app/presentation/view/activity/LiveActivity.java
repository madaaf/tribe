package com.tribe.app.presentation.view.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.f2prateek.rx.preferences.Preference;
import com.jenzz.appstate.AppStateListener;
import com.jenzz.appstate.AppStateMonitor;
import com.jenzz.appstate.RxAppStateMonitor;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.RoomConfiguration;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.LivePresenter;
import com.tribe.app.presentation.mvp.view.LiveMVPView;
import com.tribe.app.presentation.service.BroadcastUtils;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.PermissionUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerConstants;
import com.tribe.app.presentation.utils.preferences.RoutingMode;
import com.tribe.app.presentation.view.component.TileView;
import com.tribe.app.presentation.view.component.live.LiveContainer;
import com.tribe.app.presentation.view.component.live.LiveInviteView;
import com.tribe.app.presentation.view.component.live.LiveView;
import com.tribe.app.presentation.view.notification.NotificationPayload;
import com.tribe.app.presentation.view.notification.NotificationUtils;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.app.presentation.view.utils.StateManager;
import com.tribe.app.presentation.view.widget.LiveNotificationContainer;
import com.tribe.app.presentation.view.widget.LiveNotificationView;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.stream.TribeAudioManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static android.view.View.VISIBLE;

public class LiveActivity extends BaseActivity implements LiveMVPView, AppStateListener {

  private static final String EXTRA_RECIPIENT = "EXTRA_RECIPIENT";
  private static final String EXTRA_RECIPIENT_ID = "EXTRA_RECIPIENT_ID";
  private static final String EXTRA_IS_GROUP = "EXTRA_IS_GROUP";
  private static final String EXTRA_SESSION_ID = "EXTRA_SESSION_ID";
  private static final String EXTRA_COLOR = "EXTRA_COLOR";
  private final int MAX_DURATION_WAITING_LIVE = 8;

  public static Intent getCallingIntent(Context context, Recipient recipient, int color) {
    Intent intent = new Intent(context, LiveActivity.class);

    if (recipient instanceof Invite) {
      intent.putExtra(EXTRA_RECIPIENT, recipient);
    }

    intent.putExtra(EXTRA_RECIPIENT_ID,
        recipient.getSubId()); // We pass the userId for a friendship or the groupId
    intent.putExtra(EXTRA_IS_GROUP, recipient instanceof Membership);
    intent.putExtra(EXTRA_COLOR, color);
    return intent;
  }

  public static Intent getCallingIntent(Context context, String recipientId, boolean isGroup,
      String sessionId) {
    Intent intent = new Intent(context, LiveActivity.class);
    intent.putExtra(EXTRA_RECIPIENT_ID, recipientId);
    intent.putExtra(EXTRA_IS_GROUP, isGroup);
    intent.putExtra(EXTRA_SESSION_ID, sessionId);
    return intent;
  }

  @Inject SoundManager soundManager;

  @Inject ScreenUtils screenUtils;

  @Inject LivePresenter livePresenter;

  @Inject StateManager stateManager;

  @Inject @RoutingMode Preference<String> routingMode;

  @BindView(R.id.viewLive) LiveView viewLive;

  @BindView(R.id.viewInviteLive) LiveInviteView viewInviteLive;

  @BindView(R.id.viewLiveContainer) LiveContainer viewLiveContainer;

  @BindView(R.id.layoutNotifications) LiveNotificationContainer layoutNotifications;

  @BindView(R.id.remotePeerAdded) TextViewFont txtRemotePeerAdded;

  // VARIABLES
  private TribeAudioManager audioManager;
  private Unbinder unbinder;
  private String recipientId;
  private boolean isGroup;
  private String sessionId;
  private Recipient recipient;
  private int color;
  private NotificationReceiver notificationReceiver;
  private boolean receiverRegistered = false;
  private AppStateMonitor appStateMonitor;

  // RESOURCES
  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_live);

    unbinder = ButterKnife.bind(this);

    initDependencyInjector();
    initParams();
    init();
    initResources();
    initPermissions();
    initAppState();
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
    viewLive.onDestroy();

    appStateMonitor.removeListener(this);
    appStateMonitor.stop();

    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    if (audioManager != null) {
      audioManager.stop();
      audioManager = null;
    }

    if (unbinder != null) unbinder.unbind();
    if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    super.onDestroy();
  }

  private void initParams() {
    if (getIntent().hasExtra(EXTRA_RECIPIENT)) {
      recipient = (Recipient) getIntent().getSerializableExtra(EXTRA_RECIPIENT);
    } else {
      recipientId = getIntent().getStringExtra(EXTRA_RECIPIENT_ID);
      sessionId = getIntent().getStringExtra(EXTRA_SESSION_ID);
      isGroup = getIntent().getBooleanExtra(EXTRA_IS_GROUP, false);
      color = getIntent().getIntExtra(EXTRA_COLOR, Color.BLACK);
    }
  }

  private void init() {
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    viewLiveContainer.setEnabled(false);

    ViewGroup.LayoutParams params = viewInviteLive.getLayoutParams();
    params.width = screenUtils.dpToPx(LiveInviteView.WIDTH);
    viewInviteLive.setLayoutParams(params);
    viewInviteLive.requestLayout();

    if (recipient == null) {
      livePresenter.loadRecipient(recipientId, isGroup);
    } else {
      onRecipientInfos(recipient);
    }

    audioManager = TribeAudioManager.create(this);
    audioManager.start((audioDevice, availableAudioDevices) -> {

    });
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

  private void initAppState() {
    appStateMonitor = RxAppStateMonitor.create(getApplication());
    appStateMonitor.addListener(this);
    appStateMonitor.start();
  }

  private void displayStartFirstPopupTutorial() {
    if (stateManager.shouldDisplay(StateManager.START_FIRST_LIVE)) {
      subscriptions.add(DialogFactory.dialog(this,
          EmojiParser.demojizedText((getString(R.string.tips_startfirstlive_title))),
          getString(R.string.tips_startfirstlive_message),
          getString(R.string.tips_startfirstlive_action1), null)
          .filter(x -> x == true)
          .subscribe(a -> {
            Observable.timer(MAX_DURATION_WAITING_LIVE, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> viewLive.displayWaitLivePopupTutorial());
          }));
      stateManager.addTutorialKey(StateManager.START_FIRST_LIVE);
    }
  }

  private void initSubscriptions() {
    subscriptions.add(viewLive.onShouldJoinRoom().subscribe(shouldJoin -> {
      viewLiveContainer.setEnabled(true);
      tagManager.trackEvent(TagManagerConstants.KPI_Calls_StartedButton);
      joinRoom();
      displayStartFirstPopupTutorial();
    }));

    subscriptions.add(viewLive.onNotify().subscribe(aVoid -> {
      if (viewLive.getRoom() != null && viewLive.getRoom().getOptions() != null) {
        tagManager.trackEvent(TagManagerConstants.KPI_Calls_WizzedButton);
        soundManager.playSound(SoundManager.WIZZ, SoundManager.SOUND_MID);
        livePresenter.buzzRoom(viewLive.getRoom().getOptions().getRoomId());
      }
    }));

    subscriptions.add(viewLive.onLeave().subscribe(aVoid -> {
      if (stateManager.shouldDisplay(StateManager.LEAVING_ROOM)) {
        subscriptions.add(DialogFactory.dialog(this,
            EmojiParser.demojizedText(getString(R.string.tips_leavingroom_title)),
            EmojiParser.demojizedText(getString(R.string.tips_leavingroom_message)),
            getString(R.string.tips_leavingroom_action1),
            getString(R.string.tips_leavingroom_action2)).filter(x -> x == true).subscribe(a -> {
          tagManager.trackEvent(TagManagerConstants.KPI_Calls_LeaveButton);
          finish();
        }));
        stateManager.addTutorialKey(StateManager.LEAVING_ROOM);
      } else {
        tagManager.trackEvent(TagManagerConstants.KPI_Calls_LeaveButton);
        finish();
      }
    }));

    subscriptions.add(
        viewLiveContainer.onDropped().map(TileView::getRecipient).subscribe(recipient -> {
          invite(recipient.getSubId());
        }));

    subscriptions.add(viewInviteLive.onInviteLiveClick().subscribe(view -> {
      tagManager.trackEvent(TagManagerConstants.KPI_Calls_LinkButton);
      navigator.openSmsForInvite(this);
    }));

    subscriptions.add(viewLive.onNotificationRemotePeerInvited().subscribe(userName -> {
      displayNotification(getString(R.string.live_notification_peer_added, userName));
    }));

    subscriptions.add(viewLive.onNotificationonRemotePeerRemoved().subscribe(userName -> {
      displayNotification(getString(R.string.live_notification_peer_left, userName));
    }));

    subscriptions.add(viewLive.onNotificationRemoteWaiting().subscribe(userName -> {
      displayNotification(getString(R.string.live_notification_peer_joining, userName));
    }));

    subscriptions.add(viewLive.onNotificationRemoteJoined().subscribe(userName -> {
      displayNotification(getString(R.string.live_notification_peer_joined, userName));
    }));

    subscriptions.add(viewLive.onNotificationonRemotePeerBuzzed().subscribe(aVoid -> {
      displayNotification(getString(R.string.live_notification_buzzed));
    }));
  }

  private void displayNotification(String txt) {
    txtRemotePeerAdded.setText(txt);
    txtRemotePeerAdded.setVisibility(VISIBLE);
    Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_up_down_up);
    txtRemotePeerAdded.startAnimation(anim);
  }

  private void joinRoom() {
    soundManager.playSound(SoundManager.WAITING_FRIEND, SoundManager.SOUND_MID);
    livePresenter.joinRoom(recipient,
        recipient instanceof Invite ? ((Invite) recipient).getRoomId() : sessionId);
  }

  private void invite(String userId) {
    Bundle bundle = new Bundle();
    bundle.putBoolean(TagManagerConstants.Swipe, true);
    tagManager.trackEvent(TagManagerConstants.KPI_Calls_InviteAction, bundle);
    livePresenter.inviteUserToRoom(viewLive.getRoom().getOptions().getRoomId(), userId);
  }

  @Override public void finish() {
    super.finish();
    overridePendingTransition(R.anim.activity_in_scale, R.anim.activity_out_to_right);
  }

  ////////////////
  //   PUBLIC   //
  ////////////////

  @Override public void onRecipientInfos(Recipient recipient) {
    this.recipient = recipient;
    viewLive.setRecipient(recipient, color);
    initSubscriptions();
    livePresenter.loadFriendshipList();
  }

  @Override public void renderFriendshipList(List<Friendship> friendshipList) {
    Collections.sort(friendshipList, (lhs, rhs) -> Recipient.nullSafeComparator(lhs, rhs));
    if (recipient != null && recipient instanceof Membership) {
      Membership membership = (Membership) recipient;
      List<Friendship> filteredFriendships = new ArrayList<>();

      for (Friendship fr : friendshipList) {
        if (!membership.getGroup().isGroupMember(fr.getFriend().getId()) && !fr.getFriend()
            .equals(recipientId)) {
          filteredFriendships.add(fr);
        }
      }
      viewInviteLive.renderFriendshipList(filteredFriendships);
    } else {

      List<Friendship> filteredFriendships = new ArrayList<>();
      if (recipient instanceof Friendship) {
        for (Friendship fr : friendshipList) {
          Friendship friendship = (Friendship) recipient;
          if (!friendship.getFriend().getId().equals(fr.getFriend().getId())) {
            filteredFriendships.add(fr);
          }
        }
      }
      viewInviteLive.renderFriendshipList(filteredFriendships);
    }
  }

  @Override public void onJoinedRoom(RoomConfiguration roomConfiguration) {
    Timber.d("Room configuration : " + roomConfiguration);
    roomConfiguration.setRoutingMode(routingMode.get());
    viewLive.joinRoom(roomConfiguration);
    if (recipient instanceof Friendship) {
      livePresenter.inviteUserToRoom(roomConfiguration.getRoomId(), recipient.getSubId());
    }
  }

  @Override public void onAppDidEnterForeground() {
    Timber.d("Enter foreground");
    if (viewLive != null) viewLive.setCameraEnabled(true);
  }

  @Override public void onAppDidEnterBackground() {
    Timber.d("Enter background");
    if (viewLive != null) viewLive.setCameraEnabled(false);
  }

  /////////////////
  //  BROADCAST  //
  /////////////////

  class NotificationReceiver extends BroadcastReceiver {

    @Override public void onReceive(Context context, Intent intent) {
      if (!layoutNotifications.isExpanded()) { // TODO CHANGE THIS WITH A QUEUE
        NotificationPayload notificationPayload =
            (NotificationPayload) intent.getSerializableExtra(BroadcastUtils.NOTIFICATION_PAYLOAD);

        if (recipient != null && recipient instanceof Membership) {
          Membership membership = (Membership) recipient;
          notificationPayload.setShouldDisplayDrag(
              !membership.getGroup().isGroupMember(notificationPayload.getUserId()));
        }

        LiveNotificationView notificationView =
            NotificationUtils.getNotificationViewFromPayload(context, notificationPayload);

        if (notificationView != null) {
          subscriptions.add(notificationView.onClickAction()
              .doOnNext(action -> layoutNotifications.dismissNotification(notificationView))
              .delay(500, TimeUnit.MILLISECONDS)
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(action -> {
                if (action.getIntent() != null) {
                  navigator.navigateToIntent(LiveActivity.this, action.getIntent());
                  finish();
                } else if (action.getId().equals(NotificationUtils.ACTION_ADD_AS_GUEST)) {
                  TribeGuest tribeGuest = new TribeGuest(notificationPayload.getUserId(),
                      notificationPayload.getUserDisplayName(),
                      notificationPayload.getUserPicture(), false, null);
                  invite(tribeGuest.getId());
                  viewLive.addTribeGuest(tribeGuest);
                }
              }));

          notificationView.show(LiveActivity.this, layoutNotifications);
        }
      }
    }
  }
}

