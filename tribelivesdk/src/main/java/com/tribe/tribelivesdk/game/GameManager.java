package com.tribe.tribelivesdk.game;

import android.content.Context;
import android.util.Pair;
import com.tribe.tribelivesdk.BuildConfig;
import com.tribe.tribelivesdk.core.WebRTCRoom;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.model.TribeSession;
import com.tribe.tribelivesdk.util.JsonUtils;
import com.tribe.tribelivesdk.webrtc.Frame;
import com.tribe.tribelivesdk.webrtc.TribeI420Frame;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.json.JSONObject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 23/05/2017.
 */

@Singleton public class GameManager {

  private static GameManager instance;

  public static GameManager getInstance(Context context) {
    if (instance == null) {
      instance = new GameManager(context);
    }

    return instance;
  }

  public static final String[] playableGames = {
      Game.GAME_SLICE_FRUIT, Game.GAME_DRAW, Game.GAME_SPEED_RACER, Game.GAME_CHALLENGE,
      Game.GAME_INVADERS, Game.GAME_POST_IT, Game.GAME_TRIVIA, Game.GAME_BATTLE_MUSIC,
      Game.GAME_BIRD_RUSH, Game.GAME_INVADERS_CORONA, Game.GAME_COOL_CAMS, Game.GAME_BATTLE_ROYALE
  };

  // VARIABLES
  private List<Game> gameList;
  private Game currentGame = null;
  private WebRTCRoom webRTCRoom;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private CompositeSubscription subscriptionsRoom = new CompositeSubscription();
  private CompositeSubscription subscriptionsGame = new CompositeSubscription();
  private CompositeSubscription subscriptionsUI = new CompositeSubscription();
  private PublishSubject<Frame> onRemoteFrame = PublishSubject.create();
  private PublishSubject<TribeI420Frame> onLocalFrame = PublishSubject.create();
  private PublishSubject<String> onPointsDrawReceived = PublishSubject.create();
  private PublishSubject<List<String>> onNewChallengeReceived = PublishSubject.create();
  private PublishSubject<List<String>> onNewDrawReceived = PublishSubject.create();
  private PublishSubject<Void> onClearDrawReceived = PublishSubject.create();
  private PublishSubject<Game> onCurrentUserStartGame = PublishSubject.create();
  private PublishSubject<Pair<TribeSession, Game>> onRemoteUserStartGame = PublishSubject.create();
  private PublishSubject<Game> onCurrentUserNewSessionGame = PublishSubject.create();
  private PublishSubject<Pair<TribeSession, Game>> onRemoteUserNewSessionGame =
      PublishSubject.create();
  private PublishSubject<Void> onGamePlayedNotAvailable = PublishSubject.create();
  private PublishSubject<Game> onCurrentUserStopGame = PublishSubject.create();
  private PublishSubject<Pair<TribeSession, Game>> onRemoteUserStopGame = PublishSubject.create();
  private PublishSubject<Game> onCurrentUserResetScores = PublishSubject.create();
  private PublishSubject<Game> onRemoteUserResetScores = PublishSubject.create();

  @Inject public GameManager(Context context) {
    gameList = new ArrayList<>();
  }

  /**
   * PRIVATE
   */

  /**
   * PUBLIC
   */

  public void addGame(Game game) {
    gameList.add(game);
  }

  public void addGames(List<Game> games) {
    gameList.clear();

    if (games != null) {
      for (Game game : games) {
        if (Arrays.asList(playableGames).contains(game.getId())
            && game.isOnline()
            && (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N
            || !game.isWeb())
            && (game.get__typename().equals(Game.TYPE_NAME_NATIVE) || game.get__typename()
            .equals(Game.TYPE_NAME_WEBV1) || game.get__typename().equals(Game.TYPE_NAME_CORONA))) {
          gameList.add(game);
        }
      }
    }

    Collections.sort(gameList, (o1, o2) -> {
      int prio = ((Integer) o2.getPriority()).compareTo(o1.getPriority());
      int res = ((Boolean) o2.isFeatured()).compareTo(o1.isFeatured());
      int playsCount = ((Integer) o2.getPlays_count()).compareTo(o1.getPlays_count());

      if (prio != 0) {
        return prio;
      } else if (res == 0) {
        return playsCount;
      } else {
        return res;
      }
    });
  }

