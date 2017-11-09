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
import com.tribe.app.presentation.view.widget.TextViewFont;
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
import rx.subscriptions.CompositeSubscription;

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

  protected abstract GameEngine generateEngine();

  protected long startGameTimestamp() {
    return System.currentTimeMillis() + 5 * 1000;
  }

  protected void becomePlayer() {
    stopEngine();
    startEngine();
  }

  protected void becomeGameMaster() {
    startMasterEngine();

    Map<String, Integer> mapPlayerStatus = gameEngine.getMapPlayerStatus();
    long timestamp = startGameTimestamp();
    Set<String> playerIds = mapPlayerStatus.keySet();
    webRTCRoom.sendToPeers(getNewGamePayload(currentUser.getId(), timestamp,
        playerIds.toArray(new String[playerIds.size()])), true);
    setupGameLocally(currentUser.getId(), playerIds, timestamp);
  }

  @Override protected void takeOverGame() {
    startMasterEngine();
    gameEngine.checkGameOver();
  }

  @Override protected void initWebRTCRoomSubscriptions() {
    subscriptionsRoom.add(webRTCRoom.onGameMessage().subscribe(jsonObject -> {
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
    gameEngine = generateEngine();
    gameEngine.initPeerMapObservable(peerMapObservable);

    subscriptions.add(gameEngine.onPlayerPending.subscribe(userId -> {
      setStatus(RankingStatus.PENDING, userId);
      playerPending(userId);
    }));
  }

  protected void stopEngine() {
    gameEngine.stop();
    gameEngine = null;
  }

  protected void playerPending(String userId) {
    TribeGuest player = peerMap.get(userId);

    if (player != null) {
      if (player.getId().equals(currentUser.getId())) {
        changeMessageStatus(txtRestart, true, true, 100, 0, null);
        refactorPending(true);
      }
    }
  }

  protected void iLost() {
    webRTCRoom.sendToPeers(getILostPayload(currentUser.getId()), true);
    gameEngine.setUserGameOver(currentUser.getId());
    refactorPending(true);
  }

  protected void playerLost(String userId) {
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
    refactorPending(false);
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
    }
  }

  protected void changeMessageStatus(View view, boolean isVisible, boolean isAnimated, int duration,
      int delay, LabelListener listener) {
    float alpha = isVisible ? 1.0f : 0.0f;
    float translationY = isVisible ? 0.0f : screenUtils.dpToPx(30.0f);

    if (!isAnimated) {
      listener.onStart();
      view.setAlpha(alpha);
      view.setTranslationY(translationY);
      listener.onEnd();
      return;
    }

    ViewPropertyAnimator animator = view.animate()
        .alpha(alpha)
        .translationY(translationY)
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
    }

    animator.setDuration(duration).start();
  }

  protected void showMessage(String text, int duration, LabelListener completionListener) {
    txtMessage = new TextViewFont(getContext());
    FrameLayout.LayoutParams paramsMessage =
        new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    paramsMessage.gravity = Gravity.CENTER;

    TextViewCompat.setTextAppearance(txtRestart, R.style.GameBody_1_White);
    txtMessage.setText(text);
    txtRestart.setAllCaps(true);
    txtRestart.setAlpha(0);
    addView(txtRestart, paramsMessage);

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
            txtMessage = null;
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
    currentMasterId = userId;
    refactorPending(false);
    listenMessages();
    resetStatuses();
    showTitle(new LabelListener() {
      @Override public void onStart() {
      }

      @Override public void onEnd() {
        showMessage(getResources().getString(
            StringUtils.stringWithPrefix(getContext(), wordingPrefix, "lets_go")), 250, null);
      }
    });

    subscriptions.add(onGameReady.single().subscribe(aBoolean -> {
      playGame();
    }));
  }

  private void playGame() {
    gameEngine.start();
  }

  private void listenMessages() {
    if (subscriptionsSession != null) subscriptionsSession.clear();
    subscriptionsSession = new CompositeSubscription();

    subscriptionsSession.add(Observable.combineLatest(onPending.distinctUntilChanged(),
        onMessage.distinctUntilChanged((s, s2) -> s.equals(s2)), (t1, t2) -> Pair.create(t1, t2))
        .buffer(750, TimeUnit.MILLISECONDS, 0)
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
            for (String str : textList) merged += str + "\\n";
            changeMessageStatus(txtRestart, false, true, duration, 0, null);
            showMessage(merged, duration, new LabelListener() {
              @Override public void onStart() {
                changeMessageStatus(txtRestart, isPending, true, duration, 0, null);
              }

              @Override public void onEnd() {

              }
            });
          } else {
            changeMessageStatus(txtRestart, isPending, true, duration, 0, null);
          }
        }));
  }

  private void refactorPending(boolean pending) {
    this.pending = pending;
    this.onPending.onNext(pending);
  }

  private void showTitle(LabelListener listener) {
    int translation = screenUtils.dpToPx(30.0f);

    TextViewFont txtTitleBG = new TextViewFont(getContext());
    FrameLayout.LayoutParams paramsTitleBG =
        new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    paramsTitleBG.gravity = Gravity.CENTER;

    TextViewCompat.setTextAppearance(txtTitleBG, R.style.GameBody_1_White);
    txtTitleBG.setText(StringUtils.stringWithPrefix(getContext(), wordingPrefix, "title"));
    txtTitleBG.setAllCaps(true);
    txtTitleBG.setAlpha(0);
    setTranslationX(translation);
    setTranslationY(translation);
    addView(txtTitleBG, paramsTitleBG);

    TextViewFont txtTitleFront = new TextViewFont(getContext());
    FrameLayout.LayoutParams paramsTitleFront =
        new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    paramsTitleFront.gravity = Gravity.CENTER;

    TextViewCompat.setTextAppearance(txtTitleFront, R.style.GameBody_1_White);
    txtTitleFront.setText(StringUtils.stringWithPrefix(getContext(), wordingPrefix, "title"));
    txtTitleFront.setAllCaps(true);
    txtTitleFront.setAlpha(0);
    setTranslationX(translation);
    setTranslationY(translation);
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
              .translationY(0)
              .setDuration(duration)
              .setStartDelay(0)
              .setInterpolator(new DecelerateInterpolator())
              .start();

          subscriptions.add(Observable.timer(1500, TimeUnit.MILLISECONDS)
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(aLong1 -> {
                txtTitleBG.animate()
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
                    .start();
              }));
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

  public void start(Observable<Map<String, TribeGuest>> mapObservable, String userId) {
    super.start(mapObservable, userId);

    txtRestart = new TextViewFont(getContext());
    FrameLayout.LayoutParams paramsRestart =
        new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    paramsRestart.gravity = Gravity.CENTER;

    TextViewCompat.setTextAppearance(txtRestart, R.style.GameBody_1_White);
    txtRestart.setText(
        StringUtils.stringWithPrefix(getContext(), wordingPrefix, "pending_instructions"));
    txtRestart.setAllCaps(true);
    txtRestart.setAlpha(0);
    addView(txtRestart, paramsRestart);

    subscriptions.add(Observable.timer(500, TimeUnit.MILLISECONDS).subscribe(aLong -> {
      becomePlayer();
      if (userId.equals(currentUser.getId())) becomeGameMaster();
    }));
  }

  public void stop() {
    super.stop();
    gameEngine.stop();
    dispose();
  }

  public void dispose() {

  }

  /**
   * OBSERVABLE
   */
}
