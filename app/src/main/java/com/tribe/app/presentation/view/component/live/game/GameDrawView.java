package com.tribe.app.presentation.view.component.live.game;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.animation.OvershootInterpolator;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.gson.Gson;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.mvp.presenter.GamePresenter;
import com.tribe.app.presentation.mvp.view.adapter.GameMVPViewAdapter;
import com.tribe.app.presentation.view.component.live.LiveStreamView;
import com.tribe.app.presentation.view.component.live.game.common.GameView;
import com.tribe.app.presentation.view.utils.DeviceUtils;
import com.tribe.app.presentation.view.utils.ViewPagerScroller;
import com.tribe.app.presentation.view.widget.game.GameDrawViewPagerAdapter;
import com.tribe.app.presentation.view.widget.game.GameViewPager;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameDraw;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.util.JsonUtils;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.json.JSONArray;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Created by madaaflak on 31/07/2017.
 */

public class GameDrawView extends GameView {

  @Inject GamePresenter gamePresenter;

  @BindView(R.id.pager) GameViewPager viewpager;

  // VARIABLES
  private GameDrawViewPagerAdapter adapter;
  private GameDraw gameDraw;
  private GameMVPViewAdapter gameMVPViewAdapter;

  // OBSERVABLES
  private PublishSubject<Boolean> onNextDraw;

  public GameDrawView(@NonNull Context context) {
    super(context);
  }

