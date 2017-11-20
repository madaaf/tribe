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
import butterknife.OnClick;
import butterknife.Unbinder;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.R;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.Live;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.entity.RoomMember;
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
import com.tribe.app.presentation.utils.preferences.PreferencesUtils;
import com.tribe.app.presentation.view.activity.LiveActivity;
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

  @BindView(R.id.txtTooltipFirstGame) TextViewFont txtTooltipFirstGame;

  @BindView(R.id.gameChallengesView) GameChallengesView gameChallengesView;

  @BindView(R.id.gameDrawView) GameDrawView gameDrawView;

  @BindView(R.id.viewDarkOverlay) View viewDarkOverlay;

  @BindView(R.id.viewRinging) LiveRingingView viewRinging;

  @BindView(R.id.viewLiveInvite) LiveInviteView viewLiveInvite;

  @BindView(R.id.viewShadow) View viewShadow;

  // VARIABLES
  private Live live;
  private com.tribe.tribelivesdk.core.Room webRTCRoom;
  private ObservableRxHashMap<String, LiveRowView> liveRowViewMap;
  private boolean hiddenControls = false;
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
  private PublishSubject<WebSocketError> onRoomError = PublishSubject.create();
  private PublishSubject<Void> onChangeCallRouletteRoom = PublishSubject.create();
  private PublishSubject<Object> onRemotePeerClick = PublishSubject.create();
  private PublishSubject<Game> onStartGame = PublishSubject.create();
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
      } else if ((hasJoined && averageCountLive <= 1 && !live.getType().equals(Live.NEW_CALL)) || (
          live.getType().equals(Live.NEW_CALL)
              && (invitedCount > 0 || hasShared))) {
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
      tagMap.put(TagManagerUtils.POST_IT_GAME_COUNT, postItGameCount);
      tagMap.put(TagManagerUtils.TYPE, TagManagerUtils.DIRECT);
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

    if (webRTCRoom != null && !isJump) {
      Timber.d("webRTCRoom leave");
      webRTCRoom.leaveRoom();
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

    persistentSubscriptions.add(viewControlsLive.onStartGame().subscribe(game -> {
      displayStartGameNotification(game.getName(), user.getDisplayName());
      restartGame(game);
    }));

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
      webRTCRoom.sendToPeers(getNewDrawPayload(user.getId(), draw.getCurrentDrawer().getId(),
          draw.getCurrentDrawName()), false);
    }));

    persistentSubscriptions.add(gameChallengesView.onCurrentGame().subscribe(game -> {
      GameChallenge draw = (GameChallenge) game;
      webRTCRoom.sendToPeers(
          getNewChallengePayload(user.getId(), draw.getCurrentChallenger().getId(),
              draw.getCurrentChallenge()), false);
    }));

    persistentSubscriptions.add(gameDrawView.onClearDraw().subscribe(aVoid -> {
      webRTCRoom.sendToPeers(getDrawClearPayload(), false);
    }));

    persistentSubscriptions.add(gameDrawView.onDrawing().subscribe(points -> {
      webRTCRoom.sendToPeers(getDrawPointPayload(points), false);
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
                webRTCRoom.sendToPeers(getStopGamePayload(game), false);
              }

              return null;
            }))
        .subscribe());

    persistentSubscriptions.add(viewRoom.onShouldCloseInvites().subscribe(aVoid -> {
      onShouldCloseInvites.onNext(null);
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

  @OnClick(R.id.viewRoom) void onClickRoom() {
    if (hiddenControls) {
      onHiddenControls.onNext(false);
    }
  }

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

    tempSubscriptions.add(webRTCRoom.onRoomStateChanged().subscribe(state -> {
      Timber.d("Room state change : " + state);
      if (state == com.tribe.tribelivesdk.core.Room.STATE_CONNECTED) {
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

    tempSubscriptions.add(
        webRTCRoom.unlockRollTheDice().onBackpressureDrop().subscribe(unlockRollTheDice));

    tempSubscriptions.add(
        webRTCRoom.onPointsDrawReceived().onBackpressureDrop().subscribe(onPointsDrawReceived));

    tempSubscriptions.add(
        webRTCRoom.onNewChallengeReceived().onBackpressureDrop().subscribe(onNewChallengeReceived));

    tempSubscriptions.add(
        webRTCRoom.onNewDrawReceived().onBackpressureDrop().subscribe(onNewDrawReceived));

    tempSubscriptions.add(
        webRTCRoom.onClearDrawReceived().onBackpressureDrop().subscribe(onClearDrawReceived));

    tempSubscriptions.add(
        webRTCRoom.unlockedRollTheDice().onBackpressureDrop().subscribe(unlockedRollTheDice));

    tempSubscriptions.add(webRTCRoom.onJoined()
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(tribeJoinRoom -> hasJoined = true)
        .subscribe(onJoined));

    tempSubscriptions.add(webRTCRoom.onShouldLeaveRoom().subscribe(onLeave));

    tempSubscriptions.add(webRTCRoom.onRoomError().onBackpressureDrop().subscribe(onRoomError));

    tempSubscriptions.add(
        webRTCRoom.onNewGame().onBackpressureDrop().subscribe(pairSessionGame -> {//OEF
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

    tempSubscriptions.add(
        webRTCRoom.onStopGame().onBackpressureDrop().subscribe(pairSessionGame -> {
          Game game = gameManager.getGameById(pairSessionGame.second);
          if (game == null) return;
          String displayName = getDisplayNameFromSession(pairSessionGame.first);
          displayStopGameNotification(game.getName(), displayName);
          stopGame(false, game.getId());
        }));

    tempSubscriptions.add(webRTCRoom.onRemotePeerAdded()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(remotePeer -> {
          if (isFirstToJoin) {
            viewRinging.stopRinging();
            viewRinging.setVisibility(View.GONE);
            isFirstToJoin = !isFirstToJoin;
          }

          soundManager.playSound(SoundManager.JOIN_CALL, SoundManager.SOUND_MAX);
          joinLive = true;

          String username = getDisplayNameFromId(remotePeer.getSession().getUserId());

          if ((username == null || username.isEmpty()) && !remotePeer.getSession().isExternal()) {
            Timber.d("anonymous joinded in room with id : " + remotePeer.getSession().getUserId());
            onAnonymousJoined.onNext(remotePeer.getSession().getUserId());
          }

          Timber.d("Remote peer added with id : "
              + remotePeer.getSession().getPeerId()
              + " & view : "
              + remotePeer.getPeerView());
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
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(remotePeer -> {

          soundManager.playSound(SoundManager.QUIT_CALL, SoundManager.SOUND_MAX);

          Timber.d("Remote peer removed with id : " + remotePeer.getSession().getPeerId());
          removeFromPeers(remotePeer.getSession().getUserId());

          if (shouldLeave()) {
            onLeave.onNext(null);
          }

          live.getRoom().userLeftWebRTC(remotePeer.getSession().getUserId());

          onNotificationRemotePeerRemoved.onNext(
              getDisplayNameFromSession(remotePeer.getSession()));
        }));

    tempSubscriptions.add(webRTCRoom.onReceivedStream().subscribe(remotePeer -> {
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

    tempSubscriptions.add(viewRoom.onChangeCallRouletteRoom().subscribe(onChangeCallRouletteRoom));
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

    viewControlsLive.setLive(live);
    viewLiveInvite.setLive(live);

    viewRinging.setLive(live);
    viewRinging.startRinging();

    tempSubscriptions.add(Observable.timer(3000, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> onShouldJoinRoom.onNext(null)));
  }

  public com.tribe.tribelivesdk.core.Room getWebRTCRoom() {
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
    return liveRowViewMap.size() == 0 && live != null && !live.getSource()
        .equals(SOURCE_CALL_ROULETTE);
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

  private void addView(LiveRowView liveRowView, TribeGuest guest) {
    liveRowView.setGuest(guest);
    viewRoom.addView(liveRowView);
  }

  private void addView(RemotePeer remotePeer) {
    LiveRowView liveRowView = null;

    if (liveRowViewMap.getMap()
        .containsKey(remotePeer.getSession()
            .getUserId())) { // If the user was already live, usually the case on 1-1 calls

      liveRowView = liveRowViewMap.get(remotePeer.getSession().getUserId());
      liveRowView.setPeerView(remotePeer.getPeerView());
    } else {
      TribeGuest guest = guestFromRemotePeer(remotePeer);

      if (nbLiveInRoom() == 0) { // First user joining in a group call
        if (live.fromRoom()) { // if it's from a room
          String inviteId = getInviteWaiting();
          if (!StringUtils.isEmpty(inviteId)) {
            liveRowView = liveRowViewMap.remove(inviteId, true);
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
        }

        liveRowView.setPeerView(remotePeer.getPeerView());

        viewRoom.addView(liveRowView);
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
      LiveRowView liveRowView = liveRowViewMap.remove(id, true);
      liveRowView.dispose();

      if (liveRowView.getParent() != null) {
        ((ViewGroup) liveRowView.getParent()).removeView(liveRowView);
      }
    }
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

  public JSONObject getNewGamePayload(Game game) {
    JSONObject obj = new JSONObject();
    JSONObject gameStart = new JSONObject();
    jsonPut(gameStart, Game.ACTION, Game.START);
    jsonPut(gameStart, Game.ID, game.getId());
    jsonPut(obj, com.tribe.tribelivesdk.core.Room.MESSAGE_GAME, gameStart);
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
    jsonPut(path, "id", "view_recycler_message");
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
    jsonPut(obj, com.tribe.tribelivesdk.core.Room.MESSAGE_GAME, gameStop);
    return obj;
  }

  private JSONObject getInvitedPayload() {
    JSONObject jsonObject = new JSONObject();
    JSONArray array = new JSONArray();

    for (User user : live.getRoom().getInvitedUsers()) {
      JSONObject invitedGuest = new JSONObject();
      jsonPut(invitedGuest, TribeGuest.ID, user.getId());
      jsonPut(invitedGuest, TribeGuest.DISPLAY_NAME, user.getDisplayName());
      jsonPut(invitedGuest, TribeGuest.PICTURE, user.getProfilePicture());
      jsonPut(invitedGuest, TribeGuest.USERNAME, user.getUsername());
      array.put(invitedGuest);
    }

    jsonPut(jsonObject, com.tribe.tribelivesdk.core.Room.MESSAGE_INVITE_ADDED, array);
    return jsonObject;
  }

  private JSONObject getUnlockRollTheDicePayload(User user) {

    JSONObject appJson1 = new JSONObject();
    JsonUtils.jsonPut(appJson1, "by", getUserPlayload(user));
    JSONObject appJson = new JSONObject();
    JsonUtils.jsonPut(appJson, com.tribe.tribelivesdk.core.Room.MESSAGE_UNLOCK_ROLL_DICE, appJson1);

    return appJson;
  }

  private JSONObject getUnlockedRollTheDicePayload() {

    JSONObject appJson = new JSONObject();
    JsonUtils.jsonPut(appJson, com.tribe.tribelivesdk.core.Room.MESSAGE_UNLOCKED_ROLL_DICE, true);

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

    jsonPut(jsonObject, com.tribe.tribelivesdk.core.Room.MESSAGE_ROLL_THE_DICE, json);
    return jsonObject;
  }

  private JSONObject getRemovedPayload(TribeGuest guest) {
    JSONObject jsonObject = new JSONObject();
    JSONArray array = new JSONArray();

    array.put(guest.getId());

    jsonPut(jsonObject, com.tribe.tribelivesdk.core.Room.MESSAGE_INVITE_REMOVED, array);
    return jsonObject;
  }

  private void jsonPut(JSONObject json, String key, Object value) {
    try {
      json.put(key, value);
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  private int nbLiveInRoom() {
    return live.getRoom().nbUsersLive();
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

    // TODO REPLACE WITH SHORTCUTS
    //for (Friendship friendship : user.getFriendships()) {
    //  myFriendIds.add(friendship.getFriend().getId());
    //}

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

  private String getInviteWaiting() {
    String id = "";

    for (LiveRowView liveRowView : liveRowViewMap.getMap().values()) {
      if (liveRowView.isWaiting()) id = liveRowView.getGuest().getId();
    }

    return id;
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

  private void onRestartGame(Game game) {
    if (game == null) return;

    if (game instanceof GameChallenge) {
      GameChallenge challenge = (GameChallenge) game;
      if (challenge.getCurrentChallenger() != null && challenge.getCurrentChallenger()
          .getId()
          .equals(user.getId())) {
        gameChallengesView.displayPopup(txtTooltipFirstGame.getTranslationY());
        return;
      }
    }

    restartGame(game);
    displayReRollGameNotification(game.getId(), user.getDisplayName());
  }

  private void startGame(Game game, boolean isUserAction) {
    if (game == null) return;

    onTouchEnabled.onNext(false);

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
    webRTCRoom.sendToPeers(getNewGamePayload(game), false);
  }

  private void stopGame(boolean isCurrentUserAction, String gameId) {
    onTouchEnabled.onNext(true);
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

  //////////////////////
  //   OBSERVABLES    //
  //////////////////////

  public Observable<Boolean> onOpenInvite() {
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

  public Observable<Boolean> onGameMenuOpen() {
    return viewControlsLive.onGameMenuOpen();
  }

  public Observable<Boolean> onTouchEnabled() {
    return onTouchEnabled;
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

  public Observable<Map<String, LiveRowView>> onLiveChanged() {
    return liveRowViewMap.getMapObservable();
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
}

