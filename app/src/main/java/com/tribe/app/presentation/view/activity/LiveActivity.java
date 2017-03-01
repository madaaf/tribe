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
import com.tribe.app.domain.entity.Live;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.RoomConfiguration;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.LivePresenter;
import com.tribe.app.presentation.mvp.view.LiveMVPView;
import com.tribe.app.presentation.service.BroadcastUtils;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.PermissionUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.utils.preferences.RoutingMode;
import com.tribe.app.presentation.view.component.TileView;
import com.tribe.app.presentation.view.component.live.LiveContainer;
import com.tribe.app.presentation.view.component.live.LiveInviteView;
import com.tribe.app.presentation.view.component.live.LiveView;
import com.tribe.app.presentation.view.notification.Alerter;
import com.tribe.app.presentation.view.notification.NotificationPayload;
import com.tribe.app.presentation.view.notification.NotificationUtils;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.app.presentation.view.utils.StateManager;
import com.tribe.app.presentation.view.widget.LiveNotificationView;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.stream.TribeAudioManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

import static android.view.View.VISIBLE;

public class LiveActivity extends BaseActivity implements LiveMVPView, AppStateListener {

  private static final String EXTRA_LIVE = "EXTRA_LIVE";
  private final int MAX_DURATION_WAITING_LIVE = 8;

  public static Intent getCallingIntent(Context context, Recipient recipient, int color) {
    Intent intent = new Intent(context, LiveActivity.class);

    Live.Builder builder = new Live.Builder(recipient.getId(), recipient.getSubId()).color(color)
        .displayName(recipient.getDisplayName())
        .isGroup(recipient.isGroup())
        .picture(recipient.getProfilePicture());

    if (recipient instanceof Invite) {
      Invite invite = (Invite) recipient;
      if (invite.getGroup() != null) {
        builder.memberList(invite.getGroup().getMembers());
      }
      builder.sessionId(invite.getRoomId());
    } else if (recipient instanceof Membership) {
      Membership membership = (Membership) recipient;
      builder.memberList(membership.getGroup().getMembers());
    }

    intent.putExtra(EXTRA_LIVE, builder.build());
    return intent;
  }

  public static Intent getCallingIntent(Context context, String recipientId, boolean isGroup,
      String picture, String name, String sessionId) {
    Intent intent = new Intent(context, LiveActivity.class);

    Live live = new Live.Builder(recipientId, recipientId).displayName(name)
        .isGroup(isGroup)
        .picture(picture)
        .sessionId(sessionId)
        .build();
    intent.putExtra(EXTRA_LIVE, live);

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

  @BindView(R.id.remotePeerAdded) TextViewFont txtRemotePeerAdded;

  // VARIABLES
  private TribeAudioManager audioManager;
  private Unbinder unbinder;
  private Live live;
  private NotificationReceiver notificationReceiver;
  private boolean receiverRegistered = false;
  private AppStateMonitor appStateMonitor;

  // RESOURCES

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<List<Friendship>> onUpdateFriendshipList = PublishSubject.create();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_live);

    unbinder = ButterKnife.bind(this);

