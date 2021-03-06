package com.tribe.app.presentation.view.component.live.game.common;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.TextViewCompat;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import com.tribe.app.R;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.component.live.LiveStreamView;
import com.tribe.app.presentation.view.component.live.game.web.GameWebView;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.util.JsonUtils;
import com.tribe.tribelivesdk.util.ObservableRxHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Created by tiago on 10/30/2017.
 */

public abstract class GameViewWithEngine extends GameViewWithRanking {
  private static final int IOS_RATIO_PX_HEIGHT = 667;
  private static final int DURATION = 250;
  private static final int DELAY = 250;
  private static final float OVERSHOOT = 1.25f;

  protected static final String ACTION_NEW_GAME = "newGame";
  private static final String ACTION_USER_GAME_OVER = "userGameOver";
  private static final String ACTION_USER_WAITING = "userWaiting";
  private static final String ACTION_USER_READY = "userReady";
  private static final String ACTION_SHOW_USER_LOST = "showUserLost";
  private static final String ACTION_GAME_OVER = "gameOver";

  private static final String PLAYERS = "players";
  private static final String TIMESTAMP = "timestamp";

  // VARIABLES
  protected GameEngine gameEngine;
  protected TextViewFont txtRestart, txtMessage;
  protected String wordingPrefix = "";
  protected boolean pending = false;
  protected boolean ready = false;
  protected int roundPoints = 0;

  // OBSERVABLES
  protected PublishSubject<Boolean> onPending = PublishSubject.create();
  protected PublishSubject<Boolean> isGameReady = PublishSubject.create();
  protected BehaviorSubject<Boolean> onGameReady = BehaviorSubject.create();
  protected PublishSubject<String> onMessage = PublishSubject.create();

  public GameViewWithEngine(@NonNull Context context) {
    super(context);
  }

