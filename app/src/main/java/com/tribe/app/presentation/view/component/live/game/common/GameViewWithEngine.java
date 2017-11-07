package com.tribe.app.presentation.view.component.live.game.common;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.TextViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import com.tribe.app.R;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.util.JsonUtils;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 10/30/2017.
 */

public abstract class GameViewWithEngine extends GameViewWithRanking {

  private static final int DURATION = 300;
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
    //setupGameLocally(currentUser.getId(), playerIds, timestamp);
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

    subscriptions.add(gameEngine.onPlayerPending.subscribe(userId -> {
      setStatus(RankingStatus.PENDING, userId);
      playerPending(userId);
    }));
  }

  protected void stopEngine() {
    gameEngine.stop();
    gameEngine = null;
  }

  private void playerPending(String userId) {
    TribeGuest player = peerMap.get(userId);

    if (player != null) {
      if (player.getId().equals(currentUser.getId())) {
        changeMessageStatus(txtRestart, true, true, 0, null);
        onPending.onNext(true);
      }
    }
  }

  private void playerLost(String userId) {
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

  private void gameOver(String winnerId) {
    onPending.onNext(false);
    changeMessageStatus(txtRestart, false, true, DELAY, null);
    changeMessageStatus(txtMessage, false, true, DELAY, null);

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

  private void changeMessageStatus(View view, boolean isVisible, boolean isAnimated, int delay,
      LabelListener listener) {
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

    animator.setDuration(DURATION).start();
  }

  private void showMessage(String text, int delay, LabelListener completionListener) {
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

    changeMessageStatus(txtMessage, false, false, 0, null);
    changeMessageStatus(txtMessage, true, true, delay, new LabelListener() {
      @Override public void onStart() {
      }

      @Override public void onEnd() {
        changeMessageStatus(txtMessage, false, true, 0, new LabelListener() {
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

  /**
   * JSON PAYLOADS
   */

  private JSONObject getLostPayload(String userId) {
    JSONObject obj = new JSONObject();
    JSONObject game = new JSONObject();
    JsonUtils.jsonPut(game, ACTION_KEY, ACTION_SHOW_USER_LOST);
    JsonUtils.jsonPut(game, USER_KEY, userId);
    JsonUtils.jsonPut(obj, this.game.getName(), game);
    return obj;
  }

  private JSONObject getGameOverPayload(String userId) {
    JSONObject obj = new JSONObject();
    JSONObject game = new JSONObject();
    JsonUtils.jsonPut(game, ACTION_KEY, ACTION_GAME_OVER);
    JsonUtils.jsonPut(game, USER_KEY, userId);
    JsonUtils.jsonPut(obj, this.game.getName(), game);
    return obj;
  }

  private JSONObject getNewGamePayload(String userId, long timestamp, String[] playerIds) {
    JSONObject obj = new JSONObject();
    JSONObject game = new JSONObject();
    JsonUtils.jsonPut(game, ACTION_KEY, ACTION_NEW_GAME);
    JsonUtils.jsonPut(game, FROM_KEY, userId);
    JsonUtils.jsonPut(game, PLAYERS, playerIds);
    JsonUtils.jsonPut(game, TIMESTAMP, timestamp);
    JsonUtils.jsonPut(obj, this.game.getName(), game);
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
