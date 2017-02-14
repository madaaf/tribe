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
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.RoomConfiguration;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManager;
import com.tribe.app.presentation.utils.analytics.TagManagerConstants;
import com.tribe.app.presentation.view.component.TileView;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.app.presentation.view.utils.StateManager;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.tribelivesdk.TribeLiveSDK;
import com.tribe.tribelivesdk.back.TribeLiveOptions;
import com.tribe.tribelivesdk.core.Room;
import com.tribe.tribelivesdk.model.RemotePeer;
import com.tribe.tribelivesdk.model.TribeGuest;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by tiago on 01/18/2017.
 */
public class LiveView extends FrameLayout {

  private static final int DURATION = 300;
  private static final int LIVE_MAX = 8;

  @Inject SoundManager soundManager;

  @Inject User user;

  @Inject ScreenUtils screenUtils;

  @Inject AccessToken accessToken;

  @Inject TribeLiveSDK tribeLiveSDK;

  @Inject TagManager tagManager;

  @Inject StateManager stateManager;

  @BindView(R.id.viewLocalLive) LiveLocalView viewLocalLive;

  @BindView(R.id.viewRoom) LiveRoomView viewRoom;

  @BindView(R.id.btnInviteLive) View btnInviteLive;

  @BindView(R.id.btnLeave) View btnLeave;

  @BindView(R.id.btnNotify) View btnNotify;

  @BindView(R.id.txtName) TextViewFont txtName;

  // VARIABLES
  private Recipient recipient;
  private int color;
  private Room room;
  private LiveRowView latestView;
  private Map<String, LiveRowView> liveRowViewMap;
  private Map<String, LiveRowView> liveInviteMap;
  private boolean hiddenControls = false;
  private @LiveContainer.Event int stateContainer = LiveContainer.EVENT_CLOSED;

  // RESOURCES
  private int timeJoinRoom;

  // OBSERVABLES
  private Unbinder unbinder;
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Void> onOpenInvite = PublishSubject.create();
  private PublishSubject<Void> onShouldJoinRoom = PublishSubject.create();
  private PublishSubject<Void> onNotify = PublishSubject.create();
  private PublishSubject<Void> onLeave = PublishSubject.create();
  private PublishSubject<Boolean> onHiddenControls = PublishSubject.create();
  private PublishSubject<Void> onShouldCloseInvites = PublishSubject.create();

  public LiveView(Context context) {
    super(context);
    init(context, null);
  }

  public LiveView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  public void onDestroy() {
    if (room != null) {
      viewRoom.removeAllViews();
      room.leaveRoom();
      room = null;
    }

    if (subscriptions != null && subscriptions.hasSubscriptions()) {
      subscriptions.unsubscribe();
    }

    unbinder.unbind();
  }

  @Override protected void onFinishInflate() {
    LayoutInflater.from(getContext()).inflate(R.layout.view_live, this);
    unbinder = ButterKnife.bind(this);
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);

    initResources();
    initUI();
    initSubscriptions();

