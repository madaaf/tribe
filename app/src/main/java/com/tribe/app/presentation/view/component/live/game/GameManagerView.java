package com.tribe.app.presentation.view.component.live.game;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.f2prateek.rx.preferences.Preference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.mvp.presenter.GamePresenter;
import com.tribe.app.presentation.mvp.view.adapter.GameMVPViewAdapter;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.preferences.GameData;
import com.tribe.app.presentation.view.component.live.LiveStreamView;
import com.tribe.app.presentation.view.component.live.game.aliensattack.GameAliensAttackView;
import com.tribe.app.presentation.view.component.live.game.battlemusic.GameBattleMusicView;
import com.tribe.app.presentation.view.component.live.game.common.GameView;
import com.tribe.app.presentation.view.component.live.game.common.GameViewWithRanking;
import com.tribe.app.presentation.view.component.live.game.trivia.GameTriviaView;
import com.tribe.app.presentation.view.component.live.game.web.GameWebView;
import com.tribe.tribelivesdk.core.WebRTCRoom;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameManager;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.model.TribeSession;
import com.tribe.tribelivesdk.util.ObservableRxHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 10/30/2017.
 */

public class GameManagerView extends FrameLayout {

  @Inject @GameData Preference<String> gameData;

  @Inject User currentUser;

  @Inject GamePresenter gamePresenter;

  /**
   * VARIABLES
   */

  private Unbinder unbinder;
  private GameManager gameManager;
  private GameView currentGameView;
  private WebRTCRoom webRTCRoom;
  private Map<String, TribeGuest> peerMap;
  private Game currentGame;
  private Map<String, List<String>> mapGameData;
  private Observable<Map<String, LiveStreamView>> onLiveViewsChange;
  private GameMVPViewAdapter gameMVPViewAdapter;

  /**
   * OBSERVABLES
   */

  private CompositeSubscription subscriptions = new CompositeSubscription();
  private CompositeSubscription subscriptionsGame = new CompositeSubscription();
  private BehaviorSubject<Map<String, TribeGuest>> onPeerMapChange = BehaviorSubject.create();
  private PublishSubject<Game> onRestartGame = PublishSubject.create();
  private PublishSubject<Game> onStopGame = PublishSubject.create();
  private PublishSubject<Void> onPlayOtherGame = PublishSubject.create();
  private PublishSubject<Pair<String, Integer>> onAddScore = PublishSubject.create();

  public GameManagerView(@NonNull Context context) {
    super(context);
    initView();
  }

