package com.tribe.app.presentation.view.component.live;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.R;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.domain.entity.Live;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManager;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.preferences.CallTagsMap;
import com.tribe.app.presentation.utils.preferences.CounterOfCallsForGrpButton;
import com.tribe.app.presentation.utils.preferences.MinutesOfCalls;
import com.tribe.app.presentation.utils.preferences.NumberOfCalls;
import com.tribe.app.presentation.view.activity.LiveActivity;
import com.tribe.app.presentation.view.component.live.game.GameManagerView;
import com.tribe.app.presentation.view.utils.Degrees;
import com.tribe.app.presentation.view.utils.DoubleUtils;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.app.presentation.view.utils.StateManager;
import com.tribe.tribelivesdk.TribeLiveSDK;
import com.tribe.tribelivesdk.back.TribeLiveOptions;
import com.tribe.tribelivesdk.back.WebSocketConnection;
import com.tribe.tribelivesdk.core.WebRTCRoom;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameManager;
import com.tribe.tribelivesdk.model.RemotePeer;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.model.TribeJoinRoom;
import com.tribe.tribelivesdk.model.TribePeerMediaConfiguration;
import com.tribe.tribelivesdk.model.TribeSession;
import com.tribe.tribelivesdk.model.error.WebSocketError;
import com.tribe.tribelivesdk.util.JsonUtils;
import com.tribe.tribelivesdk.util.ObservableRxHashMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.tribe.app.presentation.view.activity.LiveActivity.SOURCE_CALL_ROULETTE;

/**
 * Created by tiago on 01/18/2017.
 */
public class LiveView extends FrameLayout {

  public static final int LIVE_MAX = 8;

  private static final int DURATION = 300;

  private static final float MARGIN_BOTTOM = 25;
  private static boolean joinLive = false;

  @Inject SoundManager soundManager;

  @Inject User user;

  @Inject ScreenUtils screenUtils;

  @Inject AccessToken accessToken;

  @Inject TribeLiveSDK tribeLiveSDK;

  @Inject TagManager tagManager;

  @Inject StateManager stateManager;

  @Inject PaletteGrid paletteGrid;

  @Inject @NumberOfCalls Preference<Integer> numberOfCalls;

  @Inject @CounterOfCallsForGrpButton Preference<Integer> counterOfCallsForGrpButton;

  @Inject @MinutesOfCalls Preference<Float> minutesOfCalls;

  @Inject @CallTagsMap Preference<String> callTagsMap;

  @BindView(R.id.viewRoom) LiveRoomView viewRoom;

  @BindView(R.id.viewControlsLive) LiveControlsView viewControlsLive;

  @BindView(R.id.viewDarkOverlay) View viewDarkOverlay;

  @BindView(R.id.viewRinging) LiveRingingView viewRinging;

  @BindView(R.id.viewLiveInvite) LiveInviteView viewLiveInvite;

  @BindView(R.id.viewShadow) View viewShadow;

  @BindView(R.id.viewLocalLive) LiveLocalView viewLocalLive;

  // VARIABLES
  private Live live;
  private WebRTCRoom webRTCRoom;
  private ObservableRxHashMap<String, LiveRowView> liveRowViewMap;
  private ObservableRxHashMap<String, TribeGuest> tribeGuestMap;
  private boolean hiddenControls = false;
  private Map<String, Object> tagMap;
  private int wizzCount = 0, screenshotCount = 0, invitedCount = 0, totalSizeLive = 0,
      totalSizeGameLive = 0, interval = 0, intervalGame = 0;
  private double averageCountLive = 0.0D, averageCountGameLive = 0.0D;
  private boolean hasJoined = false;
  private long timeStart = 0L, timeEnd = 0L;
  private boolean isParamExpended = false, isMicroActivated = true, isCameraActivated = true,
      hasShared = false;
  private View view;
  private List<User> anonymousInLive = new ArrayList<>();
  private boolean isFirstToJoin = true;
  private double duration;
  private GameManager gameManager;
  private String fbId;
  private GameManagerView viewGameManager;

  // RESOURCES
  private int statusBarHeight;

  // OBSERVABLES
  private Unbinder unbinder;
  private CompositeSubscription persistentSubscriptions = new CompositeSubscription();
  private CompositeSubscription tempSubscriptions = new CompositeSubscription();
  private Subscription callGameDurationSubscription, callGameAverageSubscription;

  private PublishSubject<String> onBuzzPopup = PublishSubject.create();
  private PublishSubject<Void> onShouldJoinRoom = PublishSubject.create();
  private PublishSubject<Void> onNotify = PublishSubject.create();
  private PublishSubject<Void> onLeave = PublishSubject.create();
  private PublishSubject<Long> onEndCall = PublishSubject.create();
  private PublishSubject<Void> onLeaveRoom = PublishSubject.create();
  private PublishSubject<Void> onScreenshot = PublishSubject.create();
  private PublishSubject<Boolean> onHiddenControls = PublishSubject.create();
  private PublishSubject<Void> onShouldCloseInvites = PublishSubject.create();
  private PublishSubject<String> onRoomStateChanged = PublishSubject.create();
  private PublishSubject<String> unlockRollTheDice = PublishSubject.create();
  private PublishSubject<String> unlockedRollTheDice = PublishSubject.create();
  private PublishSubject<TribeJoinRoom> onJoined = PublishSubject.create();
  private PublishSubject<String> onRollTheDice = PublishSubject.create();
  private PublishSubject<WebSocketError> onRoomError = PublishSubject.create();
  private PublishSubject<Void> onChangeCallRouletteRoom = PublishSubject.create();
  private PublishSubject<Object> onRemotePeerClick = PublishSubject.create();
  private PublishSubject<String> onDismissInvite = PublishSubject.create();
  private PublishSubject<Boolean> onOpenChat = PublishSubject.create();
  private PublishSubject<Boolean> onOpenInvite = PublishSubject.create();
  private PublishSubject<Boolean> onTouchEnabled = PublishSubject.create();

