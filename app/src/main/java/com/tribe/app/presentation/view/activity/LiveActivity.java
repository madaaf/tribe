package com.tribe.app.presentation.view.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.v4.app.NotificationManagerCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.f2prateek.rx.preferences.BuildConfig;
import com.f2prateek.rx.preferences.Preference;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.jenzz.appstate.AppStateListener;
import com.jenzz.appstate.AppStateMonitor;
import com.jenzz.appstate.RxAppStateMonitor;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.R;
import com.tribe.app.data.network.WSService;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.Live;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.RoomConfiguration;
import com.tribe.app.domain.entity.RoomMember;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.exception.ErrorMessageFactory;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.LivePresenter;
import com.tribe.app.presentation.mvp.view.LiveMVPView;
import com.tribe.app.presentation.service.BroadcastUtils;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.PermissionUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.preferences.CallTagsMap;
import com.tribe.app.presentation.utils.preferences.DataChallengesGame;
import com.tribe.app.presentation.utils.preferences.FullscreenNotificationState;
import com.tribe.app.presentation.utils.preferences.PreferencesUtils;
import com.tribe.app.presentation.utils.preferences.RoutingMode;
import com.tribe.app.presentation.view.component.TileView;
import com.tribe.app.presentation.view.component.live.LiveContainer;
import com.tribe.app.presentation.view.component.live.LiveInviteView;
import com.tribe.app.presentation.view.component.live.LiveView;
import com.tribe.app.presentation.view.component.live.ScreenshotView;
import com.tribe.app.presentation.view.notification.Alerter;
import com.tribe.app.presentation.view.notification.NotificationPayload;
import com.tribe.app.presentation.view.notification.NotificationUtils;
import com.tribe.app.presentation.view.utils.Constants;
import com.tribe.app.presentation.view.utils.DeviceUtils;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.MissedCallManager;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.RuntimePermissionUtil;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.app.presentation.view.utils.StateManager;
import com.tribe.app.presentation.view.utils.ViewUtils;
import com.tribe.app.presentation.view.widget.DiceView;
import com.tribe.app.presentation.view.widget.GameDrawView;
import com.tribe.app.presentation.view.widget.LiveNotificationView;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.notifications.CreateGroupNotificationView;
import com.tribe.app.presentation.view.widget.notifications.ErrorNotificationView;
import com.tribe.app.presentation.view.widget.notifications.NotificationContainerView;
import com.tribe.app.presentation.view.widget.notifications.RatingNotificationView;
import com.tribe.app.presentation.view.widget.notifications.SharingCardNotificationView;
import com.tribe.app.presentation.view.widget.notifications.UserInfosNotificationView;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameChallenge;
import com.tribe.tribelivesdk.game.GameDraw;
import com.tribe.tribelivesdk.game.GameManager;
import com.tribe.tribelivesdk.game.GamePostIt;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.model.TribePeerMediaConfiguration;
import com.tribe.tribelivesdk.model.error.WebSocketError;
import com.tribe.tribelivesdk.stream.TribeAudioManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static android.view.View.VISIBLE;

public class LiveActivity extends BaseActivity implements LiveMVPView, AppStateListener {

  @StringDef({
      SOURCE_GRID, SOURCE_DEEPLINK, SOURCE_SEARCH, SOURCE_CALLKIT, SOURCE_SHORTCUT_ITEM,
      SOURCE_DRAGGED_AS_GUEST, SOURCE_ONLINE_NOTIFICATION, SOURCE_LIVE_NOTIFICATION, SOURCE_FRIENDS,
      SOURCE_NEW_CALL, SOURCE_JOIN_LIVE, SOURCE_ADD_PEERS, SOURCE_CALL_ROULETTE
  }) public @interface Source {
  }

  public static final String SOURCE_GRID = "Grid";
  public static final String SOURCE_DEEPLINK = "DeepLink";
  public static final String SOURCE_SEARCH = "Search";
  public static final String SOURCE_CALLKIT = "CallKit";
  public static final String SOURCE_SHORTCUT_ITEM = "ShortcutItem";
  public static final String SOURCE_DRAGGED_AS_GUEST = "DraggedAsGuest";
  public static final String SOURCE_ONLINE_NOTIFICATION = "OnlineNotification";
  public static final String SOURCE_LIVE_NOTIFICATION = "LiveNotification";
  public static final String SOURCE_FRIENDS = "Friends";
  public static final String SOURCE_NEW_CALL = "NewCall";
  public static final String SOURCE_CALL_ROULETTE = "CallRoulette";
  public static final String SOURCE_JOIN_LIVE = "JoinLive";
  public static final String SOURCE_ADD_PEERS = "AddPeers";

  private static final String EXTRA_LIVE = "EXTRA_LIVE";
  public static final String ROOM_ID = "ROOM_ID";

  public static final String TIMEOUT_RATING_NOTIFICATON = "TIMEOUT_RATING_NOTIFICATON";
  public static String UNKNOWN_USER_FROM_DEEPLINK = "UNKNOWN_USER_FROM_DEEPLINK";
  private final int MAX_DURATION_WAITING_LIVE = 8;
  private final int MIN_LIVE_DURATION_TO_DISPLAY_RATING_NOTIF = 30;
  private final int MIN_DURATION_BEFORE_DISPLAY_TUTORIAL_DRAG_GUEST = 3;