  public void initSubscriptions() {
    for (Game game : gameList) {
      subscriptions.add(game.onLocalFrame().subscribe(onLocalFrame));
      subscriptions.add(game.onRemoteFrame().subscribe(onRemoteFrame));
    }
  }

  public void initFrameSizeChangeObs(Observable<Frame> obs) {
    subscriptions.add(obs.subscribe(frame -> {
      for (Game game : gameList) {
        game.onFrameSizeChange(frame);
      }
    }));
  }

  public void initOnNewFrameObs(Observable<Frame> obs) {
    subscriptions.add(obs.subscribe(frame -> currentGame.apply(frame)));
  }

  public void initUIControlsStartGame(Observable<Game> obs) {
    subscriptionsUI.add(obs.doOnNext(game -> webRTCRoom.sendToPeers(getNewGamePayload(game), false))
        .subscribe(onCurrentUserStartGame));
  }

  public void initUIControlsRestartGame(Observable<Game> obs) {
    subscriptionsUI.add(obs.doOnNext(game -> webRTCRoom.sendToPeers(getNewGamePayload(game), false))
        .subscribe(onCurrentUserNewSessionGame));
  }

  public void initUIControlsStopGame(Observable<Game> obs) {
    subscriptionsUI.add(
        obs.doOnNext(game -> webRTCRoom.sendToPeers(getStopGamePayload(game), false))
            .subscribe(onCurrentUserStopGame));
  }

  public void initUIControlsResetGame(Observable<Game> obs) {
    subscriptionsUI.add(obs.subscribe(onCurrentUserResetScores));
  }

  public void setWebRTCRoom(WebRTCRoom webRTCRoom) {
    this.webRTCRoom = webRTCRoom;
    subscriptionsRoom.add(
        webRTCRoom.onPointsDrawReceived().onBackpressureDrop().subscribe(onPointsDrawReceived));
    subscriptionsRoom.add(
        webRTCRoom.onNewChallengeReceived().onBackpressureDrop().subscribe(onNewChallengeReceived));
    subscriptionsRoom.add(
        webRTCRoom.onNewDrawReceived().onBackpressureDrop().subscribe(onNewDrawReceived));
    subscriptionsRoom.add(
        webRTCRoom.onClearDrawReceived().onBackpressureDrop().subscribe(onClearDrawReceived));
    subscriptionsRoom.add(webRTCRoom.onNewGame().onBackpressureDrop().subscribe(pairSessionGame -> {
      Game game = getGameById(pairSessionGame.second);
      if (game != null) {
        if (currentGame == null) {
          onRemoteUserStartGame.onNext(Pair.create(pairSessionGame.first, game));
        } else {
          onRemoteUserNewSessionGame.onNext(Pair.create(pairSessionGame.first, game));
        }
      } else {
        onGamePlayedNotAvailable.onNext(null);
      }
    }));

    subscriptionsRoom.add(
        webRTCRoom.onStopGame().onBackpressureDrop().subscribe(pairSessionGame -> {
          Game game = getGameById(pairSessionGame.second);
          if (game != null) {
            onRemoteUserStopGame.onNext(Pair.create(pairSessionGame.first, game));
          }
        }));
  }

  public List<Game> getGames() {
    return gameList;
  }


  public List<Game> getHomeGames() {
    List<Game> filtred = new ArrayList<>();
    for (Game g : gameList) {
      boolean enable = (BuildConfig.VERSION_CODE >= g.getMin_android_version());
      if (g.isIn_home() && enable) filtred.add(g);
    }
    return filtred;
  }

  public Game getGameById(String id) {
    for (Game game : gameList) {
      if (game.getId().equals(id)) return game;
    }

    return null;
  }

