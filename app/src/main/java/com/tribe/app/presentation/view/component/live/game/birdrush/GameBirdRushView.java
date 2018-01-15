package com.tribe.app.presentation.view.component.live.game.birdrush;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.presentation.view.component.live.LiveStreamView;
import com.tribe.app.presentation.view.component.live.game.aliensattack.GameAliensAttackEngine;
import com.tribe.app.presentation.view.component.live.game.common.GameEngine;
import com.tribe.app.presentation.view.component.live.game.common.GameViewWithEngine;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.model.TribeGuest;
import java.util.Map;
import rx.Observable;
import timber.log.Timber;

/**
 * Created by tiago on 10/31/2017.
 */

public class GameBirdRushView extends GameViewWithEngine {

  // VARIABLES
  private boolean startedAsSingle = false, didRestartWhenReady = false;

  final ImageView backgroundOne = (ImageView) findViewById(R.id.background_one);
  final ImageView backgroundTwo = (ImageView) findViewById(R.id.background_two);

  final ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 1.0f);

  public GameBirdRushView(@NonNull Context context) {
    super(context);
  }

  public GameBirdRushView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void initView(Context context) {
    super.initView(context);

    inflater.inflate(R.layout.view_game_bird_rush, this, true);
    unbinder = ButterKnife.bind(this);
/*
    animator.setRepeatCount(ValueAnimator.INFINITE);
    animator.setInterpolator(new LinearInterpolator());
    animator.setDuration(10000L);
    animator.addUpdateListener(animation -> {
      final float progress = (float) animation.getAnimatedValue();
      final float width = backgroundOne.getWidth();
      final float translationX = width * progress;
      backgroundOne.setTranslationX(translationX);
      backgroundTwo.setTranslationX(translationX - width);
    });
    animator.start();*/
  }

  @Override protected GameEngine generateEngine() {
    return new GameAliensAttackEngine(context);
  }

  @Override protected int getSoundtrack() {
    return SoundManager.ALIENS_ATTACK_SOUNDTRACK;
  }

  @Override protected void initWebRTCRoomSubscriptions() {
    super.initWebRTCRoomSubscriptions();
  }

  @Override protected void gameOver(String winnerId, boolean isLocal) {
    Timber.d("Game over : " + winnerId);
    super.gameOver(winnerId, isLocal);
  }

  private void killAlien() {
    if (!pending) {
      addPoints(1, currentUser.getId(), true);
    }
  }

  @Override protected void startMasterEngine() {
    super.startMasterEngine();
  }

  /**
   * JSON PAYLOAD
   */

  /**
   * PUBLIC
   */

  @Override public void start(Game game, Observable<Map<String, TribeGuest>> mapObservable,
      Observable<Map<String, LiveStreamView>> liveViewsObservable, String userId) {
    wordingPrefix = "game_aliens_attack_";
  }

  @Override public void stop() {
    super.stop();
  }

  @Override public void dispose() {
    super.dispose();
  }

  @Override public void setNextGame() {

  }

  /**
   * OBSERVABLES
   */

}
