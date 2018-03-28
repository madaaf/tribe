package com.tribe.app.presentation.view.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.transition.ChangeBounds;
import android.support.transition.Transition;
import android.support.transition.TransitionListenerAdapter;
import android.support.transition.TransitionManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.bumptech.glide.Glide;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Score;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.components.UserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.mvp.presenter.GamePresenter;
import com.tribe.app.presentation.mvp.view.adapter.GameMVPViewAdapter;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.utils.analytics.TagManager;
import com.tribe.app.presentation.view.utils.GlideUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.TextViewRanking;
import com.tribe.app.presentation.view.widget.TextViewScore;
import com.tribe.app.presentation.view.widget.avatar.NewAvatarView;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameManager;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class GameDetailsView extends FrameLayout {

  private static final int DURATION = 400;
  public static final String GAME_ID = "game_id";

  @Inject ScreenUtils screenUtils;
  @Inject TagManager tagManager;
  @Inject User user;
  @Inject Navigator navigator;
  @Inject GamePresenter gamePresenter;

  @BindView(R.id.imgBackgroundGradient) View imgBackgroundGradient;
  @BindView(R.id.imgBackgroundLogo) ImageView imgBackgroundLogo;
  @BindView(R.id.imgIcon) ImageView imgIcon;
  @BindView(R.id.imgLogo) ImageView imgLogo;
  @BindView(R.id.txtBaseline) TextViewFont txtBaseline;
  @BindView(R.id.imgRays) ImageView imgRays;
  @BindView(R.id.layoutConstraint) ConstraintLayout layoutConstraint;
  /*
  @BindView(R.id.imgAnimation1) ImageView imgAnimation1;
  @BindView(R.id.imgAnimation2) ImageView imgAnimation2;
  @BindView(R.id.imgAnimation3) ImageView imgAnimation3;*/
  @BindView(R.id.cardAvatarMyScore) CardView cardAvatarMyScore;
  @BindView(R.id.avatarMyScore) NewAvatarView avatarMyScore;
  @BindView(R.id.txtMyScoreRanking) TextViewRanking txtMyScoreRanking;
  @BindView(R.id.txtMyScoreScore) TextViewScore txtMyScoreScore;
  @BindView(R.id.txtMyScoreName) TextViewFont txtMyScoreName;
  @BindView(R.id.leaderbordContainer) View leaderbordContainer;
  @BindView(R.id.leaderbordLabel) TextViewFont leaderbordLabel;
  @BindView(R.id.leaderbordPictoStart) ImageView leaderbordPictoStart;
  @BindView(R.id.leaderbordPictoEnd) ImageView leaderbordPictoEnd;
  @BindView(R.id.leaderbordSeparator) View leaderbordSeparator;

  // VARIABLES
  private UserComponent userComponent;
  private GameManager gameManager;
  private GameMVPViewAdapter gameMVPViewAdapter;
  private Game game;
  private Context context;

  protected LayoutInflater inflater;
  protected Unbinder unbinder;
  // RESOURCES

  // OBSERVABLES
  private CompositeSubscription subscriptions;

  public GameDetailsView(@NonNull Context context, Game game) {
    super(context);
    this.game = game;
    this.context = context;
    initView(context);
  }

  public GameDetailsView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(context);
  }

  private void initView(Context context) {
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.activity_game_details, this, true);
    unbinder = ButterKnife.bind(this);
    ButterKnife.bind(this);

    initDependencyInjector();

    init();
  }

  private void init() {
    gameManager = GameManager.getInstance(context);
    initPresenter();
    initSubscriptions();
    initUI();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    gamePresenter.onViewAttached(gameMVPViewAdapter);
  }

  @Override protected void onDetachedFromWindow() {
    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.clear();
    imgRays.clearAnimation();
    clearAnimation();
    gamePresenter.onViewDetached();
    super.onDetachedFromWindow();
  }

  private void initPresenter() {
    gameMVPViewAdapter = new GameMVPViewAdapter() {
      @Override public Context context() {
        return context;
      }
    };
  }

  private void initSubscriptions() {
    subscriptions = new CompositeSubscription();
  }

  private void resetView(View v) {
    v.setAlpha(0f);
    v.setTranslationX(0);
    v.setTranslationY(0);
    v.clearAnimation();
  }

  private void initLeaderbord() {
    resetView(leaderbordContainer);
    resetView(leaderbordLabel);
    resetView(leaderbordPictoStart);
    resetView(leaderbordPictoEnd);
    resetView(leaderbordSeparator);
  }

  private void initUI() {
    initLeaderbord();
    txtBaseline.setText(game.getBaseline());

    new GlideUtils.GameImageBuilder(context, screenUtils).url(game.getIcon())
        .hasBorder(false)
        .hasPlaceholder(true)
        .rounded(true)
        .target(imgIcon)
        .load();

    Glide.with(context).load(game.getLogo()).into(imgLogo);
    Glide.with(context).load(game.getBackground()).into(imgBackgroundLogo);

    GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TR_BL, new int[] {
        Color.parseColor("#" + game.getPrimary_color()),
        Color.parseColor("#" + game.getSecondary_color())
    });

    ViewCompat.setBackground(imgBackgroundGradient, gd);

    subscriptions.add(Observable.timer(500, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          animateRays();
        }));

    subscriptions.add(Observable.timer(1000, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> showButtons()));

    Score score = user.getScoreForGame(game.getId());
    if (score == null) {
      score = new Score();
      score.setValue(0);
    }

    txtMyScoreName.setText(user.getDisplayName());
    txtMyScoreScore.setScore(score.getValue());
    txtMyScoreRanking.setRanking(score.getRanking());
    avatarMyScore.load(user.getProfilePicture());
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

  private void animateRays() {
    RotateAnimation rotate =
        new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
            0.5f);
    rotate.setDuration(30000);
    rotate.setRepeatCount(Animation.INFINITE);
    rotate.setFillAfter(true);
    imgRays.startAnimation(rotate);
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
        if (game.hasScores()) showScores();
      }
    });
  }

  private void showScores() {
    animateViewEntry(cardAvatarMyScore);
    animateViewEntry(txtMyScoreName);
    animateViewEntry(txtMyScoreRanking);
    animateViewEntry(txtMyScoreScore);
    animateViewEntry(leaderbordContainer);
    animateViewEntry(leaderbordLabel);
    animateViewEntry(leaderbordPictoStart);
    animateViewEntry(leaderbordPictoEnd);
    animateViewEntry(leaderbordSeparator);
  }

  private void animateLayoutWithConstraintSet(ConstraintSet constraintSet,
      Transition.TransitionListener transitionListener) {
    Transition transition = new ChangeBounds();
    transition.setDuration(DURATION);
    transition.setInterpolator(new OvershootInterpolator(0.45f));
    if (transitionListener != null) transition.addListener(transitionListener);
    TransitionManager.beginDelayedTransition(layoutConstraint, transition);
    constraintSet.applyTo(layoutConstraint);
  }

  private void showView(View v) {
    v.animate()
        .alpha(1)
        .setDuration(DURATION)
        .setInterpolator(new DecelerateInterpolator())
        .start();
  }

  private void animateViewEntry(View v) {
    v.setTranslationY(screenUtils.dpToPx(50));
    v.animate()
        .translationY(0)
        .setDuration(DURATION)
        .setInterpolator(new OvershootInterpolator(0.45f))
        .start();
    showView(v);
  }

  /**
   * ONCLICK
   */

  @OnClick(R.id.leaderbordContainer) void onClickLeaderbordLabel() {
    navigator.navigateToGameLeaderboard((Activity) context, game.getId());
  }
}