  public GameDrawView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    gamePresenter.onViewDetached();
  }

  @Override protected void initView(Context context) {
    super.initView(context);

    inflater.inflate(R.layout.view_game_draw, this, true);
    unbinder = ButterKnife.bind(this);

    gameMVPViewAdapter = new GameMVPViewAdapter() {
      @Override public void onGameData(List<String> data) {
        gameDraw.setDataList(data);
        Timber.d("Data loaded");
        setupNewDraw();
      }
    };
    gamePresenter.onViewAttached(gameMVPViewAdapter);

    adapter = new GameDrawViewPagerAdapter(context, currentUser);
    viewpager.setAdapter(adapter);

    changePagerScroller();

    onNextDraw = PublishSubject.create();

    subscriptions.add(adapter.onNextDraw().doOnNext(aBoolean -> Timber.d("onNextDraw adapter")).subscribe(onNextDraw));

    subscriptions.add(adapter.onCurrentGame().subscribe(game -> {
      GameDraw gameDraw = (GameDraw) game;
      webRTCRoom.sendToPeers(
          getNewDrawPayload(currentUser.getId(), gameDraw.getCurrentDrawer().getId(),
              gameDraw.getCurrentDrawName()), false);
    }));

    subscriptions.add(adapter.onClearDraw()
        .subscribe(aVoid -> webRTCRoom.sendToPeers(getDrawClearPayload(), false)));

    subscriptions.add(adapter.onDrawing()
        .subscribe(points -> webRTCRoom.sendToPeers(getDrawPointPayload(points), false)));
  }

  protected void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) getContext()));
  }

  @Override public void dispose() {
    super.dispose();
    adapter = null;
  }

  private void changePagerScroller() {
    try {
      Field mScroller = null;
      mScroller = ViewPager.class.getDeclaredField("mScroller");
      mScroller.setAccessible(true);
      ViewPagerScroller scroller =
          new ViewPagerScroller(viewpager.getContext(), new OvershootInterpolator(1.3f));
      mScroller.set(viewpager, scroller);
    } catch (Exception e) {
      Timber.e("error of change scroller " + e);
    }
  }

  private JSONObject getNewDrawPayload(String userId, String peerId, String draw) {
    JSONObject app = new JSONObject();
    JSONObject obj = new JSONObject();
    JSONObject game = new JSONObject();
    JsonUtils.jsonPut(game, "from", userId);
    JsonUtils.jsonPut(game, Game.ACTION, "newDraw");
    JsonUtils.jsonPut(game, "user", peerId);
    JsonUtils.jsonPut(game, "draw", draw);
    JsonUtils.jsonPut(obj, "draw", game);
    JsonUtils.jsonPut(app, "app", obj);
    return app;
  }

  private JSONObject getDrawClearPayload() {
    JSONObject app = new JSONObject();
    JSONObject obj = new JSONObject();
    JSONObject game = new JSONObject();
    JsonUtils.jsonPut(game, "action", "clear");
    JsonUtils.jsonPut(obj, "draw", game);
    JsonUtils.jsonPut(app, "app", obj);
    return app;
  }

  private JSONObject getDrawPointPayload(List<Float[]> map) {
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

    JsonUtils.jsonPut(path, "hexColor", "F9AD25");
    JsonUtils.jsonPut(path, "lineWidth", 6.0);
    JsonUtils.jsonPut(path, "id", "view_recycler_message");
    JsonUtils.jsonPut(path, "points", array);

    JSONObject gameObject = new JSONObject();
    JsonUtils.jsonPut(gameObject, "action", "drawPath");
    JsonUtils.jsonPut(gameObject, "path", path);

    JsonUtils.jsonPut(game, "draw", gameObject);
    JsonUtils.jsonPut(app, "app", game);
    return app;
  }

  private void onPointsDrawReceived(String pointsReceived) {
    if (adapter == null) return;
    Gson gson = new Gson();
    Float[][] str = gson.fromJson(pointsReceived, Float[][].class);
    adapter.onPointsDrawReceived(str);
  }

  private void onClearDrawReceived() {
    if (adapter == null) return;
    adapter.onClearDrawReceived();
  }

  /**
   * PUBLIC
   */

  @Override protected void initWebRTCRoomSubscriptions() {
    subscriptionsRoom.add(webRTCRoom.onPointsDrawReceived()
        .onBackpressureBuffer()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(s -> onPointsDrawReceived(s)));

    subscriptionsRoom.add(webRTCRoom.onClearDrawReceived()
        .onBackpressureLatest()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aVoid -> onClearDrawReceived()));

    subscriptionsRoom.add(webRTCRoom.onNewDrawReceived()
        .onBackpressureLatest()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(datas -> {
          TribeGuest guestChallenged = null;

          for (TribeGuest guest : peerMap.values()) {
            if (guest.getId().equals(datas.get(1))) {
              guestChallenged = guest;
            }
          }

          gameManager.setCurrentDataGame(datas.get(0), guestChallenged);
          GameDraw gameDraw = (GameDraw) gameManager.getCurrentGame();
          if (gameDraw.hasDatas()) nextDraw();
        }));
  }

  @Override public void stop() {
    super.stop();
    gameDraw = null;
    gameMVPViewAdapter = null;
  }

  @Override protected void takeOverGame() {
    Timber.d("takeOverGame");
    nextDraw();
  }

  @Override public void setNextGame() {
    Timber.d("setNextGame");
    nextDraw();
  }

  @Override public void start(Game game, Observable<Map<String, TribeGuest>> map,
      Observable<Map<String, TribeGuest>> mapInvited,
      Observable<Map<String, LiveStreamView>> liveViewsObservable, String userId) {
    super.start(game, map, mapInvited, liveViewsObservable, userId);
    gameDraw = (GameDraw) game;
    Timber.d("start");
  }

  private void nextDraw() {
    Timber.d("nextDraw");
    if (game != null) game.incrementRoundCount();
    if (adapter == null) initView(context);

    if (gameDraw.getDataList() == null || gameDraw.getDataList().size() == 0) {
      Timber.d("no data, loading data");
      gamePresenter.synchronizeGame(DeviceUtils.getLanguage(getContext()), gameDraw.getId());
    } else {
      setupNewDraw();
    }
  }

  private void setupNewDraw() {
    Timber.d("setupNewDraw");
    new Handler().postDelayed(() -> {
      int currentItem = (viewpager.getCurrentItem() + 1);
      viewpager.setCurrentItem(currentItem);
      setVisibility(VISIBLE);
    }, 500);
  }

  /**
   * OBSERVABLES
   */

  public Observable<Boolean> onNextDraw() {
    return onNextDraw;
  }
}