  public void setCurrentGame(Game game) {
    this.currentGame = game;

    if (currentGame != null) {
      if (currentGame instanceof GamePostIt) {
        GamePostIt gamePostIt = (GamePostIt) game;
        gamePostIt.generateNewName();
      } else if (currentGame instanceof GameDraw) {
        GameDraw gameDraw = (GameDraw) game;
        gameDraw.generateNewDatas();
      } else if (currentGame instanceof GameChallenge) {
        GameChallenge gameChallenge = (GameChallenge) game;
        gameChallenge.generateNewDatas();
      }
    }
  }

  public void stop() {
    this.currentGame = null;
  }

  public Game getCurrentGame() {
    return currentGame;
  }

  public boolean isFacialRecognitionNeeded() {
    return currentGame != null && (currentGame.getId().equals(Game.GAME_POST_IT)
        || currentGame.getId().equals(Game.GAME_COOL_CAMS));
  }

  public void dispose() {
    if (subscriptions != null) subscriptions.clear();
    for (Game game : gameList) {
      game.dispose();
    }
  }

  public void disposeLive() {
    subscriptionsRoom.clear();
    subscriptionsUI.clear();
    subscriptionsGame.clear();
    subscriptions.clear();
    webRTCRoom = null;
  }

  /**
   * PAYLOADS FOR WEBRTC ROOM
   */

  public JSONObject getStopGamePayload(Game game) {
    JSONObject obj = new JSONObject();
    JSONObject gameStop = new JSONObject();
    JsonUtils.jsonPut(gameStop, Game.ACTION, Game.STOP);
    JsonUtils.jsonPut(gameStop, Game.ID, game.getId());
    JsonUtils.jsonPut(obj, WebRTCRoom.MESSAGE_GAME, gameStop);
    return obj;
  }

  public JSONObject getNewGamePayload(Game game) {
    JSONObject obj = new JSONObject();
    JSONObject gameStart = new JSONObject();
    JsonUtils.jsonPut(gameStart, Game.ACTION, Game.START);
    JsonUtils.jsonPut(gameStart, Game.ID, game.getId());
    JsonUtils.jsonPut(obj, WebRTCRoom.MESSAGE_GAME, gameStart);
    return obj;
  }

  public void setCurrentDataGame(String name, TribeGuest currentPlayer) {
    if (currentGame.getId().equals(Game.GAME_DRAW)) {
      ((GameDraw) currentGame).setCurrentMaster(currentPlayer);
      ((GameDraw) currentGame).setCurrentDrawer(currentPlayer);
      ((GameDraw) currentGame).setCurrentDrawName(name);
    } else if (currentGame.getId().equals(Game.GAME_CHALLENGE)) {
      ((GameChallenge) currentGame).setCurrentMaster(currentPlayer);
      ((GameChallenge) currentGame).setCurrentChallenger(currentPlayer);
      ((GameChallenge) currentGame).setCurrentChallenge(name);
    }
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<Frame> onRemoteFrame() {
    return onRemoteFrame;
  }

  public Observable<TribeI420Frame> onLocalFrame() {
    return onLocalFrame;
  }

  public Observable<Game> onCurrentUserStartGame() {
    return onCurrentUserStartGame;
  }

  public Observable<Pair<TribeSession, Game>> onRemoteUserStartGame() {
    return onRemoteUserStartGame;
  }

  public Observable<Game> onCurrentUserNewSessionGame() {
    return onCurrentUserNewSessionGame;
  }

  public Observable<Pair<TribeSession, Game>> onRemoteUserNewSessionGame() {
    return onRemoteUserNewSessionGame;
  }

  public Observable<Game> onCurrentUserStopGame() {
    return onCurrentUserStopGame;
  }

  public Observable<Pair<TribeSession, Game>> onRemoteUserStopGame() {
    return onRemoteUserStopGame;
  }

  public Observable<Game> onCurrentUserResetScores() {
    return onCurrentUserResetScores;
  }

  public Observable<Void> onGamePlayedNotAvailable() {
    return onGamePlayedNotAvailable;
  }
}
