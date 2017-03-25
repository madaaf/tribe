package com.tribe.app.presentation.view.component.live;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Live;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.RoomConfiguration;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManager;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.view.component.TileView;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.DoubleUtils;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.app.presentation.view.utils.StateManager;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import com.tribe.tribelivesdk.TribeLiveSDK;
import com.tribe.tribelivesdk.back.TribeLiveOptions;
import com.tribe.tribelivesdk.core.Room;
import com.tribe.tribelivesdk.model.RemotePeer;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.util.ObservableRxHashMap;
import java.util.HashMap;
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

  private static final int MAX_DURATION_JOIN_LIVE = 60;
  private static final int DURATION_FAST_FURIOUS = 60;
  private static final float OVERSHOOT = 1.2f;

  private static boolean joinLive = false;

  @Inject SoundManager soundManager;

  @Inject User user;

  @Inject ScreenUtils screenUtils;

  @Inject AccessToken accessToken;

  @Inject TribeLiveSDK tribeLiveSDK;

  @Inject TagManager tagManager;

  @Inject StateManager stateManager;

  @BindView(R.id.viewLocalLive) LiveLocalView viewLocalLive;

  @BindView(R.id.viewRoom) LiveRoomView viewRoom;

  @BindView(R.id.viewControlsLive) LiveControlsView viewControlsLive;

  @BindView(R.id.viewStatusName) LiveStatusNameView viewStatusName;

  @BindView(R.id.viewBuzz) BuzzView viewBuzz;

  @BindView(R.id.btnLeave) View btnLeave;

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
  private int wizzCount = 0, invitedCount = 0, totalSizeLive = 0;
  private double averageCountLive = 0.0D;
  private boolean hasJoined = false;
  private long timeStart = 0L, timeEnd = 0L;
  private boolean isParamExpended = false, isMicroActivated = true, isCameraActivated = true;

  // RESOURCES
  private int timeJoinRoom, statusBarHeight, margin;

  // OBSERVABLES
  private Unbinder unbinder;
  private CompositeSubscription persistentSubscriptions = new CompositeSubscription();
  private CompositeSubscription tempSubscriptions = new CompositeSubscription();
  private PublishSubject<Void> onOpenInvite = PublishSubject.create();
  private PublishSubject<Void> onShouldJoinRoom = PublishSubject.create();
  private PublishSubject<Void> onNotify = PublishSubject.create();
  private PublishSubject<Void> onLeave = PublishSubject.create();
  private PublishSubject<Boolean> onHiddenControls = PublishSubject.create();
  private PublishSubject<Void> onShouldCloseInvites = PublishSubject.create();

  private PublishSubject<String> onNotificationRemotePeerInvited = PublishSubject.create();
  private PublishSubject<String> onNotificationRemotePeerRemoved = PublishSubject.create();
  private PublishSubject<String> onNotificationRemoteWaiting = PublishSubject.create();
  private PublishSubject<String> onNotificationRemotePeerBuzzed = PublishSubject.create();
  private PublishSubject<String> onNotificationRemoteJoined = PublishSubject.create();

  public LiveView(Context context) {
    super(context);
    init(context, null);
  }

  public LiveView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public void jump() {
    room.jump();
  }

  public void onDestroy(boolean isJump) {
    String state = TagManagerUtils.CANCELLED;

    double duration = 0.0D;

    if (timeStart > 0) {
      timeEnd = System.currentTimeMillis();
      long delta = timeEnd - timeStart;
      duration = (double) delta / 60000.0;
      duration = DoubleUtils.round(duration, 2);
    }

    if (hasJoined && averageCountLive > 1) {
      state = TagManagerUtils.ENDED;
      tagManager.increment(TagManagerUtils.USER_CALLS_COUNT);
      tagManager.increment(TagManagerUtils.USER_CALLS_MINUTES, duration);
    } else if (hasJoined && averageCountLive <= 1) {
      state = TagManagerUtils.MISSED;
      tagManager.increment(TagManagerUtils.USER_CALLS_MISSED_COUNT);
    }

    tagMap.put(TagManagerUtils.EVENT, TagManagerUtils.Calls);
    tagMap.put(TagManagerUtils.DURATION, duration);
    tagMap.put(TagManagerUtils.STATE, state);
    tagMap.put(TagManagerUtils.MEMBERS_INVITED, invitedCount);
    tagMap.put(TagManagerUtils.WIZZ_COUNT, wizzCount);
    tagMap.put(TagManagerUtils.TYPE,
        live.isGroup() ? TagManagerUtils.GROUP : TagManagerUtils.DIRECT);
    TagManagerUtils.manageTags(tagManager, tagMap);

    for (LiveRowView liveRowView : liveRowViewMap.getMap().values()) {
      liveRowView.dispose();
      viewRoom.removeView(liveRowView);
    }

    for (LiveRowView liveRowView : liveInviteMap.getMap().values()) {
      liveRowView.dispose();
      viewRoom.removeView(liveRowView);
    }

    if (room != null && !isJump) {
      room.leaveRoom();
    }

    if (animatorBuzzAvatar != null) {
      animatorBuzzAvatar.cancel();
    }

    viewStatusName.dispose();
    viewControlsLive.dispose();

    if (!isJump) {
      persistentSubscriptions.clear();
      tempSubscriptions.clear();
      viewLocalLive.dispose();
      unbinder.unbind();
    } else {
      tempSubscriptions.clear();
    }
  }

  @Override protected void onFinishInflate() {
    LayoutInflater.from(getContext()).inflate(R.layout.view_live, this);
    unbinder = ButterKnife.bind(this);
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);

    initResources();
    initUI();
    initSubscriptions();

    tempSubscriptions.add(Observable.timer(MAX_DURATION_JOIN_LIVE, TimeUnit.SECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> displayJoinLivePopupTutorial()));

    super.onFinishInflate();
  }

  //////////////////////
  //      INIT        //
  //////////////////////

  private void init(Context context, AttributeSet attrs) {
    liveRowViewMap = new ObservableRxHashMap<>();
    liveInviteMap = new ObservableRxHashMap<>();
    tagMap = new HashMap<>();
  }

  private void initUI() {
    setBackgroundColor(Color.BLACK);

    room = tribeLiveSDK.newRoom();
    room.initLocalStream(viewLocalLive.getLocalPeerView());
  }

  private void initResources() {
    timeJoinRoom = getResources().getInteger(R.integer.time_join_room);
    margin = getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small);

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
      onHiddenControls.onNext(isParamExpended);
    }));

    persistentSubscriptions.add(viewControlsLive.onOpenInvite().subscribe(aVoid -> {
      displayDragingGuestPopupTutorial();
      if (!hiddenControls) onOpenInvite.onNext(null);
    }));

    persistentSubscriptions.add(viewControlsLive.onClickCameraOrientation().subscribe(aVoid -> {
      viewLocalLive.switchCamera();
    }));

    persistentSubscriptions.add(viewControlsLive.onClickMicro().subscribe(aBoolean -> {
      isMicroActivated = aBoolean;
      viewControlsLive.setMicroEnabled(isMicroActivated);
      viewLocalLive.enableMicro(isMicroActivated, isCameraActivated);
    }));

    persistentSubscriptions.add(viewControlsLive.onClickParamExpand().subscribe(aVoid -> {
      onHiddenControls.onNext(isParamExpended);
    }));

    persistentSubscriptions.add(viewControlsLive.onClickCameraEnable().subscribe(aVoid -> {
      isCameraActivated = false;
      viewLocalLive.enableMicro(isMicroActivated, isCameraActivated);
      viewLocalLive.disableCamera(true);
    }));

    persistentSubscriptions.add(viewControlsLive.onClickCameraDisable().subscribe(aVoid -> {
      isCameraActivated = true;
      viewLocalLive.enableMicro(isMicroActivated, isCameraActivated);
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
    TribeLiveOptions options = new TribeLiveOptions.TribeLiveOptionsBuilder(getContext()).wsUrl(
        roomConfiguration.getWebsocketUrl())
        .tokenId(accessToken.getAccessToken())
        .iceServers(roomConfiguration.getRtcPeerConfiguration().getIceServers())
        .roomId(roomConfiguration.getRoomId())
        .routingMode(roomConfiguration.getRoutingMode())
        .build();

    tempSubscriptions.add(room.onRoomStateChanged().subscribe(state -> {
      Timber.d("Room state change : " + state);
      if (state == Room.STATE_CONNECTED) {
        timeStart = System.currentTimeMillis();

        tempSubscriptions.add(Observable.interval(10, TimeUnit.SECONDS, Schedulers.computation())
            .subscribe(intervalCount -> {
              totalSizeLive += nbLiveInRoom() + 1;
              averageCountLive = (double) totalSizeLive / (intervalCount + 1);
              averageCountLive = DoubleUtils.round(averageCountLive, 2);

              tagMap.put(TagManagerUtils.AVERAGE_MEMBERS_COUNT, averageCountLive);
            }));
      }
    }));

    tempSubscriptions.add(room.onShouldLeaveRoom().subscribe(onLeave));

    tempSubscriptions.add(
        room.onRemotePeerAdded().observeOn(AndroidSchedulers.mainThread()).subscribe(remotePeer -> {
          soundManager.playSound(SoundManager.JOIN_CALL, SoundManager.SOUND_MAX);
          joinLive = true;
          displayJoinLivePopupTutorial();

          Timber.d("Remote peer added with id : "
              + remotePeer.getSession().getPeerId()
              + " & view : "
              + remotePeer.getPeerView());
          addView(remotePeer);
          onNotificationRemoteWaiting.onNext(
              getDisplayNameNotification(remotePeer.getSession().getUserId()));

          room.sendToPeer(remotePeer, getInvitedPayload(), true);
          refactorNotifyButton();

          tempSubscriptions.add(remotePeer.getPeerView()
              .onNotificatinRemoteJoined()
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(aVoid -> {
                onNotificationRemoteJoined.onNext(
                    getDisplayNameNotification(remotePeer.getSession().getUserId()));
              }));
        }));

    tempSubscriptions.add(room.onRemotePeerRemoved()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(remotePeer -> {
          soundManager.playSound(SoundManager.QUIT_CALL, SoundManager.SOUND_MAX);

          Timber.d("Remote peer removed with id : " + remotePeer.getSession().getPeerId());
          removeFromPeers(remotePeer.getSession().getUserId());

          if (liveRowViewMap.size() == 0 && liveInviteMap.size() == 0 && live != null) {
            onLeave.onNext(null);
          }

          refactorNotifyButton();

          onNotificationRemotePeerRemoved.onNext(
              getDisplayNameNotification(remotePeer.getSession().getUserId()));
        }));

    tempSubscriptions.add(room.onRemotePeerUpdated()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(remotePeer -> {
          Timber.d("Remote peer updated with id : " + remotePeer.getSession().getPeerId());
        }));

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
          false, false, null);
      Timber.e("add onDropEnabled initOnStartDragSubscription ");
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
      btnLeave.setAlpha(alpha);
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
        invitedCount++;
        displayDroppingGuestPopupTutorial();
        latestView.showGuest(false);
        latestView.startPulse();
      }));

      subscribeOnRemovingGuestFromLive(latestView);
    }));
  }

  private void displayDroppingGuestPopupTutorial() {
    if (stateManager.shouldDisplay(StateManager.DROPPING_GUEST)) {
      tempSubscriptions.add(DialogFactory.dialog(getContext(),
          EmojiParser.demojizedText(getContext().getString(R.string.tips_droppingguest_title)),
          getContext().getString(R.string.tips_droppingguest_message),
          getContext().getString(R.string.tips_droppingguest_action1), null).subscribe(a -> {
      }));
      stateManager.addTutorialKey(StateManager.DROPPING_GUEST);
    }
  }

  public void start(Live live) {
    this.live = live;

    viewStatusName.setLive(live);

    TribeGuest guest =
        new TribeGuest(live.getSubId(), live.getDisplayName(), live.getPicture(), live.isGroup(),
            live.isInvite(), live.getMembersPics());

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
      liveRowView.startPulse();
      onJoining();
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
                live.isGroup(), live.isInvite(), live.getMembersPics()));
      }
    }
  }

  public Room getRoom() {
    return room;
  }

  public void displayWaitLivePopupTutorial() {
    if (!joinLive) {
      if (stateManager.shouldDisplay(StateManager.WAINTING_FRIENDS_LIVE)) {
        tempSubscriptions.add(DialogFactory.dialog(getContext(),
            getContext().getString(R.string.tips_waiting5sec_title),
            EmojiParser.demojizedText(getContext().getString(R.string.tips_waiting5sec_message)),
            getContext().getString(R.string.tips_waiting5sec_action1), null).subscribe(a -> {
        }));
        stateManager.addTutorialKey(StateManager.WAINTING_FRIENDS_LIVE);
      }
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
    }).subscribe());
  }

  public void setCameraEnabled(boolean enable) {
    if (enable) {
      viewLocalLive.enableCamera(false);
    } else {
      viewLocalLive.disableCamera(false);
    }
  }

  public int nbInRoom() {
    int count = 0;

    count += liveRowViewMap.getMap().size();
    count += liveInviteMap.getMap().size();

    return count + 1;
  }

  ////////////////
  //  PRIVATE   //
  ////////////////

  private void displayJoinLivePopupTutorial() {
    if (stateManager.shouldDisplay(StateManager.JOIN_FRIEND_LIVE)) {
      tempSubscriptions.add(DialogFactory.dialog(getContext(),
          EmojiParser.demojizedText(getContext().getString(R.string.tips_waiting60sec_title)),
          EmojiParser.demojizedText(getContext().getString(R.string.tips_waiting60sec_message)),
          getContext().getString(R.
              string.tips_waiting60sec_action1),
          getContext().getString(R.string.tips_waiting60sec_action2)).filter(x -> x == true).
          subscribe(a -> {
            if (!hiddenControls) onOpenInvite.onNext(null);
          }));

      stateManager.addTutorialKey(StateManager.JOIN_FRIEND_LIVE);
    }
  }

  private void refactorNotifyButton() {
    boolean enable = shouldEnableBuzz();
    viewControlsLive.refactorNotifyButton(enable);
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
            animateGroupAvatar(liveRowView);
            liveRowView.setGuest(guest);
            liveRowView.setPeerView(remotePeer.getPeerView());
          }
        } else if (live.isInvite()) { // if it's an invite
          String inviteId = getInviteWaiting();
          if (!StringUtils.isEmpty(getInviteWaiting())) {
            liveRowView = liveRowViewMap.remove(inviteId);
            animateGroupAvatar(liveRowView);
            liveRowView.setGuest(guest);
            liveRowView.setPeerView(remotePeer.getPeerView());
          }
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
    for (Friendship friendship : user.getFriendships()) {
      if (remotePeer.getSession().getUserId().equals(friendship.getSubId())) {
        return new TribeGuest(friendship.getSubId(), friendship.getDisplayName(),
            friendship.getProfilePicture(), false, false, null);
      }
    }

    return null;
  }

  private JSONObject getInvitedPayload() {
    JSONObject jsonObject = new JSONObject();
    JSONArray array = new JSONArray();
    for (LiveRowView liveRowView : liveInviteMap.getMap().values()) {
      JSONObject invitedGuest = new JSONObject();
      jsonPut(invitedGuest, TribeGuest.ID, liveRowView.getGuest().getId());
      jsonPut(invitedGuest, TribeGuest.DISPLAY_NAME, liveRowView.getGuest().getDisplayName());
      jsonPut(invitedGuest, TribeGuest.PICTURE, liveRowView.getGuest().getPicture());
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
    AvatarView fromAvatarView = liveRowView.avatar();
    avatarView = new AvatarView(getContext());

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
              getResources().getDimensionPixelSize(R.dimen.avatar_size_smaller));
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

  private String getDisplayNameNotification(String id) {
    String tribeGuestName = "Anonymous";
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
    return tribeGuestName;
  }

  private void displayDragingGuestPopupTutorial() {
    if (stateManager.shouldDisplay(StateManager.DRAGGING_GUEST)) {
      tempSubscriptions.add(DialogFactory.dialog(getContext(),
          getContext().getString(R.string.tips_draggingguest_title),
          getContext().getString(R.string.tips_draggingguest_message),
          getContext().getString(R.string.tips_draggingguest_action1), null).subscribe(a -> {
      }));
      stateManager.addTutorialKey(StateManager.DRAGGING_GUEST);
    }
  }
  //////////////////////
  //   OBSERVABLES    //
  //////////////////////

  public Observable<Void> onOpenInvite() {
    return onOpenInvite;
  }

  public Observable<Void> onShouldJoinRoom() {
    return onShouldJoinRoom;
  }

  public Observable<Void> onNotify() {
    return onNotify;
  }

  public Observable<Void> onLeave() {
    return onLeave;
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
}

