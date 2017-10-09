package com.tribe.app.presentation.view.component.live;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.R;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.Live;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.RoomConfiguration;
import com.tribe.app.domain.entity.RoomMember;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.CallLevelHelper;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManager;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.preferences.CallTagsMap;
import com.tribe.app.presentation.utils.preferences.CounterOfCallsForGrpButton;
import com.tribe.app.presentation.utils.preferences.MinutesOfCalls;
import com.tribe.app.presentation.utils.preferences.NumberOfCalls;
import com.tribe.app.presentation.utils.preferences.PreferencesUtils;
import com.tribe.app.presentation.view.activity.LiveActivity;
import com.tribe.app.presentation.view.component.TileView;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.Degrees;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.DoubleUtils;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.app.presentation.view.utils.StateManager;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.game.GameChallengesView;
import com.tribe.app.presentation.view.widget.game.GameDrawView;
import com.tribe.tribelivesdk.TribeLiveSDK;
import com.tribe.tribelivesdk.back.TribeLiveOptions;
import com.tribe.tribelivesdk.back.WebSocketConnection;
import com.tribe.tribelivesdk.core.Room;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameChallenge;
import com.tribe.tribelivesdk.game.GameDraw;
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

  @BindView(R.id.viewLocalLive) LiveLocalView viewLocalLive;

  @BindView(R.id.viewRoom) LiveRoomView viewRoom;

  @BindView(R.id.viewControlsLive) LiveControlsView viewControlsLive;

  @BindView(R.id.viewStatusName) LiveStatusNameView viewStatusName;

  @BindView(R.id.txtTooltipFirstGame) TextViewFont txtTooltipFirstGame;

  @BindView(R.id.btnScreenshot) ImageView btnScreenshot;

  @BindView(R.id.gameChallengesView) GameChallengesView gameChallengesView;

  @BindView(R.id.gameDrawView) GameDrawView gameDrawView;

  // VARIABLES
  private Live live;
  private Room room;
  private LiveRowView latestView;
  private ObservableRxHashMap<String, LiveRowView> liveRowViewMap;
  private ObservableRxHashMap<String, LiveRowView> liveInviteMap;
  private boolean hiddenControls = false;
  private @LiveContainer.Event int stateContainer = LiveContainer.EVENT_CLOSED;
  private Map<String, Object> tagMap;
  private int wizzCount = 0, screenshotCount = 0, invitedCount = 0, totalSizeLive = 0, interval = 0,
      postItGameCount = 0;
  private double averageCountLive = 0.0D;
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

  // RESOURCES
  private int timeJoinRoom, statusBarHeight, tooltipFirstGameHeight;

  // OBSERVABLES
  private Unbinder unbinder;
  private CompositeSubscription persistentSubscriptions = new CompositeSubscription();
  private CompositeSubscription tempSubscriptions = new CompositeSubscription();
  private Subscription callDurationSubscription;

  private PublishSubject<Void> onOpenInvite = PublishSubject.create();
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
  private PublishSubject<String> onPointsDrawReceived = PublishSubject.create();
  private PublishSubject<List<String>> onNewChallengeReceived = PublishSubject.create();
  private PublishSubject<List<String>> onNewDrawReceived = PublishSubject.create();
  private PublishSubject<Void> onClearDrawReceived = PublishSubject.create();
  private PublishSubject<String> unlockedRollTheDice = PublishSubject.create();
  private PublishSubject<TribeJoinRoom> onJoined = PublishSubject.create();
  private PublishSubject<String> onRollTheDice = PublishSubject.create();
  private PublishSubject<Void> onShare = PublishSubject.create();
  private PublishSubject<WebSocketError> onRoomError = PublishSubject.create();
  private PublishSubject<Void> onChangeCallRouletteRoom = PublishSubject.create();
  private PublishSubject<Object> onRemotePeerClick = PublishSubject.create();
  private PublishSubject<Game> onStartGame = PublishSubject.create();

  private PublishSubject<String> onNotificationRemotePeerInvited = PublishSubject.create();
  private PublishSubject<String> onNotificationRemotePeerRemoved = PublishSubject.create();
  private PublishSubject<String> onNotificationRemoteWaiting = PublishSubject.create();
  private PublishSubject<String> onNotificationRemotePeerBuzzed = PublishSubject.create();
  private PublishSubject<String> onNotificationRemoteJoined = PublishSubject.create();
  private PublishSubject<String> onNotificationGameStarted = PublishSubject.create();
  private PublishSubject<String> onNotificationGameStopped = PublishSubject.create();
  private PublishSubject<String> onNotificationGameRestart = PublishSubject.create();
  private PublishSubject<String> onAnonymousJoined = PublishSubject.create();
  private PublishSubject<Boolean> onBlockOpenInviteView = PublishSubject.create();

  public LiveView(Context context) {
    super(context);
    init();
  }

  public LiveView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public void jump() {
    room.jump();
  }

  public String getDisplayName() {
    return live.getDisplayName();
  }

  public int getRowsInLive() {
    return viewRoom.getRowsInLive();
  }

  public void removeUserFromGrid(String userId) {
    viewRoom.removeGuest(userId);
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
      } else if ((hasJoined && averageCountLive <= 1 && !live.getId().equals(Live.NEW_CALL)) ||
          (live.getId().equals(Live.NEW_CALL) && (invitedCount > 0 || hasShared))) {
        state = TagManagerUtils.MISSED;
        tagManager.increment(TagManagerUtils.USER_CALLS_MISSED_COUNT);
      }

      endCallLevel();

      tagMap.put(TagManagerUtils.EVENT, TagManagerUtils.Calls);
      tagMap.put(TagManagerUtils.SOURCE, live.getSource());
      tagMap.put(TagManagerUtils.IS_CALL_ROULETTE, live.isDiceDragedInRoom());
      tagMap.put(TagManagerUtils.DURATION, duration);
      tagMap.put(TagManagerUtils.STATE, state);
      tagMap.put(TagManagerUtils.MEMBERS_INVITED, invitedCount);
      tagMap.put(TagManagerUtils.WIZZ_COUNT, wizzCount);
      tagMap.put(TagManagerUtils.SCREENSHOT_COUNT, screenshotCount);
      tagMap.put(TagManagerUtils.POST_IT_GAME_COUNT, postItGameCount);
      tagMap.put(TagManagerUtils.TYPE,
          live.isGroup() ? TagManagerUtils.GROUP : TagManagerUtils.DIRECT);
      // We are entering another call, so we send the tags regarless
      // else we are pushing the tags only after the call rating popup so we save them
      if (isJump) {
        TagManagerUtils.manageTags(tagManager, tagMap);
      } else {
        PreferencesUtils.saveMapAsJson(tagMap, callTagsMap);
      }
    }

    for (LiveRowView liveRowView : liveRowViewMap.getMap().values()) {
      Timber.d("liverowview dispose");
      liveRowView.dispose();
      viewRoom.removeView(liveRowView);
    }
    liveRowViewMap.clear();

    for (LiveRowView liveRowView : liveInviteMap.getMap().values()) {
      Timber.d("liveinviteview dispose");
      liveRowView.dispose();
      viewRoom.removeView(liveRowView);
    }
    liveInviteMap.clear();

    if (room != null && !isJump) {
      Timber.d("room leave");
      room.leaveRoom();
    }

    tempSubscriptions.clear();

    if (!isJump) {
      Timber.d("dispose !isJump");
      persistentSubscriptions.clear();
      viewLocalLive.dispose();
    }
  }

  public void dispose(boolean isJump) {
    Timber.d("disposing");

    viewStatusName.dispose();
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

    gameManager = GameManager.getInstance(getContext());

    initResources();
    initUI();
    initSubscriptions();

    super.onFinishInflate();
  }

  @Override protected void onConfigurationChanged(Configuration newConfig) {
    if (viewControlsLive == null) return;

    if (room != null) {
      room.sendOrientation(Degrees.getNormalizedDegrees(getContext()),
          viewLocalLive.isFrontFacing());
    }

    onShouldCloseInvites.onNext(null);

    ViewGroup.LayoutParams lp = view.getLayoutParams();
    lp.width = screenUtils.getWidthPx();
    lp.height = screenUtils.getHeightPx() - statusBarHeight;
    view.setLayoutParams(lp);
  }

  //////////////////////
  //      INIT        //
  //////////////////////

  private void init() {
    liveRowViewMap = new ObservableRxHashMap<>();
    liveInviteMap = new ObservableRxHashMap<>();
    tagMap = new HashMap<>();
  }

  private void initUI() {
    setBackgroundColor(Color.BLACK);
  }

  private void initResources() {
    timeJoinRoom = getResources().getInteger(R.integer.time_join_room);
    tooltipFirstGameHeight =
        getContext().getResources().getDimensionPixelSize(R.dimen.game_tooltip_first_height);

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

    persistentSubscriptions.add(viewLocalLive.onClick().doOnNext(aVoid -> {
      if (stateContainer == LiveContainer.EVENT_OPENED) {
        onShouldCloseInvites.onNext(null);
      }
    }).filter(aVoid -> stateContainer == LiveContainer.EVENT_CLOSED).subscribe(aVoid -> {
      viewControlsLive.clickExpandParam();
      onHiddenControls.onNext(isParamExpended);
    }));

    persistentSubscriptions.add(
        viewLocalLive.onShare().doOnNext(aVoid -> hasShared = true).subscribe(onShare));

    persistentSubscriptions.add(viewControlsLive.onOpenInvite().subscribe(aVoid -> {
      if (!hiddenControls) onOpenInvite.onNext(null);
    }));

    persistentSubscriptions.add(viewControlsLive.onClickCameraOrientation().subscribe(aVoid -> {
      viewLocalLive.switchCamera();
      if (room != null) {
        room.sendOrientation(Degrees.getNormalizedDegrees(getContext()),
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

    persistentSubscriptions.add(viewControlsLive.onClickNotify().doOnNext(aVoid -> {
      if (!hiddenControls) {
        wizzCount++;
        onNotificationRemotePeerBuzzed.onNext(null);

        for (LiveRowView liveRowView : liveInviteMap.getMap().values()) {
          liveRowView.buzz();
        }

        for (LiveRowView liveRowView : liveRowViewMap.getMap().values()) {
          if (liveRowView.isWaiting()) liveRowView.buzz();
        }
      }
    }).subscribe(onNotify));

    persistentSubscriptions.add(viewControlsLive.onNotifyAnimationDone().subscribe(aVoid -> {
      tempSubscriptions.add(Observable.timer(1000, TimeUnit.MILLISECONDS)
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(aLong -> refactorNotifyButton()));
    }));

    viewControlsLive.onClickFilter().subscribe(aVoid -> viewLocalLive.switchFilter());

    persistentSubscriptions.add(viewControlsLive.onStartGame().subscribe(game -> {
      displayStartGameNotification(game.getName(), user.getDisplayName());
      restartGame(game);
    }));

    persistentSubscriptions.add(
        gameChallengesView.onBlockOpenInviteView().subscribe(onBlockOpenInviteView));

    persistentSubscriptions.add(
        gameDrawView.onBlockOpenInviteView().subscribe(onBlockOpenInviteView));

    persistentSubscriptions.add(viewControlsLive.onRestartGame().subscribe(game -> {
      onRestartGame(game);
    }));

    persistentSubscriptions.add(gameChallengesView.onNextChallenge().subscribe(gameChallenge -> {
      onRestartGame(gameManager.getCurrentGame());
    }));

    persistentSubscriptions.add(gameDrawView.onNextDraw().subscribe(aVoid -> {
      onRestartGame(gameManager.getCurrentGame());
    }));

    persistentSubscriptions.add(gameDrawView.onCurrentGame().subscribe(game -> {
      GameDraw draw = (GameDraw) game;
      room.sendToPeers(getNewDrawPayload(user.getId(), draw.getCurrentDrawer().getId(),
          draw.getCurrentDrawName()), false);
    }));

    persistentSubscriptions.add(gameChallengesView.onCurrentGame().subscribe(game -> {
      GameChallenge draw = (GameChallenge) game;
      room.sendToPeers(getNewChallengePayload(user.getId(), draw.getCurrentChallenger().getId(),
          draw.getCurrentChallenge()), false);
    }));

    persistentSubscriptions.add(gameDrawView.onClearDraw().subscribe(aVoid -> {
      room.sendToPeers(getDrawClearPayload(), false);
    }));

    persistentSubscriptions.add(gameDrawView.onDrawing().subscribe(points -> {
      room.sendToPeers(getDrawPointPayload(points), false);
    }));

    persistentSubscriptions.add(viewControlsLive.onGameOptions()
        .flatMap(game -> DialogFactory.showBottomSheetForGame(getContext(), game),
            ((game, labelType) -> {
              if (labelType.getTypeDef().equals(LabelType.GAME_RE_ROLL)) {
                displayReRollGameNotification(game.getId(), user.getDisplayName());
                restartGame(game);
              } else if (labelType.getTypeDef().equals(LabelType.GAME_STOP)) {
                stopGame(true, game.getId());
                displayStopGameNotification(game.getName(), user.getDisplayName());
                room.sendToPeers(getStopGamePayload(game), false);
              }

              return null;
            }))
        .subscribe());

    persistentSubscriptions.add(viewRoom.onShouldCloseInvites().subscribe(aVoid -> {
      onShouldCloseInvites.onNext(null);
    }));
  }

  ///////////////////
  //    CLICKS     //
  ///////////////////

  @OnClick(R.id.viewRoom) void onClickRoom() {
    if (stateContainer == LiveContainer.EVENT_OPENED) onShouldCloseInvites.onNext(null);
    if (hiddenControls) {
      onHiddenControls.onNext(false);
    }
  }

  @OnClick(R.id.btnScreenshot) public void onScreenshotClick() {
    onScreenshot.onNext(null);
  }

  ///////////////////
  //    PUBLIC     //
  ///////////////////

  public void joinRoom(RoomConfiguration roomConfiguration) {
    Map<String, String> headers = new HashMap<>();
    headers.put(WebSocketConnection.ORIGIN, com.tribe.app.BuildConfig.TRIBE_ORIGIN);
    TribeLiveOptions options = new TribeLiveOptions.TribeLiveOptionsBuilder(getContext()).wsUrl(
        roomConfiguration.getWebsocketUrl())
        .tokenId(accessToken.getAccessToken())
        .iceServers(roomConfiguration.getRtcPeerConfiguration().getIceServers())
        .roomId(roomConfiguration.getRoomId())
        .routingMode(roomConfiguration.getRoutingMode())
        .headers(headers)
        .orientation(Degrees.getNormalizedDegrees(getContext()))
        .frontCamera(viewLocalLive.isFrontFacing())
        .build();

    tempSubscriptions.add(room.onRoomStateChanged().subscribe(state -> {
      Timber.d("Room state change : " + state);
      if (state == Room.STATE_CONNECTED) {
        timeStart = System.currentTimeMillis();

        tempSubscriptions.add(Observable.interval(10, TimeUnit.SECONDS, Schedulers.computation())
            .onBackpressureDrop()
            .subscribe(intervalCount -> {
              interval++;
              totalSizeLive += nbLiveInRoom() + 1;
              averageCountLive = (double) totalSizeLive / interval;
              averageCountLive = DoubleUtils.round(averageCountLive, 2);

              tagMap.put(TagManagerUtils.AVERAGE_MEMBERS_COUNT, averageCountLive);
            }));
      }
    }));

    //tempSubscriptions.add(room.unlockRollTheDice().subscribe(unlockRollTheDice));

    tempSubscriptions.add(room.onPointsDrawReceived().subscribe(onPointsDrawReceived));

    tempSubscriptions.add(room.onNewChallengeReceived().subscribe(onNewChallengeReceived));

    tempSubscriptions.add(room.onNewDrawReceived().subscribe(onNewDrawReceived));

    tempSubscriptions.add(room.onClearDrawReceived().subscribe(onClearDrawReceived));

    //tempSubscriptions.add(room.unlockedRollTheDice().subscribe(unlockedRollTheDice));

    tempSubscriptions.add(room.onJoined().subscribe(onJoined));

    tempSubscriptions.add(room.onShouldLeaveRoom().subscribe(onLeave));

    tempSubscriptions.add(room.onRoomError().subscribe(onRoomError));

    tempSubscriptions.add(room.onNewGame().subscribe(pairSessionGame -> {//OEF
      Game currentGame = gameManager.getCurrentGame();
      Game game = gameManager.getGameById(pairSessionGame.second);
      if (game != null) {

        String displayName = getDisplayNameFromSession(pairSessionGame.first);

        if (currentGame == null) {
          displayStartGameNotification(game.getName(), displayName);
        } else {
          displayReRollGameNotification(game.getId(), displayName);
        }

        startGame(game, false);
      }
    }));

    tempSubscriptions.add(room.onStopGame().subscribe(pairSessionGame -> {
      Game game = gameManager.getGameById(pairSessionGame.second);
      String displayName = getDisplayNameFromSession(pairSessionGame.first);
      displayStopGameNotification(game.getName(), displayName);
      stopGame(false, game.getId());
    }));

    tempSubscriptions.add(
        room.onRemotePeerAdded().observeOn(AndroidSchedulers.mainThread()).subscribe(remotePeer -> {
          if (isFirstToJoin) {
            isFirstToJoin = !isFirstToJoin;
            if (viewStatusName != null) viewStatusName.refactorTitle();
          }
          soundManager.playSound(SoundManager.JOIN_CALL, SoundManager.SOUND_MAX);
          joinLive = true;

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

          room.sendToPeer(remotePeer, getInvitedPayload(), true);

          refactorShareOverlay();
          refactorNotifyButton();
          startCallLevel();

          LiveRowView row = liveRowViewMap.get(remotePeer.getSession().getUserId());
          if (row != null) row.guestAppear();
          tempSubscriptions.add(remotePeer.getPeerView()
              .onNotificationRemoteJoined()
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(aVoid -> onNotificationRemoteJoined.onNext(
                  getDisplayNameFromSession(remotePeer.getSession()))));
        }));

    tempSubscriptions.add(room.onRemotePeerRemoved()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(remotePeer -> {

          soundManager.playSound(SoundManager.QUIT_CALL, SoundManager.SOUND_MAX);

          Timber.d("Remote peer removed with id : " + remotePeer.getSession().getPeerId());
          removeFromPeers(remotePeer.getSession().getUserId());

          if (nbOtherUsersInRoom() == 0) {
            endCallLevel();
          }

          if (shouldLeave()) {
            onLeave.onNext(null);
          }

          refactorShareOverlay();
          refactorNotifyButton();

          onNotificationRemotePeerRemoved.onNext(
              getDisplayNameFromSession(remotePeer.getSession()));
        }));

    tempSubscriptions.add(room.onRemotePeerUpdated()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(remotePeer -> {
          if (remotePeer != null && remotePeer.getSession() != null) {
            Timber.d("Remote peer updated with id : " + remotePeer.getSession().getPeerId());
          } else {
            Timber.d("Remote peer updated with id : null");
          }
        }));

    //tempSubscriptions.add(
    //    room.onRollTheDiceReceived().observeOn(AndroidSchedulers.mainThread()).subscribe(s -> {
    //      Timber.d("rollTheDice received");
    //      if (FacebookUtils.isLoggedIn()) {
    //        viewRoom.onRollTheDiceReceived();
    //        live.setDiceDragedInRoom(true);
    //        onRollTheDice.onNext(null);
    //      } else {
    //        Timber.d("user not connected to fb");
    //      }
    //    }));

    tempSubscriptions.add(room.onInvitedTribeGuestList()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(tribeGuests -> {
          if (tribeGuests != null && tribeGuests.size() > 0) {
            for (TribeGuest trg : tribeGuests) {
              if (!liveInviteMap.getMap().containsKey(trg.getId()) &&
                  !liveRowViewMap.getMap().containsKey(trg.getId())) {
                if (!user.getId().equals(trg.getId())) {
                  addTribeGuest(trg);
                  onNotificationRemotePeerInvited.onNext(trg.getDisplayName());
                }
              }
            }
          }
        }));

    tempSubscriptions.add(room.onRemovedTribeGuestList()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(tribeGuests -> {
          if (tribeGuests != null && tribeGuests.size() > 0) {
            for (TribeGuest trg : tribeGuests) {
              if (liveInviteMap.getMap().containsKey(trg.getId())) {
                removeFromInvites(trg.getId());
              }
            }
          }
        }));

    tempSubscriptions.add(viewRoom.onChangeCallRouletteRoom().subscribe(onChangeCallRouletteRoom));
    Timber.d("Initiating Room");
    room.connect(options);
  }

  private void startCallLevel() {

    if (callDurationSubscription == null) {
      Date startedAt = new Date();

      callDurationSubscription = Observable.interval(1, TimeUnit.SECONDS)
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(tick -> {
            if (viewStatusName == null) return;
            
            String level = CallLevelHelper.getCurrentLevel(getContext(), startedAt);
            String duration = CallLevelHelper.getFormattedDuration(startedAt);

            viewStatusName.setStatusText(level, " " + duration);
          });

      tempSubscriptions.add(callDurationSubscription);
    }
  }

  private void endCallLevel() {

    if (callDurationSubscription != null) {
      tempSubscriptions.remove(callDurationSubscription);
      callDurationSubscription.unsubscribe();
      callDurationSubscription = null;
    }
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

  public void initInviteOpenSubscription(Observable<Integer> obs) {
    persistentSubscriptions.add(obs.subscribe(event -> {
      stateContainer = event;
      viewRoom.setType(
          event == LiveContainer.EVENT_OPENED ? LiveRoomView.LINEAR : LiveRoomView.GRID);
    }));
  }

  public void initOnStartDragSubscription(Observable<TileView> obs) {
    persistentSubscriptions.add(obs.subscribe(tileView -> {
      latestView = new LiveRowView(getContext());
      TribeGuest guest = new TribeGuest(tileView.getRecipient().getSubId(),
          tileView.getRecipient().getDisplayName(), tileView.getRecipient().getProfilePicture(),
          false, false, null, true, tileView.getRecipient().getUsername());

      addView(latestView, guest, tileView.getBackgroundColor(), true);
    }));
  }

  public void initOnEndDragSubscription(Observable<Void> obs) {
    persistentSubscriptions.add(obs.subscribe(aVoid -> {
      latestView.dispose();
      viewRoom.removeView(latestView);
    }));
  }

  public void initOnAlphaSubscription(Observable<Float> obs) {
    persistentSubscriptions.add(obs.subscribe(alpha -> {
      viewControlsLive.setAlpha(alpha);
      viewLocalLive.computeAlpha(alpha);
    }));
  }

  public void initDropEnabledSubscription(Observable<Boolean> obs) {
    persistentSubscriptions.add(obs.subscribe(enabled -> {
      viewRoom.onDropEnabled(enabled);
    }));
  }

  public void initDropSubscription(Observable<TileView> obs) {
    persistentSubscriptions.add(obs.subscribe(tileView -> {

      tileView.onDrop(latestView);
      latestView.prepareForDrop();

      viewRoom.onDropItem(tileView);

      if (latestView.getGuest() != null) {
        liveInviteMap.put(latestView.getGuest().getId(), latestView);
      }

      if (latestView.getGuest() != null &&
          !latestView.getGuest().getId().equals(Recipient.ID_CALL_ROULETTE)) {
        room.sendToPeers(getInvitedPayload(), true);
      }

      refactorNotifyButton();

      tempSubscriptions.add(tileView.onEndDrop().subscribe(aVoid -> {
        refactorShareOverlay();
        stateManager.addTutorialKey(StateManager.DRAG_FRIEND_POPUP);
        invitedCount++;
        latestView.showGuest(false);
        latestView.startPulse();
      }));

      subscribeOnRemovingGuestFromLive(latestView);
    }));
  }

  public void reduceParam() {
    viewControlsLive.reduceParam();
  }

  public String getFbId() {
    return fbId;
  }

  public Live getLive() {
    return live;
  }

  public void start(Live live) {
    this.live = live;
    this.fbId = live.getFbId();

    room = tribeLiveSDK.newRoom();
    room.initLocalStream(viewLocalLive.getLocalPeerView());

    viewStatusName.setLive(live);

    if (StringUtils.isEmpty(live.getLinkId())) {
      TribeGuest guest =
          new TribeGuest(live.getSubId(), live.getDisplayName(), live.getPicture(), live.isGroup(),
              live.isInvite(), live.getMembersPics(), false, live.getUserName());

      LiveRowView liveRowView = new LiveRowView(getContext());
      liveRowViewMap.put(guest.getId(), liveRowView);
      addView(liveRowView, guest, live.getColor(), false);
      liveRowView.showGuest(live.isCountdown());

      if (live.isCountdown()) {
        tempSubscriptions.add(liveRowView.onShouldJoinRoom()
            .distinct()
            .doOnNext(aVoid -> onJoining())
            .subscribe(onShouldJoinRoom));

        tempSubscriptions.add(liveRowView.onNotifyStepDone()
            .subscribe(aVoid -> viewStatusName.setStatus(LiveStatusNameView.WAITING)));
      } else {
        viewStatusName.setStatus(LiveStatusNameView.WAITING);
        liveRowView.startPulse();
        onJoining();
        onShouldJoinRoom.onNext(null);
      }
    } else {
      hasJoined = true;
      refactorNotifyButton();
      if (live.getId().equals(Live.NEW_CALL)) viewLocalLive.showShareOverlay(live.getSource());
      onShouldJoinRoom.onNext(null);
    }
  }

  private void onJoining() {
    viewStatusName.setStatus(LiveStatusNameView.NOTIFYING);
    viewControlsLive.setNotifyEnabled(live.isCountdown());
    hasJoined = true;
  }

  public void update(Live live) {
    if (live != null) {
      LiveRowView liveRowView = liveRowViewMap.get(live.getSubId());
      if (liveRowView != null) {
        liveRowView.setGuest(
            new TribeGuest(live.getSubId(), live.getDisplayName(), live.getPicture(),
                live.isGroup(), live.isInvite(), live.getMembersPics(), false, live.getUserName()));
      }
    }
  }

  public Room getRoom() {
    return room;
  }

  public void displayWaitLivePopupTutorial(String displayName) {
    if (!joinLive) {
      onBuzzPopup.onNext(displayName);
    }
  }

  public void addTribeGuest(TribeGuest trg) {
    if (!liveInviteMap.getMap().containsKey(trg.getId()) &&
        !liveRowViewMap.getMap().containsKey(trg.getId())) {
      LiveRowView liveRowView = new LiveRowView(getContext());
      liveRowView.getViewTreeObserver()
          .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override public void onGlobalLayout() {
              liveRowView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
              liveRowView.showGuest(false);
              liveRowView.startPulse();
            }
          });

      addView(liveRowView, trg, PaletteGrid.getRandomColorExcluding(Color.BLACK), false);
      liveInviteMap.put(trg.getId(), liveRowView);

      subscribeOnRemovingGuestFromLive(liveRowView);
      refactorNotifyButton();
    }
  }

  private void subscribeOnRemovingGuestFromLive(LiveRowView liveRowView) {
    tempSubscriptions.add(liveRowView.onShouldRemoveGuest().doOnNext(tribeGuest -> {
      removeFromInvites(tribeGuest.getId());
      room.sendToPeers(getRemovedPayload(tribeGuest), true);
      refactorShareOverlay();
    }).subscribe());
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
    int count = 0;

    count += liveRowViewMap.getMap().size();
    count += liveInviteMap.getMap().size();

    return count + 1;
  }

  public int nbOtherUsersInRoom() {
    return liveRowViewMap.getMap().size();
  }

  public void setOpenInviteValue(float valueOpenInvite) {
    viewRoom.setOpenInviteValue(valueOpenInvite);
  }

  public boolean shouldLeave() {
    return liveRowViewMap.size() == 0 &&
        liveInviteMap.size() == 0 &&
        live != null &&
        !live.getSource().equals(SOURCE_CALL_ROULETTE);
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
    room.sendToPeers(getUserPlayload(user), true);
  }

  public void sendUnlockDice(String peerUserId, User user) {
    room.sendToUser(peerUserId, getUnlockRollTheDicePayload(user), true);
  }

  public void sendUnlockedDice(String peerUserId) {
    room.sendToUser(peerUserId, getUnlockedRollTheDicePayload(), true);
  }
  ////////////////
  //  PRIVATE   //
  ////////////////

  private void refactorNotifyButton() {
    boolean enable = shouldEnableBuzz();
    if (viewControlsLive != null) viewControlsLive.refactorNotifyButton(enable);
  }

  private void refactorShareOverlay() {
    if (!live.getId().equals(Live.NEW_CALL)) return;

    int nbPeople = nbInRoom();
    if (nbPeople > 1) {
      viewLocalLive.hideShareOverlay();
    } else {
      viewLocalLive.showShareOverlay(live.getSource());
    }
  }

  private void addView(LiveRowView liveRowView, TribeGuest guest, int color,
      boolean guestDraggedByMe) {
    liveRowView.setColor(color);
    liveRowView.setGuest(guest);
    liveRowView.setRoomType(viewRoom.getType());
    viewRoom.addView(liveRowView, guestDraggedByMe);
  }

  private void addView(RemotePeer remotePeer) {
    LiveRowView liveRowView = null;

    if (nbLiveInRoom() == 0) {
      soundManager.cancelMediaPlayer();
      if (viewStatusName != null) viewStatusName.setStatus(LiveStatusNameView.DONE);
    }

    if (liveInviteMap.getMap()
        .containsKey(
            remotePeer.getSession().getUserId())) { // If the user was invited before joining
      if (nbLiveInRoom() == 0) { // First user joining in a group call
        if (live.isGroup()) {
          String groupId = getGroupWaiting();
          if (!StringUtils.isEmpty(groupId)) {
            liveRowView = liveRowViewMap.remove(groupId);

            if (liveRowView != null && liveRowView.getParent() != null) {
              liveRowView.dispose();
              viewRoom.removeView(liveRowView);
            }
          }
        } else if (live.isInvite()) {
          String inviteId = getInviteWaiting();
          if (!StringUtils.isEmpty(inviteId)) {
            liveRowView = liveRowViewMap.remove(inviteId);
            if (liveRowView != null && liveRowView.getParent() != null) {
              liveRowView.dispose();
              viewRoom.removeView(liveRowView);
            }
          }
        }
      }

      liveRowView = liveInviteMap.get(remotePeer.getSession().getUserId());
      liveRowView.setPeerView(remotePeer.getPeerView());
      liveInviteMap.remove(remotePeer.getSession().getUserId());
      liveRowViewMap.put(remotePeer.getSession().getUserId(), liveRowView);
    } else if (liveRowViewMap.getMap()
        .containsKey(remotePeer.getSession()
            .getUserId())) { // If the user was already live, usually the case on 1-1 calls

      liveRowView = liveRowViewMap.get(remotePeer.getSession().getUserId());
      liveRowView.setPeerView(remotePeer.getPeerView());
    } else {
      TribeGuest guest = guestFromRemotePeer(remotePeer);

      if (nbLiveInRoom() == 0) { // First user joining in a group call
        if (live.isGroup()) { // if it's a group
          String groupId = getGroupWaiting();
          if (!StringUtils.isEmpty(groupId)) {
            liveRowView = liveRowViewMap.remove(groupId);
          }
        } else if (live.isInvite()) { // if it's an invite
          String inviteId = getInviteWaiting();
          if (!StringUtils.isEmpty(inviteId)) {
            liveRowView = liveRowViewMap.remove(inviteId);
          }
        }

        if (liveRowView != null) {
          liveRowView.setGuest(guest);
          liveRowView.setPeerView(remotePeer.getPeerView());
        }
      }

      if (liveRowView == null) {
        liveRowView = new LiveRowView(getContext());

        if (guest != null) {
          liveRowView.setGuest(guest);
          liveRowView.showGuest(false);
        }

        liveRowView.setRoomType(viewRoom.getType());
        liveRowView.setPeerView(remotePeer.getPeerView());

        viewRoom.addView(liveRowView, false);
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

  private void removeFromPeers(String id) {
    if (liveRowViewMap.getMap().containsKey(id)) {
      LiveRowView liveRowView = liveRowViewMap.remove(id);
      liveRowView.dispose();
      //viewRoom.removeView(liveRowView);

      if (liveRowView.getParent() != null) {
        ((ViewGroup) liveRowView.getParent()).removeView(liveRowView);
      }
    }
  }

  private void removeFromInvites(String id) {
    if (liveInviteMap.getMap().containsKey(id)) {
      LiveRowView liveRowView = liveInviteMap.remove(id);
      liveRowView.dispose();
      viewRoom.removeView(liveRowView);

      refactorNotifyButton();
    }
  }

  private TribeGuest guestFromRemotePeer(RemotePeer remotePeer) {
    TribeGuest guest = null;

    for (Friendship friendship : user.getFriendships()) {
      if (remotePeer.getSession().getUserId().equals(friendship.getSubId())) {
        guest = new TribeGuest(friendship.getSubId(), friendship.getDisplayName(),
            friendship.getProfilePicture(), false, false, null, true, friendship.getUsername());
        guest.setExternal(remotePeer.getSession().isExternal());
      }
    }

    if (guest == null) {
      guest = new TribeGuest(remotePeer.getSession().getUserId(),
          getDisplayNameFromId(remotePeer.getSession().getUserId()), null, false, false, null,
          false, "");
      guest.setExternal(remotePeer.getSession().isExternal());
    }

    return guest;
  }

  public JSONObject getNewGamePayload(Game game) {
    JSONObject obj = new JSONObject();
    JSONObject gameStart = new JSONObject();
    jsonPut(gameStart, Game.ACTION, Game.START);
    jsonPut(gameStart, Game.ID, game.getId());
    jsonPut(obj, Room.MESSAGE_GAME, gameStart);
    return obj;
  }

  public JSONObject getNewChallengePayload(String userId, String peerId, String challengeMessage) {
    JSONObject app = new JSONObject();
    JSONObject obj = new JSONObject();
    JSONObject challenge = new JSONObject();
    jsonPut(challenge, "from", userId);
    jsonPut(challenge, Game.ACTION, Game.NEW_CHALLENGE);
    jsonPut(challenge, "user", peerId);
    jsonPut(challenge, Game.CHALLENGE, challengeMessage);
    jsonPut(obj, Game.GAME_CHALLENGE, challenge);
    jsonPut(app, "app", obj);
    return app;
  }

  public JSONObject getNewDrawPayload(String userId, String peerId, String draw) {
    JSONObject app = new JSONObject();
    JSONObject obj = new JSONObject();
    JSONObject game = new JSONObject();
    jsonPut(game, "from", userId);
    jsonPut(game, Game.ACTION, "newDraw");
    jsonPut(game, "user", peerId);
    jsonPut(game, "draw", draw);
    jsonPut(obj, "draw", game);
    jsonPut(app, "app", obj);
    return app;
  }

  public JSONObject getDrawClearPayload() {
    JSONObject app = new JSONObject();
    JSONObject obj = new JSONObject();
    JSONObject game = new JSONObject();
    jsonPut(game, "action", "clear");
    jsonPut(obj, "draw", game);
    jsonPut(app, "app", obj);
    return app;
  }

  public JSONObject getDrawPointPayload(List<Float[]> map) {
    JSONObject app = new JSONObject();
    JSONObject game = new JSONObject();
    JSONObject path = new JSONObject();
    JSONArray array = new JSONArray();

    for (Float[] value : map) {
      JSONArray coord = new JSONArray();
      coord.put(value[0]);
      coord.put(value[1]);
      array.put(coord);
    }

    jsonPut(path, "hexColor", "F9AD25");
    jsonPut(path, "lineWidth", 6.0);
    jsonPut(path, "id", "test");
    jsonPut(path, "points", array);

    JSONObject gameObject = new JSONObject();
    jsonPut(gameObject, "action", "drawPath");
    jsonPut(gameObject, "path", path);

    jsonPut(game, "draw", gameObject);
    jsonPut(app, "app", game);
    return app;
  }

  public JSONObject getStopGamePayload(Game game) {
    JSONObject obj = new JSONObject();
    JSONObject gameStop = new JSONObject();
    jsonPut(gameStop, Game.ACTION, Game.STOP);
    jsonPut(gameStop, Game.ID, game.getId());
    jsonPut(obj, Room.MESSAGE_GAME, gameStop);
    return obj;
  }

  private JSONObject getInvitedPayload() {
    JSONObject jsonObject = new JSONObject();
    JSONArray array = new JSONArray();
    for (LiveRowView liveRowView : liveInviteMap.getMap().values()) {
      JSONObject invitedGuest = new JSONObject();
      jsonPut(invitedGuest, TribeGuest.ID, liveRowView.getGuest().getId());
      jsonPut(invitedGuest, TribeGuest.DISPLAY_NAME, liveRowView.getGuest().getDisplayName());
      jsonPut(invitedGuest, TribeGuest.PICTURE, liveRowView.getGuest().getPicture());
      jsonPut(invitedGuest, TribeGuest.USERNAME, liveRowView.getGuest().getUserName());
      array.put(invitedGuest);
    }
    jsonPut(jsonObject, Room.MESSAGE_INVITE_ADDED, array);
    return jsonObject;
  }

  private JSONObject getUnlockRollTheDicePayload(User user) {

    JSONObject appJson1 = new JSONObject();
    JsonUtils.jsonPut(appJson1, "by", getUserPlayload(user));
    JSONObject appJson = new JSONObject();
    JsonUtils.jsonPut(appJson, Room.MESSAGE_UNLOCK_ROLL_DICE, appJson1);

    return appJson;
  }

  private JSONObject getUnlockedRollTheDicePayload() {

    JSONObject appJson = new JSONObject();
    JsonUtils.jsonPut(appJson, Room.MESSAGE_UNLOCKED_ROLL_DICE, true);

    return appJson;
  }

  private JSONObject getUserPlayload(User user) {
    JSONObject jsonObject = new JSONObject();

    JSONObject userJson = new JSONObject();
    jsonPut(userJson, User.ID, user.getId());
    jsonPut(userJson, User.FBID, user.getFbid());
    jsonPut(userJson, User.USERNAME, user.getUsername());
    jsonPut(userJson, User.DISPLAY_NAME, user.getDisplayName());
    jsonPut(userJson, User.PICTURE, user.getProfilePicture());

    JSONObject json = new JSONObject();
    try {
      json.put("by", userJson);
    } catch (JSONException e) {
      e.printStackTrace();
    }

    jsonPut(jsonObject, Room.MESSAGE_ROLL_THE_DICE, json);
    return jsonObject;
  }

  private JSONObject getRemovedPayload(TribeGuest guest) {
    JSONObject jsonObject = new JSONObject();
    JSONArray array = new JSONArray();

    array.put(guest.getId());

    jsonPut(jsonObject, Room.MESSAGE_INVITE_REMOVED, array);
    return jsonObject;
  }

  private void jsonPut(JSONObject json, String key, Object value) {
    try {
      json.put(key, value);
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean shouldEnableBuzz() {
    boolean result = true;
    int nbLiveInRoom = nbLiveInRoom() + 1;

    if (live == null) return false;
    if (nbLiveInRoom == LIVE_MAX) return false;

    if (live.isGroup()) {
      if (live.getMembers() != null && live.getMembers().size() == nbLiveInRoom) result = false;
    } else if (!isTherePeopleWaiting()) {
      result = false;
    }

    return result;
  }

  private int nbLiveInRoom() {
    int count = 0;

    for (LiveRowView liveRowView : liveRowViewMap.getMap().values()) {
      if (!liveRowView.isWaiting()) count++;
    }

    return count;
  }

  public RoomMember getUsersInLiveRoom() {
    ArrayList<TribeGuest> usersInLive = new ArrayList<>();
    ArrayList<String> myFriendIds = new ArrayList<>();
    ArrayList<TribeGuest> anonymousGuestInLive = new ArrayList<>();
    ArrayList<TribeGuest> externalInRoom = new ArrayList<>();

    for (String liveRowViewId : liveRowViewMap.getMap().keySet()) {
      LiveRowView liveRowView = liveRowViewMap.getMap().get(liveRowViewId);
      if (!liveRowView.isWaiting()) {
        TribeGuest guest = liveRowView.getGuest();
        if (guest != null) {
          usersInLive.add(guest);
        }
        if (guest != null && guest.isExternal()) {
          externalInRoom.add(guest);
        }
      }
    }

    for (Friendship friendship : user.getFriendships()) {
      myFriendIds.add(friendship.getFriend().getId());
    }

    for (TribeGuest guest : usersInLive) {
      if (myFriendIds.contains(guest.getId())) {
        guest.setAnonymous(false);
      } else {
        guest.setAnonymous(true);
      }
    }

    for (User user : anonymousInLive) {
      TribeGuest guest = new TribeGuest(user.getId());
      guest.setDisplayName(user.getDisplayName());
      guest.setPicture(user.getProfilePicture());
      guest.setUserName(user.getUsername());
      guest.setAnonymous(true);
      anonymousGuestInLive.add(guest);
    }

    return new RoomMember(usersInLive, anonymousGuestInLive, externalInRoom);
  }

  private boolean isTherePeopleWaiting() {
    boolean waiting = false;

    for (LiveRowView liveRowView : liveRowViewMap.getMap().values()) {
      if (liveRowView.isWaiting()) waiting = true;
    }

    if (!waiting) waiting = liveInviteMap.size() > 0;

    return waiting;
  }

  private String getGroupWaiting() {
    String id = "";

    for (LiveRowView liveRowView : liveRowViewMap.getMap().values()) {
      if (liveRowView.isWaiting() && liveRowView.isGroup()) id = liveRowView.getGuest().getId();
    }

    return id;
  }

  private String getInviteWaiting() {
    String id = "";

    for (LiveRowView liveRowView : liveRowViewMap.getMap().values()) {
      if (liveRowView.isWaiting() && liveRowView.isInvite()) id = liveRowView.getGuest().getId();
    }

    return id;
  }

  private String getDisplayNameFromSession(TribeSession tribeSession) {
    return tribeSession.isExternal() ? getContext().getString(R.string.live_external_user)
        : getDisplayNameFromId(tribeSession.getUserId());
  }

  private String getDisplayNameFromId(String id) {
    String tribeGuestName = "";
    for (Membership membership : user.getMembershipList()) {
      for (User member : membership.getGroup().getMembers()) {
        if (member.getId().equals(id)) {
          tribeGuestName = member.getDisplayName();
        }
      }
    }

    for (Friendship friendship : user.getFriendships()) {
      User friend = friendship.getFriend();
      if (friend.getId().equals(id)) {
        tribeGuestName = friend.getDisplayName();
      }
    }

    for (User anonymousUser : anonymousInLive) {
      if (anonymousUser.getId().equals(id)) {
        tribeGuestName = anonymousUser.getDisplayName();
      }
    }

    return tribeGuestName;
  }

  private Object computeGuest(String id) {
    for (Friendship friendship : user.getFriendships()) {
      User friend = friendship.getFriend();
      if (friend.getId().equals(id)) {
        return friendship;
      }
    }

    for (User anonymousUser : anonymousInLive) {
      if (anonymousUser.getId().equals(id)) {
        return anonymousUser;
      }
    }

    return null;
  }

  private void onRestartGame(Game game) {
    if (game == null) return;

    if (game instanceof GameChallenge) {
      GameChallenge challenge = (GameChallenge) game;
      if (challenge.getCurrentChallenger().getId().equals(user.getId())) {
        gameChallengesView.displayPopup(txtTooltipFirstGame.getTranslationY());
        return;
      }
    }

    restartGame(game);
    displayReRollGameNotification(game.getId(), user.getDisplayName());
  }

  private void startGame(Game game, boolean isUserAction) {
    if (game == null) return;

    if (!isUserAction) viewControlsLive.startGameFromAnotherUser(game);
    postItGameCount++;
    game.setUserAction(isUserAction);
    gameManager.setCurrentGame(game);
    onStartGame.onNext(game);
    viewLocalLive.startGame(game);
    if (stateManager.shouldDisplay(StateManager.NEW_GAME_START)) {
      AnimationUtils.animateBottomMargin(viewControlsLive, tooltipFirstGameHeight, DURATION);
      txtTooltipFirstGame.animate()
          .translationY(0)
          .setDuration(DURATION)
          .setInterpolator(new DecelerateInterpolator())
          .start();
    }
  }

  private void restartGame(Game game) {
    startGame(game, true);
    room.sendToPeers(getNewGamePayload(game), false);
  }

  private void stopGame(boolean isCurrentUserAction, String gameId) {
    switch (gameId) {
      case Game.GAME_CHALLENGE:
        gameChallengesView.setVisibility(GONE);
        gameChallengesView.close();
        break;
      case Game.GAME_DRAW:
        gameDrawView.setVisibility(GONE);
        gameDrawView.close();
        break;
    }
    gameManager.setCurrentGame(null);
    viewControlsLive.stopGame();
    viewLocalLive.stopGame();
    if (stateManager.shouldDisplay(StateManager.NEW_GAME_START)) {
      if (isCurrentUserAction) stateManager.addTutorialKey(StateManager.NEW_GAME_START);
      txtTooltipFirstGame.animate()
          .translationY(txtTooltipFirstGame.getHeight())
          .setDuration(DURATION)
          .setInterpolator(new DecelerateInterpolator())
          .start();
      AnimationUtils.animateBottomMargin(viewControlsLive, 0, DURATION);
    }
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

  public User getUser() {
    return user;
  }

  public void blockOpenInviteView(boolean b) {
    viewControlsLive.blockOpenInviteViewBtn(b);
  }

  //////////////////////
  //   OBSERVABLES    //
  //////////////////////

  public Observable<Void> onOpenInvite() {
    return onOpenInvite;
  }

  public Observable<String> onBuzzPopup() {
    return onBuzzPopup;
  }

  public Observable<Void> onShouldJoinRoom() {
    return onShouldJoinRoom;
  }

  public Observable<List<String>> onNewChallengeReceived() {
    return onNewChallengeReceived;
  }

  public Observable<List<String>> onNewDrawReceived() {
    return onNewDrawReceived;
  }

  public Observable<Void> onClearDrawReceived() {
    return onClearDrawReceived;
  }

  public Observable<String> unlockRollTheDice() {
    return unlockRollTheDice;
  }

  public Observable<String> onPointsDrawReceived() {
    return onPointsDrawReceived;
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

  public Observable<Map<String, LiveRowView>> onInvitesChanged() {
    return liveInviteMap.getMapObservable();
  }

  public Observable<Map<String, LiveRowView>> onLiveChanged() {
    return liveRowViewMap.getMapObservable();
  }

  public Observable<Void> onShare() {
    return onShare;
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

  public Observable<Game> onStartGame() {
    return onStartGame;
  }

  public Observable<View> onGameUIActive() {
    return viewControlsLive.onGameUIActive();
  }

  public Observable<Boolean> onBlockOpenInviteView() {
    return onBlockOpenInviteView;
  }
}

