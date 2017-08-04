package com.tribe.app.presentation.view.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.ViewPagerScroller;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameChallenge;
import com.tribe.tribelivesdk.game.GameManager;
import java.lang.reflect.Field;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by madaaflak on 19/07/2017.
 */

public class GameChallengesView extends FrameLayout {

  private static int DURATION_EXIT_POPUP = 300;

  @Inject User user;

  @BindView(R.id.pager) ViewPager viewpager;
  @BindView(R.id.popupChallenge) FrameLayout popup;

  private LayoutInflater inflater;
  private Unbinder unbinder;
  private Context context;
  private GameChallengeViewPagerAdapter adapter;
  private boolean popupDisplayed = false;
  private GameManager gameManager;

  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<GameChallenge> onNextChallenge = PublishSubject.create();
  private PublishSubject<Boolean> onBlockOpenInviteView = PublishSubject.create();
  private PublishSubject<Game> onCurrentGame = PublishSubject.create();

  public GameChallengesView(@NonNull Context context) {
    super(context);
    initView(context);
  }

  public GameChallengesView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(context);
  }

  private void initView(Context context) {
    this.context = context;
    initDependencyInjector();
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_game_challenges, this, true);
    unbinder = ButterKnife.bind(this);
    gameManager = GameManager.getInstance(context);

    adapter = new GameChallengeViewPagerAdapter(context, user);
    viewpager.setAdapter(adapter);

    viewpager.setOnTouchListener((v, event) -> {
      if (popupDisplayed) hidePopup();
      return true;
    });
    changePagerScroller();

    subscriptions.add(adapter.onBlockOpenInviteView().subscribe(onBlockOpenInviteView));
    subscriptions.add(adapter.onCurrentGame().subscribe(onCurrentGame));
  }

  public void close() {
    adapter = null;
  }

  public void setNextChallenge() {
    if (adapter == null) initView(context);
    if (popupDisplayed) hidePopup();
    new Handler().post(() -> {
      int currentItem = (viewpager.getCurrentItem() + 1);
      viewpager.setCurrentItem(currentItem);
      setVisibility(VISIBLE);
    });
  }

  private void changePagerScroller() {
    try {
      Field mScroller = null;
      mScroller = ViewPager.class.getDeclaredField("mScroller");
      mScroller.setAccessible(true);
      ViewPagerScroller scroller =
          new ViewPagerScroller(viewpager.getContext(), new OvershootInterpolator(1.3f));
      mScroller.set(viewpager, scroller);
    } catch (Exception e) {
      Timber.e("error of change scroller " + e);
    }
  }

  private void hidePopup() {
    popupDisplayed = false;
    ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(popup, "scaleX", 0f);
    ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(popup, "scaleY", 0f);
    scaleDownX.setDuration(DURATION_EXIT_POPUP);
    scaleDownY.setDuration(DURATION_EXIT_POPUP);

    AnimatorSet scaleDown = new AnimatorSet();
    scaleDown.play(scaleDownX).with(scaleDownY);
    scaleDownX.addUpdateListener(valueAnimator -> {
      float value = (float) valueAnimator.getAnimatedValue();
      popup.setAlpha(value);
    });

    scaleDownX.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        popup.setVisibility(View.INVISIBLE);
      }
    });
    scaleDown.start();
  }

  public void displayPopup() {
    popup.setVisibility(VISIBLE);
    popup.setAlpha(1);
    popupDisplayed = true;
    SpringSystem springSystem = SpringSystem.create();
    Spring spring = springSystem.createSpring();
    SpringConfig config = new SpringConfig(400, 20);
    spring.setSpringConfig(config);

    spring.addListener(new SimpleSpringListener() {
      @Override public void onSpringUpdate(Spring spring) {
        float value = (float) spring.getCurrentValue();
        popup.setScaleX(value);
        popup.setScaleY(value);
      }
    });
    spring.setEndValue(1);
  }

  protected void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) getContext()));
  }

  public Observable<GameChallenge> onNextChallenge() {
    return onNextChallenge;
  }

  public Observable<Boolean> onBlockOpenInviteView() {
    return onBlockOpenInviteView;
  }

  public Observable<Game> onCurrentGame() {
    return onCurrentGame;
  }
}
