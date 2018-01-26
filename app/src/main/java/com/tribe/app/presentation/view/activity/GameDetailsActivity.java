package com.tribe.app.presentation.view.activity;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.transition.ChangeBounds;
import android.support.transition.Transition;
import android.support.transition.TransitionListenerAdapter;
import android.support.transition.TransitionManager;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.bumptech.glide.Glide;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Score;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.components.UserComponent;
import com.tribe.app.presentation.mvp.presenter.GamePresenter;
import com.tribe.app.presentation.mvp.view.adapter.GameMVPViewAdapter;
import com.tribe.app.presentation.view.utils.GlideUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class GameDetailsActivity extends BaseActivity {

  private static final int DURATION_MOVING = 2500;
  private static final int DURATION = 300;

  public static final String GAME_ID = "game_id";

  public static Intent getCallingIntent(Activity activity, String gameId) {
    Intent intent = new Intent(activity, GameDetailsActivity.class);
    intent.putExtra(GAME_ID, gameId);
    return intent;
  }

  @Inject ScreenUtils screenUtils;

  @Inject GamePresenter gamePresenter;

  @BindView(R.id.imgBackgroundGradient) View imgBackgroundGradient;
  @BindView(R.id.imgBackgroundLogo) ImageView imgBackgroundLogo;
  @BindView(R.id.imgIcon) ImageView imgIcon;
  @BindView(R.id.imgLogo) ImageView imgLogo;
  @BindView(R.id.txtBaseline) TextViewFont txtBaseline;
  @BindView(R.id.imgRays) ImageView imgRays;
  @BindView(R.id.layoutConstraint) ConstraintLayout layoutConstraint;
  @BindView(R.id.imgAnimation1) ImageView imgAnimation1;
  @BindView(R.id.imgAnimation2) ImageView imgAnimation2;
  @BindView(R.id.imgAnimation3) ImageView imgAnimation3;
  @BindView(R.id.avatarMyScore) ImageView avatarMyScore;
  @BindView(R.id.txtMyScoreScore) TextViewFont txtMyScoreScore;
  @BindView(R.id.txtMyScoreDesc) TextViewFont txtMyScoreDesc;
  @BindView(R.id.avatarBestScore) ImageView avatarBestScore;
  @BindView(R.id.txtBestScoreScore) TextViewFont txtBestScoreScore;
  @BindView(R.id.txtBestScoreDesc) TextViewFont txtBestScoreDesc;

  // VARIABLES
  private UserComponent userComponent;
  private GameManager gameManager;
  private GameMVPViewAdapter gameMVPViewAdapter;
  private Game game;
  private Map<String, ValueAnimator> mapAnimator;

  // RESOURCES

  // OBSERVABLES
  private CompositeSubscription subscriptions;

  @Override protected void onCreate(Bundle savedInstanceState) {
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_game_details);

    ButterKnife.bind(this);

    initDependencyInjector();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      layoutConstraint.setPadding(0, getStatusBarHeight(), 0, 0);
    }

    gameManager = GameManager.getInstance(this);
    game = gameManager.getGameById(getIntent().getStringExtra(GAME_ID));
    mapAnimator = new HashMap<>();

    initPresenter();
    initSubscriptions();
    initUI();
  }

  @Override protected void onStart() {
    super.onStart();
    gamePresenter.onViewAttached(gameMVPViewAdapter);
  }

  @Override protected void onStop() {
    super.onStop();
    gamePresenter.onViewDetached();
  }

  @Override protected void onDestroy() {
    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    imgRays.clearAnimation();
    for (ValueAnimator animator : mapAnimator.values()) animator.cancel();
    super.onDestroy();
  }

  private int getStatusBarHeight() {
    int result = 0;
    int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
    if (resourceId > 0) {
      result = getResources().getDimensionPixelSize(resourceId);
    }
    return result;
  }

  private int getNavigationBarHeight() {
    return screenUtils.dpToPx(50);
  }

  private void initPresenter() {
    gameMVPViewAdapter = new GameMVPViewAdapter() {
      @Override public Context context() {
        return GameDetailsActivity.this;
      }
    };
  }

  private void initSubscriptions() {
    subscriptions = new CompositeSubscription();
  }

  private void initUI() {
    txtBaseline.setText(game.getBaseline());

    new GlideUtils.GameImageBuilder(this, screenUtils).url(game.getIcon())
        .hasBorder(false)
        .hasPlaceholder(true)
        .rounded(true)
        .target(imgIcon)
        .load();

    Glide.with(this).load(game.getLogo()).into(imgLogo);
    Glide.with(this).load(game.getBackground()).into(imgBackgroundLogo);

    GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] {
        Color.parseColor("#" + game.getPrimary_color()),
        Color.parseColor("#" + game.getSecondary_color())
    });

    ViewCompat.setBackground(imgBackgroundGradient, gd);

    for (int i = 0; i < game.getAnimation_icons().size(); i++) {
      String url = game.getAnimation_icons().get(i);
      ImageView imageView = null;

      if (i == 0) {
        imageView = imgAnimation1;
      } else if (i == 1) {
        imageView = imgAnimation2;
      } else if (i == 2) {
        imageView = imgAnimation3;
      }

      Glide.with(this).load(url).into(imageView);
    }

    animateImg(imgAnimation1, true);
    animateImg(imgAnimation1, false);
    animateImg(imgAnimation2, true);
    animateImg(imgAnimation2, false);
    animateImg(imgAnimation3, true);
    animateImg(imgAnimation3, false);

    subscriptions.add(Observable.timer(500, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          showImgAnimations();
          animateRays();
        }));

    subscriptions.add(Observable.timer(1000, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> showButtons()));

    Score score = getCurrentUser().getScoreForGame(game.getId());
    if (score != null) {
      txtMyScoreScore.setText(score.getValue() + getString(R.string.leaderboards_points));
      txtMyScoreDesc.setText(R.string.leaderboards_your_score);
    } else {
      txtMyScoreScore.setText("0 " + getString(R.string.leaderboards_points));
      txtMyScoreDesc.setText(R.string.leaderboards_no_score);
    }

    new GlideUtils.Builder(this).url(getCurrentUser().getProfilePicture())
        .hasPlaceholder(true)
        .target(avatarMyScore)
        .load();

    if (game.getFriendLeader() != null) {
      int leaderScore = game.getFriendLeader().getScoreValue();
      txtBestScoreScore.setText(leaderScore + getString(R.string.leaderboards_points));
      txtBestScoreDesc.setText(R.string.leaderboards_score);

      new GlideUtils.Builder(this).url(game.getFriendLeader().getPicture())
          .hasPlaceholder(true)
          .target(avatarBestScore)
          .load();
    }
  }

  private void initDependencyInjector() {
    this.userComponent = DaggerUserComponent.builder()
        .applicationComponent(getApplicationComponent())
        .activityModule(getActivityModule())
        .build();

    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  private void animateRays() {
    RotateAnimation rotate =
        new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
            0.5f);
    rotate.setDuration(30000);
    rotate.setRepeatCount(Animation.INFINITE);
    rotate.setFillAfter(true);
    imgRays.startAnimation(rotate);
  }

  private void showImgAnimations() {
    imgAnimation1.animate()
        .scaleX(1)
        .scaleY(1)
        .setInterpolator(new DecelerateInterpolator())
        .setDuration(DURATION)
        .start();
    imgAnimation2.animate()
        .scaleX(1)
        .scaleY(1)
        .setInterpolator(new DecelerateInterpolator())
        .setDuration(DURATION)
        .start();
    imgAnimation3.animate()
        .scaleX(1)
        .scaleY(1)
        .setInterpolator(new DecelerateInterpolator())
        .setDuration(DURATION)
        .start();
  }

  private void showButtons() {
    ConstraintSet set = new ConstraintSet();
    set.clone(layoutConstraint);
    set.connect(R.id.btnSingle, ConstraintSet.BOTTOM, R.id.btnMulti, ConstraintSet.TOP);
    set.clear(R.id.btnSingle, ConstraintSet.TOP);
    set.connect(R.id.btnMulti, ConstraintSet.BOTTOM, layoutConstraint.getId(),
        ConstraintSet.BOTTOM);
    set.clear(R.id.btnMulti, ConstraintSet.TOP);
    set.setAlpha(R.id.btnSingle, 1);
    set.setAlpha(R.id.btnMulti, 1);
    animateLayoutWithConstraintSet(set, new TransitionListenerAdapter() {
      @Override public void onTransitionEnd(@NonNull Transition transition) {
        showScores();
      }
    });
  }

  private void showScores() {
    ConstraintSet set = new ConstraintSet();
    set.clone(layoutConstraint);
    set.connect(R.id.cardAvatarMyScore, ConstraintSet.BOTTOM, R.id.btnSingle, ConstraintSet.TOP);
    set.clear(R.id.cardAvatarMyScore, ConstraintSet.TOP);

    if (game.getFriendLeader() != null) {
      set.clear(R.id.imgConnect, ConstraintSet.TOP);
      set.connect(R.id.imgConnect, ConstraintSet.BOTTOM, R.id.cardAvatarMyScore, ConstraintSet.TOP);
      set.clear(R.id.cardAvatarBestScore, ConstraintSet.TOP);
      set.connect(R.id.cardAvatarBestScore, ConstraintSet.BOTTOM, R.id.imgConnect,
          ConstraintSet.TOP);
    }

    animateLayoutWithConstraintSet(set, null);
  }

  private void animateLayoutWithConstraintSet(ConstraintSet constraintSet,
      Transition.TransitionListener transitionListener) {
    Transition transition = new ChangeBounds();
    transition.setDuration(DURATION);
    transition.setInterpolator(new OvershootInterpolator(0.75f));
    if (transitionListener != null) transition.addListener(transitionListener);
    TransitionManager.beginDelayedTransition(layoutConstraint, transition);
    constraintSet.applyTo(layoutConstraint);
  }

  private void animateImg(ImageView imgAnimation, boolean isX) {
    int rdm = new Random().nextInt(50) - 25;

    ValueAnimator animator = mapAnimator.get(imgAnimation.getId());
    if (animator != null) {
      animator.cancel();
    }

    animator = ValueAnimator.ofInt(0, screenUtils.dpToPx(rdm));
    animator.setDuration(DURATION_MOVING);
    animator.setRepeatCount(ValueAnimator.INFINITE);
    animator.setRepeatMode(ValueAnimator.REVERSE);
    animator.addUpdateListener(animation -> {
      int translation = (int) animation.getAnimatedValue();
      if (isX) {
        imgAnimation.setTranslationX(translation);
      } else {
        imgAnimation.setTranslationY(translation);
      }
    });
    animator.start();
  }

  /**
   * ONCLICK
   */

  @OnClick(R.id.btnClose) void close() {
    finish();
  }

  @OnClick(R.id.btnLeaderboards) void openLeaderboards() {
    // TODO open leaderboards
  }

  /**
   * PUBLIC
   */

  @Override public void finish() {
    super.finish();
    overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
  }

  /**
   * OBSERVABLES
   */
}
