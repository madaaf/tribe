package com.tribe.app.presentation.view.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
import com.tarek360.instacapture.InstaCapture;
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
import com.tribe.app.presentation.utils.preferences.CallTagsMap;
import com.tribe.app.presentation.utils.preferences.PreferencesUtils;
import com.tribe.app.presentation.utils.preferences.RoutingMode;
import com.tribe.app.presentation.view.component.TileView;
import com.tribe.app.presentation.view.component.live.LiveContainer;
import com.tribe.app.presentation.view.component.live.LiveInviteView;
import com.tribe.app.presentation.view.component.live.LiveView;
import com.tribe.app.presentation.view.listener.AnimationListenerAdapter;
import com.tribe.app.presentation.view.notification.Alerter;
import com.tribe.app.presentation.view.notification.NotificationPayload;
import com.tribe.app.presentation.view.notification.NotificationUtils;
import com.tribe.app.presentation.view.utils.BitmapUtils;
import com.tribe.app.presentation.view.utils.Constants;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.RuntimePermissionUtil;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.app.presentation.view.utils.StateManager;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.app.presentation.view.widget.CreateGroupNotificationView;
import com.tribe.app.presentation.view.widget.LiveNotificationView;
import com.tribe.app.presentation.view.widget.NotificationContainerView;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.model.TribePeerMediaConfiguration;
import com.tribe.tribelivesdk.stream.TribeAudioManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

import static android.view.View.VISIBLE;

public class LiveActivity extends BaseActivity implements LiveMVPView, AppStateListener {

  private static final String EXTRA_LIVE = "EXTRA_LIVE";
  public static final String ROOM_ID = "ROOM_ID";
  public static final int FLASH_DURATION = 500;
  public static final String TIMEOUT_RATING_NOTIFICATON = "TIMEOUT_RATING_NOTIFICATON";
  private final int MAX_DURATION_WAITING_LIVE = 8;
  private final int MIN_LIVE_DURATION_TO_DISPLAY_RATING_NOTIF = 30;
  private final int MIN_DURATION_BEFORE_DISPLAY_TUTORIAL_DRAG_GUEST = 3;
  private final int CORNER_SCREENSHOT = 5;
  private final int SCREENSHOT_DURATION = 300;
  private final int SCALE_DOWN_SCREENSHOT_DURATION = 600;

  public static Intent getCallingIntent(Context context, Recipient recipient, int color) {
    Intent intent = new Intent(context, LiveActivity.class);

    Live.Builder builder = new Live.Builder(recipient.getId(), recipient.getSubId()).color(color)
        .displayName(recipient.getDisplayName())
        .isGroup(recipient.isGroup())
        .countdown(!recipient.isLive())
        .picture(recipient.getProfilePicture());

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
      String picture, String name, String sessionId) {
    Intent intent = new Intent(context, LiveActivity.class);

    Live live = new Live.Builder(recipientId, recipientId).displayName(name)
        .isGroup(isGroup)
        .picture(picture)
        .countdown(StringUtils.isEmpty(sessionId))
        .sessionId(sessionId)
        .intent(true)
        .build();

    intent.putExtra(EXTRA_LIVE, live);

    return intent;
  }

  @Inject NotificationManagerCompat notificationManager;

  @Inject SoundManager soundManager;

  @Inject ScreenUtils screenUtils;

  @Inject LivePresenter livePresenter;

  @Inject StateManager stateManager;

  @Inject @RoutingMode Preference<String> routingMode;

  @Inject @CallTagsMap Preference<String> callTagsMap;

  @BindView(R.id.viewLive) LiveView viewLive;

  @BindView(R.id.viewInviteLive) LiveInviteView viewInviteLive;

  @BindView(R.id.viewLiveContainer) LiveContainer viewLiveContainer;

  @BindView(R.id.remotePeerAdded) TextViewFont txtRemotePeerAdded;

  @BindView(R.id.viewScreenShot) ImageView viewScreenShot;

  @BindView(R.id.viewBGScreenshot) View viewBGScreenshot;

  @BindView(R.id.viewFlash) FrameLayout viewFlash;

