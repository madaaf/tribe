package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.domain.entity.Live;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.presentation.mvp.presenter.common.RoomPresenter;
import com.tribe.app.presentation.mvp.view.adapter.RoomMVPViewAdapter;
import com.tribe.app.presentation.view.component.live.LiveLocalView;
import com.tribe.app.presentation.view.component.live.LiveRoomView;
import com.tribe.app.presentation.view.component.live.LiveRowView;
import com.tribe.app.presentation.view.component.live.LiveStreamView;
import com.tribe.app.presentation.view.component.live.game.corona.GameCoronaView;
import com.tribe.app.presentation.view.utils.Degrees;
import com.tribe.tribelivesdk.TribeLiveSDK;
import com.tribe.tribelivesdk.back.TribeLiveOptions;
import com.tribe.tribelivesdk.back.WebSocketConnection;
import com.tribe.tribelivesdk.core.WebRTCRoom;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameManager;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.util.ObservableRxHashMap;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import rx.subjects.BehaviorSubject;
import rx.subscriptions.CompositeSubscription;

public class CoronaGameActivity extends BaseActivity {

  public static Intent getCallingIntent(Context context) {
    Intent intent = new Intent(context, CoronaGameActivity.class);
    return intent;
  }

  @Inject RoomPresenter roomPresenter;

  @Inject AccessToken accessToken;

  @Inject TribeLiveSDK tribeLiveSDK;

  @BindView(R.id.viewRoot) FrameLayout viewRoot;
  @BindView(R.id.viewRoom) LiveRoomView viewRoom;
  @BindView(R.id.viewLocalLive) LiveLocalView viewLocalLive;

  // VARIABLES
  private Unbinder unbinder;
  private GameManager gameManager;
  private GameCoronaView gameCoronaView;
  private RoomMVPViewAdapter roomMVPViewAdapter;
  private WebRTCRoom webRTCRoom;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private ObservableRxHashMap<String, TribeGuest> masterMap = new ObservableRxHashMap();
  private BehaviorSubject<Map<String, TribeGuest>> mapObservable = BehaviorSubject.create();
  private BehaviorSubject<Map<String, TribeGuest>> mapInvitedObservable = BehaviorSubject.create();
  private BehaviorSubject<Map<String, LiveStreamView>> mapViewsObservable =
      BehaviorSubject.create();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_corona);

    unbinder = ButterKnife.bind(this);

    roomMVPViewAdapter = new RoomMVPViewAdapter() {
      @Override public void onRoomInfos(Room room) {
        Map<String, String> headers = new HashMap<>();
        headers.put(WebSocketConnection.ORIGIN, com.tribe.app.BuildConfig.TRIBE_ORIGIN);

        TribeLiveOptions options =
            new TribeLiveOptions.TribeLiveOptionsBuilder(CoronaGameActivity.this).wsUrl(
                room.getRoomCoordinates().getUrl())
                .tokenId(accessToken.getAccessToken())
                .iceServers(room.getRoomCoordinates().getIceServers())
                .roomId(room.getId())
                .routingMode(TribeLiveOptions.ROUTED)
                .headers(headers)
                .orientation(Degrees.getNormalizedDegrees(CoronaGameActivity.this))
                .frontCamera(viewLocalLive.isFrontFacing())
                .build();

        webRTCRoom.connect(options);
        viewRoom.setType(LiveRoomView.TYPE_LIST);

        gameCoronaView = new GameCoronaView(CoronaGameActivity.this,
            gameManager.getGameById(Game.GAME_INVADERS_CORONA));
        FrameLayout.LayoutParams params =
            new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        viewRoot.addView(gameCoronaView, 0, params);

        gameCoronaView.start(gameManager.getGameById(Game.GAME_INVADERS_CORONA),
            masterMap.getObservable(), mapObservable, mapInvitedObservable, mapViewsObservable,
            getCurrentUser().getId());
      }
    };

    gameManager = GameManager.getInstance(this);

    initDependencyInjector();
    init();
  }

  @Override protected void onStart() {
    super.onStart();
    roomPresenter.onViewAttached(roomMVPViewAdapter);
  }

  @Override protected void onStop() {
    roomPresenter.onViewDetached();
    super.onStop();
  }

  @Override protected void onPause() {
    super.onPause();
    gameCoronaView.onPause();
  }

  @Override protected void onDestroy() {
    if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    webRTCRoom.leaveRoom();
    viewRoom.dispose();
    gameCoronaView.stop();
    if (unbinder != null) unbinder.unbind();
    super.onDestroy();
  }

  private void init() {
    webRTCRoom = tribeLiveSDK.newRoom(true);
    webRTCRoom.initLocalStream(viewLocalLive.getLocalPeerView());

    TribeGuest tribeGuest = getCurrentUser().asTribeGuest();

    masterMap.put(tribeGuest.getId(), tribeGuest);
    mapObservable.onNext(masterMap.getMap());
    Map<String, LiveStreamView> mapLiveStream = new HashMap<>();
    mapLiveStream.put(tribeGuest.getId(), new LiveRowView(this));
    mapViewsObservable.onNext(mapLiveStream);

    roomPresenter.createRoom(new Live.Builder(Live.NEW_CALL).build());
  }

  private void initDependencyInjector() {
    this.getApplicationComponent().inject(this);
  }

  @Override public void finish() {
    super.finish();
    overridePendingTransition(R.anim.activity_in_scale, R.anim.activity_out_to_right);
  }
}