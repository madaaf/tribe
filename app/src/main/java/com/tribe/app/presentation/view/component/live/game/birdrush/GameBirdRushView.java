package com.tribe.app.presentation.view.component.live.game.birdrush;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.view.component.live.LiveStreamView;
import com.tribe.app.presentation.view.component.live.game.aliensattack.GameAliensAttackEngine;
import com.tribe.app.presentation.view.component.live.game.common.GameEngine;
import com.tribe.app.presentation.view.component.live.game.common.GameViewWithEngine;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.model.TribeGuest;
import java.util.Map;
import javax.inject.Inject;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by tiago on 10/31/2017.
 */

public class GameBirdRushView extends GameViewWithEngine {

  private static final Long SPEED_BACK_SCROLL = 5000L;
  private boolean startedAsSingle = false, didRestartWhenReady = false;

  @BindView(R.id.background_one) ImageView backgroundOne;
  @BindView(R.id.background_two) ImageView backgroundTwo;
  @BindView(R.id.bird) ImageView bird;

  @Inject ScreenUtils screenUtils;

  private ValueAnimator animator;
  private BirdController controller;

  private Drawable[] birds = new Drawable[] {
      ContextCompat.getDrawable(getContext(), R.drawable.game_bird1),
      ContextCompat.getDrawable(getContext(), R.drawable.game_bird2),
      ContextCompat.getDrawable(getContext(), R.drawable.game_bird3),
      ContextCompat.getDrawable(getContext(), R.drawable.game_bird4),
      ContextCompat.getDrawable(getContext(), R.drawable.game_bird5),
      ContextCompat.getDrawable(getContext(), R.drawable.game_bird6),
      ContextCompat.getDrawable(getContext(), R.drawable.game_bird7),
      ContextCompat.getDrawable(getContext(), R.drawable.game_bird8)
  };

  // OBSERVABLES
  protected CompositeSubscription subscriptions;

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
    setBackScrolling();
    controller = new BirdController(context);

    initSubscriptions();
    down();
    setOnTouchListener(controller);
  }

  @Override protected void onFinishInflate() {

    super.onFinishInflate();
  }

  private void initSubscriptions() {
    subscriptions = new CompositeSubscription();
    Timber.e("ON ");
    subscriptions.add(controller.onTap().subscribe(aVoid -> {
      jump();
    }));
  }

  public void animateBurd() {

  }

  ValueAnimator va;

  public void jump() {

    if (va != null) {
      va.cancel();
      // bird.clearAnimation();
    }
    va = ValueAnimator.ofFloat(bird.getY(), bird.getY() - 100);
    va.setDuration(100);
    va.addUpdateListener(animation -> {
      Float value = (float) animation.getAnimatedValue();
      Timber.e("SOEF VALUE " + value + " " + bird.getTop());
      bird.setY(value);
    });
    va.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        down();
      }
    });
    //va.addUpdateListener() ;
    va.start();
  }

  public void down() {
    if (va != null) {
      va.cancel();
      // bird.clearAnimation();
    }
    
    va = ValueAnimator.ofFloat(bird.getY(), bird.getY() + screenUtils.getHeightPx());
    va.setDuration(1000);
    va.setInterpolator(new AccelerateInterpolator());
    va.addUpdateListener(animation -> {
      Float value = (float) animation.getAnimatedValue();
      Timber.e("SOEF VALUE " + value + " " + bird.getTop() + " " + screenUtils.getHeightPx());
      bird.setY(value);
    });

    //va.addUpdateListener() ;
    va.start();
  }

  private void setBackScrolling() {
    Timber.e("SOEF ");
    getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override public void onGlobalLayout() {
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
        animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(SPEED_BACK_SCROLL);
        animator.addUpdateListener(animation -> {
          final float progress = (float) animation.getAnimatedValue();
          final float width = backgroundOne.getWidth();
          final float translationX = -width * progress;
          backgroundOne.setTranslationX(translationX);
          backgroundTwo.setTranslationX(translationX + width);
        });
        animator.start();
      }
    });
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

  protected void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  @Override public void start(Game game, Observable<Map<String, TribeGuest>> mapObservable,
      Observable<Map<String, LiveStreamView>> liveViewsObservable, String userId) {
    wordingPrefix = "game_aliens_attack_";
  }

  @Override public void stop() {
    super.stop();
  }

  @Override public void dispose() {
    Timber.e(" DISPOSE");
    subscriptions.unsubscribe();
    super.dispose();
  }

  @Override public void setNextGame() {

  }

  /**
   * OBSERVABLES
   */

}