  public GameViewWithEngine(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void initView(Context context) {
    super.initView(context);
  }

  /**
   * PRIVATE
   */

  protected GameEngine generateEngine() {
    return gameEngine = new GameEngine(getContext());
  }

  protected abstract int getSoundtrack();

  protected abstract String getStyleFont();

  protected void becomePlayer() {
    Timber.d("becomePlayer");
    if (subscriptionsSession != null) subscriptionsSession.clear();
    stopEngine();
    startEngine();
  }

  protected void becomeGameMaster() {
    Timber.d("becomeGameMaster");

    startMasterEngine();

    Map<String, Integer> mapPlayerStatus = gameEngine.getMapPlayerStatus();
    Set<String> playerIds = mapPlayerStatus.keySet();
    newGame(playerIds);
  }

  protected void newGame(Set<String> playerIds) {
    Timber.d("newGame");
    long timestamp = startGameTimestamp();
    webRTCRoom.sendToPeers(getNewGamePayload(currentUser.getId(), timestamp,
        playerIds.toArray(new String[playerIds.size()])), true);
    setupGameLocally(currentUser.getId(), playerIds, timestamp);
    resetScores(false);
  }

  @Override protected void takeOverGame() {
    Timber.d("takeOverGame");
    startMasterEngine();
    gameEngine.checkGameOver();
  }

  @Override protected void initWebRTCRoomSubscriptions() {
    super.initWebRTCRoomSubscriptions();

    subscriptionsRoom.add(webRTCRoom.onGameMessage()
        .onBackpressureDrop()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(pair -> {
          JSONObject jsonObject = pair.second;
          if (jsonObject.has(game.getId())) {
            try {
              JSONObject message = jsonObject.getJSONObject(game.getId());
              if (message.has(ACTION_KEY)) {
                String actionKey = message.getString(ACTION_KEY);
                if (actionKey.equals(ACTION_USER_GAME_OVER)) {
                  String fromUserId = message.getString(USER_KEY);
                  setStatus(RankingStatus.LOST, fromUserId);
                  gameEngine.setUserGameOver(fromUserId);
                } else if (actionKey.equals(ACTION_NEW_GAME)) {
                  String fromUserId = message.getString(FROM_KEY);
                  JSONArray jsonArray = message.getJSONArray(PLAYERS);
                  Set<String> players = new HashSet<>();
                  for (int i = 0; i < jsonArray.length(); i++) {
                    players.add(jsonArray.getString(i));
                  }

                  if (subscriptionsSession != null) subscriptionsSession.clear();
                  setupGameLocally(fromUserId, players, message.getLong(TIMESTAMP) * 1000);
                } else if (actionKey.equals(ACTION_SHOW_USER_LOST)) {
                  playerLost(message.getString(USER_KEY));
                } else if (actionKey.equals(ACTION_GAME_OVER)) {
                  gameOver(message.getString(USER_KEY), false);
                } else if (actionKey.equals(ACTION_USER_READY)) {
                  if (message.has(USER_KEY)) {
                    if (gameEngine != null) gameEngine.setUserReady(message.getString(USER_KEY));
                  }
                }
              }
            } catch (JSONException e) {
              e.printStackTrace();
            }
          }
        }));
  }

  protected void startMasterEngine() {
    Timber.d("startMasterEngine");

    subscriptions.add(gameEngine.onPlayerLost.subscribe(userId -> {
      webRTCRoom.sendToPeers(getLostPayload(userId), true);
      playerLost(userId);
    }));

    subscriptions.add(gameEngine.onGameOver.subscribe(winnerId -> {
      stopEngine();
      webRTCRoom.sendToPeers(getGameOverPayload(winnerId), true);
      gameOver(winnerId, true);
    }));
  }

  protected void startEngine() {
    Timber.d("startEngine");
    if (gameEngine != null) gameEngine.stop();
    gameEngine = generateEngine();
    gameEngine.initPeerMapObservable(peerMapObservable);

    subscriptions.add(gameEngine.onPlayerPending.subscribe(userId -> {
      setStatus(RankingStatus.PENDING, userId);
      playerPending(userId);
    }));
  }

  protected void stopEngine() {
    Timber.d("stopEngine");
    if (gameEngine != null) gameEngine.stop();
    gameEngine = null;
  }

  protected void playerPending(String userId) {
    Timber.d("playerPending (userId : " + userId + " )");
    TribeGuest player = peerMap.get(userId);

    if (player != null) {
      if (player.getId().equals(currentUser.getId())) {
        changeMessageStatus(txtRestart, true, true, 100, 0, null, null);
        refactorPending(true);
      }
    }
  }

  protected void imReady() {
    webRTCRoom.sendToPeers(getImReadyPayload(currentUser.getId()), true);
    if (gameEngine != null) gameEngine.setUserReady(currentUser.getId());
  }

  @Override protected void addPoints(int points, String userId, boolean shouldBroadcast) {
    super.addPoints(points, userId, shouldBroadcast);

    if (userId.equals(currentUser.getId())) {
      roundPoints += points;
    }
  }

  protected void iLost() {
    if (game == null || gameEngine == null) return;

    if (roundPoints > 0) {
      onAddScore.onNext(Pair.create(game.getId(), roundPoints));
    }

    roundPoints = 0;
    webRTCRoom.sendToPeers(getILostPayload(currentUser.getId()), true);
    gameEngine.setUserGameOver(currentUser.getId());
    refactorPending(true);
  }

  protected void playerLost(String userId) {
    Timber.d("playerLost (userId : " + userId + " )");
    TribeGuest player = peerMap.get(userId);

    if (player != null) {
      if (player.getId().equals(currentUser.getId())) {
        onMessage.onNext(getContext().getString(
            StringUtils.stringWithPrefix(getContext(), wordingPrefix, "you_lost")));
      } else {
        onMessage.onNext(getContext().getString(
            StringUtils.stringWithPrefix(getContext(), wordingPrefix, "someone_lost"),
            player.getDisplayName()));
      }
    }
  }

  protected void gameOver(String winnerId, boolean isLocal) {
    Timber.d("gameOver (winnerId : " + winnerId + " )");

    if (subscriptionsSession != null) subscriptionsSession.clear();
    soundManager.playSound(SoundManager.GAME_PLAYER_LOST, SoundManager.SOUND_MAX);
    refactorPending(false);
    onMessage.onNext(null);
    changeMessageStatus(txtRestart, false, true, DURATION, 0, null, null);
    changeMessageStatus(txtMessage, false, true, DURATION, 0, null, null);

    TribeGuest player = peerMap.get(winnerId);

    if (player != null) {
      becomePlayer();

      if (player.getId().equals(currentUser.getId())) {
        showMessage(getResources().getString(
            StringUtils.stringWithPrefix(getContext(), wordingPrefix,
                gameEngine.mapPlayerStatus.size() > 1 ? "you_won" : "you_lost")), 250, null,
            () -> becomeGameMaster());
      } else {
        showMessage(getResources().getString(
            StringUtils.stringWithPrefix(getContext(), wordingPrefix, "someone_won"),
            player.getDisplayName()), 250, null, null);
      }
    } else {
      resetScores(false);

      showMessage("Game Over", 250, null, () -> {
        becomePlayer();
        if (isLocal) {
          becomeGameMaster();
        }
      });
    }
  }

  protected void changeMessageStatus(View view, boolean isVisible, boolean isAnimated, int duration,
      int delay, LabelListener blockToCall, LabelListener completionListener) {
    if (view == null) {
      if (blockToCall != null) blockToCall.call();
      if (completionListener != null) completionListener.call();
      return;
    }

    float alpha = isVisible ? 1.0f : 0.0f;
    float translationY = isVisible ? 0.0f : screenUtils.dpToPx(30.0f);
    float translationX = 0.0f;

    if (!isAnimated) {
      if (blockToCall != null) blockToCall.call();
      view.setAlpha(alpha);
      view.setTranslationY(translationY);
      if (completionListener != null) completionListener.call();
      return;
    }

    ViewPropertyAnimator animator = view.animate()
        .alpha(alpha)
        .translationY(translationY)
        .translationX(translationX)
        .setStartDelay(delay)
        .setInterpolator(new OvershootInterpolator());

    if (blockToCall != null || completionListener != null) {
      animator.setListener(new AnimatorListenerAdapter() {
        @Override public void onAnimationStart(Animator animation) {
          if (blockToCall != null) blockToCall.call();
        }

        @Override public void onAnimationEnd(Animator animation) {
          if (completionListener != null) completionListener.call();
        }
      });
    } else {
      animator.setListener(null);
    }

    animator.setDuration(duration).start();
  }

  protected void showMessage(String text, int duration, LabelListener willDisappear,
      LabelListener completionListener) {
    TextViewFont textViewFont = new TextViewFont(getContext());
    FrameLayout.LayoutParams paramsMessage =
        new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    paramsMessage.gravity = Gravity.CENTER;

    TextViewCompat.setTextAppearance(textViewFont, R.style.GameBody_1_White);
    textViewFont.setCustomFont(context, getStyleFont());
    textViewFont.setText(text);
    textViewFont.setAllCaps(true);
    textViewFont.setAlpha(0);
    addView(textViewFont, paramsMessage);

    txtMessage = textViewFont;

    changeMessageStatus(txtMessage, false, false, 100, 0, null, null);
    changeMessageStatus(txtMessage, true, true, duration, 0, null,
        () -> changeMessageStatus(txtMessage, false, true, duration, 1000, willDisappear, () -> {
          if (txtMessage != null) removeView(txtMessage);
          if (completionListener != null) completionListener.call();
        }));
  }

  protected void setupGameLocally(String userId, Set<String> players, long timestamp) {
    Timber.d("setupGameLocally : " + userId + " / players : " + players);
    currentMasterId = userId;
    game.setCurrentMaster(peerMap.get(userId));
    game.incrementRoundCount();
    listenMessages();
    resetStatuses();
    refactorPending(false);
    showTitle(() -> showMessage(getResources().getString(
        StringUtils.stringWithPrefix(getContext(), wordingPrefix, "lets_go")), 250, () -> {
      if (GameViewWithEngine.this == null || (GameViewWithEngine.this instanceof GameWebView)) {
        return;
      }

      long now = System.nanoTime() * 1000;
      if (timestamp > System.currentTimeMillis()) {
        subscriptions.add(Observable.timer(timestamp - now, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(aLong -> {
              playGame();
            }));
      } else {
        playGame();
      }
    }, null), getStyleFont());

    if (this instanceof GameWebView) {
      subscriptions.add(onGameReady.subscribe(gameReady -> {
        long now = System.currentTimeMillis();
        if (timestamp > System.currentTimeMillis()) {
          subscriptions.add(Observable.timer(timestamp - now, TimeUnit.MILLISECONDS)
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(aLong -> playGame()));
        } else {
          playGame();
        }
      }));
    }
  }

  protected void playGame() {
    Timber.d("playGame");

    if (gameEngine != null) {
      gameEngine.start();

      if (gameEngine.mapPlayerStatus.size() <= 1) {
        if (txtRestart != null && invitedMap.size() > 0) {
          txtRestart.setText(
              StringUtils.stringWithPrefix(getContext(), wordingPrefix, "waiting_instructions"));
          changeMessageStatus(txtRestart, true, true, DURATION, 0, null, null);
        }
      } else {
        txtRestart.setText(
            StringUtils.stringWithPrefix(getContext(), wordingPrefix, "pending_instructions"));
      }
    }
  }

  private void listenMessages() {
    subscriptionsSession.add(Observable.combineLatest(onPending.distinctUntilChanged(),
        onMessage.distinctUntilChanged((s, s2) -> s.equals(s2)), (t1, t2) -> Pair.create(t1, t2))
        .buffer(750, TimeUnit.MILLISECONDS, 2)
        .onBackpressureDrop()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(values -> {
          if (values.size() == 0) return;

          boolean isPending = values.get(values.size() - 1).first;
          Set<String> textList = new HashSet<>();

          for (Pair<Boolean, String> pair : values) {
            textList.add(pair.second);
          }

          int duration = 250;
          if (textList.size() > 0) {
            String merged = "";
            int count = 0;
            for (String str : textList) {
              merged += str + (count < textList.size() - 1 ? "\\n" : "");
              count++;
            }
            changeMessageStatus(txtRestart, false, true, duration, 0, null, null);
            showMessage(merged, duration,
                () -> changeMessageStatus(txtRestart, isPending, true, duration, 0, null, null),
                null);
          } else {
            changeMessageStatus(txtRestart, isPending, true, duration, 0, null, null);
          }
        }));
  }

  private void refactorPending(boolean pending) {
    Timber.d("Refactor pending : " + pending);
    this.pending = pending;
    this.onPending.onNext(pending);
  }

  protected void refactorReady(boolean ready) {
    Timber.d("Refactor ready : " + ready);
    this.ready = ready;
    this.onGameReady.onNext(ready);
  }

  protected void showTitle(LabelListener listener, String styleFontType) {
    int translation = screenUtils.dpToPx(30.0f);

    int titleResId = StringUtils.stringWithPrefix(getContext(), wordingPrefix, "title");
    String title = titleResId == 0 ? "" : getContext().getString(titleResId);

    TextViewFont txtTitleBG = new TextViewFont(getContext());
    FrameLayout.LayoutParams paramsTitleBG =
        new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    paramsTitleBG.gravity = Gravity.CENTER;
    txtTitleBG.setGravity(Gravity.CENTER);

    TextViewCompat.setTextAppearance(txtTitleBG, R.style.GameTitle_1_White40);
    txtTitleBG.setCustomFont(context, styleFontType);
    txtTitleBG.setText(title);
    txtTitleBG.setAllCaps(true);
    txtTitleBG.setAlpha(0);
    txtTitleBG.setTranslationX(translation);
    txtTitleBG.setTranslationY(translation);
    addView(txtTitleBG, paramsTitleBG);

    TextViewFont txtTitleFront = new TextViewFont(getContext());
    FrameLayout.LayoutParams paramsTitleFront =
        new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    paramsTitleFront.gravity = Gravity.CENTER;
    txtTitleFront.setGravity(Gravity.CENTER);
    TextViewCompat.setTextAppearance(txtTitleFront, R.style.GameTitle_1_White);
    txtTitleFront.setCustomFont(context, styleFontType);
    txtTitleFront.setText(title);
    txtTitleFront.setAllCaps(true);
    txtTitleFront.setAlpha(0);
    txtTitleFront.setTranslationX(translation);
    txtTitleFront.setTranslationY(translation);
    addView(txtTitleFront, paramsTitleFront);

    int duration = 250;

    changeMessageStatus(txtTitleBG, true, true, duration, 0, null, null);
    changeMessageStatus(txtTitleFront, true, true, duration, 0, null, null);

    subscriptions.add(Observable.timer(1500, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          int translate = screenUtils.dpToPx(4);
          txtTitleBG.animate()
              .translationX(translate)
              .translationY(translate)
              .setDuration(duration)
              .setStartDelay(0)
              .setInterpolator(new DecelerateInterpolator())
              .start();

          subscriptions.add(Observable.timer(1500, TimeUnit.MILLISECONDS)
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(aLong1 -> txtTitleBG.animate()
                  .translationX(0)
                  .translationY(0)
                  .setDuration(duration)
                  .setStartDelay(0)
                  .setInterpolator(new DecelerateInterpolator())
                  .setListener(new AnimatorListenerAdapter() {
                    @Override public void onAnimationEnd(Animator animation) {
                      animation.removeAllListeners();

                      if (listener != null) listener.call();

                      txtTitleBG.animate()
                          .setDuration(0)
                          .setListener(null)
                          .setStartDelay(0)
                          .start();

                      changeMessageStatus(txtTitleFront, false, true, duration, 0, null, null);
                      changeMessageStatus(txtTitleBG, false, true, duration, 0, null, () -> {
                        removeView(txtTitleBG);
                        removeView(txtTitleFront);
                      });
                    }
                  })
                  .start()));
        }));
  }

  /**
   * JSON PAYLOADS
   */

  private JSONObject getLostPayload(String userId) {
    JSONObject obj = new JSONObject();
    JSONObject game = new JSONObject();
    JsonUtils.jsonPut(game, ACTION_KEY, ACTION_SHOW_USER_LOST);
    JsonUtils.jsonPut(game, USER_KEY, userId);
    JsonUtils.jsonPut(obj, this.game.getId(), game);
    return obj;
  }

  private JSONObject getImReadyPayload(String userId) {
    JSONObject obj = new JSONObject();
    JSONObject game = new JSONObject();
    JsonUtils.jsonPut(game, ACTION_KEY, ACTION_USER_READY);
    JsonUtils.jsonPut(game, USER_KEY, userId);
    JsonUtils.jsonPut(obj, this.game.getId(), game);
    return obj;
  }

  private JSONObject getILostPayload(String userId) {
    JSONObject obj = new JSONObject();
    JSONObject game = new JSONObject();
    JsonUtils.jsonPut(game, ACTION_KEY, ACTION_USER_GAME_OVER);
    JsonUtils.jsonPut(game, USER_KEY, userId);
    JsonUtils.jsonPut(obj, this.game.getId(), game);
    return obj;
  }

  private JSONObject getGameOverPayload(String userId) {
    JSONObject obj = new JSONObject();
    JSONObject game = new JSONObject();
    JsonUtils.jsonPut(game, ACTION_KEY, ACTION_GAME_OVER);
    JsonUtils.jsonPut(game, USER_KEY, userId);
    JsonUtils.jsonPut(obj, this.game.getId(), game);
    return obj;
  }

  private JSONObject getNewGamePayload(String userId, long timestamp, String[] playerIds) {
    JSONObject obj = new JSONObject();
    JSONObject game = new JSONObject();
    JsonUtils.jsonPut(game, ACTION_KEY, ACTION_NEW_GAME);
    JsonUtils.jsonPut(game, FROM_KEY, userId);
    JSONArray jsonArray = new JSONArray();
    for (String id : playerIds) jsonArray.put(id);
    JsonUtils.jsonPut(game, PLAYERS, jsonArray);
    JsonUtils.jsonPut(game, TIMESTAMP, Long.valueOf(timestamp).doubleValue() / 1000);
    JsonUtils.jsonPut(obj, this.game.getId(), game);
    return obj;
  }

  /**
   * PUBLIC
   */

  @Override public void start(Game game,
      Observable<ObservableRxHashMap.RxHashMap<String, TribeGuest>> masterMapObs,
      Observable<Map<String, TribeGuest>> mapObservable,
      Observable<Map<String, TribeGuest>> mapInvitedObservable,
      Observable<Map<String, LiveStreamView>> liveViewsObservable, String userId) {
    Timber.d("start : " + userId);
    super.start(game, masterMapObs, mapObservable, mapInvitedObservable, liveViewsObservable,
        userId);

    txtRestart = new TextViewFont(getContext());
    FrameLayout.LayoutParams paramsRestart =
        new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    paramsRestart.gravity = Gravity.CENTER;

    TextViewCompat.setTextAppearance(txtRestart, R.style.GameBody_1_White40);
    txtRestart.setCustomFont(context, getStyleFont());
    txtRestart.setText(
        StringUtils.stringWithPrefix(getContext(), wordingPrefix, "pending_instructions"));
    txtRestart.setAllCaps(true);
    txtRestart.setAlpha(0);
    txtRestart.setGravity(Gravity.CENTER);
    addView(txtRestart, paramsRestart);

    subscriptions.add(Observable.timer(500, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          becomePlayer();
          if (userId.equals(currentUser.getId())) becomeGameMaster();
        }));

    if (getSoundtrack() != -1) soundManager.playSound(getSoundtrack(), SoundManager.SOUND_MAX);
  }

  public void stop() {
    Timber.d("stop");
    if (gameEngine != null) {
      if (gameEngine.mapPlayerStatus.size() > 1 && roundPoints > 0) {
        onAddScore.onNext(Pair.create(game.getId(), roundPoints));
      }

      roundPoints = 0;

      gameEngine.stop();
    }

    soundManager.cancelMediaPlayer();
    super.stop();
  }

  @Override public void resetScores(boolean shouldSendGameOver) {
    super.resetScores(shouldSendGameOver);
    if (shouldSendGameOver) {
      webRTCRoom.sendToPeers(getGameOverPayload(""), true);
      gameOver("", true);
    }
  }

  public void dispose() {
    super.dispose();

    if (txtMessage != null) {
      txtMessage.clearAnimation();
      txtMessage.animate().setDuration(0).setListener(null).start();
    }

    if (txtRestart != null) {
      txtRestart.clearAnimation();
      txtRestart.animate().setDuration(0).setListener(null).start();
    }
  }

  /**
   * OBSERVABLE
   */

}