    super.onFinishInflate();
  }

  //////////////////////
  //      INIT        //
  //////////////////////

  private void init(Context context, AttributeSet attrs) {
    liveRowViewMap = new HashMap<>();
    liveInviteMap = new HashMap<>();
  }

  private void initUI() {
    btnNotify.setEnabled(false);

    setBackgroundColor(Color.BLACK);

    room = tribeLiveSDK.newRoom();
    room.initLocalStream(viewLocalLive.getLocalPeerView());
  }

  private void initResources() {
    timeJoinRoom = getResources().getInteger(R.integer.time_join_room);
  }

  private void initSubscriptions() {
    subscriptions.add(onHiddenControls().doOnNext(aBoolean -> {
      hiddenControls = aBoolean;
      viewLocalLive.hideControls(!hiddenControls);
    }).subscribe());

    subscriptions.add(viewLocalLive.onClick().doOnNext(aVoid -> {
      if (stateContainer == LiveContainer.EVENT_OPENED) {
        onShouldCloseInvites.onNext(null);
      }
    }).filter(aVoid -> stateContainer == LiveContainer.EVENT_CLOSED).subscribe(aVoid -> {
      if (hiddenControls) {
        displayControls(1);
        onHiddenControls.onNext(false);
      } else {
        displayControls(0);
        onHiddenControls.onNext(true);
      }
    }));
  }

  private void displayPopupTutorial() {
    if (stateManager.shouldDisplay(StateManager.DRAGGING_GUEST)) {
      DialogFactory.dialog(getContext(), getContext().getString(R.string.tips_draggingguest_title),
          getContext().getString(R.string.tips_draggingguest_message),
          getContext().getString(R.string.tips_draggingguest_action1), null).subscribe(a -> {
      });
      stateManager.addTutorialKey(StateManager.DRAGGING_GUEST);
    }
  }

  ///////////////////
  //    CLICKS     //
  ///////////////////

  @OnClick(R.id.btnInviteLive) void openInvite() {
    if (!hiddenControls) onOpenInvite.onNext(null);
  }

  @OnClick(R.id.btnNotify) void onClickNotify() {
    if (!hiddenControls) {
      btnNotify.setEnabled(false);

      for (LiveRowView liveRowView : liveInviteMap.values()) {
        liveRowView.buzz();
      }

      for (LiveRowView liveRowView : liveRowViewMap.values()) {
        if (liveRowView.isWaiting()) liveRowView.buzz();
      }

      btnNotify.animate()
          .scaleX(1.25f)
          .scaleY(1.25f)
          .translationY(-screenUtils.dpToPx(10))
          .rotation(10)
          .setDuration(DURATION)
          .setInterpolator(new DecelerateInterpolator())
          .setListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
              ObjectAnimator animatorRotation = ObjectAnimator.ofFloat(btnNotify, ROTATION, 7, -7);
              animatorRotation.setDuration(100);
              animatorRotation.setRepeatCount(3);
              animatorRotation.setRepeatMode(ValueAnimator.REVERSE);
              animatorRotation.addListener(new AnimatorListenerAdapter() {
                @Override public void onAnimationEnd(Animator animation) {
                  btnNotify.animate()
                      .scaleX(1)
                      .scaleY(1)
                      .rotation(0)
                      .translationY(0)
                      .setDuration(DURATION)
                      .setInterpolator(new DecelerateInterpolator())
                      .setListener(new AnimatorListenerAdapter() {
                        @Override public void onAnimationEnd(Animator animation) {
                          btnNotify.setEnabled(true);
                          btnNotify.animate().setListener(null);
                        }
                      });
                }
              });
              animatorRotation.start();
            }
          })
          .start();

      onNotify.onNext(null);
    }
  }

  @OnClick(R.id.btnLeave) void onClickLeave() {
    if (!hiddenControls) onLeave.onNext(null);
  }

  @OnClick(R.id.viewRoom) void onClickRoom() {
    if (stateContainer == LiveContainer.EVENT_OPENED) onShouldCloseInvites.onNext(null);
    if (hiddenControls) {
      displayControls(1);
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
        .build();

    subscriptions.add(room.onRoomStateChanged().subscribe(state -> {
      Timber.d("Room state change : " + state);
    }));

    subscriptions.add(
        room.onRemotePeerAdded().observeOn(AndroidSchedulers.mainThread()).subscribe(remotePeer -> {
          soundManager.playSound(SoundManager.JOIN_CALL, SoundManager.SOUND_MAX);

          Timber.d("Remote peer added with id : "
              + remotePeer.getSession().getPeerId()
              + " & view : "
              + remotePeer.getPeerView());
          addView(remotePeer);
          refactorNotifyButton();
        }));

    subscriptions.add(room.onRemotePeerRemoved()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(remotePeer -> {
          soundManager.playSound(SoundManager.QUIT_CALL, SoundManager.SOUND_MAX);

          Timber.d("Remote peer removed with id : " + remotePeer.getSession().getPeerId());
          removeFromPeers(remotePeer.getSession().getUserId());

          if (liveRowViewMap.size() == 0 && liveInviteMap.size() == 0 && recipient != null) {
            onLeave.onNext(null);
          }

          refactorNotifyButton();
        }));

    subscriptions.add(room.onRemotePeerUpdated()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(remotePeer -> {
          Timber.d("Remote peer updated with id : " + remotePeer.getSession().getPeerId());
        }));

    subscriptions.add(room.onInvitedTribeGuestList()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(tribeGuests -> {
          if (tribeGuests != null && tribeGuests.size() > 0) {
            for (TribeGuest trg : tribeGuests) {
              if (!liveInviteMap.containsKey(trg.getId()) && !liveRowViewMap.containsKey(
                  trg.getId())) {
                LiveRowView liveRowView = new LiveRowView(getContext());
                liveRowView.getViewTreeObserver()
                    .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                      @Override public void onGlobalLayout() {
                        liveRowView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        liveRowView.showGuest(false);
                        liveRowView.startPulse();
                      }
                    });
                addView(liveRowView, trg, PaletteGrid.getRandomColorExcluding(Color.BLACK));
                liveInviteMap.put(trg.getId(), liveRowView);
              }
            }
          }
        }));

    subscriptions.add(room.onRemovedTribeGuestList()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(tribeGuests -> {
          if (tribeGuests != null && tribeGuests.size() > 0) {
            for (TribeGuest trg : tribeGuests) {
              if (liveInviteMap.containsKey(trg.getId())) {
                removeFromInvites(trg.getId());
              }
            }
          }
        }));

    Timber.d("Initiating Room");
    room.connect(options);
  }

  public void initInviteOpenSubscription(Observable<Integer> obs) {
    subscriptions.add(obs.subscribe(event -> {
      stateContainer = event;
      viewRoom.setType(
          event == LiveContainer.EVENT_OPENED ? LiveRoomView.LINEAR : LiveRoomView.GRID);
    }));
  }

  public void initOnStartDragSubscription(Observable<TileView> obs) {
    subscriptions.add(obs.subscribe(tileView -> {
      latestView = new LiveRowView(getContext());
      TribeGuest guest = new TribeGuest(tileView.getRecipient().getSubId(),
          tileView.getRecipient().getDisplayName(), tileView.getRecipient().getProfilePicture(),
          false);
      addView(latestView, guest, tileView.getBackgroundColor());
    }));
  }

  public void initOnEndDragSubscription(Observable<Void> obs) {
    subscriptions.add(obs.subscribe(aVoid -> {
      latestView.dispose();
      viewRoom.removeView(latestView);
    }));
  }

  public void initOnAlphaSubscription(Observable<Float> obs) {
    subscriptions.add(obs.subscribe(alpha -> {
      btnNotify.setAlpha(alpha);
      btnInviteLive.setAlpha(alpha);
      btnLeave.setAlpha(alpha);
    }));
  }

  public void initDropEnabledSubscription(Observable<Boolean> obs) {
    subscriptions.add(obs.subscribe(enabled -> {
      // TODO DO SOMETHING WITH THIS ?
    }));
  }

  public void initDropSubscription(Observable<TileView> obs) {
    subscriptions.add(obs.subscribe(tileView -> {
      tileView.onDrop(latestView);

      liveInviteMap.put(latestView.getGuest().getId(), latestView);
      room.sendToPeers(getInvitedPayload());

      subscriptions.add(tileView.onEndDrop().subscribe(aVoid -> {
        tagManager.trackEvent(TagManagerConstants.KPI_Calls_DragAndDrop);
        displayPopupTutorial();
        latestView.showGuest(false);
        latestView.startPulse();
      }));

      subscriptions.add(latestView.onShouldRemoveGuest()
          .doOnNext(tribeGuest -> removeFromInvites(latestView.getGuest().getId()))
          .subscribe(tribeGuest -> {
            room.sendToPeers(getRemovedPayload(latestView.getGuest()));
          }));
    }));
  }

  public void setRecipient(Recipient recipient, int color) {
    this.recipient = recipient;
    this.color = color;

    if (recipient instanceof Membership) {
      txtName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.picto_group_small, 0, 0, 0);
    } else {
      txtName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
    }

    txtName.setText(recipient.getDisplayName());

    TribeGuest guest = new TribeGuest(recipient.getSubId(), recipient.getDisplayName(),
        recipient.getProfilePicture(), recipient instanceof Membership);
    LiveRowView liveRowView = new LiveRowView(getContext());
    liveRowViewMap.put(guest.getId(), liveRowView);
    addView(liveRowView, guest, color);
    liveRowView.showGuest(true);

    subscriptions.add(liveRowView.onShouldJoinRoom()
        .distinct()
        .doOnNext(aVoid -> btnNotify.setEnabled(true))
        .subscribe(onShouldJoinRoom));
  }

  public Room getRoom() {
    return room;
  }

  ////////////////
  //  PRIVATE   //
  ////////////////

  private void refactorNotifyButton() {
    boolean enable = shouldEnableBuzz();

    if (enable != btnNotify.isEnabled()) {
      btnNotify.setEnabled(shouldEnableBuzz());
      btnNotify.animate()
          .alpha(enable ? 1 : 0.2f)
          .setDuration(DURATION)
          .setInterpolator(new DecelerateInterpolator())
          .start();
    }
  }

  private void scale(View v, int scale) {
    v.animate()
        .scaleX(scale)
        .scaleY(scale)
        .setDuration(DURATION)
        .setListener(new AnimatorListenerAdapter() {
          @Override public void onAnimationStart(Animator animation) {
            v.setVisibility(scale == 1 ? View.VISIBLE : View.GONE);
          }

          @Override public void onAnimationEnd(Animator animation) {
            v.setVisibility(scale == 0 ? View.GONE : View.VISIBLE);
            v.animate().setListener(null).start();
          }
        })
        .start();
  }

  private void displayControls(int scale) {
    scale(btnLeave, scale);
    scale(btnNotify, scale);
    scale(btnInviteLive, scale);
  }

  private void addView(LiveRowView liveRowView, TribeGuest guest, int color) {
    liveRowView.setColor(color);
    liveRowView.setGuest(guest);
    liveRowView.setRoomType(viewRoom.getType());
    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT);
    viewRoom.addView(liveRowView, params);
  }

  private void addView(RemotePeer remotePeer) {
    LiveRowView liveRowView;

    if (liveInviteMap.containsKey(
        remotePeer.getSession().getUserId())) { // If the user was invited before joining
      liveRowView = liveInviteMap.get(remotePeer.getSession().getUserId());
      liveRowView.setPeerView(remotePeer.getPeerView());
      liveInviteMap.remove(remotePeer.getSession().getUserId());
      liveRowViewMap.put(remotePeer.getSession().getUserId(), liveRowView);
    } else if (liveRowViewMap.containsKey(remotePeer.getSession()
        .getUserId())) { // If the user was already live, usually the case on 1-1 calls
      liveRowView = liveRowViewMap.get(remotePeer.getSession().getUserId());
      liveRowView.setPeerView(remotePeer.getPeerView());
    } else {
      liveRowView = new LiveRowView(getContext());
      liveRowView.setRoomType(viewRoom.getType());
      liveRowView.setPeerView(remotePeer.getPeerView());
      ViewGroup.LayoutParams params =
          new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT);

      if (nbLiveInRoom() == 0) { // First user joining in a group call
        String groupId = getGroupWaiting();
        if (!StringUtils.isEmpty(getGroupWaiting())) {
          removeFromPeers(groupId);
        }
      }

      TribeGuest guest = guestFromRemotePeer(remotePeer);
      if (guest != null) {
        liveRowView.setGuest(guest);
        liveRowView.showGuest(false);
      }

      viewRoom.addView(liveRowView, params);
      liveRowViewMap.put(remotePeer.getSession().getUserId(), liveRowView);
    }
  }

  private void removeFromPeers(String id) {
    if (liveRowViewMap.containsKey(id)) {
      LiveRowView liveRowView = liveRowViewMap.remove(id);
      liveRowView.dispose();
      viewRoom.removeView(liveRowView);
    }
  }

  private void removeFromInvites(String id) {
    if (liveInviteMap.containsKey(id)) {
      LiveRowView liveRowView = liveInviteMap.remove(id);
      liveRowView.dispose();
      viewRoom.removeView(liveRowView);
    }
  }

  private TribeGuest guestFromRemotePeer(RemotePeer remotePeer) {
    for (Friendship friendship : user.getFriendships()) {
      if (remotePeer.getSession().getUserId().equals(friendship.getSubId())) {
        return new TribeGuest(friendship.getSubId(), friendship.getDisplayName(),
            friendship.getProfilePicture(), false);
      }
    }

    return null;
  }

  private JSONObject getInvitedPayload() {
    JSONObject jsonObject = new JSONObject();
    JSONArray array = new JSONArray();
    for (LiveRowView liveRowView : liveInviteMap.values()) {
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

    JSONObject removedGuest = new JSONObject();
    jsonPut(removedGuest, TribeGuest.ID, guest.getId());
    jsonPut(removedGuest, TribeGuest.DISPLAY_NAME, guest.getDisplayName());
    jsonPut(removedGuest, TribeGuest.PICTURE, guest.getPicture());
    array.put(removedGuest);

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
    int nbLiveInRoom = nbLiveInRoom();

    if (recipient == null) return false;
    if (nbLiveInRoom == LIVE_MAX) return false;

    if (recipient instanceof Membership) {
      Membership membership = (Membership) recipient;
      if (membership.getGroup().getMembers().size() == nbLiveInRoom()) result = false;
    } else if (!isTherePeopleWaiting()) {
      result = false;
    }

    return result;
  }

  private int nbLiveInRoom() {
    int count = 0;

    for (LiveRowView liveRowView : liveRowViewMap.values()) {
      if (!liveRowView.isWaiting()) count++;
    }

    return count;
  }

  private boolean isTherePeopleWaiting() {
    boolean waiting = false;

    for (LiveRowView liveRowView : liveRowViewMap.values()) {
      if (liveRowView.isWaiting()) waiting = true;
    }

    if (!waiting) waiting = liveInviteMap.size() > 0;

    return waiting;
  }

  private String getGroupWaiting() {
    String id = "";

    for (LiveRowView liveRowView : liveRowViewMap.values()) {
      if (liveRowView.isWaiting() && liveRowView.isGroup()) id = liveRowView.getGuest().getId();
    }

    return id;
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
}