  private PublishSubject<String> onNotificationRemotePeerInvited = PublishSubject.create();
  private PublishSubject<String> onNotificationRemotePeerRemoved = PublishSubject.create();
  private PublishSubject<String> onNotificationRemoteWaiting = PublishSubject.create();
  private PublishSubject<String> onNotificationRemotePeerBuzzed = PublishSubject.create();
  private PublishSubject<String> onNotificationRemoteJoined = PublishSubject.create();
  private PublishSubject<String> onNotificationGameStarted = PublishSubject.create();
  private PublishSubject<String> onNotificationGameStopped = PublishSubject.create();
  private PublishSubject<String> onNotificationGameRestart = PublishSubject.create();
  private PublishSubject<String> onAnonymousJoined = PublishSubject.create();

  public LiveView(Context context) {
    super(context);
    init();
  }

  public LiveView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public void jump() {
    if (webRTCRoom != null) webRTCRoom.jump();
  }

  public double getDuration() {
    return duration;
  }

  public void setSourceLive(@LiveActivity.Source String source) {
    viewRoom.setSource(source);
  }

  public void hideGamesBtn() {
    viewControlsLive.hideGamesBtn();
  }

  public void displayGamesBtn() {
    viewControlsLive.displayGamesBtn();
  }

  public View getLayoutStream() {
    return liveRowViewMap.getMap().entrySet().iterator().next().getValue();
  }

  public void endCall(boolean isJump) {
    String state = TagManagerUtils.CANCELLED;

    if (live != null) {
      duration = 0.0D;
      Long durationInSeconds = 0L;

      if (timeStart > 0) {
        timeEnd = System.currentTimeMillis();
        long delta = timeEnd - timeStart;
        duration = (double) delta / 60000.0;
        duration = DoubleUtils.round(duration, 2);
        durationInSeconds = delta / 1000L;
      }

      if (hasJoined && averageCountLive > 1) {
        state = TagManagerUtils.ENDED;
        counterOfCallsForGrpButton.set(counterOfCallsForGrpButton.get() + 1);
        numberOfCalls.set(numberOfCalls.get() + 1);
        Float totalDuration = minutesOfCalls.get() + (float) duration;
        minutesOfCalls.set(totalDuration);
        tagManager.increment(TagManagerUtils.USER_CALLS_COUNT);
        tagManager.increment(TagManagerUtils.USER_CALLS_MINUTES, duration);

        onEndCall.onNext(durationInSeconds);
      } else if ((hasJoined && averageCountLive <= 1 && !live.getType().equals(Live.NEW_CALL)) ||
          (live.getType().equals(Live.NEW_CALL) && (invitedCount > 0 || hasShared))) {
        state = TagManagerUtils.MISSED;
        tagManager.increment(TagManagerUtils.USER_CALLS_MISSED_COUNT);
      }

      tagMap.put(TagManagerUtils.EVENT, TagManagerUtils.Calls);
      tagMap.put(TagManagerUtils.SOURCE, live.getSource());
      tagMap.put(TagManagerUtils.SECTION, live.getSection());
      tagMap.put(TagManagerUtils.GESTURE, live.getGesture());
      tagMap.put(TagManagerUtils.IS_CALL_ROULETTE, live.isDiceDragedInRoom());
      tagMap.put(TagManagerUtils.DURATION, duration);
      tagMap.put(TagManagerUtils.STATE, state);
      tagMap.put(TagManagerUtils.MEMBERS_INVITED, invitedCount);
      tagMap.put(TagManagerUtils.WIZZ_COUNT, wizzCount);
      tagMap.put(TagManagerUtils.SCREENSHOT_COUNT, screenshotCount);
      tagMap.put(TagManagerUtils.TYPE, TagManagerUtils.DIRECT);

      TagManagerUtils.manageTags(tagManager, tagMap);
    }

    for (LiveRowView liveRowView : liveRowViewMap.getMap().values()) {
      Timber.d("liverowview dispose");
      liveRowView.dispose();
      viewRoom.removeView(liveRowView.getGuest().getId(), liveRowView);
    }

    liveRowViewMap.clear();
    tribeGuestMap.clear();

    if (webRTCRoom != null && !isJump) {
      Timber.d("webRTCRoom leave");
      webRTCRoom.leaveRoom();
    }

    if (!isJump) {
      Timber.d("dispose !isJump");
      persistentSubscriptions.unsubscribe();
      viewLocalLive.dispose();
      gameManager.disposeLive();
      if (viewGameManager != null) viewGameManager.dispose();
      tempSubscriptions.unsubscribe();
    } else {
      tempSubscriptions.clear();
    }
  }

  public void dispose(boolean isJump) {
    Timber.d("disposing");

    viewControlsLive.dispose();

    Timber.d("isJump : " + isJump);
    if (!isJump) {
      Timber.d("dispose !isJump");
      unbinder.unbind();
    }
  }

  @Override protected void onFinishInflate() {
    view = LayoutInflater.from(getContext()).inflate(R.layout.view_live, this);
    unbinder = ButterKnife.bind(this);
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);

    viewLiveInvite.updateWidth(getLiveInviteViewPartialWidth());

    gameManager = GameManager.getInstance(getContext());

    initResources();
    initUI();
    initSubscriptions();