  public static Intent getCallingIntent(Context context, Recipient recipient, int color,
      @Source String source) {
    Intent intent = new Intent(context, LiveActivity.class);

    Live.Builder builder = new Live.Builder(recipient.getId(), recipient.getSubId()).color(color)
        .displayName(recipient.getDisplayName())
        .userName(recipient.getUsername())
        .isGroup(recipient.isGroup())
        .countdown(!recipient.isLive())
        .picture(recipient.getProfilePicture())
        .source(source);
    if (recipient instanceof Friendship) {
      String fbId = ((Friendship) recipient).getFriend().getFbid();
      builder.fbId(fbId);
    }
    if (recipient instanceof Invite) {
      Invite invite = (Invite) recipient;
      builder.memberList(invite.getMembers());
      builder.sessionId(invite.getRoomId());
      builder.isInvite(true);
    } else if (recipient instanceof Membership) {
      Membership membership = (Membership) recipient;
      builder.memberList(membership.getGroup().getMembers());
    }

    intent.putExtra(EXTRA_LIVE, builder.build());
    return intent;
  }

  public static Intent getCallingIntent(Context context, String recipientId, boolean isGroup,
      String picture, String name, String sessionId, @Source String source) {
    Intent intent = new Intent(context, LiveActivity.class);

    if (StringUtils.isEmpty(recipientId)) recipientId = Live.WEB;

    Live live = new Live.Builder(recipientId, recipientId).displayName(name)
        .isGroup(isGroup)
        .picture(picture)
        .countdown(StringUtils.isEmpty(sessionId))
        .sessionId(sessionId)
        .intent(true)
        .source(source)
        .build();

    intent.putExtra(EXTRA_LIVE, live);

    return intent;
  }

  public static Intent getCallingIntent(Context context, String linkId, String url,
      @Source String source) {
    Intent intent = new Intent(context, LiveActivity.class);

    Live live = new Live.Builder(Live.WEB, Live.WEB).linkId(linkId).url(url).source(source).build();

    intent.putExtra(EXTRA_LIVE, live);

    return intent;
  }

  public static Intent getCallingIntent(Context context, @Source String source) {
    Intent intent = new Intent(context, LiveActivity.class);

    String linkId = StringUtils.generateLinkId();
    String url = StringUtils.getUrlFromLinkId(context, linkId);

    Live live = new Live.Builder(Live.NEW_CALL, Live.NEW_CALL).linkId(linkId)
        .url(url)
        .source(source)
        .build();

    intent.putExtra(EXTRA_LIVE, live);

    return intent;
  }

  @Inject NotificationManagerCompat notificationManager;

  @Inject SoundManager soundManager;

  @Inject ScreenUtils screenUtils;

  @Inject LivePresenter livePresenter;

  @Inject StateManager stateManager;

  @Inject User user;

  @Inject @RoutingMode Preference<String> routingMode;

  @Inject @CallTagsMap Preference<String> callTagsMap;

  @Inject @FullscreenNotificationState Preference<Set<String>> fullScreenNotificationState;

  @Inject @DataChallengesGame Preference<Set<String>> dataChallengesGames;

  @Inject MissedCallManager missedCallManager;

  @BindView(R.id.viewLive) LiveView viewLive;

  @BindView(R.id.viewInviteLive) LiveInviteView viewInviteLive;

  @BindView(R.id.viewLiveContainer) LiveContainer viewLiveContainer;

  @BindView(R.id.remotePeerAdded) TextViewFont txtRemotePeerAdded;

  @BindView(R.id.userInfosNotificationView) UserInfosNotificationView userInfosNotificationView;

  @BindView(R.id.screenShotView) ScreenshotView screenshotView;

  @BindView(R.id.diceLayoutRoomView) DiceView diceView;

  @BindView(R.id.notificationContainerView) NotificationContainerView notificationContainerView;

  @BindView(R.id.blockView) FrameLayout blockView;

  @BindView(R.id.gameDrawView) GameDrawView gameDrawView;

