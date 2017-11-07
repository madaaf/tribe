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
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.presentation.view.component.live.game.common.GameEngine;
import com.tribe.app.presentation.view.component.live.game.common.GameViewWithEngine;
import com.tribe.tribelivesdk.model.TribeGuest;
import java.util.Map;
import rx.Observable;

/**
 * Created by tiago on 10/31/2017.
 */

public class GameAliensAttackView extends GameViewWithEngine {

  @BindView(R.id.viewBackground) GameAliensAttackBackground viewBackground;

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

  @Override public void dispose() {
    super.dispose();
    viewBackground.dispose();
  }

  @Override public void setNextGame() {

  }

  @Override protected void initWebRTCRoomSubscriptions() {

  }

  @Override protected void takeOverGame() {

  }

  private void animateAlien(GameAliensAttackAlienView alienView) {
    alienView.setId(View.generateViewId());
    FrameLayout.LayoutParams params =
        new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT);
    params.gravity = Gravity.TOP;
    params.topMargin = -(screenUtils.getHeightPx() / 6);
    alienView.setScaleX(alienView.getStartScale());
    alienView.setScaleY(alienView.getStartScale());
    addView(alienView, params);

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

            ValueAnimator animatorTranslation = ObjectAnimator.ofFloat(alienView, "translationY",
                screenUtils.getHeightPx() + params.topMargin -
                    viewBackground.getRoadBottomMargin());
            animatorTranslation.setDuration((long) (alienView.getSpeed() * 1000));
            animatorTranslation.setInterpolator(new AccelerateInterpolator());
            animatorTranslation.addListener(new AnimatorListenerAdapter() {
              @Override public void onAnimationEnd(Animator animation) {
                animation.removeAllListeners();
                animatorRotation.cancel();
                animatorRotation.end();
                animatorTranslation.cancel();

                alienView.lost();
              }
            });
            animatorTranslation.start();
          }
        });

    alienView.setOnClickListener(view -> alienView.animateKill());
  }

  @Override protected void startMasterEngine() {
    super.startMasterEngine();

    subscriptions.add(((GameAliensAttackEngine) gameEngine).onAlien().subscribe(alienView -> {
      // TODO
    }));
  }

  /**
   * PUBLIC
   */

  @Override public void start(Observable<Map<String, TribeGuest>> mapObservable, String userId) {
    wordingPrefix = "game_aliens_attack_";
    super.start(mapObservable, userId);
    viewBackground.start();
    subscriptions.add(((GameAliensAttackEngine) gameEngine).onAlien()
        .subscribe(gameAliensAttackAlienView -> animateAlien(gameAliensAttackAlienView)));
  }

  @Override public void stop() {
    super.stop();
    viewBackground.stop();
  }

  /**
   * OBSERVABLES
   */

}
