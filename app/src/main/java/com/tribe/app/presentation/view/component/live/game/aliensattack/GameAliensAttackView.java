package com.tribe.app.presentation.view.component.live.game.aliensattack;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.presentation.view.component.live.LiveStreamView;
import com.tribe.app.presentation.view.component.live.game.common.GameEngine;
import com.tribe.app.presentation.view.component.live.game.common.GameViewWithEngine;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.util.JsonUtils;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

/**
 * Created by tiago on 10/31/2017.
 */

public class GameAliensAttackView extends GameViewWithEngine {

  private static final String ACTION_POP_ALIEN = "popAlien";
  private static final String ALIEN_KEY = "alien";

  @BindView(R.id.viewBackground) GameAliensAttackBackground viewBackground;
  @BindView(R.id.viewAliens) FrameLayout viewAliens;

  // VARIABLES
  private boolean startedAsSingle = false, didRestartWhenReady = false;

  public GameAliensAttackView(@NonNull Context context) {
    super(context);
  }

  public GameAliensAttackView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void initView(Context context) {
    super.initView(context);

    inflater.inflate(R.layout.view_game_aliens_attack, this, true);
    unbinder = ButterKnife.bind(this);
  }

  @Override protected GameEngine generateEngine() {
    return new GameAliensAttackEngine(context);
  }

  @Override protected int getSoundtrack() {
    return SoundManager.ALIENS_ATTACK_SOUNDTRACK;
  }

  @Override protected void initWebRTCRoomSubscriptions() {
    super.initWebRTCRoomSubscriptions();
    subscriptionsRoom.add(webRTCRoom.onGameMessage()
        .onBackpressureDrop()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(pair -> {
          if (pair.second.has(game.getId())) {
            try {
              JSONObject message = pair.second.getJSONObject(game.getId());
              if (message.has(ACTION_KEY)) {
                String actionKey = message.getString(ACTION_KEY);
                if (actionKey.equals(ACTION_POP_ALIEN)) {
                  JSONObject alienJson = message.getJSONObject(ALIEN_KEY);
                  GameAliensAttackAlienView alienView =
                      new GameAliensAttackAlienView(getContext(), alienJson);
                  if (alienView != null) animateAlien(alienView);
                }
              }
            } catch (JSONException e) {
              e.printStackTrace();
            }
          }
        }));
  }

  protected void setupGameLocally(String userId, Set<String> players, long timestamp) {
    super.setupGameLocally(userId, players, timestamp);

    subscriptionsSession.add(onPending.subscribe(aBoolean -> {
      for (int i = 0; i < viewAliens.getChildCount(); i++) {
        if (viewAliens.getChildAt(i) instanceof GameAliensAttackAlienView) {
          GameAliensAttackAlienView alienView =
              (GameAliensAttackAlienView) viewAliens.getChildAt(i);
          float alpha = aBoolean && !alienView.isLost() ? 0.5f : 1f;
          alienView.setAlpha(alpha);
        }
      }
    }));
  }

  @Override protected void gameOver(String winnerId, boolean isLocal) {
    Timber.d("Game over : " + winnerId);
    super.gameOver(winnerId, isLocal);
    viewAliens.removeAllViews();
  }

  private void killAlien() {
    if (!pending) {
      addPoints(1, currentUser.getId(), true);
    }
  }

  private void animateAlien(GameAliensAttackAlienView alienView) {
    alienView.setId(View.generateViewId());
    FrameLayout.LayoutParams params =
        new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT);
    params.gravity = Gravity.TOP;
    params.topMargin = -(screenUtils.getHeightPx() / 5);
    alienView.setScaleX(alienView.getStartScale());
    alienView.setScaleY(alienView.getStartScale());
    viewAliens.addView(alienView, params);