  // VARIABLES
  private TribeAudioManager audioManager;
  private GameManager gameManager;
  private Unbinder unbinder;
  private Live live;
  private RoomConfiguration roomConfiguration;
  private boolean liveIsInvite = false;
  private NotificationReceiver notificationReceiver;
  private boolean receiverRegistered = false;
  private AppStateMonitor appStateMonitor;
  private boolean liveDurationIsMoreThan30sec = false;
  private FirebaseRemoteConfig firebaseRemoteConfig;
  private RxPermissions rxPermissions;
  private List<String> usersIdsInvitedInLiveRoom = new ArrayList<>();
  private List<String> activeUersIdsInvitedInLiveRoom = new ArrayList<>();
  private Intent returnIntent = new Intent();
  private List anonymousIdList = new ArrayList();
  private boolean finished = false;
  private boolean shouldOverridePendingTransactions = false;
  private List<String> userUnder13List = new ArrayList<>();
  private float initialBrightness = -1;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<List<Friendship>> onUpdateFriendshipList = PublishSubject.create();
  private PublishSubject<List<User>> onAnonymousReceived = PublishSubject.create();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_live);

    unbinder = ButterKnife.bind(this);

    initDependencyInjector();
    initParams(getIntent());
    init();
    initResources();
    initRemoteConfig();
    manageClickNotification(getIntent());
    initAppState();
    initGameManager();
    initBrightness();
  }

  @Override protected void onNewIntent(Intent intent) {
    if (subscriptions.hasSubscriptions()) subscriptions.clear();
    boolean isJump = true;
    manageClickNotification(getIntent());
    viewLive.endCall(isJump);
    viewLive.dispose(isJump);
    viewLive.jump();
    initParams(intent);
    live.setCountdown(false);
    initRoom();
  }

  @Override protected void onStart() {
    super.onStart();
    notificationManager.cancelAll();
    fullScreenNotificationState.set(new HashSet<>());
    livePresenter.onViewAttached(this);
  }

  @Override protected void onStop() {
    livePresenter.onViewDetached();
    super.onStop();
  }

  @Override protected void onResume() {
    super.onResume();

    onResumeLockPhone();

    if (!receiverRegistered) {
      if (notificationReceiver == null) notificationReceiver = new NotificationReceiver();

      registerReceiver(notificationReceiver,
          new IntentFilter(BroadcastUtils.BROADCAST_NOTIFICATIONS));
      receiverRegistered = true;
    }

    if (shouldOverridePendingTransactions) {
      overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
      shouldOverridePendingTransactions = false;
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
    Timber.d("onDestroy");

    resetBrightness();

    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    soundManager.cancelMediaPlayer();

    viewLiveContainer.dispose();
    viewLive.dispose(false);

    gameManager.setCurrentGame(null);

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
    rxPermissions = new RxPermissions(this);

    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    initRoom();

    audioManager = TribeAudioManager.create(this);
    audioManager.start((audioDevice, availableAudioDevices) -> {

    });

    viewLiveContainer.setOnTouchListener((v, event) -> false);
  }

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .applicationComponent(getApplicationComponent())
        .activityModule(getActivityModule())
        .build()
        .inject(this);
  }

  private void initRoom() {
    subscriptions.add(rxPermissions.request(PermissionUtils.PERMISSIONS_LIVE).subscribe(granted -> {
      if (granted) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(TagManagerUtils.USER_CAMERA_ENABLED,
            PermissionUtils.hasPermissionsCamera(rxPermissions));
        bundle.putBoolean(TagManagerUtils.USER_MICROPHONE_ENABLED,
            PermissionUtils.hasPermissionsCamera(rxPermissions));
        tagManager.setProperty(bundle);

        viewLiveContainer.setEnabled(false);

        ViewGroup.LayoutParams params = viewInviteLive.getLayoutParams();
        params.width = screenUtils.dpToPx(LiveInviteView.WIDTH);
        viewInviteLive.setLayoutParams(params);
        viewInviteLive.requestLayout();

        initSubscriptions();

        livePresenter.loadFriendshipList();

        if (live.getSource().equals(LiveActivity.SOURCE_CALL_ROULETTE)) launchCallRoulette();

        if (live.isGroup()) {
          viewLive.start(live);
          livePresenter.loadRecipient(live);
        } else if (live.isSessionOrLink()) {
          viewLive.start(live);
          ready();
        } else if (!live.isGroup()) {
          if (live.isIntent()) {
            livePresenter.loadRecipient(live);
          } else {
            viewLive.start(live);
          }
        }
      } else {
        finish();
      }
    }));
    //livePresenter.fbidUpdated();
  }

  private void initBrightness() {

    int hour = Calendar.getInstance().get(Calendar.HOUR);

    if (hour <= 7 || hour >= 20) {
      WindowManager.LayoutParams attributes = getWindow().getAttributes();
      initialBrightness = attributes.screenBrightness;
      attributes.screenBrightness = 1;

      getWindow().setAttributes(attributes);
    }
  }

  private void resetBrightness() {

    WindowManager.LayoutParams attributes = getWindow().getAttributes();
    if (initialBrightness > 0 && attributes.screenBrightness == 1) {

      attributes.screenBrightness = initialBrightness;
      getWindow().setAttributes(attributes);
    }
  }

  private void initCallRouletteService() {
    viewLive.setSourceLive(SOURCE_CALL_ROULETTE);
    startService(WSService.getCallingIntent(this, WSService.CALL_ROULETTE_TYPE));
    livePresenter.randomRoomAssigned();
  }

  private void stopCallRouletteService() {
    Intent i = new Intent(this, WSService.class);
    stopService(i);
  }

  private void initResources() {
    int result = 0;
    int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");

    if (resourceId > 0) {
      result = getResources().getDimensionPixelSize(resourceId);
    }

    viewLiveContainer.setStatusBarHeight(result);
  }

  private void initAppState() {
    if (appStateMonitor == null) {
      appStateMonitor = RxAppStateMonitor.create(getApplication());
      appStateMonitor.addListener(this);
      appStateMonitor.start();
    }
  }

  private void initGameManager() {
    this.gameManager = GameManager.getInstance(this);
  }

  private void ratingNotificationSubscribe() {
    subscriptions.add(Observable.timer(MIN_LIVE_DURATION_TO_DISPLAY_RATING_NOTIF, TimeUnit.SECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aVoid -> liveDurationIsMoreThan30sec = true));
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
      }
    });
  }

  private void manageClickNotification(Intent intent) {
    if (intent != null && intent.hasExtra(Constants.NOTIFICATION_LIVE)) {
      Bundle bundle = new Bundle();
      bundle.putString(TagManagerUtils.CATEGORY,
          intent.getStringExtra(Constants.NOTIFICATION_LIVE));
      tagManager.trackEvent(TagManagerUtils.Notification_AppOpen, bundle);
    }
  }

  private Boolean displayRatingNotifDependingFirebaseTrigger() {
    Random rn = new Random();
    int randomNumber = rn.nextInt(101);
    return randomNumber > firebaseRemoteConfig.getLong(Constants.FIREBASE_RATING_NOTIF_TRIGGER);
  }

  private Long getFirebaseTimeoutConfig() {
    Long time = firebaseRemoteConfig.getLong(Constants.FIREBASE_RATING_NOTIF_TIMEOUT);
    return time == 0 ? 10 : time;
  }

  private void launchCallRoulette() {
    if (!FacebookUtils.isLoggedIn()) {
      //if (true) {
      Timber.d("not logged on fb ");
      blockView.setVisibility(VISIBLE);
      blockView.setOnTouchListener((v, event) -> true);
      notificationContainerView.
          showNotification(null, NotificationContainerView.DISPLAY_FB_CALL_ROULETTE);
    } else {
      viewLiveContainer.blockOpenInviteView(true);
      initCallRouletteService();
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

          usersIdsInvitedInLiveRoom.addAll(invitesMap.keySet());
          Collections.sort(friendshipList, (lhs, rhs) -> Recipient.nullSafeComparator(lhs, rhs));

          List<Friendship> filteredFriendships = new ArrayList<>();
          liveIsInvite = live.isInvite();
          if (live.getMembers() != null) {
            for (Friendship fr : friendshipList) {
              if (!live.isGroupMember(fr.getFriend().getId()) && !fr.getFriend()
                  .getId()
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
        .subscribe(filteredFriendships -> viewInviteLive.renderFriendshipList(filteredFriendships,
            live.getSource())));

    subscriptions.add(viewLive.onShouldJoinRoom().subscribe(shouldJoin -> {
      viewLiveContainer.setEnabled(true);
      if (StringUtils.isEmpty(live.getLinkId())) displayBuzzPopupTutorial();
      if (!live.getSource().equals(SOURCE_CALL_ROULETTE)) joinRoom();
    }));

    subscriptions.add(viewLive.onNewChallengeReceived()
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(datas -> {//SOEF MADA LOOPER
          Timber.e("SOEF ON NEW CHALLENGE RECEIVED challenfe :"
              + datas.get(0)
              + " peetId :  "
              + datas.get(1));
          List<TribeGuest> guests = viewLive.getUsersInLiveRoom().getPeopleInRoom();
          TribeGuest guestChallenged = new TribeGuest(datas.get(1));
          for (TribeGuest guest : guests) {
            if (guest.getId().equals(datas.get(1))) {
              guestChallenged = guest;
            }
          }
          viewLive.setNextChallengePager(datas.get(0), guestChallenged);
        }));

    subscriptions.add(viewLive.onBlockOpenInviteView().subscribe(blockInviteView -> {
      Timber.e("SOEF BLOCK INVITE VIEW " + blockInviteView);
      viewLiveContainer.blockOpenInviteView(blockInviteView);
    }));

    subscriptions.add(viewLive.onJoined().subscribe(tribeJoinRoom -> {
    }));

    subscriptions.add(viewLive.onNotify().subscribe(aVoid -> {
      if (viewLive.getRoom() != null && viewLive.getRoom().getOptions() != null) {
        soundManager.playSound(SoundManager.WIZZ, SoundManager.SOUND_MID);
        livePresenter.buzzRoom(viewLive.getRoom().getOptions().getRoomId());
      }
    }));

    subscriptions.add(
        viewLive.onLeave().observeOn(AndroidSchedulers.mainThread()).subscribe(aVoid -> leave()));

    subscriptions.add(
        viewLiveContainer.onDropped().map(TileView::getRecipient).subscribe(recipient -> {
          if (recipient.getId().equals(Recipient.ID_CALL_ROULETTE)) {
            Timber.d("on dropped the dice :" + live.getSessionId());
            livePresenter.roomAcceptRandom(live.getSessionId());
            reRollTheDiceFromLiveRoom();
          } else {
            invite(recipient.getSubId());
          }
        }));

    subscriptions.add(viewLiveContainer.onDroppedUnder13().subscribe(peerId -> {
      Timber.d("user under 13 dropped" + peerId);
      userUnder13List.add(peerId);
      viewLive.sendUnlockDice(peerId, user);
    }));

    subscriptions.add(viewLive.unlockRollTheDice()
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(s -> {
          Timber.d("unlockRollTheDice reveived " + s);
          if (!FacebookUtils.isLoggedIn()) {
            notificationContainerView.setUnlockRollTheDiceSenderId(s);
            notificationContainerView.
                showNotification(null, NotificationContainerView.DISPLAY_FB_CALL_ROULETTE);
          }
        }));

    subscriptions.add(viewLive.unlockedRollTheDice()
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(s -> {
          userUnder13List.remove(s);
          if (userUnder13List.isEmpty()) {
            livePresenter.roomAcceptRandom(live.getSessionId());
            diceView.setVisibility(VISIBLE);
            diceView.startDiceAnimation();
          }
        }));

    subscriptions.add(viewLive.onEndCall()
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(duration -> {
          livePresenter.incrementTimeInCall(user.getId(), duration);
        }));

    subscriptions.add(notificationContainerView.onFacebookSuccess()
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(unlockRollTheDiceSenderId -> {
          blockView.setVisibility(View.GONE);
          viewLiveContainer.blockOpenInviteView(false);
          initCallRouletteService();
          viewLive.sendUnlockedDice(unlockRollTheDiceSenderId);
        }));

    subscriptions.add(viewLive.onRollTheDice().subscribe(s -> {
      viewInviteLive.diceDragued();
      viewInviteLive.requestLayout();
    }));

    subscriptions.add(viewLiveContainer.onDropDiceWithoutFbAuth().subscribe(aVoid -> {
      Timber.d("drag dice, user not fb loged, display fb notif auth");
      notificationContainerView.
          showNotification(null, NotificationContainerView.DISPLAY_FB_CALL_ROULETTE);
    }));

    subscriptions.add(
        Observable.merge(viewInviteLive.onInviteLiveClick(), viewLive.onShare()).subscribe(view -> {
          share();
        }));

    subscriptions.add(viewLive.onNotificationRemotePeerInvited()
        .subscribe(userName -> displayNotification(
            getString(R.string.live_notification_peer_added, userName))));

    subscriptions.add(viewLive.onNotificationonRemotePeerRemoved()
        .subscribe(userName -> displayNotification(
            getString(R.string.live_notification_peer_left, userName))));

    subscriptions.add(viewLive.onNotificationRemoteWaiting()
        .subscribe(userName -> displayNotification(
            getString(R.string.live_notification_peer_joining, userName))));

    subscriptions.add(viewLive.onNotificationRemoteJoined().subscribe(userName -> {
      ratingNotificationSubscribe();
      displayNotification(getString(R.string.live_notification_peer_joined, userName));
    }));

    subscriptions.add(viewLive.onNotificationonRemotePeerBuzzed()
        .subscribe(aVoid -> displayNotification(getString(R.string.live_notification_buzzed))));

    subscriptions.add(viewLive.onNotificationOnGameStarted()
        .subscribe(theString -> displayNotification(theString)));

    subscriptions.add(viewLive.onNotificationOnGameStopped()
        .subscribe(theString -> displayNotification(theString)));

    subscriptions.add(viewLive.onNotificationOnGameRestart()
        .subscribe(theString -> displayNotification(theString)));

    subscriptions.add(viewLive.onScreenshot().subscribe(aVoid -> {
      if (RuntimePermissionUtil.checkPermission(context(), this)) {
        takeScreenShot();
      }
    }));

    subscriptions.add(viewLive.onAnonymousJoined().subscribe(anonymousId -> {
      anonymousIdList.clear();
      anonymousIdList.add(anonymousId);
      if (!anonymousIdList.isEmpty()) livePresenter.getUsersInfoListById(anonymousIdList);
    }));

    subscriptions.add(viewLive.onRoomError().subscribe(error -> roomError(error)));

    subscriptions.add(viewLive.onRemotePeerClick().subscribe(o -> {
      if (o != null) userInfosNotificationView.displayView(o);
    }));

    subscriptions.add(viewLive.onItemsChallengeEmpty().subscribe(aVoid -> {
      launchChallengeGame();
    }));

    subscriptions.add(viewLive.onStartGame()
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(game -> {
          if (game != null) {
            switch (game.getId()) {
              case Game.GAME_POST_IT:
                GamePostIt gamePostIt = (GamePostIt) game;
                if (!gamePostIt.hasNames()) {
                  livePresenter.getNamesPostItGame(DeviceUtils.getLanguage(this));
                }
                break;

              case Game.GAME_DRAW:
                Timber.e("soef onStartGame DRAW");
                GameDraw gameDraw = (GameDraw) game;
                if (!gameDraw.hasNames()) {
                  livePresenter.getNamesDrawGame(DeviceUtils.getLanguage(this));
                } else {
                  setNextDrawGame();
                }
                break;
              case Game.GAME_CHALLENGE:
                GameChallenge gameChallenge = (GameChallenge) game;
                if (!gameChallenge.hasNames()) {
                  gameManager.setCurrentGame(game);
                  livePresenter.getDataChallengesGame(DeviceUtils.getLanguage(this));
                  Timber.e("soef load data to launchChallengeGame "
                      + gameChallenge.getCurrentChallenge());
                } else {
                  Timber.e("soef data already exist launchChallengeGame");
                  // launchChallengeGame();
                  //viewLive.setNextChallengePager();
                }

                Timber.e("soef llaunchChallengeGame "
                    + gameChallenge.getCurrentChallengerId()
                    + " "
                    + user.getId()
                    + " game.isUserAction() "
                    + game.isUserAction());
                if (game.isUserAction()) launchChallengeGame();
                break;
            }
          }
        }));

    subscriptions.add(userInfosNotificationView.onClickInvite().subscribe(contact -> {
      Bundle bundle = new Bundle();
      bundle.putString(TagManagerUtils.SCREEN, TagManagerUtils.LIVE);
      bundle.putString(TagManagerUtils.ACTION, TagManagerUtils.UNKNOWN);
      tagManager.trackEvent(TagManagerUtils.Invites, bundle);
      shouldOverridePendingTransactions = true;
      String linkId =
          navigator.sendInviteToCall(this, firebaseRemoteConfig, TagManagerUtils.INVITE, null,
              roomConfiguration.getRoomId(), false);
      livePresenter.bookRoomLink(linkId);
    }));

    subscriptions.add(userInfosNotificationView.onClickMore().subscribe(tribeGuest -> {
      DialogFactory.showBottomSheetForMoreBtn(this, tribeGuest.getDisplayName())
          .subscribe(labelType -> {
            if (labelType.getTypeDef().equals(LabelType.REPORT)) {
              Timber.e("report user " + tribeGuest.getId());
              livePresenter.reportUser(tribeGuest.getId());
            } else {
              Timber.d("cancel report user");
            }
          });
    }));

    subscriptions.add(userInfosNotificationView.onAdd().subscribe(user -> {

      if (user != null) {
        if (user.isInvisible()) {
          DialogFactory.dialog(context(), user.getDisplayName(),
              EmojiParser.demojizedText(context().getString(R.string.add_friend_error_invisible)),
              context().getString(R.string.add_friend_error_invisible_invite_android),
              context().getString(R.string.add_friend_error_invisible_cancel))
              .filter(x -> x == true)
              .subscribe(a -> share());
        } else {
          livePresenter.createFriendship(user.getId());
        }
      }
    }));

    subscriptions.add(userInfosNotificationView.onUnblock().subscribe(recipient -> {
      livePresenter.updateFriendship(recipient.getId(), recipient.isMute(),
          FriendshipRealm.DEFAULT);
    }));

    viewLive.initAnonymousSubscription(onAnonymousReceived());

    subscriptions.add(diceView.onNextDiceClick().subscribe(aVoid -> {
      if (live.getSource().equals(SOURCE_CALL_ROULETTE)) {
        reRollTheDiceFromCallRoulette(false);
      } else {
        reRollTheDiceFromLiveRoom();
      }
    }));

    subscriptions.add(viewLive.onChangeCallRouletteRoom().subscribe(aVoid -> {
      reRollTheDiceFromCallRoulette(true);
    }));
  }

  private void share() {

    Bundle bundle = new Bundle();
    bundle.putString(TagManagerUtils.SCREEN, TagManagerUtils.LIVE);
    bundle.putString(TagManagerUtils.ACTION, TagManagerUtils.UNKNOWN);
    tagManager.trackEvent(TagManagerUtils.Invites, bundle);

    if (StringUtils.isEmpty(live.getLinkId())) {
      livePresenter.getRoomLink(roomConfiguration.getRoomId());
      Toast.makeText(this, R.string.group_details_invite_link_generating, Toast.LENGTH_LONG).show();
    } else {
      navigator.sendInviteToCall(this, firebaseRemoteConfig, TagManagerUtils.CALL, live.getLinkId(),
          null, false);
    }
  }

  private void reRollTheDiceFromCallRoulette(boolean isFromOthers) {

    if (isFromOthers) {
      Toast.makeText(this,
          EmojiParser.demojizedText(getString(R.string.roll_the_dice_kicked_notification)),
          Toast.LENGTH_LONG).show();
    }

    if (subscriptions.hasSubscriptions()) subscriptions.clear();
    viewLive.endCall(true);
    viewLive.dispose(true);
    viewLive.jump();
    initRoom();
  }

  private void reRollTheDiceFromLiveRoom() {
    viewLive.reRollTheDiceFromLiveRoom();
  }

  @Override public boolean dispatchTouchEvent(MotionEvent ev) {
    if (userInfosNotificationView.getVisibility() == VISIBLE && !ViewUtils.isIn(
        userInfosNotificationView, (int) ev.getX(), (int) ev.getY())) {
      userInfosNotificationView.hideView();
    }

    return super.dispatchTouchEvent(ev);
  }

  private void takeScreenShot() {
    viewLive.hideGamesBtn();
    screenshotView.takeScreenshot();
    viewLive.displayGamesBtn();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull final String[] permissions,
      @NonNull final int[] grantResults) {
    switch (requestCode) {
      case 100: {

        RuntimePermissionUtil.onRequestPermissionsResult(grantResults,
            new RuntimePermissionUtil.RPResultListener() {
              @Override public void onPermissionGranted() {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                  takeScreenShot();
                }
              }

              @Override public void onPermissionDenied() {
                Toast.makeText(LiveActivity.this, "Permission Denied! You cannot save image!",
                    Toast.LENGTH_SHORT).show();
              }
            });
        break;
      }
    }
  }

  public void displayBuzzPopupTutorial() {
    subscriptions.add(Observable.timer(MAX_DURATION_WAITING_LIVE, TimeUnit.SECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> viewLive.displayWaitLivePopupTutorial(live.getDisplayName())));
  }

  private void leave() {
    if (stateManager.shouldDisplay(StateManager.LEAVING_ROOM_POPUP)) {
      subscriptions.add(DialogFactory.dialog(this,
          EmojiParser.demojizedText(getString(R.string.tips_leavingroom_title)),
          EmojiParser.demojizedText(getString(R.string.tips_leavingroom_message)),
          getString(R.string.tips_leavingroom_action1),
          getString(R.string.tips_leavingroom_action2))
          .filter(x -> x == true)
          .subscribe(a -> finish()));
      stateManager.addTutorialKey(StateManager.LEAVING_ROOM_POPUP);
    } else {
      finish();
    }
  }

  private void putExtraRatingNotif() {
    boolean willDisplayPopup = false;
    if (liveDurationIsMoreThan30sec && displayRatingNotifDependingFirebaseTrigger()) {
      if (roomConfiguration != null && !StringUtils.isEmpty(roomConfiguration.getRoomId())) {
        returnIntent.putExtra(ROOM_ID, roomConfiguration.getRoomId());
      }
      returnIntent.putExtra(RatingNotificationView.DISPLAY_RATING_NOTIF, true);
      returnIntent.putExtra(TIMEOUT_RATING_NOTIFICATON, getFirebaseTimeoutConfig());
      willDisplayPopup = true;
      liveDurationIsMoreThan30sec = false;
    }

    Map<String, Object> tagMap = PreferencesUtils.getMapFromJson(callTagsMap);
    if (!willDisplayPopup && tagMap != null) {
      TagManagerUtils.manageTags(tagManager, tagMap);
      callTagsMap.set("");
    }
  }

  private void putExtraErrorNotif() {
    returnIntent.putExtra(ErrorNotificationView.DISPLAY_ERROR_NOTIF, true);
  }

  private void putExtraDisplayGrpNotif() {
    RoomMember roomMember = viewLive.getUsersInLiveRoom();
    List<TribeGuest> friendInLive = roomMember.getPeopleInRoom();
    List<TribeGuest> anonymousInLive = roomMember.getAnonymousInRoom();
    List<String> guestsIdsInLive = new ArrayList<>();

    for (TribeGuest guest : friendInLive) {
      guestsIdsInLive.add(guest.getId());
    }

    List<TribeGuest> peopleInLive = new ArrayList<>();
    peopleInLive.addAll(friendInLive);

    for (TribeGuest anonymous : anonymousInLive) {
      if (!guestsIdsInLive.contains(anonymous.getId())) peopleInLive.add(anonymous);
    }

    for (TribeGuest guest : peopleInLive) {
      if (usersIdsInvitedInLiveRoom.contains(guest.getId())) {
        activeUersIdsInvitedInLiveRoom.add(guest.getId());
      }
    }

    if (peopleInLive.size() > 0) {
      returnIntent.putExtra(NotificationContainerView.DISPLAY_SHARING_NOTIF, true);
      Bundle extra = new Bundle();
      extra.putSerializable(SharingCardNotificationView.CALL_GRP_MEMBERS,
          (Serializable) peopleInLive);
      extra.putDouble(SharingCardNotificationView.DURATION_CALL, viewLive.getDuration());
      returnIntent.putExtras(extra);
    } else if ((liveIsInvite
        || !activeUersIdsInvitedInLiveRoom.isEmpty()
        || !anonymousInLive.isEmpty()) && peopleInLive.size() > 1) {
      returnIntent.putExtra(NotificationContainerView.DISPLAY_CREATE_GRP_NOTIF, true);
      Bundle extra = new Bundle();
      extra.putSerializable(CreateGroupNotificationView.PREFILLED_GRP_MEMBERS,
          (Serializable) peopleInLive);
      returnIntent.putExtras(extra);
      liveIsInvite = false;
      usersIdsInvitedInLiveRoom.clear();
      activeUersIdsInvitedInLiveRoom.clear();
    }
  }

  @Override public void onReceivedAnonymousMemberInRoom(List<User> users) {
    onAnonymousReceived.onNext(users);
  }

  @Override public void onRoomLink(String roomLink) {
    String linkId = StringUtils.getLinkIdFromUrl(roomLink);
    navigator.sendInviteToCall(this, firebaseRemoteConfig, TagManagerUtils.CALL, linkId, null,
        false);
  }

  @Override public void onAddError() {
    Toast.makeText(context(),
        EmojiParser.demojizedText(context().getString(R.string.add_friend_error_invisible)),
        Toast.LENGTH_SHORT).show();
  }

  @Override public void onAddSuccess(Friendship friendship) {
    userInfosNotificationView.update(friendship);
  }

  private void setNextDrawGame() {
    gameDrawView.setNextGame();
  }

  /***
   *
   *  GENERATE DATA FOR GAMES
   */

  @Override public void onNamesPostItGame(List<String> nameList) {
    Game game = gameManager.getCurrentGame();

    if (game != null && game instanceof GamePostIt) {
      GamePostIt gamePostIt = (GamePostIt) game;
      gamePostIt.setNameList(nameList);
    }
  }

  @Override public void onNamesDrawGame(List<String> nameList) {
    Game game = gameManager.getCurrentGame();

    if (game != null && game instanceof GameDraw) {
      GameDraw gameDraw = (GameDraw) game;
      List<TribeGuest> guestList = viewLive.getUsersInLiveRoom().getPeopleInRoom();
      TribeGuest me =
          new TribeGuest(user.getId(), user.getDisplayName(), user.getProfilePicture(), false,
              false, null, false, null);
      guestList.add(me);
      gameDraw.setNewDatas(nameList, guestList);
    }

    setNextDrawGame();
  }

  @Override public void onDataChallengesGame(List<String> nameList) {
    Timber.e("soefonDataChallengesGame set data on preference   " + nameList.size());
    List<String> challengeList = new ArrayList<>();
    challengeList.addAll(nameList);
    dataChallengesGames.set(new HashSet<>());
    PreferencesUtils.addListToSet(dataChallengesGames, nameList);
  }

  private void launchChallengeGame() {
    Game game = gameManager.getCurrentGame();
    List<TribeGuest> guestList = viewLive.getUsersInLiveRoom().getPeopleInRoom();
    List<String> challengeList = new ArrayList<>();
    challengeList.addAll(dataChallengesGames.get());

    if (game != null && game instanceof GameChallenge) {
      GameChallenge gameChallenge = (GameChallenge) game;
      gameChallenge.setNameList(challengeList);
      gameChallenge.setGuestList(guestList);
      Timber.e("soef launchChallengeGame "
          + gameChallenge.getCurrentChallenge()
          + " "
          + challengeList.size()
          + " "
          + guestList.size());
      viewLive.setGameChallenge(gameChallenge);
    }
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
    String roomId = "";
    livePresenter.inviteUserToRoom(viewLive.getRoom().getOptions().getRoomId(), userId);
  }

  private void ready() {
    viewLive.update(live);
  }

  private void roomFull() {
    putExtraErrorNotif();
    finish();
  }

  private void roomError(WebSocketError error) {

    if (error.getId() == WebSocketError.ERROR_ROOM_FULL) {
      roomFull();
    } else {
      Toast.makeText(getApplicationContext(), ErrorMessageFactory.create(getBaseContext(), null),
          Toast.LENGTH_LONG).show();

      finish();
    }
  }

  private void putExtraHomeIntent() {
    putExtraRatingNotif();
    putExtraDisplayGrpNotif();
    setResult(Activity.RESULT_OK, returnIntent);
  }

  private void finishActivityAfterCallDeclined(NotificationPayload notificationPayload) {
    returnIntent.putExtra(NotificationPayload.CLICK_ACTION_DECLINE, notificationPayload);
    setResult(Activity.RESULT_OK, returnIntent);
    finish();
  }

  private void setExtraForCallFromUnknownUser() {
    if (viewLive.getUser().getPhone() == null) {
      returnIntent.putExtra(UNKNOWN_USER_FROM_DEEPLINK, true);
      setResult(Activity.RESULT_OK, returnIntent);
    }
  }

  @Override public void finish() {
    if (finished) return;

    finished = true;

    if (appStateMonitor != null) {
      appStateMonitor.stop();
      appStateMonitor.removeListener(this);
    }

    viewLive.endCall(false);
    putExtraHomeIntent();
    setExtraForCallFromUnknownUser();

    super.finish();
    overridePendingTransition(R.anim.activity_in_scale, R.anim.activity_out_to_right);
  }

  ////////////////
  //   PUBLIC   //
  ////////////////

  public Observable<List<User>> onAnonymousReceived() {
    return onAnonymousReceived;
  }

  @Override public void onRecipientInfos(Recipient recipient) {
    if (recipient instanceof Membership) {
      Membership membership = (Membership) recipient;
      live.setMembers(membership.getGroup().getMembers());
    } else if (recipient instanceof Friendship) {
      live.setId(recipient.getId());
      viewLive.start(live);
    }

    ready();
  }

  @Override public void renderFriendshipList(List<Friendship> friendshipList) {
    onUpdateFriendshipList.onNext(friendshipList);
  }

  @Override public void randomRoomAssignedSubscriber(String roomId) {
    Timber.d("random room assigned " + roomId);
    viewLiveContainer.blockOpenInviteView(false);
    live.setSessionId(roomId);
    joinRoom();
  }

  @Override public void fbIdUpdatedSubscriber(User userUpdated) {
    Timber.d("user fbId updated " + userUpdated.getId() + " " + userUpdated.getFbid());
  }

  @Override public void onJoinedRoom(RoomConfiguration roomConfiguration) {
    this.roomConfiguration = roomConfiguration;
    this.roomConfiguration.setRoutingMode(routingMode.get());
    viewLive.joinRoom(this.roomConfiguration);
    if (!live.isGroup() && !live.isSessionOrLink()) {
      livePresenter.inviteUserToRoom(this.roomConfiguration.getRoomId(), live.getSubId()); // MADA
    }
    live.setSessionId(roomConfiguration.getRoomId());

    if (!StringUtils.isEmpty(live.getLinkId()) && !StringUtils.isEmpty(
        roomConfiguration.getInitiatorId()) && !roomConfiguration.getInitiatorId()
        .equals(getCurrentUser().getId())) {
      NotificationPayload notificationPayload = new NotificationPayload();
      notificationPayload.setBody(EmojiParser.demojizedText(
          getString(R.string.live_notification_initiator_has_been_notified,
              roomConfiguration.getInitiatorName())));
      LiveNotificationView liveNotificationView =
          NotificationUtils.getNotificationViewFromPayload(this, notificationPayload,
              missedCallManager);

      if (liveNotificationView != null) {
        Alerter.create(LiveActivity.this, liveNotificationView).show();
      }
    }
  }

  @Override public void onRoomFull(String message) {
    roomFull();
  }

  @Override public void onJoinRoomError(String message) {
    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    finish();
  }

  @Override public Context context() {
    return this;
  }

  @Override public void onAppDidEnterForeground() {
    if (viewLive != null) {
      viewLive.setCameraEnabled(true, TribePeerMediaConfiguration.APP_IN_BACKGROUND);
    }
  }

  @Override public void onAppDidEnterBackground() {
    if (viewLive != null) {
      viewLive.setCameraEnabled(false, TribePeerMediaConfiguration.APP_IN_BACKGROUND);
    }
  }

  /////////////////
  //  BROADCAST  //
  /////////////////

  private void declineInvitation(String sessionId) {
    livePresenter.declineInvite(sessionId);
  }

  class NotificationReceiver extends BroadcastReceiver {

    @Override public void onReceive(Context context, Intent intent) {
      NotificationPayload notificationPayload =
          (NotificationPayload) intent.getSerializableExtra(BroadcastUtils.NOTIFICATION_PAYLOAD);

      if (live.getSubId().equals(notificationPayload.getUserId()) || live.getSubId()
          .equals(notificationPayload.getGroupId()) || (live.getSessionId() != null
          && live.getSessionId().equals(notificationPayload.getSessionId()))) {

        if (notificationPayload.getClickAction().equals(NotificationPayload.CLICK_ACTION_DECLINE)) {
          displayNotification(EmojiParser.demojizedText(
              context.getString(R.string.live_notification_guest_declined,
                  notificationPayload.getUserDisplayName())));
          if (viewLive.getRowsInLive() < 3 && !live.isGroup()) {
            finishActivityAfterCallDeclined(notificationPayload);
          } else {
            viewLive.removeUserFromGrid(notificationPayload.getUserId());
          }
        }

        return;
      }

      LiveNotificationView liveNotificationView =
          NotificationUtils.getNotificationViewFromPayload(context, notificationPayload,
              missedCallManager);

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
                    false, false, null, true, null);
                invite(tribeGuest.getId());
                viewLive.addTribeGuest(tribeGuest);
              } else if (action.getId().equals(NotificationUtils.ACTION_DECLINE)) {
                declineInvitation(action.getSessionId());
              }
            }));

        Alerter.create(LiveActivity.this, liveNotificationView).show();
      }
    }
  }
}

