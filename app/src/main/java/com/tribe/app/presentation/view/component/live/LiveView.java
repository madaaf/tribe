package com.tribe.app.presentation.view.component.live;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
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
import com.tribe.app.domain.entity.Live;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.RoomConfiguration;
import com.tribe.app.domain.entity.RoomMember;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManager;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.utils.preferences.CallTagsMap;
import com.tribe.app.presentation.utils.preferences.CounterOfCallsForGrpButton;
import com.tribe.app.presentation.utils.preferences.MinutesOfCalls;
import com.tribe.app.presentation.utils.preferences.NumberOfCalls;
import com.tribe.app.presentation.utils.preferences.PreferencesUtils;
import com.tribe.app.presentation.view.component.TileView;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.Degrees;
import com.tribe.app.presentation.view.utils.DoubleUtils;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.app.presentation.view.utils.StateManager;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import com.tribe.tribelivesdk.TribeLiveSDK;
import com.tribe.tribelivesdk.back.TribeLiveOptions;
import com.tribe.tribelivesdk.back.WebSocketConnection;
import com.tribe.tribelivesdk.core.Room;
import com.tribe.tribelivesdk.model.RemotePeer;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.model.TribeJoinRoom;
import com.tribe.tribelivesdk.model.TribePeerMediaConfiguration;
import com.tribe.tribelivesdk.model.TribeSession;
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
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by tiago on 01/18/2017.
 */
public class LiveView extends FrameLayout {

  public static final int LIVE_MAX = 8;

  private static final int DURATION = 300;

  private static final int DURATION_FAST_FURIOUS = 60;
  private static final float OVERSHOOT = 1.2f;
  private static final float MARGIN_BOTTOM = 25;
  private static final float AVATAR_SCALING = 0.5f;
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

  @BindView(R.id.viewBuzz) BuzzView viewBuzz;

  @BindView(R.id.btnLeave) View btnLeave;

  @BindView(R.id.btnScreenshot) ImageView btnScreenshot;

  // VARIABLES
  private Live live;
  private Room room;
  private LiveRowView latestView;
  private ObservableRxHashMap<String, LiveRowView> liveRowViewMap;
  private ObservableRxHashMap<String, LiveRowView> liveInviteMap;
  private boolean hiddenControls = false;
  private @LiveContainer.Event int stateContainer = LiveContainer.EVENT_CLOSED;
  private AvatarView avatarView;
  private ObjectAnimator animatorBuzzAvatar;
  private Map<String, Object> tagMap;
  private int wizzCount = 0, screenshotCount = 0, invitedCount = 0, totalSizeLive = 0;
  private double averageCountLive = 0.0D;
  private boolean hasJoined = false;
  private long timeStart = 0L, timeEnd = 0L;
  private boolean isParamExpended = false, isMicroActivated = true, isCameraActivated = true, hasShared = false;
  private View view;
  private int sizeAnimAvatarMax;
  private List<User> anonymousInLive = new ArrayList<>();
  private boolean isFirstToJoin = true;

  // RESOURCES
  private int timeJoinRoom, statusBarHeight, margin;

  // OBSERVABLES
  private Unbinder unbinder;
  private CompositeSubscription persistentSubscriptions = new CompositeSubscription();
  private CompositeSubscription tempSubscriptions = new CompositeSubscription();
  private PublishSubject<Void> onOpenInvite = PublishSubject.create();
  private PublishSubject<String> onBuzzPopup = PublishSubject.create();
  private PublishSubject<Void> onShouldJoinRoom = PublishSubject.create();
  private PublishSubject<Void> onNotify = PublishSubject.create();
  private PublishSubject<Void> onLeave = PublishSubject.create();
  private PublishSubject<Void> onScreenshot = PublishSubject.create();
  private PublishSubject<Boolean> onHiddenControls = PublishSubject.create();
  private PublishSubject<Void> onShouldCloseInvites = PublishSubject.create();
  private PublishSubject<String> onRoomStateChanged = PublishSubject.create();
  private PublishSubject<TribeJoinRoom> onJoined = PublishSubject.create();
  private PublishSubject<Void> onShare = PublishSubject.create();
  private PublishSubject<Void> onRoomFull = PublishSubject.create();

