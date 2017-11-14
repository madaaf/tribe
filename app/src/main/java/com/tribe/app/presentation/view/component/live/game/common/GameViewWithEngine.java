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
import com.tribe.app.presentation.utils.FontUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.component.live.LiveStreamView;
import com.tribe.app.presentation.view.component.live.game.web.GameWebView;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.util.JsonUtils;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Created by tiago on 10/30/2017.
 */

public abstract class GameViewWithEngine extends GameViewWithRanking {

  private static final int DURATION = 250;
  private static final int DELAY = 250;
  private static final float OVERSHOOT = 1.25f;

  private static final String ACTION_NEW_GAME = "newGame";
  private static final String ACTION_USER_GAME_OVER = "userGameOver";
  private static final String ACTION_USER_WAITING = "userWaiting";
  private static final String ACTION_SHOW_USER_LOST = "showUserLost";
  private static final String ACTION_GAME_OVER = "gameOver";

  private static final String PLAYERS = "players";
  private static final String TIMESTAMP = "timestamp";

  // VARIABLES
  protected GameEngine gameEngine;
  protected TextViewFont txtRestart, txtMessage;
  protected String wordingPrefix = "";
  protected boolean pending = false;

  // OBSERVABLES
  protected PublishSubject<Boolean> onPending = PublishSubject.create();
  protected PublishSubject<Boolean> onGameReady = PublishSubject.create();
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

  protected long startGameTimestamp() {
    return System.currentTimeMillis() + 5 * 1000;
  }

  protected void becomePlayer() {
    Timber.d("becomePlayer");
    stopEngine();
    startEngine();
  }

  protected void becomeGameMaster() {
    Timber.d("becomeGameMaster");
    if (subscriptionsSession != null) subscriptionsSession.clear();

    startMasterEngine();

    Map<String, Integer> mapPlayerStatus = gameEngine.getMapPlayerStatus();
    long timestamp = startGameTimestamp();
    Set<String> playerIds = mapPlayerStatus.keySet();
    webRTCRoom.sendToPeers(getNewGamePayload(currentUser.getId(), timestamp,
        playerIds.toArray(new String[playerIds.size()])), true);
    setupGameLocally(currentUser.getId(), playerIds, timestamp);
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
        .subscribe(jsonObject -> {
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
                  setupGameLocally(fromUserId, players, message.getLong(TIMESTAMP));
                } else if (actionKey.equals(ACTION_SHOW_USER_LOST)) {
                  playerLost(message.getString(USER_KEY));
                } else if (actionKey.equals(ACTION_GAME_OVER)) {
                  gameOver(message.getString(USER_KEY));
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
      gameOver(winnerId);
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
        changeMessageStatus(txtRestart, true, true, 100, 0, null);
        refactorPending(true);
      }
    }
  }