    initDependencyInjector();
    initParams(getIntent());
    init();
    initResources();
    initPermissions();
    initAppState();
  }

  @Override protected void onNewIntent(Intent intent) {
    viewLive.onDestroy(true);
    viewLive.jump();
    initParams(intent);
    live.setCountdown(false);
    initRoom();
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
    viewLive.onDestroy(false);

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

  private void initParams(Intent intent) {
    if (intent.hasExtra(EXTRA_LIVE)) {
      live = (Live) intent.getSerializableExtra(EXTRA_LIVE);
    }

    if (live.getColor() == 0 || live.getColor() == Color.BLACK) {
      live.setColor(PaletteGrid.getRandomColorExcluding(Color.BLACK));
    }
  }

  private void init() {
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    initRoom();

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

  private void initRoom() {
    viewLiveContainer.setEnabled(false);

    ViewGroup.LayoutParams params = viewInviteLive.getLayoutParams();
    params.width = screenUtils.dpToPx(LiveInviteView.WIDTH);
    viewInviteLive.setLayoutParams(params);
    viewInviteLive.requestLayout();
    viewLive.start(live);

    if (live.isGroup()) {
      livePresenter.loadRecipient(live);
    } else {
      ready();
    }
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
            subscriptions.add(Observable.timer(MAX_DURATION_WAITING_LIVE, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> viewLive.displayWaitLivePopupTutorial()));
          }));
      stateManager.addTutorialKey(StateManager.START_FIRST_LIVE);
    }
  }

  private void initSubscriptions() {
    subscriptions.add(Observable.combineLatest(onUpdateFriendshipList,
        viewLive.onLiveChanged().startWith(new HashMap<>()),
        viewLive.onInvitesChanged().startWith(new HashMap<>()),
        (friendshipList, liveMap, invitesMap) -> {
          List<String> idsToFilter = new ArrayList<>();
          idsToFilter.addAll(liveMap.keySet());
          idsToFilter.addAll(invitesMap.keySet());

          Collections.sort(friendshipList, (lhs, rhs) -> Recipient.nullSafeComparator(lhs, rhs));

          List<Friendship> filteredFriendships = new ArrayList<>();

          if (live.getMembers() != null) {
            for (Friendship fr : friendshipList) {
              if (!live.isGroupMember(fr.getFriend().getId()) && !fr.getFriend()
                  .equals(live.getSubId()) && !idsToFilter.contains(fr.getFriend().getId())) {
                filteredFriendships.add(fr);
              }
            }
          } else {
            for (Friendship fr : friendshipList) {
              if (!live.getSubId().equals(fr.getFriend().getId()) && !idsToFilter.contains(
                  fr.getFriend().getId())) {
                filteredFriendships.add(fr);
              }
            }
          }

          return filteredFriendships;
        })
        .delay(500, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            filteredFriendships -> viewInviteLive.renderFriendshipList(filteredFriendships)));

    subscriptions.add(viewLive.onShouldJoinRoom().subscribe(shouldJoin -> {
      viewLiveContainer.setEnabled(true);
      joinRoom();
      displayStartFirstPopupTutorial();
    }));

    subscriptions.add(viewLive.onNotify().subscribe(aVoid -> {
      if (viewLive.getRoom() != null && viewLive.getRoom().getOptions() != null) {
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
          finish();
        }));
        stateManager.addTutorialKey(StateManager.LEAVING_ROOM);
      } else {
        finish();
      }
    }));

    subscriptions.add(
        viewLiveContainer.onDropped().map(TileView::getRecipient).subscribe(recipient -> {
          invite(recipient.getSubId());
        }));

    subscriptions.add(viewInviteLive.onInviteLiveClick().subscribe(view -> {
      Bundle bundle = new Bundle();
      bundle.putString(TagManagerUtils.SCREEN, TagManagerUtils.LIVE);
      bundle.putString(TagManagerUtils.ACTION, TagManagerUtils.UNKNOWN);
      tagManager.trackEvent(TagManagerUtils.Invites, bundle);
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
    livePresenter.joinRoom(live);
  }

  private void invite(String userId) {
    Bundle bundle = new Bundle();
    bundle.putBoolean(TagManagerUtils.SWIPE, true);
    livePresenter.inviteUserToRoom(viewLive.getRoom().getOptions().getRoomId(), userId);
  }

  private void ready() {
    viewLive.update(live);
    initSubscriptions();
    livePresenter.loadFriendshipList();
  }

  @Override public void finish() {
    super.finish();
    overridePendingTransition(R.anim.activity_in_scale, R.anim.activity_out_to_right);
  }

  ////////////////
  //   PUBLIC   //
  ////////////////

  @Override public void onRecipientInfos(Recipient recipient) {
    if (recipient instanceof Membership) {
      Membership membership = (Membership) recipient;
      live.setMembers(membership.getGroup().getMembers());
    }

    ready();
  }

  @Override public void renderFriendshipList(List<Friendship> friendshipList) {
    onUpdateFriendshipList.onNext(friendshipList);
  }

  @Override public void onJoinedRoom(RoomConfiguration roomConfiguration) {
    roomConfiguration.setRoutingMode(routingMode.get());
    viewLive.joinRoom(roomConfiguration);
    if (!live.isGroup() && StringUtils.isEmpty(live.getSessionId())) {
      livePresenter.inviteUserToRoom(roomConfiguration.getRoomId(), live.getSubId());
    }
  }

  @Override public void onAppDidEnterForeground() {
    if (viewLive != null) viewLive.setCameraEnabled(true);
  }

  @Override public void onAppDidEnterBackground() {
    if (viewLive != null) viewLive.setCameraEnabled(false);
  }

  /////////////////
  //  BROADCAST  //
  /////////////////

  class NotificationReceiver extends BroadcastReceiver {

    @Override public void onReceive(Context context, Intent intent) {
      NotificationPayload notificationPayload =
          (NotificationPayload) intent.getSerializableExtra(BroadcastUtils.NOTIFICATION_PAYLOAD);

      if (live.getSubId().equals(notificationPayload.getUserId()) || live.getSubId()
          .equals(notificationPayload.getGroupId()) || (live.getSessionId() != null
          && live.getSessionId().equals(notificationPayload.getSessionId()))) {
        return;
      }

      LiveNotificationView liveNotificationView =
          NotificationUtils.getNotificationViewFromPayload(context, notificationPayload);

      if (liveNotificationView != null) {
        subscriptions.add(liveNotificationView.onClickAction()
            .delay(500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(action -> {
              if (action.getIntent() != null) {
                navigator.navigateToIntent(LiveActivity.this, action.getIntent());
              } else if (action.getId().equals(NotificationUtils.ACTION_ADD_AS_GUEST)) {
                TribeGuest tribeGuest = new TribeGuest(notificationPayload.getUserId(),
                    notificationPayload.getUserDisplayName(), notificationPayload.getUserPicture(),
                    false, null);
                invite(tribeGuest.getId());
                viewLive.addTribeGuest(tribeGuest);
              }
            }));

        Alerter.create(LiveActivity.this, liveNotificationView).show();
      }
    }
  }
}