  // VARIABLES
  private TribeAudioManager audioManager;
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
  private boolean takeScreenshotEnable = true;
  private List<String> usersIdsInvitedInLiveRoom = new ArrayList<>();
  private List<String> activeUersIdsInvitedInLiveRoom = new ArrayList<>();
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
    initAppState();
    initRemoteConfig();
  }

  @Override protected void onNewIntent(Intent intent) {
    viewLive.dispose(true);
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

  @Override protected void onRestart() {
    super.onRestart();
    notificationManager.cancelAll();
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
  }

  @Override protected void onPause() {
    if (receiverRegistered) {
      unregisterReceiver(notificationReceiver);
      receiverRegistered = false;
    }

    super.onPause();
  }

  @Override protected void onDestroy() {
    appStateMonitor.removeListener(this);
    appStateMonitor.stop();

    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    soundManager.cancelMediaPlayer();

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

        if (live.isGroup()) {
          viewLive.start(live);
          livePresenter.loadRecipient(live);
        } else if (!StringUtils.isEmpty(live.getSessionId())) {
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
    appStateMonitor = RxAppStateMonitor.create(getApplication());
    appStateMonitor.addListener(this);
    appStateMonitor.start();
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

  private Boolean displayRatingNotifDependingFirebaseTrigger() {
    Random rn = new Random();
    int randomNumber = rn.nextInt(101);
    return randomNumber > firebaseRemoteConfig.getLong(Constants.FIREBASE_RATING_NOTIF_TRIGGER);
  }

  private Long getFirebaseTimeoutConfig() {
    Long time = firebaseRemoteConfig.getLong(Constants.FIREBASE_RATING_NOTIF_TIMEOUT);
    return time == 0 ? 10 : time;
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

  private boolean isRoomHasGuest() {
    if (live.isInvite()) return true;
    if (live.isGroup()) {
      //int nbrPersonneInGrp = live.isGroup();
    }
    return false;
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
        .subscribe(filteredFriendships -> {
          viewInviteLive.renderFriendshipList(filteredFriendships);
        }));

    subscriptions.add(viewLive.onShouldJoinRoom().subscribe(shouldJoin -> {
      viewLiveContainer.setEnabled(true);
      joinRoom();
      displayStartFirstPopupTutorial();
    }));

    subscriptions.add(viewLive.onJoined().subscribe(tribeJoinRoom -> {
      if (!live.isGroup() && tribeJoinRoom.getRoomSize() < 2) {
        viewLiveContainer.openInviteView();
        if (stateManager.shouldDisplay(StateManager.DRAGGING_GUEST)) {
          subscriptions.add(
              Observable.timer(MIN_DURATION_BEFORE_DISPLAY_TUTORIAL_DRAG_GUEST, TimeUnit.SECONDS)
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(aVoid -> displayDragingGuestPopupTutorial()));
        }
      }
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
      navigator.openSmsForInvite(this, null);
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
      ratingNotificationSubscribe();
      displayNotification(getString(R.string.live_notification_peer_joined, userName));
    }));

    subscriptions.add(viewLive.onNotificationonRemotePeerBuzzed().subscribe(aVoid -> {
      displayNotification(getString(R.string.live_notification_buzzed));
    }));

    subscriptions.add(viewLive.onScreenshot().subscribe(aVoid -> {
      if (RuntimePermissionUtil.checkPermission(context(), this)) {
        takeScreenshot();
      }
    }));
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
                  takeScreenshot();
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

  private void takeScreenshot() {
    if (takeScreenshotEnable) {
      takeScreenshotEnable = false;
      subscriptions.add(
          InstaCapture.getInstance(this).captureRx().subscribe(new Subscriber<Bitmap>() {
            @Override public void onCompleted() {
            }

            @Override public void onError(Throwable e) {
            }

            @Override public void onNext(Bitmap bitmap) {
              viewLive.screenshotDone();

              Bitmap bitmapWatermarked =
                  BitmapUtils.watermarkBitmap(screenUtils, getResources(), bitmap);

              Bitmap roundedBitmap =
                  UIUtils.getRoundedCornerBitmap(bitmapWatermarked, Color.WHITE, CORNER_SCREENSHOT,
                      CORNER_SCREENSHOT * 2, context());

              boolean result =
                  BitmapUtils.saveScreenshotToDefaultDirectory(context(), bitmapWatermarked);

              viewScreenShot.setImageBitmap(roundedBitmap);
              viewScreenShot.setVisibility(View.VISIBLE);
              viewScreenShot.animate()
                  .alpha(1f)
                  .setDuration(SCREENSHOT_DURATION)
                  .setStartDelay(FLASH_DURATION)
                  .setListener(new AnimatorListenerAdapter() {
                    @Override public void onAnimationEnd(Animator animation) {
                      viewScreenShot.animate().setListener(null).start();
                      setScreenShotAnimation();
                    }
                  })
                  .start();

              viewFlash.animate()
                  .setDuration(FLASH_DURATION)
                  .alpha(1f)
                  .withEndAction(() -> viewFlash.animate()
                      .setDuration(FLASH_DURATION)
                      .alpha(0f)
                      .withEndAction(() -> viewFlash.animate().setListener(null).start()));

              viewBGScreenshot.setVisibility(View.VISIBLE);
              subscriptions.add(Observable.timer(2000 + SCREENSHOT_DURATION, TimeUnit.MILLISECONDS)
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(aLong -> viewBGScreenshot.setVisibility(View.GONE)));
            }
          }));
    }
  }

  private void setScreenShotAnimation() {
    Animation scaleAnim = AnimationUtils.loadAnimation(this, R.anim.screenshot_anim);
    scaleAnim.setAnimationListener(new AnimationListenerAdapter() {

      @Override public void onAnimationEnd(Animation animation) {
        super.onAnimationEnd(animation);
        viewScreenShot.setAlpha(0f);
        viewScreenShot.setVisibility(View.GONE);
        takeScreenshotEnable = true;

        Toast.makeText(LiveActivity.this,
            EmojiParser.demojizedText(getString(R.string.live_screenshot_saved_toast)),
            Toast.LENGTH_SHORT).show();

        animation.setAnimationListener(null);
      }
    });

    viewScreenShot.startAnimation(scaleAnim);
  }

  private void putExtraHomeIntent() {
    boolean willDisplayPopup = false;
    Intent returnIntent = new Intent();

    if (liveDurationIsMoreThan30sec && displayRatingNotifDependingFirebaseTrigger()) {
      if (roomConfiguration != null && !StringUtils.isEmpty(roomConfiguration.getRoomId())) {
        returnIntent.putExtra(ROOM_ID, roomConfiguration.getRoomId());
      }
      returnIntent.putExtra(NotificationContainerView.DISPLAY_RATING_NOTIFICATON, true);
      returnIntent.putExtra(TIMEOUT_RATING_NOTIFICATON, getFirebaseTimeoutConfig());
      willDisplayPopup = true;
    }

    if (!willDisplayPopup) {
      TagManagerUtils.manageTags(tagManager, PreferencesUtils.getMapFromJson(callTagsMap));
      callTagsMap.set("");
    }

    returnIntent.putExtra(DISPLAY_ENJOYING_NOTIFICATON, true);

    List<TribeGuest> peopleInLive = viewLive.getUsersInLiveRoom();
    for (TribeGuest guest : peopleInLive) {
      if (usersIdsInvitedInLiveRoom.contains(guest.getId())) {
        activeUersIdsInvitedInLiveRoom.add(guest.getId());
      }
    }

    if ((liveIsInvite || !activeUersIdsInvitedInLiveRoom.isEmpty()) && peopleInLive.size() > 1) {
      returnIntent.putExtra(NotificationContainerView.DISPLAY_CREATE_GROUPE_NOTIFICATION, true);
      Bundle extra = new Bundle();
      extra.putSerializable(CreateGroupNotificationView.PREFILLED_GRP_MEMBERS,
          (Serializable) peopleInLive);
      returnIntent.putExtras(extra);
      liveIsInvite = false;
      usersIdsInvitedInLiveRoom.clear();
      activeUersIdsInvitedInLiveRoom.clear();
    }

    setResult(Activity.RESULT_OK, returnIntent);
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

  private void displayDragingGuestPopupTutorial() {
    if (stateManager.shouldDisplay(StateManager.DRAGGING_GUEST)) {
      subscriptions.add(DialogFactory.dialog(this, getString(R.string.tips_draggingguest_title),
          getString(R.string.tips_draggingguest_message),
          getString(R.string.tips_draggingguest_action1), null).subscribe(a -> {
      }));
      stateManager.addTutorialKey(StateManager.DRAGGING_GUEST);
    }
  }

  private void ready() {
    viewLive.update(live);
  }

  @Override public void finish() {
    viewLive.dispose(false);
    putExtraHomeIntent();
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
    } else if (recipient instanceof Friendship) {
      live.setId(recipient.getId());
      viewLive.start(live);
    }

    ready();
  }

  @Override public void renderFriendshipList(List<Friendship> friendshipList) {
    onUpdateFriendshipList.onNext(friendshipList);
  }

  @Override public void onJoinedRoom(RoomConfiguration roomConfiguration) {
    this.roomConfiguration = roomConfiguration;
    this.roomConfiguration.setRoutingMode(routingMode.get());
    viewLive.joinRoom(this.roomConfiguration);
    if (!live.isGroup() && StringUtils.isEmpty(live.getSessionId())) {
      livePresenter.inviteUserToRoom(this.roomConfiguration.getRoomId(), live.getSubId());
    }
  }

  @Override public void onJoinRoomFailed(String message) {
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

  class NotificationReceiver extends BroadcastReceiver {

    @Override public void onReceive(Context context, Intent intent) {
      NotificationPayload notificationPayload =
          (NotificationPayload) intent.getSerializableExtra(BroadcastUtils.NOTIFICATION_PAYLOAD);

      if (live.getSubId().equals(notificationPayload.getGroupId()) || (live.getSessionId() != null
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
                    false, false, null, true);
                invite(tribeGuest.getId());
                viewLive.addTribeGuest(tribeGuest);
              }
            }));

        Alerter.create(LiveActivity.this, liveNotificationView).show();
      }
    }
  }
}

