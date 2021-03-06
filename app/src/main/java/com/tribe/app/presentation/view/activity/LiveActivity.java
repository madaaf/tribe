package com.tribe.app.presentation.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.v4.app.NotificationManagerCompat;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.f2prateek.rx.preferences.Preference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jenzz.appstate.AppStateListener;
import com.jenzz.appstate.AppStateMonitor;
import com.jenzz.appstate.RxAppStateMonitor;
import com.tarek360.instacapture.InstaCapture;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.R;
import com.tribe.app.data.network.WSService;
import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.data.utils.ShortcutUtils;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.Live;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.exception.ErrorMessageFactory;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.LivePresenter;
import com.tribe.app.presentation.mvp.view.LiveMVPView;
import com.tribe.app.presentation.mvp.view.RoomMVPView;
import com.tribe.app.presentation.mvp.view.ShortcutMVPView;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.PermissionUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.Triplet;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.mediapicker.RxImagePicker;
import com.tribe.app.presentation.utils.mediapicker.Sources;
import com.tribe.app.presentation.utils.preferences.CallTagsMap;
import com.tribe.app.presentation.utils.preferences.FullscreenNotificationState;
import com.tribe.app.presentation.utils.preferences.RoutingMode;
import com.tribe.app.presentation.view.ShortcutUtil;
import com.tribe.app.presentation.view.adapter.model.ShareTypeModel;
import com.tribe.app.presentation.view.adapter.viewholder.BaseListViewHolder;
import com.tribe.app.presentation.view.adapter.viewholder.BaseNotifViewHolder;
import com.tribe.app.presentation.view.component.live.LiveContainer;
import com.tribe.app.presentation.view.component.live.LiveInviteView;
import com.tribe.app.presentation.view.component.live.LiveView;
import com.tribe.app.presentation.view.component.live.ScreenshotView;
import com.tribe.app.presentation.view.notification.Alerter;
import com.tribe.app.presentation.view.notification.NotificationPayload;
import com.tribe.app.presentation.view.utils.Constants;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.MissedCallManager;
import com.tribe.app.presentation.view.utils.RemoteConfigManager;
import com.tribe.app.presentation.view.utils.RuntimePermissionUtil;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.app.presentation.view.utils.StateManager;
import com.tribe.app.presentation.view.utils.ViewUtils;
import com.tribe.app.presentation.view.widget.DiceView;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.chat.ChatView;
import com.tribe.app.presentation.view.widget.notifications.ErrorNotificationView;
import com.tribe.app.presentation.view.widget.notifications.NotificationContainerView;
import com.tribe.app.presentation.view.widget.notifications.UserInfosNotificationView;
import com.tribe.tribelivesdk.game.GameManager;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.model.TribePeerMediaConfiguration;
import com.tribe.tribelivesdk.model.error.WebSocketError;
import com.tribe.tribelivesdk.stream.TribeAudioManager;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class LiveActivity extends BaseBroadcastReceiverActivity
    implements LiveMVPView, ShortcutMVPView, RoomMVPView, AppStateListener {

  @StringDef({
      SOURCE_GRID, SOURCE_DEEPLINK, SOURCE_SEARCH, SOURCE_CALLKIT, SOURCE_SHORTCUT_ITEM,
      SOURCE_DRAGGED_AS_GUEST, SOURCE_ONLINE_NOTIFICATION, SOURCE_LIVE_NOTIFICATION, SOURCE_FRIENDS,
      SOURCE_NEW_CALL, SOURCE_JOIN_LIVE, SOURCE_ADD_PEERS, SOURCE_CALL_ROULETTE,
      SOURCE_IN_APP_NOTIFICATION, SOURCE_HOME
  }) public @interface Source {
  }

  private static final String EXTRA_LIVE = "EXTRA_LIVE";
  private static final String EXTRA_SECTION = "EXTRA_SECTION";
  private static final String EXTRA_GESTURE = "EXTRA_GESTURE";

  public static final String SOURCE_GRID = "Grid";
  public static final String SOURCE_DEEPLINK = "DeepLink";
  public static final String SOURCE_SEARCH = "Search";
  public static final String SOURCE_CALLKIT = "CallKit";
  public static final String SOURCE_SHORTCUT_ITEM = "ShortcutItem";
  public static final String SOURCE_DRAGGED_AS_GUEST = "DraggedAsGuest";
  public static final String SOURCE_ONLINE_NOTIFICATION = "OnlineNotification";
  public static final String SOURCE_IN_APP_NOTIFICATION = "in-appNotification";
  public static final String SOURCE_LIVE_NOTIFICATION = "LiveNotification";
  public static final String SOURCE_FRIENDS = "Friends";
  public static final String SOURCE_NEW_CALL = "NewCall";
  public static final String SOURCE_CALL_ROULETTE = "CallRoulette";
  public static final String SOURCE_JOIN_LIVE = "JoinLive";
  public static final String SOURCE_ADD_PEERS = "AddPeers";
  public static final String SOURCE_HOME = TagManagerUtils.HOME;
  public static final String SOURCE_CHAT = TagManagerUtils.CHAT;

  public static final String ROOM_ID = "ROOM_ID";
  public static final String TIMEOUT_RATING_NOTIFICATON = "TIMEOUT_RATING_NOTIFICATON";
  public static final String UNKNOWN_USER_FROM_DEEPLINK = "UNKNOWN_USER_FROM_DEEPLINK";
  public static final String USER_IDS_FOR_NEW_SHORTCUT = "USER_IDS_FOR_NEW_SHORTCUT";
  public static final String LAUNCH_DICE = "LAUNCH_DICE";
  public static final String LAUNCH_SEARCH = "LAUNCH_SEARCH";

  private final int MAX_DURATION_WAITING_LIVE = 8;
  private final int MIN_LIVE_DURATION_TO_DISPLAY_RATING_NOTIF = 30;
  private final int MIN_DURATION_BEFORE_DISPLAY_TUTORIAL_DRAG_GUEST = 3;

  @Inject NotificationManagerCompat notificationManager;
  @Inject SoundManager soundManager;
  @Inject ScreenUtils screenUtils;
  @Inject LivePresenter livePresenter;
  @Inject StateManager stateManager;
  @Inject User user;
  @Inject @RoutingMode Preference<String> routingMode;
  @Inject @CallTagsMap Preference<String> callTagsMap;
  @Inject @FullscreenNotificationState Preference<Set<String>> fullScreenNotificationState;
  @Inject MissedCallManager missedCallManager;
  @Inject RxImagePicker rxImagePicker;
  @Inject com.tribe.app.presentation.utils.DateUtils dateUtils;
  @BindView(R.id.viewLive) LiveView viewLive;
  @BindView(R.id.viewLiveInvite) LiveInviteView viewLiveInvite;
  @BindView(R.id.remotePeerAdded) TextViewFont txtRemotePeerAdded;
  @BindView(R.id.userInfosNotificationView) UserInfosNotificationView userInfosNotificationView;
  @BindView(R.id.screenShotView) ScreenshotView screenshotView;
  @BindView(R.id.diceLayoutRoomView) DiceView diceView;
  @BindView(R.id.notificationContainerView) NotificationContainerView notificationContainerView;
  @BindView(R.id.blockView) FrameLayout blockView;
  @BindView(R.id.chatview) FrameLayout chatViewContainer;
  @BindView(R.id.viewLiveContainer) LiveContainer viewLiveContainer;

  // VARIABLES
  private TribeAudioManager audioManager;
  private GameManager gameManager;
  private Unbinder unbinder;
  private Live live;
  private Room room;
  private AppStateMonitor appStateMonitor;
  private RemoteConfigManager remoteConfigManager;
  private RxPermissions rxPermissions;
  private Intent returnIntent = new Intent();
  private List userIdList = new ArrayList();
  private boolean finished = false, isChatViewOpen = false;
  private boolean shouldOverridePendingTransactions = false;
  private float initialBrightness = -1;
  private int createRoomErrorCount = 0;
  private HashSet<String> usersThatWereLive = new HashSet<>();
  private ChatView chatView;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<List<User>> onAnonymousReceived = PublishSubject.create();

  public static Intent getCallingIntent(Context context, Recipient recipient, @Source String source,
      String gameId, String userAsk) {
    Intent intent = new Intent(context, LiveActivity.class);

    intent.putExtra(EXTRA_LIVE, computeLive(recipient, source, gameId, userAsk));
    return intent;
  }

  private static Live computeLive(Recipient recipient, @Source String source, String gameId,
      String userAsk) {
    Live.Builder builder = new Live.Builder(Live.FRIEND_CALL).countdown(!recipient.isLive())
        .source(source)
        .userAsk(userAsk);

    if (recipient instanceof Shortcut) {
      Shortcut shortcut = (Shortcut) recipient;
      builder.shortcut(shortcut);
    } else if (recipient instanceof Invite) {
      Invite invite = (Invite) recipient;
      Room room = invite.getRoom();
      room.setInviter(invite.getInviter());
      builder.room(room);
    }

    if (!StringUtils.isEmpty(gameId)) builder.gameId(gameId);

    return builder.build();
  }

  public static Intent getCallingIntent(Context context, String recipientId, String picture,
      String name, String roomId, @Source String source, Shortcut shortcut) {
    Intent intent = new Intent(context, LiveActivity.class);

    User user = new User(recipientId);
    user.setDisplayName(name);
    user.setProfilePicture(picture);

    Live.Builder builder =
        new Live.Builder(StringUtils.isEmpty(recipientId) ? Live.WEB : Live.FRIEND_CALL).users(user)
            .shortcut(shortcut)
            .countdown(StringUtils.isEmpty(roomId))
            .roomId(roomId)
            .intent(true)
            .source(source);

    intent.putExtra(EXTRA_LIVE, builder.build());

    return intent;
  }

  public static Intent getCallingIntent(Context context, String linkId, String url,
      @Source String source) {
    Intent intent = new Intent(context, LiveActivity.class);

    Live live = new Live.Builder(Live.WEB).linkId(linkId).url(url).source(source).build();

    intent.putExtra(EXTRA_LIVE, live);

    return intent;
  }

  public static Intent getCallingIntent(Context context, @Source String source, String gameId) {
    Intent intent = new Intent(context, LiveActivity.class);

    Live live = new Live.Builder(Live.NEW_CALL).source(source).gameId(gameId).build();

    intent.putExtra(EXTRA_LIVE, live);

    return intent;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_live);

    unbinder = ButterKnife.bind(this);

    initDependencyInjector();
    initParams(getIntent());
    initGameManager();
    init();
    initResources();
    initRemoteConfig();
    manageClickNotification(getIntent());
    initAppState();
    initBrightness();
  }

  @Override protected void onNewIntent(Intent intent) {
    if (subscriptions.hasSubscriptions()) subscriptions.clear();
    manageClickNotification(getIntent());
    disposeCall(true);
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
    if (chatView != null) chatView.onResumeView();
    onResumeLockPhone();

    subscriptionsBroadcastReceiver.add(
        notificationReceiver.onShowNotificationLive().subscribe(pair -> {
          NotificationPayload payload = pair.first;

          if (room == null) return;

          boolean shouldDisplay = true;

          if (room.getId().equals(payload.getSessionId())) {
            String action = payload.getAction();
            if (action != null &&
                (action.equals(NotificationPayload.ACTION_LEFT) ||
                    action.equals(NotificationPayload.ACTION_JOINED))) {
              shouldDisplay = false;
            }
          }

          if (shouldDisplay) Alerter.create(this, pair.second).show();
        }));

    subscriptionsBroadcastReceiver.add(notificationReceiver.onCreateInvite()
        .filter(userId -> room != null && !StringUtils.isEmpty(userId))
        .subscribe(userId -> livePresenter.createInvite(room.getId(), false, userId)));

    if (shouldOverridePendingTransactions) {
      overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
      shouldOverridePendingTransactions = false;
    }
  }

  @Override protected void onPause() {
    if (chatView != null) chatView.dispose();

    super.onPause();
  }

  @Override protected void onDestroy() {
    Timber.d("onDestroy");

    resetBrightness();

    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    soundManager.cancelMediaPlayer();

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

  @Override public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    if (hasFocus) {
      getWindow().getDecorView()
          .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
              View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
              View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
              View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
              View.SYSTEM_UI_FLAG_FULLSCREEN |
              View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == Navigator.FROM_NEW_GAME && data != null) {
      String gameId = data.getStringExtra(NewGameActivity.GAME_ID);
      viewLive.startGame(gameId);
    }
  }

  private void initParams(Intent intent) {
    if (intent.hasExtra(EXTRA_LIVE)) {
      live = (Live) intent.getSerializableExtra(EXTRA_LIVE);
      setWaitingToJoin(live.getUserAsk());
      live.init();
    }
  }

  private void init() {
    rxPermissions = new RxPermissions(this);

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

  private void launchRomm() {
    Bundle bundle = new Bundle();
    bundle.putBoolean(TagManagerUtils.USER_CAMERA_ENABLED,
        PermissionUtils.hasPermissionsCamera(rxPermissions));
    bundle.putBoolean(TagManagerUtils.USER_MICROPHONE_ENABLED,
        PermissionUtils.hasPermissionsCamera(rxPermissions));
    tagManager.setProperty(bundle);

    initSubscriptions();

    boolean isCallRoulette = live.getSource().equals(LiveActivity.SOURCE_CALL_ROULETTE);

    if (isCallRoulette) launchCallRoulette();
    userInfosNotificationView.setCallRoulette(isCallRoulette);
    viewLive.start(live);
  }

  private void initRoom() {
    livePresenter.onViewAttached(this);

    boolean displayPermissionNotif = notificationContainerView.
        showNotification(null, NotificationContainerView.DISPLAY_PERMISSION_NOTIF);

    notificationContainerView.onDismissPopup().subscribe(isDismiss -> {
      finish();
    });

    if (displayPermissionNotif) {
      notificationContainerView.onAcceptedPermission().subscribe(permissionGranted -> {
        if (permissionGranted) {
          launchRomm();
        } else {
          finish();
        }
      });
    } else {
      launchRomm();
    }

    livePresenter.fbidUpdated();
  }

  private void initBrightness() {
    int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

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
    startService(WSService.getCallingIntent(this, WSService.CALL_ROULETTE_TYPE, null));
    livePresenter.randomRoomAssigned();
  }

  private void initRoomSubscription() {
    startService(WSService.getCallingIntentSubscribeRoom(this, room.getId()));
    //  livePresenter.subscribeToRoomUpdates(room.getIdOb());
  }

  private void removeRoomSubscription() {
    startService(WSService.getCallingIntentUnsubscribeRoom(this, room.getId()));
  }

  private void stopCallRouletteService() {
    // TODO remove subscription from call_roulette and call it
  }

  private void initResources() {
    int result = 0;
    int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");

    if (resourceId > 0) {
      result = getResources().getDimensionPixelSize(resourceId);
    }

    viewLiveContainer.setStatusBarHeight(result);
  }

  private void initRemoteConfig() {
    remoteConfigManager = RemoteConfigManager.getInstance(this);
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

  private void disposeCall(boolean isJump) {
    if (room != null) removeRoomSubscription();
    live.dispose();
    livePresenter.onViewDetached();
    viewLive.endCall(isJump);

    if (isJump) {
      viewLive.dispose(isJump);
      viewLive.jump();
    }
  }

  private void manageClickNotification(Intent intent) {
    if (intent != null && intent.hasExtra(Constants.NOTIFICATION_LIVE)) {
      Bundle bundle = new Bundle();
      bundle.putString(TagManagerUtils.CATEGORY,
          intent.getStringExtra(Constants.NOTIFICATION_LIVE));
      tagManager.trackEvent(TagManagerUtils.Notification_AppOpen, bundle);
    }
  }

  private void launchCallRoulette() {
    if (!FacebookUtils.isLoggedIn()) {
      Timber.d("not logged on fb ");
      blockView.setVisibility(VISIBLE);
      blockView.setOnTouchListener((v, event) -> true);
      notificationContainerView.
          showNotification(null, NotificationContainerView.DISPLAY_FB_CALL_ROULETTE);
    } else {
      initCallRouletteService();
    }
  }

  @Override public void onShortcutCreatedSuccess(Shortcut shortcut) {
    initChatView(shortcut);
  }

  private void initChatView(Shortcut shortcut) {
    Timber.i("init chat view from live activity");
    if (live.getSource().equals(SOURCE_CALL_ROULETTE) ||
        (live.getRoom() != null && live.getRoom().acceptsRandom())) {
      return;
    }
    if (shortcut != null) {
      if (chatView != null) {
        chatView.dispose();
        chatView.destroyDrawingCache();
        chatView = null;
        chatViewContainer.removeAllViews();
        setChatView(shortcut);
      } else {
        setChatView(shortcut);
      }
    } else if (live.getRoom() != null) {
      String[] arrids =
          live.getRoom().getUserIds().toArray(new String[live.getRoom().getUserIds().size()]);
      if (arrids != null && arrids.length > 0) {
        livePresenter.createShortcut(arrids);
      }
    }
  }

  private void setChatView(Shortcut shortcut) {
    if (shortcut == null || shortcut.getMembers() == null || shortcut.getMembers().isEmpty()) {
      String source = (live != null) ? live.getSource() : "";
      Timber.e("try to set chat view, but members is empty " + source);
      return;
    }
    chatView = new ChatView(context(), ChatView.FROM_LIVE);
    chatView.setChatId(shortcut, null);
    chatView.onResumeView();
    chatViewContainer.addView(chatView);
  }

  private void setChatVisibility(int visibility) {
    chatViewContainer.setVisibility(visibility);
    chatView.setVisibility(visibility);
  }

  private void animateChatView() {
    chatView.setAlpha(0);
    chatView.setTranslationX(-screenUtils.getWidthPx());
    setChatVisibility(VISIBLE);
    chatView.animate()
        .setInterpolator(new AccelerateDecelerateInterpolator())
        .setDuration(300)
        .alpha(1f)
        .withStartAction(() -> chatView.setAlpha(0f))
        .translationX(0)
        .setListener(null);
  }

  public void notififyNewMessage() {
    if (!isChatViewOpen) {
      viewLive.onNewMessage();
    }
  }

  private void initSubscriptions() {
    initChatView(getShortcut());
    livePresenter.getRandomBannedUntil();
    if (!live.getSource().equals(SOURCE_CALL_ROULETTE)) {
      subscriptions.add(live.onRoomUpdated().subscribe(room -> {
        if ((room == null || room.acceptsRandom()) && chatView == null) return;

        if (room != null && room.getLiveUsers() != null) {
          for (User user : room.getLiveUsers()) {
            if (!user.equals(getCurrentUser())) usersThatWereLive.add(user.getId());
          }
        }

        List<User> allUsers = ShortcutUtil.removeMe(room.getAllUsers(), user);

        if (chatView != null &&
            chatView.getShortcut() != null &&
            chatView.getShortcut().getMembers() != null &&
            !chatView.getShortcut().getMembers().isEmpty()) {

          if (!allUsers.isEmpty() &&
              !ShortcutUtil.equalShortcutMembers(chatView.getShortcut().getMembers(), allUsers,
                  user)) {
            chatView.dispose();
            Shortcut shortcut = getShortcut();
            if (shortcut != null) {
              shortcut.setMembers(allUsers);
              initChatView(shortcut);
            }
          }
        }
      }));

      subscriptions.add(viewLive.onOpenChat().subscribe(open -> {
        isChatViewOpen = open;
        if (open && chatView != null) {
          animateChatView();
        } else if (chatView != null) {
          chatView.animate()
              .setDuration(300)
              .alpha(0f)
              .withStartAction(() -> chatView.setAlpha(1f))
              .translationX(-screenUtils.getWidthPx())
              .withEndAction(() -> setChatVisibility(INVISIBLE))
              .setListener(null);
        }
      }));
    } else {
      subscriptions.add(live.onRoomUpdated().subscribe(room1 -> {
        boolean isCallRoulette = live.getSource().equals(LiveActivity.SOURCE_CALL_ROULETTE) ||
            (live.getRoom() != null && live.getRoom().acceptsRandom());
        userInfosNotificationView.setCallRoulette(isCallRoulette);
      }));
    }

    subscriptions.add(viewLive.onShouldJoinRoom().subscribe(shouldJoin -> {
      if (!live.getSource().equals(LiveActivity.SOURCE_CALL_ROULETTE)) {
        if (live.fromRoom() || !StringUtils.isEmpty(live.getLinkId())) {
          getRoomInfos();
        } else {
          getAllInvites();
        }
      }
    }));

    subscriptions.add(viewLive.onJoined().doOnNext(tribeJoinRoom -> {
      if (!live.getSource().equals(SOURCE_CALL_ROULETTE)) {
        if (live.fromRoom() &&
            (tribeJoinRoom.getSessionList() == null ||
                tribeJoinRoom.getSessionList().size() == 0)) {
          if (room.getInviter() != null) {
            Toast.makeText(this,
                getString(R.string.live_other_user_hung_up, room.getInviter().getDisplayName()),
                Toast.LENGTH_SHORT).show();
            livePresenter.createInvite(room.getId(), false, room.getInviter().getId());
          }
        } else if (!live.fromRoom()) {
          List<String> userIds = live.getUserIdsOfShortcut();
          livePresenter.createInvite(this.room.getId(), !StringUtils.isEmpty(live.getUserAsk()),
              userIds.toArray(new String[userIds.size()]));
        }
      }
    }).subscribe(tribeJoinRoom -> initRoomSubscription()));

    subscriptions.add(
        viewLive.onLeave().observeOn(AndroidSchedulers.mainThread()).subscribe(aVoid -> leave()));

    subscriptions.add(viewLive.onDismissInvite()
        .subscribe(userId -> livePresenter.removeInvite(room.getId(), userId)));

    subscriptions.add(viewLive.onEdit()
        .filter(aVoid -> !StringUtils.isEmpty(live.getShortcutId()))
        .flatMap(view -> DialogFactory.showBottomSheetForCustomizeShortcut(this),
            (pair, labelType) -> {
              if (labelType != null) {
                if (labelType.getTypeDef().equals(LabelType.CHANGE_NAME)) {
                  subscriptions.add(DialogFactory.inputDialog(this,
                      getString(R.string.shortcut_update_name_title),
                      getString(R.string.shortcut_update_name_description),
                      getString(R.string.shortcut_update_name_validate),
                      getString(R.string.action_cancel), InputType.TYPE_CLASS_TEXT)
                      .subscribe(s -> livePresenter.updateShortcutName(live.getShortcutId(), s)));
                }
              }

              return labelType;
            })
        .filter(labelType -> labelType.getTypeDef().equals(LabelType.CHANGE_PICTURE))
        .flatMap(pair -> DialogFactory.showBottomSheetForCamera(this), (pair, labelType) -> {
          if (labelType.getTypeDef().equals(LabelType.OPEN_CAMERA)) {
            subscriptions.add(rxImagePicker.requestImage(Sources.CAMERA, true)
                .subscribe(uri -> livePresenter.updateShortcutPicture(live.getShortcutId(),
                    uri.toString())));
          } else if (labelType.getTypeDef().equals(LabelType.OPEN_PHOTOS)) {
            subscriptions.add(rxImagePicker.requestImage(Sources.GALLERY, true)
                .subscribe(uri -> livePresenter.updateShortcutPicture(live.getShortcutId(),
                    uri.toString())));
          }

          return null;
        })
        .subscribe());

    subscriptions.add(viewLive.onShareLink().subscribe(aString -> {
      if (room == null) return;

      if (aString.equals(ShareTypeModel.SHARE_TYPE_MESSENGER)) {
        navigator.sendInviteToMessenger(this, TagManagerUtils.CALL, room.getLink());
      } else {
        share(true);
      }
    }));

    subscriptions.add(viewLive.unlockRollTheDice().
        subscribeOn(Schedulers.newThread()).
        observeOn(AndroidSchedulers.mainThread()).
        subscribe(s -> {
          Timber.d("unlockRollTheDice reveived " + s);
          if (!FacebookUtils.isLoggedIn()) {
            notificationContainerView.setUnlockRollTheDiceSenderId(s);
            notificationContainerView.
                showNotification(null, NotificationContainerView.DISPLAY_FB_CALL_ROULETTE);
          }
        }));

    subscriptions.add(viewLive.onEndCall().
        observeOn(AndroidSchedulers.mainThread()).
        subscribe(duration -> livePresenter.incrementTimeInCall(user.getId(), duration)));

    subscriptions.add(notificationContainerView.onFacebookSuccess().
        subscribeOn(Schedulers.newThread()).
        observeOn(AndroidSchedulers.mainThread()).
        subscribe(unlockRollTheDiceSenderId -> {
          blockView.setVisibility(View.GONE);
          initCallRouletteService();
          viewLive.sendUnlockedDice(unlockRollTheDiceSenderId);
        }));

    subscriptions.add(viewLive.onRollTheDice().
        subscribe(s -> {
          // TODO CALLROULETTE
          //viewInviteLive.diceDragued();
          //viewInviteLive.requestLayout();
        }));

    // TODO CALLROULETTE
    //subscriptions.add(viewLiveContainer.onDropDiceWithoutFbAuth().subscribe(aVoid -> {
    //  Timber.d("drag dice, user not fb loged, display fb notif auth");
    //  notificationContainerView.
    //      showNotification(null, NotificationContainerView.DISPLAY_FB_CALL_ROULETTE);
    //}));

    subscriptions.add(viewLiveInvite.onStopAndLaunchDice().subscribe(aVoid -> {
      returnIntent.putExtra(LAUNCH_DICE, true);
      setResult(RESULT_OK, returnIntent);
      leave();
    }));

    subscriptions.add(viewLiveInvite.onInviteMessenger()
        .subscribe(
            aVoid -> navigator.sendInviteToMessenger(this, TagManagerUtils.CALL, room.getLink())));

    subscriptions.add(viewLiveInvite.onInviteSms().subscribe(aVoid -> share(true)));

    subscriptions.add(viewLiveInvite.onLaunchSearch().subscribe(aVoid -> {
      returnIntent.putExtra(LAUNCH_SEARCH, true);
      setResult(RESULT_OK, returnIntent);
      leave();
    }));

    subscriptions.add(viewLive.onNotificationRemotePeerInvited().
        subscribe(userName -> displayNotification(
            getString(R.string.live_notification_peer_added, userName))));

    subscriptions.add(viewLive.onNotificationonRemotePeerRemoved().
        subscribe(userName -> displayNotification(
            getString(R.string.live_notification_peer_left, userName))));

    subscriptions.add(viewLive.onNotificationRemoteWaiting().
        subscribe(userName -> displayNotification(
            getString(R.string.live_notification_peer_joining, userName))));

    subscriptions.add(viewLive.onNotificationRemoteJoined().
        subscribe(userName -> displayNotification(
            getString(R.string.live_notification_peer_joined, userName))));

    subscriptions.add(viewLive.onNotificationonRemotePeerBuzzed().
        subscribe(aVoid -> displayNotification(getString(R.string.live_notification_buzzed))));

    subscriptions.add(viewLive.onNotificationOnGameStarted().
        subscribe(theString -> displayNotification(theString)));

    subscriptions.add(viewLive.onNotificationOnGameStopped().
        subscribe(theString -> displayNotification(theString)));

    subscriptions.add(viewLive.onNotificationOnGameRestart().
        subscribe(theString -> displayNotification(theString)));

    subscriptions.add(viewLive.onScreenshot().
        subscribe(aVoid -> {
          if (RuntimePermissionUtil.checkPermission(context(), this)) {
            takeScreenShot();
          }
        }));

    subscriptions.add(viewLive.onUserJoined().
        subscribe(anonymousId -> {
          userIdList.clear();
          userIdList.add(anonymousId);
          if (!userIdList.isEmpty()) livePresenter.getUsersInfoListById(userIdList);
        }));

    subscriptions.add(viewLive.onRoomError().
        subscribe(error -> roomError(error)));

    subscriptions.add(viewLive.onRemotePeerClick().
        subscribe(o -> {
          if (o != null) userInfosNotificationView.displayView(o);
        }));

    subscriptions.add(userInfosNotificationView.onClickInvite().
        subscribe(contact -> {
          Bundle bundle = new Bundle();
          bundle.putString(TagManagerUtils.SCREEN, TagManagerUtils.LIVE);
          bundle.putString(TagManagerUtils.ACTION, TagManagerUtils.UNKNOWN);
          tagManager.trackEvent(TagManagerUtils.Invites, bundle);
          shouldOverridePendingTransactions = true;
          navigator.sendInviteToCall(this, TagManagerUtils.INVITE, live.getLinkId(), room.getId(),
              false);
        }));

    subscriptions.add(userInfosNotificationView.onClickMore()
        .flatMap(pairGuestView -> (stateManager.shouldDisplay(StateManager.REPORT_USER)
            ? DialogFactory.dialog(this, getString(R.string.tips_report_title),
            getString(R.string.tips_report_message), getString(R.string.tips_report_action1),
            getString(R.string.tips_report_action2)) : Observable.just(true)).map(
            aBoolean -> new Triplet<TribeGuest, BaseNotifViewHolder, Boolean>(pairGuestView.first,
                pairGuestView.second, aBoolean)))
        .filter(triplet -> {
          stateManager.addTutorialKey(StateManager.REPORT_USER);
          if (!triplet.third) {
            triplet.second.btnMore.setScaleX(1);
            triplet.second.btnMore.setScaleY(1);
            triplet.second.progressView.setVisibility(View.GONE);
          }

          return triplet.third == true;
        })
        .subscribe(triplet -> {
          TribeGuest tribeGuest = triplet.first;
          BaseNotifViewHolder holder = triplet.second;
          userInfosNotificationView.setVisibility(View.GONE);
          setImageFirebase(tribeGuest.getId(), holder);
        }));

    subscriptions.add(userInfosNotificationView.onAdd()
        .flatMap(pair -> DialogFactory.dialog(this, getString(R.string.tips_addfriend_title),
            getString(R.string.tips_addfriend_message), getString(R.string.tips_addfriend_action1),
            getString(R.string.tips_addfriend_action2))
            .map(
                aBoolean -> new Triplet<User, BaseNotifViewHolder, Boolean>(pair.first, pair.second,
                    aBoolean)))
        .filter(triplet -> {
          if (!triplet.third) {
            triplet.second.btnAdd.setImageResource(R.drawable.btn_add);
          }

          return triplet.third == true;
        })
        .subscribe(triplet -> {
          if (triplet.first != null) {
            if (triplet.first.isInvisible()) {
              subscriptions.add(DialogFactory.dialog(context(), triplet.first.getDisplayName(),
                  EmojiParser.demojizedText(
                      context().getString(R.string.add_friend_error_invisible)),
                  context().getString(R.string.add_friend_error_invisible_invite_android),
                  context().getString(R.string.add_friend_error_invisible_cancel))
                  .filter(x -> x == true)
                  .subscribe(a -> share(false)));
            } else {
              livePresenter.createShortcut(triplet.first.getId());
            }
          }
        }));

    subscriptions.add(userInfosNotificationView.onUnblock().
        subscribe(recipient -> livePresenter.updateShortcutStatus(recipient.getId(),
            ShortcutRealm.DEFAULT)));

    viewLive.initAnonymousSubscription(onAnonymousReceived());

    subscriptions.add(diceView.onNextDiceClick().
        subscribe(aVoid -> {
          viewLive.stopGameDice();

          if (live.getSource().equals(SOURCE_CALL_ROULETTE)) {
            reRollTheDiceFromCallRoulette(false);
          } else {
            reRollTheDiceFromLiveRoom();
          }
        }));

    subscriptions.add(viewLive.onChangeCallRouletteRoom().
        subscribe(aVoid -> reRollTheDiceFromCallRoulette(true)));

    subscriptions.add(
        viewLive.openGameStore().subscribe(aVoid -> navigator.navigateToGameStoreNewGame(this)));

    subscriptions.add(viewLive.onOpenLeaderboard()
        .subscribe(game -> navigator.navigateToGameLeaderboard(this, game.getId())));

    subscriptions.add(
        viewLive.onSwipeUp().subscribe(aVoid -> navigator.navigateToGameStoreNewGame(this)));

    subscriptions.add(viewLive.onAddScore()
        .filter(pair -> pair.second > 0)
        .subscribe(pair -> livePresenter.addScore(pair.first, pair.second)));

    subscriptions.add(gameManager.onCurrentUserStartGame().subscribe(game -> {
      userInfosNotificationView.setCurrentGame(game);

      if (room == null || !viewLive.hasJoined()) {
        live.setGameId(game.getId());
      } else {
        livePresenter.roomStartGame(room.getId(), game.getId());
      }
    }));

    subscriptions.add(gameManager.onCurrentUserStopGame().subscribe(game -> {
      userInfosNotificationView.setCurrentGame(null);

      if (room == null || !viewLive.hasJoined()) {
        live.setGameId(null);
      } else {
        livePresenter.roomStopGame(room.getId());
      }
    }));
  }

  private void share(boolean shouldOpenSMSDefault) {
    if (room == null) return;

    Bundle bundle = new Bundle();
    bundle.putString(TagManagerUtils.SCREEN, TagManagerUtils.LIVE);
    bundle.putString(TagManagerUtils.ACTION, TagManagerUtils.UNKNOWN);
    tagManager.trackEvent(TagManagerUtils.Invites, bundle);
    navigator.sendInviteToCall(this, TagManagerUtils.CALL, room.getLink(), null,
        shouldOpenSMSDefault);
  }

  private void reRollTheDiceFromCallRoulette(boolean isFromOthers) {
    if (isFromOthers) {
      Toast.makeText(this,
          EmojiParser.demojizedText(getString(R.string.roll_the_dice_kicked_notification)),
          Toast.LENGTH_LONG).show();
    }

    if (subscriptions.hasSubscriptions()) subscriptions.clear();
    disposeCall(true);
    initRoom();
  }

  private void reRollTheDiceFromLiveRoom() {
    viewLive.reRollTheDiceFromLiveRoom();
  }

  @Override public boolean dispatchTouchEvent(MotionEvent ev) {
    if (userInfosNotificationView.getVisibility() == VISIBLE &&
        !ViewUtils.isIn(userInfosNotificationView, (int) ev.getX(), (int) ev.getY())) {
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

  private void leave() {
    finish();
  }

  private void putExtraErrorNotif() {
    returnIntent.putExtra(ErrorNotificationView.DISPLAY_ERROR_NOTIF, true);
  }

  @Override public void onReceivedAnonymousMemberInRoom(List<User> users) {
    onAnonymousReceived.onNext(users);
  }

  private void setImageFirebase(String tribeGuestId, BaseNotifViewHolder holder) {
    subscriptions.add(InstaCapture.getInstance((Activity) context())
        .captureRx(userInfosNotificationView, notificationContainerView, screenshotView)
        .subscribe(new Subscriber<Bitmap>() {
          @Override public void onCompleted() {
          }

          @Override public void onError(Throwable e) {
          }

          @Override public void onNext(Bitmap bitmap) {
            String suffix = ".jpg";
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] data = baos.toByteArray();
            StorageReference riversRef = storageRef.child("app/uploads/reported-users/" +
                user.getId() +
                "/" +
                dateUtils.getUTCDateAsString() +
                suffix);
            UploadTask uploadTask = riversRef.putBytes(data);
            uploadTask.addOnFailureListener(exception -> {
              Timber.e("error fetching image on firebase ");
            }).addOnSuccessListener(taskSnapshot -> {
              Uri downloadUrl = taskSnapshot.getDownloadUrl();
              livePresenter.reportUser(tribeGuestId, downloadUrl.toString());

              holder.btnMore.setImageResource(R.drawable.picto_ban_active);
              holder.btnMore.setClickable(false);
              holder.progressView.animate()
                  .scaleX(0)
                  .scaleY(0)
                  .setDuration(300)
                  .withEndAction(() -> {
                    holder.progressView.setVisibility(View.GONE);
                    holder.btnMore.animate().scaleX(1).scaleY(1).setDuration(300).start();
                  })
                  .start();
              Toast.makeText((Activity) context(), getString(R.string.action_user_reported),
                  Toast.LENGTH_SHORT).show();
            });
          }
        }));
  }

  @Override public void onRoomUpdate(Room room) {
  }

  @Override public void onInvites(List<Invite> invites) {
    if (invites != null && invites.size() > 0) {
      String hashMembersShortcut = ShortcutUtils.hashShortcut(getCurrentUser().getId(),
          live.getUserIdsOfShortcut().toArray(new String[live.getUserIdsOfShortcut().size()]));

      for (Invite invite : invites) {
        List<String> usersInviteList = invite.getRoomUserIds();
        String hashMembersInvite = ShortcutUtils.hashShortcut(getCurrentUser().getId(),
            usersInviteList.toArray(new String[usersInviteList.size()]));

        if (hashMembersShortcut.equals(hashMembersInvite) &&
            invite.getRoom() != null &&
            !invite.getRoom().isUserInitiator(getCurrentUser().getId())) {
          Live.Builder builder = new Live.Builder(Live.FRIEND_CALL).source(live.getSource());
          live = builder.room(invite.getRoom()).build();
          getRoomInfos();
        }
      }
    } else {
      createRoom();
    }
  }

  @Override public void onRandomBannedUntil(String date) {
    DialogFactory.dialog(this, getString(R.string.error_just_banned_kicked_title),
        getString(R.string.error_just_banned_kicked_message),
        getString(R.string.walkthrough_action_step2), null)
        .filter(aBoolean -> aBoolean)
        .subscribeOn(Schedulers.newThread());

    finish();
  }

  @Override public void onDisplayNotificationForNewHighScore() {
    NotificationPayload notificationPayload = new NotificationPayload();
    notificationPayload.setSound("game_friend_leader.ogg");
    notificationPayload.setTitle(getString(R.string.leaderboards_you));
    notificationPayload.setUserId(user.getId());
    notificationPayload.setUserDisplayName(user.getDisplayName());
    notificationPayload.setUserPicture(user.getProfilePicture());
    notificationPayload.setBody(getString(R.string.leaderboard_new_high_score));
    notificationPayload.setClickAction(NotificationPayload.CLICK_ACTION_GAME_SCORE);
    notificationReceiver.computeNotificationPayload(this, notificationPayload);
  }

  private void displayNotification(String txt) {
    txtRemotePeerAdded.setText(txt);
    txtRemotePeerAdded.setVisibility(VISIBLE);
    Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_up_down_up);
    txtRemotePeerAdded.startAnimation(anim);
  }

  private void getRoomInfos() {
    livePresenter.getRoomInfos(live);
  }

  private void createRoom() {
    livePresenter.createRoom(live);
  }

  private void getAllInvites() {
    livePresenter.getInvites();
  }

  private void roomFull() {
    putExtraErrorNotif();
    finish();
  }

  private void roomError(WebSocketError error) {
    Bundle bundle = new Bundle();
    bundle.putString(TagManagerUtils.FEATURE, TagManagerUtils.ROOM_STATUS);
    bundle.putInt(TagManagerUtils.SIGNALING_ERROR_CODE, error.getId());
    bundle.putString(TagManagerUtils.SIGNALING_ERROR, error.getStringId());
    bundle.putString(TagManagerUtils.REASON, TagManagerUtils.WEB_SIGNALING);
    bundle.putString(TagManagerUtils.DESCRIPTION, error.getMessage());
    if (room != null) bundle.putString(TagManagerUtils.SERVER, room.getRoomCoordinates().getUrl());

    if (error.getId() == WebSocketError.ERROR_CODE_ROOM_IS_FULL) {
      roomFull();
    } else {
      Toast.makeText(getApplicationContext(), ErrorMessageFactory.create(getBaseContext(), null),
          Toast.LENGTH_LONG).show();

      finish();
    }
  }

  private void putExtraHomeIntent() {
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

  private void setExtraForShortcut() {
    if (usersThatWereLive.size() > 0 &&
        !live.getSource().equals(SOURCE_CALL_ROULETTE) &&
        !room.acceptsRandom()) {
      returnIntent.putExtra(USER_IDS_FOR_NEW_SHORTCUT, usersThatWereLive);
      setResult(Activity.RESULT_OK, returnIntent);
    }
  }

  @Override public void finish() {
    if (finished) return;

    if (stateManager.shouldDisplay(StateManager.FIRST_LEAVE_ROOM)) {
      isFirstLeaveRoom = true;
      stateManager.addTutorialKey(StateManager.FIRST_LEAVE_ROOM);
    } else {
      isFirstLeaveRoom = false;
    }

    if (!viewLive.hasJoined() &&
        room != null &&
        !StringUtils.isEmpty(room.getId()) &&
        !live.getSource().equals(SOURCE_CALL_ROULETTE)) {
      livePresenter.deleteRoom(room.getId());
    }

    finished = true;

    if (appStateMonitor != null) {
      appStateMonitor.stop();
      appStateMonitor.removeListener(this);
    }
    disposeCall(false);
    putExtraHomeIntent();
    setExtraForCallFromUnknownUser();
    setExtraForShortcut();

    super.finish();
    overridePendingTransition(R.anim.activity_in_scale, R.anim.activity_out_to_right);
  }

  public String getShortcutId() {
    return getShortcut().getId();
  }

  public Shortcut getShortcut() {
    Shortcut shortcut = null;
    if (live != null && live.getRoom() != null && live.getRoom().getShortcut() != null) {
      shortcut = live.getRoom().getShortcut();
    } else if (live != null && live.getShortcut() != null) {
      shortcut = live.getShortcut();
    } else if (chatView != null && chatView.getShortcut() != null) {
      shortcut = chatView.getShortcut();
    }
    return shortcut;
  }

  public Observable<List<User>> onAnonymousReceived() {
    return onAnonymousReceived;
  }

  ////////////////
  //   PUBLIC   //
  ////////////////

  @Override public void onSingleShortcutsLoaded(List<Shortcut> singleShortcutList) {

  }

  @Override public void randomRoomAssignedSubscriber(String roomId) {
    Timber.d("random room assigned " + roomId);
    live.setCallRouletteSessionId(roomId);
    getRoomInfos();
  }

  @Override public void fbIdUpdatedSubscriber(User userUpdated) {
    Timber.d("user fbId updated " + userUpdated.getId() + " " + userUpdated.getFbid());
  }

  @Override public void onRoomInfos(Room room) {
    if (live.fromRoom()) room.setInviter(live.getRoom().getInviter());
    this.room = room;

    if (this.room.getShortcut() == null) {
      livePresenter.shortcutForUserIds(live.getUserIdsOfShortcut());
    }

    live.setRoom(room);
    viewLive.joinRoom(this.room);
  }

  @Override public void onRoomFull(String message) {
    roomFull();
  }

  @Override public void onRoomInfosError(String message) {
    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    finish();
  }

  @Override public void onShortcut(Shortcut shortcut) {
    live.setShortcut(shortcut);
  }

  @Override public void onShortcutCreatedError() {
    Toast.makeText(context(),
        EmojiParser.demojizedText(context().getString(R.string.add_friend_error_invisible)),
        Toast.LENGTH_SHORT).show();
  }

  @Override public void onShortcutRemovedSuccess() {

  }

  @Override public void onShortcutRemovedError() {

  }

  @Override public void onShortcutUpdatedSuccess(Shortcut shortcut, BaseListViewHolder viewHolder) {

  }

  @Override public void onShortcutUpdatedError() {

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

  @Override public void onBackPressed() {
    if (!FacebookUtils.isLoggedIn() && live.getSource().equals(SOURCE_CALL_ROULETTE)) {
      super.onBackPressed();
    }
    // disable back btn
  }
}