  protected void iLost() {
    Timber.d("iLost");
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

  protected void gameOver(String winnerId) {
    Timber.d("gameOver (winnerId : " + winnerId + " )");

    if (subscriptionsSession != null) subscriptionsSession.clear();

    refactorPending(false);
    onMessage.onNext(null);
    changeMessageStatus(txtRestart, false, true, DURATION, 0, null);
    changeMessageStatus(txtMessage, false, true, DURATION, 0, null);

    TribeGuest player = peerMap.get(winnerId);

    if (player != null) {
      becomePlayer();

      if (player.getId().equals(currentUser.getId())) {
        showMessage(getResources().getString(
            StringUtils.stringWithPrefix(getContext(), wordingPrefix, "you_won")), 250,
            new LabelListener() {
              @Override public void onStart() {
              }

              @Override public void onEnd() {
                becomeGameMaster();
              }
            });
      } else {
        showMessage(getResources().getString(
            StringUtils.stringWithPrefix(getContext(), wordingPrefix, "someone_won"),
            player.getDisplayName()), 250, null);
      }
    } else {
      becomePlayer();
      showMessage("Game Over", 250, new LabelListener() {
        @Override public void onStart() {

        }

        @Override public void onEnd() {
          if (gameEngine != null) {
            resetScores();
            becomeGameMaster();
          }
        }
      });
    }
  }

  protected void changeMessageStatus(View view, boolean isVisible, boolean isAnimated, int duration,
      int delay, LabelListener listener) {
    if (view == null) {
      if (listener != null) listener.onEnd();
      return;
    }

    float alpha = isVisible ? 1.0f : 0.0f;
    float translationY = isVisible ? 0.0f : screenUtils.dpToPx(30.0f);
    float translationX = 0.0f;

    if (!isAnimated) {
      if (listener != null) listener.onStart();
      view.setAlpha(alpha);
      view.setTranslationY(translationY);
      if (listener != null) listener.onEnd();
      return;
    }

    ViewPropertyAnimator animator = view.animate()
        .alpha(alpha)
        .translationY(translationY)
        .translationX(translationX)
        .setStartDelay(delay)
        .setInterpolator(new OvershootInterpolator());

    if (listener != null) {
      animator.setListener(new AnimatorListenerAdapter() {
        @Override public void onAnimationStart(Animator animation) {
          listener.onStart();
        }

        @Override public void onAnimationEnd(Animator animation) {
          listener.onEnd();
        }
      });
    } else {
      animator.setListener(null);
    }

    animator.setDuration(duration).start();
  }

  protected void showMessage(String text, int duration, LabelListener completionListener) {
    TextViewFont textViewFont = new TextViewFont(getContext());
    FrameLayout.LayoutParams paramsMessage =
        new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    paramsMessage.gravity = Gravity.CENTER;

    TextViewCompat.setTextAppearance(textViewFont, R.style.GameBody_1_White);
    textViewFont.setCustomFont(context, FontUtils.GULKAVE_REGULAR);
    textViewFont.setText(text);
    textViewFont.setAllCaps(true);
    textViewFont.setAlpha(0);
    addView(textViewFont, paramsMessage);

    txtMessage = textViewFont;

    changeMessageStatus(txtMessage, false, false, 100, 0, null);
    changeMessageStatus(txtMessage, true, true, duration, 0, new LabelListener() {
      @Override public void onStart() {
      }

      @Override public void onEnd() {
        changeMessageStatus(txtMessage, false, true, duration, 1000, new LabelListener() {
          @Override public void onStart() {
          }

          @Override public void onEnd() {
            if (completionListener != null) completionListener.onEnd();
            removeView(txtMessage);
          }
        });
      }
    });
  }

  protected interface LabelListener {
    void onStart();

    void onEnd();
  }

  protected void setupGameLocally(String userId, Set<String> players, long timestamp) {
    Timber.d("setupGameLocally : " + userId + " / players : " + players);
    currentMasterId = userId;
    listenMessages();
    resetStatuses();
    refactorPending(false);
    showTitle(new LabelListener() {
      @Override public void onStart() {
      }

      @Override public void onEnd() {
        showMessage(getResources().getString(
            StringUtils.stringWithPrefix(getContext(), wordingPrefix, "lets_go")), 250,
            new LabelListener() {
              @Override public void onStart() {

              }

              @Override public void onEnd() {
                if (GameViewWithEngine.this == null ||
                    (GameViewWithEngine.this instanceof GameWebView)) {
                  return;
                }

                long now = System.currentTimeMillis();
                if (timestamp > System.currentTimeMillis()) {
                  subscriptions.add(Observable.timer(timestamp - now, TimeUnit.MILLISECONDS)
                      .observeOn(AndroidSchedulers.mainThread())
                      .subscribe(aLong -> playGame()));
                } else {
                  playGame();
                }
              }
            });
      }
    });
  }

  private void playGame() {
    Timber.d("playGame");
    gameEngine.start();
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
            changeMessageStatus(txtRestart, false, true, duration, 0, null);
            showMessage(merged, duration, new LabelListener() {
              @Override public void onStart() {
              }

              @Override public void onEnd() {
                changeMessageStatus(txtRestart, isPending, true, duration, 0, null);
              }
            });
          } else {
            changeMessageStatus(txtRestart, isPending, true, duration, 0, null);
          }
        }));
  }

  private void refactorPending(boolean pending) {
    Timber.d("Refactor pending : " + pending);
    this.pending = pending;
    this.onPending.onNext(pending);
  }

  private void showTitle(LabelListener listener) {
    int translation = screenUtils.dpToPx(30.0f);

    int titleResId = StringUtils.stringWithPrefix(getContext(), wordingPrefix, "title");
    String title = titleResId == 0 ? "" : getContext().getString(titleResId);

    TextViewFont txtTitleBG = new TextViewFont(getContext());
    FrameLayout.LayoutParams paramsTitleBG =
        new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    paramsTitleBG.gravity = Gravity.CENTER;

    TextViewCompat.setTextAppearance(txtTitleBG, R.style.GameTitle_1_White40);
    txtTitleBG.setCustomFont(context, FontUtils.GULKAVE_REGULAR);
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

    TextViewCompat.setTextAppearance(txtTitleFront, R.style.GameTitle_1_White);
    txtTitleFront.setCustomFont(context, FontUtils.GULKAVE_REGULAR);
    txtTitleFront.setText(title);
    txtTitleFront.setAllCaps(true);
    txtTitleFront.setAlpha(0);
    txtTitleFront.setTranslationX(translation);
    txtTitleFront.setTranslationY(translation);
    addView(txtTitleFront, paramsTitleFront);

    int duration = 250;

    changeMessageStatus(txtTitleBG, true, true, duration, 0, null);
    changeMessageStatus(txtTitleFront, true, true, duration, 0, null);

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

                      if (listener != null) listener.onEnd();

                      txtTitleBG.animate()
                          .setDuration(0)
                          .setListener(null)
                          .setStartDelay(0)
                          .start();

                      changeMessageStatus(txtTitleFront, false, true, duration, 0, null);
                      changeMessageStatus(txtTitleBG, false, true, duration, 0,
                          new LabelListener() {
                            @Override public void onStart() {

                            }

                            @Override public void onEnd() {
                              removeView(txtTitleBG);
                              removeView(txtTitleFront);
                            }
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
    JsonUtils.jsonPut(game, PLAYERS, playerIds);
    JsonUtils.jsonPut(game, TIMESTAMP, timestamp);
    JsonUtils.jsonPut(obj, this.game.getId(), game);
    return obj;
  }

  /**
   * PUBLIC
   */

  @Override public void start(Game game, Observable<Map<String, TribeGuest>> mapObservable,
      Observable<Map<String, LiveStreamView>> liveViewsObservable, String userId) {
    Timber.d("start : " + userId);
    super.start(game, mapObservable, liveViewsObservable, userId);

    txtRestart = new TextViewFont(getContext());
    FrameLayout.LayoutParams paramsRestart =
        new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    paramsRestart.gravity = Gravity.CENTER;

    TextViewCompat.setTextAppearance(txtRestart, R.style.GameBody_1_White40);
    txtRestart.setCustomFont(context, FontUtils.GULKAVE_REGULAR);
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
    super.stop();
    if (gameEngine != null) gameEngine.stop();
    soundManager.cancelMediaPlayer();
  }

  public void dispose() {
    super.dispose();
  }

  /**
   * OBSERVABLE
   */
}