  public GameManagerView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    gamePresenter.onViewAttached(gameMVPViewAdapter);
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    gamePresenter.onViewDetached();
  }

  private void initView() {
    initDependencyInjector();
    unbinder = ButterKnife.bind(this);
    gameManager = GameManager.getInstance(getContext());
    peerMap = new HashMap<>();

    if (!StringUtils.isEmpty(gameData.get())) {
      mapGameData = new Gson().fromJson(gameData.get(), new TypeToken<HashMap<String, Object>>() {
      }.getType());
    } else {
      mapGameData = new HashMap<>();
    }

    setBackground(null);

    initSubscriptions();
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

  private void initSubscriptions() {
    subscriptions.add(Observable.merge(gameManager.onCurrentUserStartGame()
        .map(game -> Pair.create(new TribeSession(TribeSession.PUBLISHER_ID, currentUser.getId()),
            game)), gameManager.onRemoteUserStartGame())
        .filter(sessionPairgame -> sessionPairgame.second.hasView())
        .subscribe(sessionGamePair -> {
          currentGame = sessionGamePair.second;

          if (currentGameView == null) {
            addGameView(computeGameView(currentGame, sessionGamePair.first.getUserId()));
          }

          if (currentGameView instanceof GameChallengesView) {
            GameChallengesView gameChallengesView = (GameChallengesView) currentGameView;
            gameChallengesView.displayPopup();
          }

          currentGameView.setNextGame();
        }));

    subscriptions.add(Observable.merge(gameManager.onCurrentUserNewSessionGame(),
        gameManager.onRemoteUserNewSessionGame()
            .map(tribeSessionGamePair -> tribeSessionGamePair.second))
        .filter(game -> game.hasView())
        .subscribe(game -> {

        }));

    subscriptions.add(Observable.merge(gameManager.onCurrentUserStopGame(),
        gameManager.onRemoteUserStopGame().map(tribeSessionGamePair -> tribeSessionGamePair.second))
        .filter(game -> game.hasView())
        .subscribe(game -> {
          if (currentGameView != null) {
            currentGameView.stop();
            removeView(currentGameView);
          }

          currentGameView = null;
          currentGame = null;
        }));

    subscriptions.add(gameManager.onCurrentUserResetScores().subscribe(game -> {
      if (currentGameView != null && currentGameView instanceof GameViewWithRanking) {
        ((GameViewWithRanking) currentGameView).resetScores(true);
      }
    }));

    subscriptions.add(gameManager.onGamePlayedNotAvailable()
        .subscribe(
            aVoid -> Toast.makeText(getContext(), R.string.game_unavailable, Toast.LENGTH_SHORT)
                .show()));
  }

  public void initPeerGuestObservable(
      Observable<ObservableRxHashMap.RxHashMap<String, TribeGuest>> obs) {
    peerMap.put(currentUser.getId(), currentUser.asTribeGuest());
    onPeerMapChange.onNext(peerMap);

    subscriptions.add(obs.subscribe(rxHashMapAction -> {
      if (rxHashMapAction.changeType.equals(ObservableRxHashMap.ADD)) {
        peerMap.put(rxHashMapAction.item.getId(), rxHashMapAction.item);
      } else if (rxHashMapAction.changeType.equals(ObservableRxHashMap.REMOVE)) {
        peerMap.remove(rxHashMapAction.item.getId());
        if (currentGameView != null) currentGameView.userLeft(rxHashMapAction.item.getId());
      }

      onPeerMapChange.onNext(peerMap);
    }));
  }

  public void initLiveViewsObservable(Observable<Map<String, LiveStreamView>> observable) {
    this.onLiveViewsChange = observable;
  }

  private void addGameView(GameView currentGameView) {
    this.currentGameView = currentGameView;
    FrameLayout.LayoutParams params =
        new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT);
    addView(this.currentGameView, params);
  }

  private GameView computeGameView(Game game, String userId) {
    GameView gameView = null;

    if (game.getId().equals(Game.GAME_CHALLENGE)) {
      GameChallengesView gameChallengesView = new GameChallengesView(getContext());
      gameView = gameChallengesView;
      subscriptionsGame.add(gameChallengesView.onNextChallenge()
          .doOnNext(game1 -> gameChallengesView.setNextGame())
          .subscribe(onRestartGame));
    } else if (game.getId().equals(Game.GAME_DRAW)) {
      GameDrawView gameDrawView = new GameDrawView(getContext());
      gameView = gameDrawView;
      subscriptionsGame.add(gameDrawView.onNextDraw()
          .map(aBoolean -> gameManager.getCurrentGame())
          .doOnNext(game1 -> gameDrawView.setNextGame())
          .subscribe(onRestartGame));
    } else if (game.getId().equals(Game.GAME_INVADERS)) {
      GameAliensAttackView gameAlienAttacksView = new GameAliensAttackView(getContext());
      subscriptionsGame.add(gameAlienAttacksView.onAddScore().subscribe(onAddScore));
      gameView = gameAlienAttacksView;
    } else if (game.getId().equals(Game.GAME_TRIVIA)) {
      GameTriviaView gameTriviaView = new GameTriviaView(getContext());
      subscriptionsGame.add(gameTriviaView.onAddScore().subscribe(onAddScore));
      subscriptionsGame.add(gameTriviaView.onStop().subscribe(onStopGame));
      subscriptionsGame.add(gameTriviaView.onRestart().subscribe(onRestartGame));
      subscriptionsGame.add(gameTriviaView.onPlayOtherGame()
          .doOnNext(aVoid -> onStopGame.onNext(currentGame))
          .subscribe(onPlayOtherGame));
      gameView = gameTriviaView;
    } else if (game.getId().equals(Game.GAME_BATTLE_MUSIC)) {
      GameBattleMusicView gameBattleMusicView = new GameBattleMusicView(getContext());
      subscriptionsGame.add(gameBattleMusicView.onAddScore().subscribe(onAddScore));
      subscriptionsGame.add(gameBattleMusicView.onStop().subscribe(onStopGame));
      subscriptionsGame.add(gameBattleMusicView.onRestart().subscribe(onRestartGame));
      subscriptionsGame.add(gameBattleMusicView.onPlayOtherGame()
          .doOnNext(aVoid -> onStopGame.onNext(currentGame))
          .subscribe(onPlayOtherGame));
      gameView = gameBattleMusicView;
    } else if (game.isWeb()) {
      GameWebView gameWebView = new GameWebView(getContext());
      subscriptionsGame.add(gameWebView.onAddScore().subscribe(onAddScore));
      gameView = gameWebView;
    }

    gameView.setWebRTCRoom(webRTCRoom);
    gameView.start(game, onPeerMapChange, onLiveViewsChange, userId);
    game.initPeerMapObservable(onPeerMapChange);
    game.setDataList(mapGameData.get(game.getId()));

    return gameView;
  }

  /**
   * PUBLIC
   */

  public void setWebRTCRoom(WebRTCRoom webRTCRoom) {
    this.webRTCRoom = webRTCRoom;
  }

  public void disposeGame() {
    subscriptionsGame.clear();
    if (currentGameView != null) {
      currentGameView.stop();
      removeView(currentGameView);
      currentGameView = null;
    }
  }

  public void dispose() {
    subscriptions.clear();
    disposeGame();
  }

  /**
   * OBSERVABLES
   */

  public Observable<Game> onRestartGame() {
    return onRestartGame;
  }

  public Observable<Pair<String, Integer>> onAddScore() {
    return onAddScore;
  }

  public Observable<Game> onStopGame() {
    return onStopGame;
  }

  public Observable<Void> onPlayOtherGame() {
    return onPlayOtherGame;
  }
}
