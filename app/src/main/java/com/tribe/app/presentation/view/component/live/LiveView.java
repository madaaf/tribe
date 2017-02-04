package com.tribe.app.presentation.view.component.live;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.tribelivesdk.TribeLiveSDK;
import com.tribe.tribelivesdk.back.TribeLiveOptions;
import com.tribe.tribelivesdk.core.Room;
import com.tribe.tribelivesdk.model.RemotePeer;
import com.tribe.tribelivesdk.view.LocalPeerView;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
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

  @Inject User user;

  @Inject ScreenUtils screenUtils;

  @Inject AccessToken accessToken;

  @Inject TribeLiveSDK tribeLiveSDK;

  @BindView(R.id.viewRoom) LiveRoomView viewRoom;

  @BindView(R.id.btnInviteLive) View btnInviteLive;

  @BindView(R.id.btnLeave) View btnLeave;

  @BindView(R.id.btnNotify) View btnNotify;

  @BindView(R.id.txtName) TextViewFont txtName;

  // VARIABLES
  private Recipient recipient;
  private LocalPeerView viewLocalPeer;
  private Room room;
  private LiveRowView latestView;
  private Map<String, LiveRowView> liveRowViewMap;

  // RESOURCES
  private int timeJoinRoom;

  // OBSERVABLES
  private Unbinder unbinder;
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private Subscription subscriptionJoinRoom;
  private PublishSubject<Void> onOpenInvite = PublishSubject.create();
  private PublishSubject<Boolean> onShouldJoinRoom = PublishSubject.create();
  private PublishSubject<Void> onNotify = PublishSubject.create();

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
      room.leaveRoom();
      viewRoom.removeAllViews();
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
  }

  private void initUI() {
    setBackgroundColor(Color.BLACK);
    viewLocalPeer = new LocalPeerView(getContext());
    viewRoom.addView(viewLocalPeer);

    room = tribeLiveSDK.newRoom();
    room.initLocalStream(viewLocalPeer);
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
  }

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

    subscriptions.add(room.onRemotePeerAdded().subscribe(remotePeer -> {
      Timber.d("Remote peer added with id : "
          + remotePeer.getSession().getPeerId()
          + " & view : "
          + remotePeer.getPeerView());
      addView(remotePeer);
    }));

    subscriptions.add(room.onRemotePeerRemoved().subscribe(remotePeer -> {
      Timber.d("Remote peer removed with id : " + remotePeer.getSession().getPeerId());
    }));

    subscriptions.add(room.onRemotePeerUpdated().subscribe(remotePeer -> {
      Timber.d("Remote peer updated with id : " + remotePeer.getSession().getPeerId());
    }));

    Timber.d("Initiating Room");
    room.connect(options);
  }

  ///////////////////
  //    CLICKS     //
  ///////////////////

  @OnClick(R.id.btnInviteLive) void openInvite() {
    onOpenInvite.onNext(null);
  }

  @OnClick(R.id.btnNotify) void onClickNotify() {
    onNotify.onNext(null);
  }

  ///////////////////
  //    PUBLIC     //
  ///////////////////

  public void initInviteOpenSubscription(Observable<Integer> obs) {
    subscriptions.add(obs.subscribe(event -> {
      viewRoom.setType(
          event == LiveContainer.EVENT_OPENED ? LiveRoomView.LINEAR : LiveRoomView.GRID);
    }));
  }

  public void initOnStartDragSubscription(Observable<TileView> obs) {
    subscriptions.add(obs.subscribe(tileView -> {
      latestView = new LiveRowView(getContext());
      addView(latestView, tileView.getRecipient(), tileView.getBackgroundColor());
    }));
  }

  public void initOnEndDragSubscription(Observable<Void> obs) {
    subscriptions.add(obs.subscribe(aVoid -> {
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
      LiveRowView liveRowView = new LiveRowView(getContext());
      liveRowViewMap.put(recipient.getSubId(), liveRowView);
      addView(liveRowView, this.recipient, color);
      liveRowView.startPulse();
    }
  }

  public Room getRoom() {
    return room;
  }

  ////////////////
  //  PRIVATE   //
  ////////////////

  private void addView(LiveRowView liveRowView, Recipient recipient, int color) {
    liveRowView.setColor(color);
    liveRowView.setRecipient(recipient);
    liveRowView.setRoomType(viewRoom.getType());
    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT);
    viewRoom.addView(liveRowView, params);
  }

  private void addView(RemotePeer remotePeer) {
    LiveRowView liveRowView;

    if (liveRowViewMap.containsKey(remotePeer.getSession().getUserId())) {
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
}

