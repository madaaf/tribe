package com.tribe.app.presentation.view.component.live;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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
import com.tribe.app.presentation.view.component.TileView;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.tribelivesdk.TribeLiveSDK;
import com.tribe.tribelivesdk.back.TribeLiveOptions;
import com.tribe.tribelivesdk.core.Room;
import com.tribe.tribelivesdk.model.RemotePeer;
import com.tribe.tribelivesdk.model.TribeGuest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by tiago on 01/18/2017.
 */
public class LiveView extends FrameLayout {

  private static final int DURATION = 300;

  @Inject SoundManager soundManager;

  @Inject User user;

  @Inject ScreenUtils screenUtils;

  @Inject AccessToken accessToken;

  @Inject TribeLiveSDK tribeLiveSDK;

  @BindView(R.id.viewLocalLive) LiveLocalView viewLocalLive;

  @BindView(R.id.viewRoom) LiveRoomView viewRoom;

  @BindView(R.id.btnInviteLive) View btnInviteLive;

  @BindView(R.id.btnLeave) View btnLeave;

  @BindView(R.id.btnNotify) View btnNotify;

  @BindView(R.id.txtName) TextViewFont txtName;

  // VARIABLES
  private Recipient recipient;
  private Room room;
  private LiveRowView latestView;
  private Map<String, LiveRowView> liveRowViewMap;
  private Map<String, LiveRowView> liveInviteMap;
  private boolean hiddenControls = false;

  // RESOURCES
  private int timeJoinRoom;

  // OBSERVABLES
  private Unbinder unbinder;
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private Subscription subscriptionJoinRoom;
  private PublishSubject<Void> onOpenInvite = PublishSubject.create();
  private PublishSubject<Boolean> onShouldJoinRoom = PublishSubject.create();
  private PublishSubject<Void> onNotify = PublishSubject.create();
  private PublishSubject<Void> onLeave = PublishSubject.create();
  private PublishSubject<Boolean> onHiddenControls = PublishSubject.create();

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

    Observable.timer(2000, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            aLong -> soundManager.playSound(SoundManager.WAITING_FRIEND, SoundManager.SOUND_MAX));
  }

  private void initUI() {
    setBackgroundColor(Color.BLACK);

    room = tribeLiveSDK.newRoom();
    room.initLocalStream(viewLocalLive.getLocalPeerView());
  }

  private void initResources() {
    timeJoinRoom = getResources().getInteger(R.integer.time_join_room);
  }

  private void initSubscriptions() {
    subscriptionJoinRoom = Observable.timer(timeJoinRoom, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          subscriptionJoinRoom.unsubscribe();
          onShouldJoinRoom.onNext(true);
        });

    subscriptions.add(onHiddenControls().doOnNext(aBoolean -> {
      hiddenControls = aBoolean;
      viewLocalLive.hideControls(!hiddenControls);
    }).subscribe());

    subscriptions.add(viewLocalLive.onClick().subscribe(aVoid -> {
      if (hiddenControls) {
        displayControls(1);
        onHiddenControls.onNext(false);
      } else {
        displayControls(0);
        onHiddenControls.onNext(true);
      }
    }));
  }

  ///////////////////
  //    CLICKS     //
  ///////////////////

  @OnClick(R.id.btnInviteLive) void openInvite() {
    if (!hiddenControls) onOpenInvite.onNext(null);
  }

  @OnClick(R.id.btnNotify) void onClickNotify() {
    if (!hiddenControls) onNotify.onNext(null);
  }

  @OnClick(R.id.btnLeave) void onClickLeave() {
    if (!hiddenControls) onLeave.onNext(null);
  }

  @OnClick(R.id.viewRoom) void onClickRoom() {
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
        }));

    subscriptions.add(room.onRemotePeerRemoved()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(remotePeer -> {
          soundManager.playSound(SoundManager.QUIT_CALL, SoundManager.SOUND_MAX);

          Timber.d("Remote peer removed with id : " + remotePeer.getSession().getPeerId());
          if (liveRowViewMap.containsKey(remotePeer.getSession().getUserId())) {
            LiveRowView liveRowView = liveRowViewMap.remove(remotePeer.getSession().getUserId());
            liveRowView.dispose();
            viewRoom.removeView(liveRowView);
            liveRowView = null;
          }
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
                        liveRowView.startPulse();
                      }
                    });
                addView(liveRowView, trg, PaletteGrid.getRandomColorExcluding(Color.BLACK));
              }
            }
          }
        }));

    Timber.d("Initiating Room");
    room.connect(options);
  }

  public void initInviteOpenSubscription(Observable<Integer> obs) {
    subscriptions.add(obs.subscribe(event -> {
      viewLocalLive.setEnabled(event == LiveContainer.EVENT_OPENED ? false : true);
      viewRoom.setType(
          event == LiveContainer.EVENT_OPENED ? LiveRoomView.LINEAR : LiveRoomView.GRID);
    }));
  }

  public void initOnStartDragSubscription(Observable<TileView> obs) {
    subscriptions.add(obs.subscribe(tileView -> {
      latestView = new LiveRowView(getContext());
      TribeGuest guest = new TribeGuest(tileView.getRecipient().getSubId(),
          tileView.getRecipient().getDisplayName(), tileView.getRecipient().getProfilePicture());
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

      subscriptions.add(tileView.onEndDrop().subscribe(aVoid -> latestView.startPulse()));
    }));
  }

  public void setRecipient(Recipient recipient, int color) {
    this.recipient = recipient;

    if (recipient instanceof Membership) {
      txtName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.picto_group_small, 0, 0, 0);
    } else {
      txtName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
    }

    txtName.setText(recipient.getDisplayName());

    if (recipient instanceof Friendship) {
      TribeGuest guest = new TribeGuest(recipient.getSubId(), recipient.getDisplayName(),
          recipient.getProfilePicture());
      LiveRowView liveRowView = new LiveRowView(getContext());
      liveRowViewMap.put(guest.getId(), liveRowView);
      addView(liveRowView, guest, color);
      liveRowView.startPulse();
    }
  }

  public Room getRoom() {
    return room;
  }

  ////////////////
  //  PRIVATE   //
  ////////////////

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

    if (liveInviteMap.containsKey(remotePeer.getSession().getUserId())) {
      liveRowView = liveInviteMap.get(remotePeer.getSession().getUserId());
      liveRowView.setPeerView(remotePeer.getPeerView());
      liveInviteMap.remove(remotePeer.getSession().getUserId());
      liveRowViewMap.put(remotePeer.getSession().getUserId(), liveRowView);
    } else if (liveRowViewMap.containsKey(remotePeer.getSession().getUserId())) {
      liveRowView = liveRowViewMap.get(remotePeer.getSession().getUserId());
      liveRowView.setPeerView(remotePeer.getPeerView());
    } else {
      liveRowView = new LiveRowView(getContext());
      liveRowView.setRoomType(viewRoom.getType());
      liveRowView.setPeerView(remotePeer.getPeerView());
      ViewGroup.LayoutParams params =
          new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT);
      viewRoom.addView(liveRowView, params);
      liveRowViewMap.put(remotePeer.getSession().getUserId(), liveRowView);
    }
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

  private static void jsonPut(JSONObject json, String key, Object value) {
    try {
      json.put(key, value);
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  //////////////////////
  //   OBSERVABLES    //
  //////////////////////

  public Observable<Void> onOpenInvite() {
    return onOpenInvite;
  }

  public Observable<Boolean> onShouldJoinRoom() {
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
}