    alienView.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override public void onGlobalLayout() {
            alienView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

            if (pending) alienView.setAlpha(0.5f);

            FrameLayout.LayoutParams params = (LayoutParams) alienView.getLayoutParams();
            int leftMargin = (int) (alienView.getStartX() * screenUtils.getWidthPx());
            if (leftMargin > (screenUtils.getWidthPx() - alienView.getMeasuredWidth())) {
              leftMargin = screenUtils.getWidthPx() - alienView.getMeasuredWidth();
            }
            params.leftMargin = leftMargin;
            alienView.setLayoutParams(params);

            ValueAnimator animatorRotation =
                ObjectAnimator.ofFloat(alienView.getAlienImageView(), "rotation",
                    -screenUtils.dpToPx(3), screenUtils.dpToPx(3));
            animatorRotation.setRepeatCount(ValueAnimator.INFINITE);
            animatorRotation.setRepeatMode(ValueAnimator.REVERSE);
            animatorRotation.setDuration(1000);
            animatorRotation.setInterpolator(new DecelerateInterpolator());
            animatorRotation.start();

            alienView.animate()
                .translationY(getMeasuredHeight() -
                    params.topMargin -
                    alienView.getHeight() -
                    viewBackground.getRoadBottomMargin())
                .setDuration((long) (alienView.getSpeed() * 1000))
                .setStartDelay(0)
                .setInterpolator(new LinearInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                  @Override public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    //Timber.d("Animation cancel : " + alienView.getBirdId());
                  }

                  @Override public void onAnimationEnd(Animator animation) {
                    animation.removeAllListeners();
                    animatorRotation.cancel();
                    alienView.clearAnimation();

                    Timber.d("Pending alien + " + alienView.getId() + " : " + pending);
                    if (pending) {
                      AnimationUtils.fadeOut(alienView, 250, new AnimatorListenerAdapter() {
                        @Override public void onAnimationEnd(Animator animation) {
                          animation.removeAllListeners();
                          viewAliens.removeView(alienView);
                        }
                      });

                      return;
                    }

                    alienView.lost();
                    iLost();
                  }
                })
                .start();
          }
        });

    alienView.setOnClickListener(view -> {
      soundManager.playSound(SoundManager.ALIENS_ATTACK_KILLED, SoundManager.SOUND_MAX);
      killAlien();
      alienView.animateKill();
    });
  }

  @Override protected void startMasterEngine() {
    super.startMasterEngine();

    subscriptionsSession.add(
        ((GameAliensAttackEngine) gameEngine).onAlien().subscribe(alienView -> {
          webRTCRoom.sendToPeers(getAlienPayload(alienView.asJSON()), true);
          animateAlien(alienView);
        }));

    subscriptions.add(Observable.timer(500, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          didRestartWhenReady = false;

          Map<String, Integer> mapPlayerStatus = gameEngine.getMapPlayerStatus();
          int countPlaying = 0;
          for (Integer i : mapPlayerStatus.values()) if (i == GameEngine.PLAYING) countPlaying++;

          startedAsSingle = gameEngine != null && countPlaying == 1;

          subscriptions.add(gameEngine.onPlayerReady().subscribe(userId -> {
            if (!didRestartWhenReady && startedAsSingle) {
              if (mapPlayerStatus.size() == 2) {
                didRestartWhenReady = true;
                startedAsSingle = false;

                subscriptions.add(Observable.timer(1000, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong1 -> {
                      resetScores(true);
                      iLost();
                    }));
              }
            }
          }));
        }));
  }

  /**
   * JSON PAYLOAD
   */

  private JSONObject getAlienPayload(JSONObject alienJson) {
    JSONObject obj = new JSONObject();
    JSONObject game = new JSONObject();
    JsonUtils.jsonPut(game, ACTION_KEY, ACTION_POP_ALIEN); // SOEF
    JsonUtils.jsonPut(game, ALIEN_KEY, alienJson);
    JsonUtils.jsonPut(obj, this.game.getId(), game);
    return obj;
  }

  /**
   * PUBLIC
   */

  @Override public void start(Game game, Observable<Map<String, TribeGuest>> mapObservable,
      Observable<Map<String, TribeGuest>> mapInvitedObservable,
      Observable<Map<String, LiveStreamView>> liveViewsObservable, String userId) {
    wordingPrefix = "game_aliens_attack_";
    super.start(game, mapObservable, mapInvitedObservable, liveViewsObservable, userId);
    viewBackground.start();

    subscriptions.add(Observable.timer(500, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> imReady()));
  }

  @Override public void stop() {
    super.stop();
    viewBackground.stop();
  }

  @Override public void dispose() {
    super.dispose();
    viewBackground.dispose();
    viewAliens.removeAllViews();
  }

  @Override public void setNextGame() {

  }

  /**
   * OBSERVABLES
   */

}