  private PublishSubject<String> onNotificationRemotePeerInvited = PublishSubject.create();
  private PublishSubject<String> onNotificationRemotePeerRemoved = PublishSubject.create();
  private PublishSubject<String> onNotificationRemoteWaiting = PublishSubject.create();
  private PublishSubject<String> onNotificationRemotePeerBuzzed = PublishSubject.create();
  private PublishSubject<String> onNotificationRemoteJoined = PublishSubject.create();
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
    room.jump();
  }

  public String getDisplayName() {
    return live.getDisplayName();
  }

  public void endCall(boolean isJump) {
    String state = TagManagerUtils.CANCELLED;

    if (live != null) {
      double duration = 0.0D;

      if (timeStart > 0) {
        timeEnd = System.currentTimeMillis();
        long delta = timeEnd - timeStart;
        duration = (double) delta / 60000.0;
        duration = DoubleUtils.round(duration, 2);
      }

      if (hasJoined && averageCountLive > 1) {
        state = TagManagerUtils.ENDED;
        counterOfCallsForGrpButton.set(counterOfCallsForGrpButton.get() + 1);
        numberOfCalls.set(numberOfCalls.get() + 1);
        Float totalDuration = minutesOfCalls.get() + (float) duration;
        minutesOfCalls.set(totalDuration);
        tagManager.increment(TagManagerUtils.USER_CALLS_COUNT);
        tagManager.increment(TagManagerUtils.USER_CALLS_MINUTES, duration);
      } else if ((hasJoined && averageCountLive <= 1 && !live.getId().equals(Live.NEW_CALL)) ||
          (live.getId().equals(Live.NEW_CALL) && (invitedCount > 0 || hasShared))) {
        state = TagManagerUtils.MISSED;
        tagManager.increment(TagManagerUtils.USER_CALLS_MISSED_COUNT);
      }

      tagMap.put(TagManagerUtils.EVENT, TagManagerUtils.Calls);
      tagMap.put(TagManagerUtils.SOURCE, live.getSource());
      tagMap.put(TagManagerUtils.DURATION, duration);
      tagMap.put(TagManagerUtils.STATE, state);
      tagMap.put(TagManagerUtils.MEMBERS_INVITED, invitedCount);
      tagMap.put(TagManagerUtils.WIZZ_COUNT, wizzCount);
      tagMap.put(TagManagerUtils.SCREENSHOT_COUNT, screenshotCount);
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

    for (LiveRowView liveRowView : liveInviteMap.getMap().values()) {
      Timber.d("liveinviteview dispose");
      liveRowView.dispose();
      viewRoom.removeView(liveRowView);
    }

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

    if (animatorBuzzAvatar != null) {
      animatorBuzzAvatar.cancel();
    }

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

    initResources();
    initUI();
    initSubscriptions();

    super.onFinishInflate();
  }

  @Override public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (viewControlsLive == null) return;

    if (room != null) {
      room.sendOrientation(Degrees.getNormalizedDegrees(getContext()),
          viewLocalLive.isFrontFacing());
    }

    onShouldCloseInvites.onNext(null);

    ViewGroup.LayoutParams lp = view.getLayoutParams();
    lp.width = screenUtils.getWidthPx();
    lp.height = screenUtils.getHeightPx();
    view.setLayoutParams(lp);

    FrameLayout.LayoutParams params =
        new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    params.gravity = Gravity.BOTTOM;
    params.bottomMargin = screenUtils.dpToPx(MARGIN_BOTTOM);
    viewControlsLive.setLayoutParams(params);
  }

  //////////////////////
  //      INIT        //
  //////////////////////

  private void init() {
    liveRowViewMap = new ObservableRxHashMap<>();
    liveInviteMap = new ObservableRxHashMap<>();
    tagMap = new HashMap<>();
    sizeAnimAvatarMax = getResources().getDimensionPixelSize(R.dimen.avatar_size_smaller);
  }

  private void initUI() {
    setBackgroundColor(Color.BLACK);
  }

  private void initResources() {
    timeJoinRoom = getResources().getInteger(R.integer.time_join_room);
    btnScreenshot.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override public void onGlobalLayout() {
            btnScreenshot.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            margin = btnScreenshot.getTop() + getResources().getDimensionPixelSize(
                R.dimen.horizontal_margin_smaller) + screenUtils.dpToPx(0.5f);
          }
        });
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

    persistentSubscriptions.add(viewLocalLive.onShare().doOnNext(aVoid -> hasShared = true).subscribe(onShare));

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
        viewBuzz.buzz();

        if (avatarView != null) {
          animatorBuzzAvatar = ObjectAnimator.ofFloat(avatarView, TRANSLATION_X, 3, -3);
          animatorBuzzAvatar.setDuration(DURATION_FAST_FURIOUS);
          animatorBuzzAvatar.setRepeatCount(ValueAnimator.INFINITE);
          animatorBuzzAvatar.setRepeatMode(ValueAnimator.REVERSE);
          animatorBuzzAvatar.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationCancel(Animator animation) {
              animatorBuzzAvatar.removeAllListeners();
              avatarView.setTranslationX(0);
            }
          });
          animatorBuzzAvatar.start();
        }

        for (LiveRowView liveRowView : liveInviteMap.getMap().values()) {
          liveRowView.buzz();
        }

        for (LiveRowView liveRowView : liveRowViewMap.getMap().values()) {
          if (liveRowView.isWaiting()) liveRowView.buzz();
        }
      }
    }).subscribe(onNotify));

    persistentSubscriptions.add(viewControlsLive.onNotifyAnimationDone().subscribe(aVoid -> {
      if (animatorBuzzAvatar != null) animatorBuzzAvatar.cancel();

      tempSubscriptions.add(Observable.timer(1000, TimeUnit.MILLISECONDS)
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(aLong -> refactorNotifyButton()));
    }));
  }

  ///////////////////
  //    CLICKS     //
  ///////////////////

  @OnClick(R.id.btnLeave) void onClickLeave() {
    onLeave.onNext(null);
  }

  @OnClick(R.id.btnScreenshot) void onClickScreenshot() {
    screenshotCount++;
    viewControlsLive.prepareForScreenshot();
    onScreenshot.onNext(null);
  }

  @OnClick(R.id.viewRoom) void onClickRoom() {
    if (stateContainer == LiveContainer.EVENT_OPENED) onShouldCloseInvites.onNext(null);
    if (hiddenControls) {
      onHiddenControls.onNext(false);
    }
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
              totalSizeLive += nbLiveInRoom() + 1;
              averageCountLive = (double) totalSizeLive / (intervalCount + 1);
              averageCountLive = DoubleUtils.round(averageCountLive, 2);

              tagMap.put(TagManagerUtils.AVERAGE_MEMBERS_COUNT, averageCountLive);
            }));
      }
    }));

    tempSubscriptions.add(room.onJoined().subscribe(onJoined));

    tempSubscriptions.add(room.onShouldLeaveRoom().subscribe(onLeave));

    tempSubscriptions.add(room.onRoomFull().subscribe(onRoomFull));

    tempSubscriptions.add(
        room.onRemotePeerAdded().observeOn(AndroidSchedulers.mainThread()).subscribe(remotePeer -> {
          if (isFirstToJoin) {
            isFirstToJoin = !isFirstToJoin;
            viewStatusName.refactorTitle();
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
          btnScreenshot.setVisibility(VISIBLE);
          AnimationUtils.fadeIn(btnScreenshot, 300);
          onNotificationRemoteWaiting.onNext(getDisplayNameFromSession(remotePeer.getSession()));

          room.sendToPeer(remotePeer, getInvitedPayload(), true);

          refactorShareOverlay();
          refactorNotifyButton();

          tempSubscriptions.add(remotePeer.getPeerView()
              .onNotificatinRemoteJoined()
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
        .subscribe(remotePeer -> Timber.d(
            "Remote peer updated with id : " + remotePeer.getSession().getPeerId())));

    tempSubscriptions.add(room.onInvitedTribeGuestList()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(tribeGuests -> {
          if (tribeGuests != null && tribeGuests.size() > 0) {
            for (TribeGuest trg : tribeGuests) {
              if (!liveInviteMap.getMap().containsKey(trg.getId()) && !liveRowViewMap.getMap()
                  .containsKey(trg.getId())) {
                addTribeGuest(trg);
                onNotificationRemotePeerInvited.onNext(trg.getDisplayName());
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

    Timber.d("Initiating Room");
    room.connect(options);
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

      if (avatarView != null) {
        float scaling = (2.0f - alpha);
        avatarView.setScaleX(scaling);
        avatarView.setScaleY(scaling);
      }

      btnScreenshot.setScaleX(alpha);
      btnScreenshot.setScaleY(alpha);
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

      liveInviteMap.put(latestView.getGuest().getId(), latestView);
      room.sendToPeers(getInvitedPayload(), true);
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

  public void start(Live live) {
    this.live = live;

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
      if (live.getId().equals(Live.NEW_CALL)) viewLocalLive.showShareOverlay();
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
    if (!liveInviteMap.getMap().containsKey(trg.getId()) && !liveRowViewMap.getMap()
        .containsKey(trg.getId())) {
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

  public void screenshotDone() {
    viewControlsLive.screenshotDone();
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

  public void setOpenInviteValue(float valueOpenInvite) {
    viewRoom.setOpenInviteValue(valueOpenInvite);
  }

  public boolean shouldLeave() {
    return liveRowViewMap.size() == 0 && liveInviteMap.size() == 0 && live != null;
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
      viewLocalLive.showShareOverlay();
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
      viewStatusName.setStatus(LiveStatusNameView.DONE);
    }

    if (liveInviteMap.getMap()
        .containsKey(
            remotePeer.getSession().getUserId())) { // If the user was invited before joining
      if (nbLiveInRoom() == 0) { // First user joining in a group call
        if (live.isGroup()) {
          String groupId = getGroupWaiting();
          if (!StringUtils.isEmpty(groupId)) {
            liveRowView = liveRowViewMap.remove(groupId);
            animateGroupAvatar(liveRowView);
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
          animateGroupAvatar(liveRowView);
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
  }

  private void removeFromPeers(String id) {
    if (liveRowViewMap.getMap().containsKey(id)) {
      LiveRowView liveRowView = liveRowViewMap.remove(id);
      liveRowView.dispose();
      viewRoom.removeView(liveRowView);
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

    for (String liveRowViewId : liveRowViewMap.getMap().keySet()) {
      LiveRowView liveRowView = liveRowViewMap.getMap().get(liveRowViewId);
      if (!liveRowView.isWaiting()) {
        TribeGuest guest = liveRowView.getGuest();
        if (guest != null && !guest.isExternal()) {
          usersInLive.add(guest);
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

    return new RoomMember(usersInLive, anonymousGuestInLive);
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

  private void animateGroupAvatar(LiveRowView liveRowView) {
    btnScreenshot.setImageResource(R.drawable.picto_screenshot_without_center);

    AvatarView fromAvatarView = liveRowView.avatar();
    avatarView = new AvatarView(getContext());
    avatarView.setType(AvatarView.REGULAR);
    avatarView.setHasHole(false);
    avatarView.setHasInd(false);
    avatarView.setHasShadow(true);

    if (fromAvatarView.getMembersPic() != null && fromAvatarView.getMembersPic().size() > 0) {
      avatarView.loadGroupAvatar(fromAvatarView.getUrl(), null, fromAvatarView.getGroupId(),
          fromAvatarView.getMembersPic());
    } else {
      avatarView.load(fromAvatarView.getUrl());
    }

    int[] location = new int[2];
    fromAvatarView.getLocationOnScreen(location);

    FrameLayout.LayoutParams params =
        new FrameLayout.LayoutParams(fromAvatarView.getWidth(), fromAvatarView.getHeight());
    params.leftMargin = location[0];
    params.topMargin = location[1] - statusBarHeight;
    avatarView.setLayoutParams(params);
    addView(avatarView);

    tempSubscriptions.add(Observable.timer(50, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          Animator animatorSize = AnimationUtils.getSizeAnimator(avatarView,
              (int) (sizeAnimAvatarMax * AVATAR_SCALING));
          animatorSize.setDuration(DURATION);
          animatorSize.setInterpolator(new DecelerateInterpolator());
          animatorSize.start();

          Animator animatorTopMargin = AnimationUtils.getTopMarginAnimator(avatarView, margin);
          animatorTopMargin.setDuration(DURATION);
          animatorTopMargin.setInterpolator(new OvershootInterpolator(OVERSHOOT));
          animatorTopMargin.start();
          Animator animatorLeftMargin = AnimationUtils.getLeftMarginAnimator(avatarView, margin);
          animatorLeftMargin.setDuration(DURATION);
          animatorLeftMargin.setInterpolator(new OvershootInterpolator(OVERSHOOT));
          animatorLeftMargin.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationCancel(Animator animation) {
              animatorLeftMargin.removeAllListeners();
            }

            @Override public void onAnimationEnd(Animator animation) {
              if (viewBuzz == null) return;

              viewBuzz.setVisibility(View.VISIBLE);
              viewBuzz.getViewTreeObserver()
                  .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override public void onGlobalLayout() {
                      viewBuzz.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                      MarginLayoutParams viewBuzzParams =
                          (MarginLayoutParams) viewBuzz.getLayoutParams();
                      viewBuzzParams.leftMargin =
                          margin + (avatarView.getWidth() >> 1) - (viewBuzz.getWidth() >> 1);
                      viewBuzzParams.topMargin =
                          margin + (avatarView.getHeight() >> 1) - (viewBuzz.getHeight() >> 1);
                    }
                  });
            }
          });

          animatorLeftMargin.start();
        }));
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

  public Observable<TribeJoinRoom> onJoined() {
    return onJoined;
  }

  public Observable<Void> onNotify() {
    return onNotify;
  }

  public Observable<Void> onLeave() {
    return onLeave;
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

  public Observable<Map<String, LiveRowView>> onInvitesChanged() {
    return liveInviteMap.getMapObservable();
  }

  public Observable<Map<String, LiveRowView>> onLiveChanged() {
    return liveRowViewMap.getMapObservable();
  }

  public Observable<Void> onShare() {
    return onShare;
  }

  public Observable<Void> onRoomFull() {
    return onRoomFull;
  }
}

