package com.tribe.app.presentation.view.component.live.game.AliensAttack;

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
import com.tribe.app.presentation.view.component.live.game.common.GameEngine;
import com.tribe.app.presentation.view.component.live.game.common.GameViewWithEngine;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.util.JsonUtils;
import java.util.Map;
import java.util.Set;
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

  @Override protected void initWebRTCRoomSubscriptions() {
    super.initWebRTCRoomSubscriptions();
    subscriptionsRoom.add(webRTCRoom.onGameMessage()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(jsonObject -> {
          if (jsonObject.has(game.getId())) {
            try {
              JSONObject message = jsonObject.getJSONObject(game.getId());
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
          alienView.animate()
              .alpha(alpha)
              .setDuration(100)
              .setInterpolator(new DecelerateInterpolator())
              .start();
        }
      }
    }));
  }

  @Override protected void gameOver(String winnerId) {
    super.gameOver(winnerId);
    viewAliens.removeAllViews();
  }

  private void killAlien() {
    if (!pending) {
      addPoint(currentUser.getId(), true);
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
                .translationY(screenUtils.getHeightPx() + params.topMargin + alienView.getHeight() -
                    viewBackground.getRoadBottomMargin())
                .setDuration((long) (alienView.getSpeed() * 1000))
                .setStartDelay(0)
                .setInterpolator(new LinearInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                  @Override public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    Timber.d("Animation cancel : " + alienView.getId());
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
      killAlien();
      alienView.animateKill();
    });
  }

  @Override protected void startMasterEngine() {
    super.startMasterEngine();

    subscriptions.add(((GameAliensAttackEngine) gameEngine).onAlien().subscribe(alienView -> {
      webRTCRoom.sendToPeers(getAlienPayload(alienView.asJSON()), true);
      animateAlien(alienView);
    }));
  }

  /**
   * JSON PAYLOAD
   */

  private JSONObject getAlienPayload(JSONObject alienJson) {
    JSONObject obj = new JSONObject();
    JSONObject game = new JSONObject();
    JsonUtils.jsonPut(game, ACTION_KEY, ACTION_POP_ALIEN);
    JsonUtils.jsonPut(game, ALIEN_KEY, alienJson);
    JsonUtils.jsonPut(obj, this.game.getId(), game);
    return obj;
  }

  /**
   * PUBLIC
   */

  @Override public void start(Game game, Observable<Map<String, TribeGuest>> mapObservable, String userId) {
    wordingPrefix = "game_aliens_attack_";
    super.start(game, mapObservable, userId);
    viewBackground.start();
  }

  @Override public void stop() {
    super.stop();
    viewBackground.stop();
  }

  @Override public void dispose() {
    super.dispose();
    viewBackground.dispose();
  }

  @Override public void setNextGame() {

  }

  /**
   * OBSERVABLES
   */

}