    super.onFinishInflate();
  }

  @Override protected void onConfigurationChanged(Configuration newConfig) {
    if (viewControlsLive == null) return;

    if (webRTCRoom != null) {
      webRTCRoom.sendOrientation(Degrees.getNormalizedDegrees(getContext()),
          viewLocalLive.isFrontFacing());
    }

    onShouldCloseInvites.onNext(null);

    ViewGroup.LayoutParams lp = view.getLayoutParams();
    lp.width = screenUtils.getWidthPx();
    lp.height = screenUtils.getHeightPx() - statusBarHeight;
    view.setLayoutParams(lp);
  }

  private void updateInviteViewWidth(int width) {
    viewLiveInvite.updateWidth(width);
  }

  //////////////////////
  //      INIT        //
  //////////////////////

  private void init() {
    liveRowViewMap = new ObservableRxHashMap<>();
    tribeGuestMap = new ObservableRxHashMap<>();
    tagMap = new HashMap<>();

    viewGameManager = new GameManagerView(getContext());
  }

  private void initUI() {
    setBackgroundColor(Color.BLACK);
  }

  private void initResources() {
    statusBarHeight = 0;
    int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
    if (resourceId > 0) {
      statusBarHeight = getResources().getDimensionPixelSize(resourceId);
    }
  }

  private void setAlphaOnGuestWhenHideControls(boolean hiddenControls) {
    if (hiddenControls) {
      for (LiveRowView liveRowView : liveRowViewMap.getMap().values()) {
        if (!liveRowView.isWaiting()) {
          liveRowView.setAlphaOnBackground(0.5f);
        }
      }
    } else {
      for (LiveRowView liveRowView : liveRowViewMap.getMap().values()) {
        liveRowView.setAlphaOnBackground(1f);
      }
    }
  }

  private void initSubscriptions() {
    persistentSubscriptions.add(onHiddenControls().doOnNext(aBoolean -> {
      hiddenControls = aBoolean;
      viewLocalLive.hideControls(!hiddenControls);
      setAlphaOnGuestWhenHideControls(hiddenControls);
    }).subscribe());

    persistentSubscriptions.add(
        viewControlsLive.onOpenInvite().subscribe(aBoolean -> onOpenInvite.onNext(true)));

    persistentSubscriptions.add(
        viewControlsLive.onCloseInvite().subscribe(aBoolean -> onOpenInvite.onNext(false)));

    persistentSubscriptions.add(
        viewControlsLive.onOpenChat().subscribe(aBoolean -> openChat(true)));

    persistentSubscriptions.add(
        viewControlsLive.onCloseChat().subscribe(aBoolean -> openChat(false)));

    persistentSubscriptions.add(viewControlsLive.onClickCameraOrientation().subscribe(aVoid -> {
      viewLocalLive.switchCamera();
      if (webRTCRoom != null) {
        webRTCRoom.sendOrientation(Degrees.getNormalizedDegrees(getContext()),
            viewLocalLive.isFrontFacing());
      }
    }));

    persistentSubscriptions.add(viewControlsLive.onClickMicro().subscribe(aBoolean -> {
      isMicroActivated = aBoolean;
      viewControlsLive.setMicroEnabled(isMicroActivated);
      viewLocalLive.enableMicro(isMicroActivated, TribePeerMediaConfiguration.USER_UPDATE);
    }));

    persistentSubscriptions.add(viewControlsLive.onClickParamExpand().subscribe(expanded -> {
      isParamExpended = expanded;
      onHiddenControls.onNext(isParamExpended);
    }));

    persistentSubscriptions.add(viewControlsLive.onClickCameraEnable().subscribe(aVoid -> {
      isCameraActivated = false;
      viewLocalLive.disableCamera(true, TribePeerMediaConfiguration.USER_UPDATE);
    }));

    persistentSubscriptions.add(viewControlsLive.onClickCameraDisable().subscribe(aVoid -> {
      isCameraActivated = true;
      viewLocalLive.enableCamera(true);
    }));

    viewControlsLive.onClickFilter().subscribe(aVoid -> viewLocalLive.switchFilter());

    persistentSubscriptions.add(viewGameManager.onRestartGame()
        .subscribe(game -> onRestartGame(gameManager.getCurrentGame())));

    viewGameManager.initPeerGuestObservable(tribeGuestMap.getObservable());
    viewGameManager.initLiveViewsObservable(viewRoom.onLiveViewsChange());

    gameManager.initUIControlsStartGame(viewControlsLive.onStartGame());
    gameManager.initUIControlsRestartGame(viewControlsLive.onRestartGame());
    gameManager.initUIControlsStopGame(viewControlsLive.onStopGame());
    gameManager.initUIControlsResetGame(viewControlsLive.onResetScores());

    persistentSubscriptions.add(gameManager.onCurrentUserStartGame().subscribe(game -> {
      startGameStats(game.getId());
      displayStartGameNotification(game.getTitle(), user.getDisplayName());
      restartGame(game);
    }));

    persistentSubscriptions.add(
        gameManager.onCurrentUserNewSessionGame().subscribe(game -> onRestartGame(game)));

    persistentSubscriptions.add(
        gameManager.onRemoteUserNewSessionGame().subscribe(pairSessionGame -> {
          if (pairSessionGame.second != null) {
            String displayName = getDisplayNameFromSession(pairSessionGame.first);
            displayReRollGameNotification(pairSessionGame.second.getId(), displayName);
            startGame(pairSessionGame.second, false);
          }
        }));

    persistentSubscriptions.add(gameManager.onRemoteUserStartGame().subscribe(pairSessionGame -> {
      if (pairSessionGame.second != null) {
        startGameStats(pairSessionGame.second.getId());
        String displayName = getDisplayNameFromSession(pairSessionGame.first);
        displayStartGameNotification(pairSessionGame.second.getTitle(), displayName);
        startGame(pairSessionGame.second, false);
      }
    }));

    persistentSubscriptions.add(gameManager.onCurrentUserStopGame().subscribe(game -> {
      stopGame();
      displayStopGameNotification(game.getTitle(), user.getDisplayName());
    }));

    persistentSubscriptions.add(gameManager.onRemoteUserStopGame().subscribe(pairSessionGame -> {
      Game game = pairSessionGame.second;
      String displayName = getDisplayNameFromSession(pairSessionGame.first);
      displayStopGameNotification(game.getTitle(), displayName);
      stopGame();
    }));
  }

  private void openChat(boolean open) {
    if (open) {
      viewDarkOverlay.animate()
          .setInterpolator(new DecelerateInterpolator())
          .alpha(1)
          .setDuration(DURATION)
          .start();

      viewRinging.hide();
    } else {
      viewDarkOverlay.animate()
          .setInterpolator(new DecelerateInterpolator())
          .alpha(0)
          .setDuration(DURATION)
          .start();

      viewRinging.show();
    }

    onOpenChat.onNext(open);
  }

  ///////////////////
  //    CLICKS     //
  ///////////////////

  ///////////////////
  //    PUBLIC     //
  ///////////////////

  public void joinRoom(Room room) {
    Map<String, String> headers = new HashMap<>();
    headers.put(WebSocketConnection.ORIGIN, com.tribe.app.BuildConfig.TRIBE_ORIGIN);
    TribeLiveOptions options = new TribeLiveOptions.TribeLiveOptionsBuilder(getContext()).wsUrl(
        room.getRoomCoordinates().getUrl())
        .tokenId(accessToken.getAccessToken())
        .iceServers(room.getRoomCoordinates().getIceServers())
        .roomId(room.getId())
        .routingMode(TribeLiveOptions.ROUTED)
        .headers(headers)
        .orientation(Degrees.getNormalizedDegrees(getContext()))
        .frontCamera(viewLocalLive.isFrontFacing())
        .build();

    tempSubscriptions.add(webRTCRoom.onRoomStateChanged().onBackpressureDrop().subscribe(state -> {
      Timber.d("Room state change : " + state);
      if (state == WebRTCRoom.STATE_CONNECTED) {
        timeStart = System.currentTimeMillis();

        tempSubscriptions.add(Observable.interval(10, TimeUnit.SECONDS, Schedulers.computation())
            .onBackpressureDrop()
            .subscribe(intervalCount -> {
              interval++;
              totalSizeLive += nbLiveInRoom();
              averageCountLive = (double) totalSizeLive / interval;
              averageCountLive = DoubleUtils.round(averageCountLive, 2);

              tagMap.put(TagManagerUtils.AVERAGE_MEMBERS_COUNT, averageCountLive);
            }));
      }
    }));

    tempSubscriptions.add(
        webRTCRoom.unlockRollTheDice().onBackpressureDrop().subscribe(unlockRollTheDice));

    tempSubscriptions.add(
        webRTCRoom.unlockedRollTheDice().onBackpressureDrop().subscribe(unlockedRollTheDice));

    tempSubscriptions.add(webRTCRoom.onJoined()
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(tribeJoinRoom -> hasJoined = true)
        .doOnNext(tribeJoinRoom -> {
          // TODO SEE WITH #backend solution to launch game in call roulette
          if (!StringUtils.isEmpty(live.getGameId()) &&
              !live.getSource().equals(SOURCE_CALL_ROULETTE) &&
              StringUtils.isEmpty(room.getGameId())) {
            viewControlsLive.startGame(gameManager.getGameById(live.getGameId()));
          }
        })
        .subscribe(onJoined));

    tempSubscriptions.add(webRTCRoom.onShouldLeaveRoom().onBackpressureDrop().subscribe(onLeave));

    tempSubscriptions.add(webRTCRoom.onRoomError().onBackpressureDrop().subscribe(onRoomError));

    tempSubscriptions.add(webRTCRoom.onRemotePeerAdded()
        .onBackpressureDrop()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(remotePeer -> {
          if (isFirstToJoin) {
            viewRinging.stopRinging();
            viewRinging.setVisibility(View.GONE);
            isFirstToJoin = !isFirstToJoin;
          }

          soundManager.playSound(SoundManager.JOIN_CALL, SoundManager.SOUND_MAX);
          joinLive = true;

          tribeGuestMap.put(remotePeer.getSession().getUserId(), guestFromRemotePeer(remotePeer));

          String username = getDisplayNameFromId(remotePeer.getSession().getUserId());

          if ((username == null || username.isEmpty()) && !remotePeer.getSession().isExternal()) {
            Timber.d("anonymous joinded in room with id : " + remotePeer.getSession().getUserId());
            onAnonymousJoined.onNext(remotePeer.getSession().getUserId());
          }

          Timber.d("Remote peer added with id : " +
              remotePeer.getSession().getPeerId() +
              " & view : " +
              remotePeer.getPeerView());
          addView(remotePeer);
          onNotificationRemoteWaiting.onNext(getDisplayNameFromSession(remotePeer.getSession()));

          webRTCRoom.sendToPeer(remotePeer, getInvitedPayload(), true);

          live.getRoom().userJoinedWebRTC(remotePeer.getSession().getUserId());

          LiveRowView row = liveRowViewMap.get(remotePeer.getSession().getUserId());
          if (row != null) row.guestAppear();
          tempSubscriptions.add(remotePeer.getPeerView()
              .onNotificationRemoteJoined()
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(aVoid -> onNotificationRemoteJoined.onNext(
                  getDisplayNameFromSession(remotePeer.getSession()))));
        }));

    tempSubscriptions.add(webRTCRoom.onRemotePeerRemoved()
        .onBackpressureDrop()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(remotePeer -> {
          soundManager.playSound(SoundManager.QUIT_CALL, SoundManager.SOUND_MAX);

          Timber.d("Remote peer removed with id : " + remotePeer.getSession().getPeerId());
          removeFromPeers(remotePeer.getSession().getUserId());

          viewRoom.removeView(remotePeer.getSession().getUserId(),
              viewRoom.getLiveRowViewFromId(remotePeer.getSession().getUserId()));

          if (shouldLeave()) {
            if (!live.getSource().equals(SOURCE_CALL_ROULETTE)) {
              onLeave.onNext(null);
            } else {
              stopGame();
            }
          }

          live.getRoom().userLeftWebRTC(remotePeer.getSession().getUserId());

          onNotificationRemotePeerRemoved.onNext(
              getDisplayNameFromSession(remotePeer.getSession()));
        }));

    tempSubscriptions.add(
        webRTCRoom.onReceivedStream().onBackpressureDrop().subscribe(remotePeer -> {
          LiveRowView row = liveRowViewMap.get(remotePeer.getSession().getUserId());

          if (row != null) {
            row.setPeerView(remotePeer.getPeerView());
          }

          live.getRoom().userJoinedStream(remotePeer.getSession().getUserId());

          tempSubscriptions.add(remotePeer.getPeerView()
              .onNotificationRemoteJoined()
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(aVoid -> onNotificationRemoteJoined.onNext(
                  getDisplayNameFromSession(remotePeer.getSession()))));
        }));

    tempSubscriptions.add(webRTCRoom.onRollTheDiceReceived()
        .onBackpressureDrop()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(s -> {
          Timber.d("rollTheDice received");
          if (FacebookUtils.isLoggedIn()) {
            viewRoom.onRollTheDiceReceived();
            live.setDiceDragedInRoom(true);
            onRollTheDice.onNext(null);
          } else {
            Timber.d("user not connected to fb");
          }
        }));

    tempSubscriptions.add(viewRoom.onChangeCallRouletteRoom()
        .onBackpressureDrop()
        .subscribe(onChangeCallRouletteRoom));
    Timber.d("Initiating Room");

    // We just want to trigger the updates to update the UI
    live.getRoom().update(room, false);

    webRTCRoom.connect(options);
  }

  public void initDrawerEventChangeObservable(Observable<Integer> obs) {
    viewLiveInvite.initDrawerEventChangeObservable(obs);
    viewControlsLive.initDrawerEventChangeObservable(obs);
  }

  public void initOnShouldOpenChat(Observable<Boolean> obs) {
    viewControlsLive.initOnShouldOpenChat(obs);
  }

  public void initAnonymousSubscription(Observable<List<User>> obs) {
    persistentSubscriptions.add(obs.subscribe(userList -> {
      if (!userList.isEmpty()) {
        anonymousInLive.addAll(userList);

        for (User user : userList) {
          if (liveRowViewMap.get(user.getId()) != null) {
            LiveRowView liveRowView = liveRowViewMap.get(user.getId());
            liveRowView.setGuest(user.asTribeGuest());
          }
        }

        onNotificationRemoteJoined.onNext(userList.get(0).getDisplayName());
      }
    }));
  }

  public void onNewMessage() {
    viewControlsLive.onNewMessage();
  }

  public String getFbId() {
    return fbId;
  }

  public Live getLive() {
    return live;
  }

  public void applyTranslateX(float value) {
    viewControlsLive.setTranslationX(value);
    viewRoom.setTranslationX(value);
    viewRinging.applyTranslationX(value);
    viewDarkOverlay.setTranslationX(value);
    viewShadow.setTranslationX(value);

    if (Math.abs(value) >= getLiveInviteViewPartialWidth()) {
      updateInviteViewWidth((int) Math.abs(value));
    }
  }

  public boolean hasJoined() {
    return hasJoined;
  }

  public int getLiveInviteViewPartialWidth() {
    return screenUtils.dpToPx(LiveInviteView.WIDTH_PARTIAL);
  }

  public int getLiveInviteViewFullWidth() {
    return screenUtils.dpToPx(LiveInviteView.WIDTH_FULL);
  }

  public void start(Live live) {
    this.live = live;
    this.fbId = live.hasUsers() ? live.getUsersOfShortcut().get(0).getFbid() : "";

    if (live.getSource().equals(SOURCE_CALL_ROULETTE)) {
      viewControlsLive.btnChat.setVisibility(INVISIBLE);
      viewRinging.setVisibility(INVISIBLE);
    }

    webRTCRoom = tribeLiveSDK.newRoom();
    webRTCRoom.initLocalStream(viewLocalLive.getLocalPeerView());

    gameManager.setWebRTCRoom(webRTCRoom);
    viewGameManager.setWebRTCRoom(webRTCRoom);

    viewControlsLive.setLive(live);
    viewLiveInvite.setLive(live);

    viewRinging.setLive(live);
    viewRinging.startRinging();

    if (StringUtils.isEmpty(live.getGameId())) {
      tempSubscriptions.add(Observable.timer(3000, TimeUnit.MILLISECONDS)
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(aLong -> onShouldJoinRoom.onNext(null)));
    } else {
      onShouldJoinRoom.onNext(null);
    }
  }

  public void startGame(String gameId) {
    viewControlsLive.startGame(gameManager.getGameById(gameId));
  }

  public void stopGameDice() {
    stopGame();
  }

  public WebRTCRoom getWebRTCRoom() {
    return webRTCRoom;
  }

  public void setCameraEnabled(boolean enable,
      @TribePeerMediaConfiguration.MediaConfigurationType String type) {
    if (viewLocalLive == null) return;

    if (enable) {
      viewLocalLive.enableCamera(false);
    } else {
      viewLocalLive.disableCamera(false, type);
    }
  }

  public int nbInRoom() {
    return live.getRoom().nbUsersTotal();
  }

  public boolean shouldLeave() {
    return liveRowViewMap.size() == 0 && live != null;
  }

  public @LiveActivity.Source String getSource() {
    return live.getSource();
  }

  public boolean isDiceDragedInRoom() {
    return live.isDiceDragedInRoom();
  }

  public void reRollTheDiceFromLiveRoom() {
    Timber.d("roll the dice");
    live.setDiceDragedInRoom(true);
    webRTCRoom.sendToPeers(getUserPlayload(user), true);
  }

  public void sendUnlockDice(String peerUserId, User user) {
    webRTCRoom.sendToUser(peerUserId, getUnlockRollTheDicePayload(user), true);
  }

  public void sendUnlockedDice(String peerUserId) {
    webRTCRoom.sendToUser(peerUserId, getUnlockedRollTheDicePayload(), true);
  }
  ////////////////
  //  PRIVATE   //
  ////////////////

  private void addView(RemotePeer remotePeer) {
    if (viewRoom == null) return;

    LiveRowView liveRowView = null;

    if (liveRowViewMap.getMap()
        .containsKey(remotePeer.getSession()
            .getUserId())) { // If the user was already live, usually the case on 1-1 calls

      liveRowView = liveRowViewMap.get(remotePeer.getSession().getUserId());
      liveRowView.setPeerView(remotePeer.getPeerView());
    } else {
      TribeGuest guest = guestFromRemotePeer(remotePeer);

      if (liveRowView == null) {
        liveRowView = new LiveRowView(getContext());

        if (guest != null) {
          liveRowView.setGuest(guest);
        }

        liveRowView.setPeerView(remotePeer.getPeerView());
        liveRowView.setId(View.generateViewId());
        viewRoom.addViewConstraint(remotePeer.getSession().getUserId(), liveRowView);
      }

      liveRowViewMap.put(remotePeer.getSession().getUserId(), liveRowView);
    }

    if (liveRowView != null) {
      tempSubscriptions.add(liveRowView.onClick().map(tribeGuest -> {
        Object o = computeGuest(tribeGuest.getId());
        if (o == null) {
          return tribeGuest;
        } else {
          return o;
        }
      }).subscribe(onRemotePeerClick));
    }
  }

  private void removeFromPeers(String userId) {
    if (liveRowViewMap.getMap().containsKey(userId)) {
      LiveRowView liveRowView = liveRowViewMap.remove(userId, true);
      liveRowView.dispose();
      viewRoom.removeView(liveRowView);
    }

    tribeGuestMap.remove(userId, true);
  }

  private TribeGuest guestFromRemotePeer(RemotePeer remotePeer) {
    TribeGuest guest = null;

    for (User user : live.getRoom().getAllUsers()) {
      if (remotePeer.getSession().getUserId().equals(user.getId())) {
        guest = new TribeGuest(user.getId(), user.getDisplayName(), user.getProfilePicture(), false,
            true, user.getUsername());
        guest.setExternal(remotePeer.getSession().isExternal());
      }
    }

    for (Shortcut shortcut : user.getShortcutList()) {
      if (shortcut.isSingle()) {
        User friend = shortcut.getSingleFriend();
        if (remotePeer.getSession().getUserId().equals(friend.getId())) {
          guest =
              new TribeGuest(friend.getId(), friend.getDisplayName(), friend.getProfilePicture(),
                  false, true, friend.getUsername());
          guest.setExternal(remotePeer.getSession().isExternal());
        }
      }
    }

    if (guest == null) {
      guest = new TribeGuest(remotePeer.getSession().getUserId(),
          getDisplayNameFromId(remotePeer.getSession().getUserId()), null, false, false, "");
      guest.setExternal(remotePeer.getSession().isExternal());
    }

    return guest;
  }

  private JSONObject getInvitedPayload() {
    JSONObject jsonObject = new JSONObject();
    JSONArray array = new JSONArray();

    for (User user : live.getRoom().getInvitedUsers()) {
      JSONObject invitedGuest = new JSONObject();
      JsonUtils.jsonPut(invitedGuest, TribeGuest.ID, user.getId());
      JsonUtils.jsonPut(invitedGuest, TribeGuest.DISPLAY_NAME, user.getDisplayName());
      JsonUtils.jsonPut(invitedGuest, TribeGuest.PICTURE, user.getProfilePicture());
      JsonUtils.jsonPut(invitedGuest, TribeGuest.USERNAME, user.getUsername());
      array.put(invitedGuest);
    }

    JsonUtils.jsonPut(jsonObject, WebRTCRoom.MESSAGE_INVITE_ADDED, array);
    return jsonObject;
  }

  private JSONObject getUnlockRollTheDicePayload(User user) {
    JSONObject appJson1 = new JSONObject();
    JsonUtils.jsonPut(appJson1, "by", getUserPlayload(user));
    JSONObject appJson = new JSONObject();
    JsonUtils.jsonPut(appJson, WebRTCRoom.MESSAGE_UNLOCK_ROLL_DICE, appJson1);

    return appJson;
  }

  private JSONObject getUnlockedRollTheDicePayload() {
    JSONObject appJson = new JSONObject();
    JsonUtils.jsonPut(appJson, WebRTCRoom.MESSAGE_UNLOCKED_ROLL_DICE, true);

    return appJson;
  }

  private JSONObject getUserPlayload(User user) {
    JSONObject jsonObject = new JSONObject();

    JSONObject userJson = new JSONObject();
    JsonUtils.jsonPut(userJson, User.ID, user.getId());
    JsonUtils.jsonPut(userJson, User.FBID, user.getFbid());
    JsonUtils.jsonPut(userJson, User.USERNAME, user.getUsername());
    JsonUtils.jsonPut(userJson, User.DISPLAY_NAME, user.getDisplayName());
    JsonUtils.jsonPut(userJson, User.PICTURE, user.getProfilePicture());

    JSONObject json = new JSONObject();
    try {
      json.put("by", userJson);
    } catch (JSONException e) {
      e.printStackTrace();
    }

    JsonUtils.jsonPut(jsonObject, WebRTCRoom.MESSAGE_ROLL_THE_DICE, json);
    return jsonObject;
  }

  private int nbLiveInRoom() {
    return live.getRoom().nbUsersLive();
  }

  private String getDisplayNameFromSession(TribeSession tribeSession) {
    return tribeSession.isExternal() ? getContext().getString(R.string.live_external_user)
        : getDisplayNameFromId(tribeSession.getUserId());
  }

  private String getDisplayNameFromId(String id) {
    for (User user : live.getRoom().getAllUsers()) {
      if (id.equals(user.getId()) && !user.isEmpty()) {
        return user.getDisplayName();
      }
    }

    for (Shortcut shortcut : user.getShortcutList()) {
      if (shortcut.isSingle()) {
        User friend = shortcut.getSingleFriend();
        if (friend.getId().equals(id)) {
          return friend.getDisplayName();
        }
      }
    }

    for (User anonymousUser : anonymousInLive) {
      if (anonymousUser.getId().equals(id)) {
        return anonymousUser.getDisplayName();
      }
    }

    return "";
  }

  private Object computeGuest(String id) {
    for (User user : live.getRoom().getAllUsers()) {
      if (id.equals(user.getId()) && !user.isEmpty()) {
        return user;
      }
    }

    for (Shortcut shortcut : user.getShortcutList()) {
      if (shortcut.isSingle()) {
        User friend = shortcut.getSingleFriend();
        if (friend.getId().equals(id)) {
          return friend;
        }
      }
    }

    for (User anonymousUser : anonymousInLive) {
      if (anonymousUser.getId().equals(id)) {
        return anonymousUser;
      }
    }

    return null;
  }

  public User getUser() {
    return user;
  }

  private void onRestartGame(Game game) {
    if (game == null) return;
    restartGame(game);
    displayReRollGameNotification(game.getId(), user.getDisplayName());
  }

  private void startGame(Game game, boolean isUserAction) {
    if (game == null) return;

    onTouchEnabled.onNext(false);

    if (!isUserAction) viewControlsLive.startGameFromAnotherUser(game);
    game.setUserAction(isUserAction);
    gameManager.setCurrentGame(game);
    viewLocalLive.startGame(game);
    viewRinging.hide();

    int indexOfViewRoom = indexOfChild(viewRoom);

    if (game.isOverLive()) {
      viewRoom.setType(LiveRoomView.TYPE_LIST);
      if (viewGameManager.getParent() == null) {
        viewGameManager.setBackgroundColor(Color.BLACK);
        addViewGameManagerAtPosition(indexOfViewRoom);
      }
    } else {
      viewRoom.setType(LiveRoomView.TYPE_GRID);
      if (viewGameManager.getParent() == null) {
        viewGameManager.setBackground(null);
        addViewGameManagerAtPosition(indexOfViewRoom + 1);
      }
    }
  }

  private void addViewGameManagerAtPosition(int position) {
    FrameLayout.LayoutParams params =
        new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT);
    addView(viewGameManager, position, params);
  }

  private void restartGame(Game game) {
    startGame(game, true);
  }

  private void stopGame() {
    endGameStats(gameManager.getCurrentGame().getId());
    onTouchEnabled.onNext(true);
    gameManager.setCurrentGame(null);
    removeView(viewGameManager);
    viewGameManager.disposeGame();
    viewControlsLive.stopGame();
    viewLocalLive.stopGame();
    viewRinging.startRinging();
    viewRoom.setType(LiveRoomView.TYPE_GRID);
  }

  private void displayStartGameNotification(String gameName, String userDisplayName) {
    onNotificationGameStarted.onNext(EmojiParser.demojizedText(
        getResources().getString(R.string.game_event_started, userDisplayName, gameName)));
  }

  private void displayReRollGameNotification(String gameId, String userDisplayName) {
    String comment = "";
    if (gameId.equals(Game.GAME_POST_IT)) {
      comment = getResources().getString(R.string.game_event_post_it_re_roll, userDisplayName);
    } else if (gameId.equals(Game.GAME_CHALLENGE)) {
      comment =
          getResources().getString(R.string.game_challenges_re_roll_notification, userDisplayName);
    } else if (gameId.equals(Game.GAME_DRAW)) {
      comment = getResources().getString(R.string.game_draw_re_roll_notification, userDisplayName);
    }
    onNotificationGameRestart.onNext(EmojiParser.demojizedText(comment));
  }

  private void displayStopGameNotification(String gameName, String userDisplayName) {
    onNotificationGameStopped.onNext(EmojiParser.demojizedText(
        getResources().getString(R.string.game_event_stopped, userDisplayName, gameName)));
  }

  private void startGameStats(String gameId) {
    String tagCount = gameId + TagManagerUtils.tagGameCountSuffix;

    int count = (int) tagMap.getOrDefault(tagCount, 0) + 1;
    tagMap.put(tagCount, count);
    tagManager.increment(tagCount, 1);

    startGameTimerSubscription(gameId);
  }

  private void endGameStats(String gameId) {
    endGameTimerSubscription(gameId);
  }

  private void startGameTimerSubscription(String gameId) {
    if (callGameAverageSubscription == null) {
      String tagAverage = gameId + TagManagerUtils.tagGameAverageMembersSuffix;

      callGameAverageSubscription =
          Observable.interval(10, TimeUnit.SECONDS, Schedulers.computation())
              .onBackpressureDrop()
              .doOnUnsubscribe(() -> {
                if (!tagMap.containsKey(tagAverage)) {
                  tagMap.put(tagAverage, averageCountGameLive);
                } else {
                  tagMap.put(tagAverage,
                      (averageCountGameLive + (double) tagMap.get(tagAverage)) / 2);
                }
              })
              .subscribe(intervalCount -> {
                intervalGame++;
                totalSizeGameLive += nbLiveInRoom();
                averageCountGameLive = (double) totalSizeGameLive / intervalGame;
                averageCountGameLive = DoubleUtils.round(averageCountGameLive, 2);
              });

      tempSubscriptions.add(callGameAverageSubscription);
    }

    if (callGameDurationSubscription == null) {
      Date startedAt = new Date();
      String tagDuration = gameId + TagManagerUtils.tagGameDurationSuffix;

      callGameDurationSubscription = Observable.interval(1, TimeUnit.SECONDS)
          .observeOn(AndroidSchedulers.mainThread())
          .doOnUnsubscribe(() -> {
            double durationGame = getDuration(startedAt);
            double totalGameDuration =
                (double) tagMap.getOrDefault(TagManagerUtils.GAME_DURATION, 0.0D) + durationGame;
            tagMap.put(TagManagerUtils.GAME_DURATION, totalGameDuration);
            tagManager.increment(TagManagerUtils.GAME_DURATION, totalGameDuration);
          })
          .subscribe(tick -> {
            double durationGame = getDuration(startedAt);
            tagMap.put(tagDuration, durationGame);
            tagManager.increment(tagDuration, durationGame);
          });

      tempSubscriptions.add(callGameDurationSubscription);
    }
  }

  private double getDuration(Date startDate) {
    long delta = new Date().getTime() - startDate.getTime();
    double durationGame = (double) delta / 60000.0;
    return DoubleUtils.round(durationGame, 2);
  }

  private void endGameTimerSubscription(String gameId) {
    if (callGameDurationSubscription != null) {
      tempSubscriptions.remove(callGameDurationSubscription);
      callGameDurationSubscription.unsubscribe();
      callGameDurationSubscription = null;
    }

    if (callGameAverageSubscription != null) {
      tempSubscriptions.remove(callGameAverageSubscription);
      callGameAverageSubscription.unsubscribe();
      callGameAverageSubscription = null;
    }

    String tagCount = gameId + TagManagerUtils.tagGameCountSuffix;
    int totalGameCount = (int) tagMap.getOrDefault(TagManagerUtils.GAME_COUNT, 0) +
        (int) tagMap.getOrDefault(tagCount, 0);
    tagMap.put(TagManagerUtils.GAME_COUNT, totalGameCount);
    tagManager.increment(tagCount, totalGameCount);
  }

  //////////////////////
  //   OBSERVABLES    //
  //////////////////////

  public Observable<Boolean> onOpenInvite() {
    return onOpenInvite;
  }

  public Observable<Boolean> onTouchEnabled() {
    return onTouchEnabled;
  }

  public Observable<String> onBuzzPopup() {
    return onBuzzPopup;
  }

  public Observable<Void> onShouldJoinRoom() {
    return onShouldJoinRoom;
  }

  public Observable<String> unlockRollTheDice() {
    return unlockRollTheDice;
  }

  public Observable<String> unlockedRollTheDice() {
    return unlockedRollTheDice;
  }

  public Observable<String> onRollTheDice() {
    return onRollTheDice;
  }

  public Observable<TribeJoinRoom> onJoined() {
    return onJoined;
  }

  public Observable<Void> onNotify() {
    return onNotify;
  }

  public Observable<Void> onLeave() {
    return Observable.merge(onLeave, viewControlsLive.onLeave());
  }

  public Observable<Void> onScreenshot() {
    return onScreenshot;
  }

  public Observable<Boolean> onHiddenControls() {
    return onHiddenControls;
  }

  public Observable<Void> onShouldCloseInvites() {
    return onShouldCloseInvites;
  }

  public Observable<String> onNotificationRemotePeerInvited() {
    return onNotificationRemotePeerInvited;
  }

  public Observable<String> onNotificationRemoteWaiting() {
    return onNotificationRemoteWaiting;
  }

  public Observable<String> onNotificationRemoteJoined() {
    return onNotificationRemoteJoined;
  }

  public Observable<String> onAnonymousJoined() {
    return onAnonymousJoined;
  }

  public Observable<String> onNotificationonRemotePeerRemoved() {
    return onNotificationRemotePeerRemoved;
  }

  public Observable<String> onNotificationonRemotePeerBuzzed() {
    return onNotificationRemotePeerBuzzed;
  }

  public Observable<String> onNotificationOnGameStarted() {
    return onNotificationGameStarted;
  }

  public Observable<String> onNotificationOnGameStopped() {
    return onNotificationGameStopped;
  }

  public Observable<String> onNotificationOnGameRestart() {
    return onNotificationGameRestart;
  }

  public Observable<Long> onEndCall() {
    return onEndCall;
  }

  public Observable<Void> onChangeCallRouletteRoom() {
    return onChangeCallRouletteRoom;
  }

  public Observable<WebSocketError> onRoomError() {
    return onRoomError;
  }

  public Observable<Object> onRemotePeerClick() {
    return onRemotePeerClick;
  }

  public Observable<Boolean> onGameMenuOpen() {
    return viewControlsLive.onGameMenuOpen();
  }

  public Observable<String> onDismissInvite() {
    return onDismissInvite;
  }

  public Observable<View> onEdit() {
    return viewLiveInvite.onClickEdit();
  }

  public Observable<Boolean> onOpenChat() {
    return onOpenChat;
  }

  public Observable<Void> onShareLink() {
    return viewLiveInvite.onShareLink();
  }

  public Observable<Void> onInviteMoreClick() {
    return viewLiveInvite.onClickBottom();
  }

  public Observable<Void> openGameStore() {
    return viewControlsLive.openGameStore();
  }
}

