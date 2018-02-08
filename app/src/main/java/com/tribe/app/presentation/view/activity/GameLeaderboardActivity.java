package com.tribe.app.presentation.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.transition.ChangeBounds;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.bumptech.glide.Glide;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Score;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.mvp.presenter.GamePresenter;
import com.tribe.app.presentation.mvp.view.adapter.GameMVPViewAdapter;
import com.tribe.app.presentation.view.adapter.decorator.BaseListDividerDecoration;
import com.tribe.app.presentation.view.adapter.manager.LeaderboardDetailsLayoutManager;
import com.tribe.app.presentation.view.adapter.viewholder.LeaderboardDetailsAdapter;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.TextViewRanking;
import com.tribe.app.presentation.view.widget.avatar.NewAvatarView;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class GameLeaderboardActivity extends BaseActivity {

  private static final int DURATION = 500;
  private static final float OVERSHOOT = 1.25f;

  public static final String GAME_ID = "game_id";

  public static Intent getCallingIntent(Activity activity, String gameId) {
    Intent intent = new Intent(activity, GameLeaderboardActivity.class);
    intent.putExtra(GAME_ID, gameId);
    return intent;
  }

  @Inject ScreenUtils screenUtils;

  @Inject GamePresenter gamePresenter;

  @BindView(R.id.imgBackgroundGradient) View imgBackgroundGradient;

  @BindView(R.id.imgBackgroundLogo) ImageView imgBackgroundLogo;

  @BindView(R.id.txtTitle) TextViewFont txtTitle;

  @BindView(R.id.recyclerViewLeaderboard) RecyclerView recyclerView;

  @BindView(R.id.appBar) AppBarLayout appBarLayout;

  @BindView(R.id.collapsingToolbar) CollapsingToolbarLayout collapsingToolbar;

  @BindView(R.id.layoutConstraint) ConstraintLayout layoutConstraint;

  @BindView(R.id.layoutContent) LinearLayout layoutContent;

  @BindView(R.id.cardAvatarFirst) CardView cardAvatarFirst;

  @BindView(R.id.avatarFirst) NewAvatarView avatarFirst;

  @BindView(R.id.avatarEmptyFirst) ImageView avatarEmptyFirst;

  @BindView(R.id.txtNameFirst) TextViewFont txtNameFirst;

  @BindView(R.id.txtRankingFirst) TextViewRanking txtRankingFirst;

  @BindView(R.id.txtScoreFirst) TextViewFont txtScoreFirst;

  @BindView(R.id.cardAvatarSecond) CardView cardAvatarSecond;

  @BindView(R.id.avatarSecond) NewAvatarView avatarSecond;

  @BindView(R.id.avatarEmptySecond) ImageView avatarEmptySecond;

  @BindView(R.id.txtNameSecond) TextViewFont txtNameSecond;

  @BindView(R.id.txtRankingSecond) TextViewRanking txtRankingSecond;

  @BindView(R.id.txtScoreSecond) TextViewFont txtScoreSecond;

  @BindView(R.id.cardAvatarThird) CardView cardAvatarThird;

  @BindView(R.id.avatarThird) NewAvatarView avatarThird;

  @BindView(R.id.avatarEmptyThird) ImageView avatarEmptyThird;

  @BindView(R.id.txtNameThird) TextViewFont txtNameThird;

  @BindView(R.id.txtRankingThird) TextViewRanking txtRankingThird;

  @BindView(R.id.txtScoreThird) TextViewFont txtScoreThird;

  // VARIABLES
  private LeaderboardDetailsLayoutManager layoutManager;
  private LeaderboardDetailsAdapter adapter;
  private List<Score> items;
  private GameManager gameManager;
  private GameMVPViewAdapter gameMVPViewAdapter;
  private String gameId;
  private Game game;

  // RESOURCES

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  @Override protected void onCreate(Bundle savedInstanceState) {
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_game_leaderboards);

    ButterKnife.bind(this);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      layoutContent.setPadding(0, getStatusBarHeight(), 0, 0);
    }

    gameId = getIntent().getStringExtra(GAME_ID);

    gameManager = GameManager.getInstance(this);
    game = gameManager.getGameById(getIntent().getStringExtra(GAME_ID));
    items = new ArrayList<>();

    initDependencyInjector();
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
    super.onDestroy();
  }

  protected void initPresenter() {
    gameMVPViewAdapter = new GameMVPViewAdapter() {
      @Override public void onGameLeaderboard(List<Score> scoreList, boolean cloud) {
        if (scoreList == null) return;

        Collections.sort(scoreList, (o1, o2) -> ((Integer) o2.getValue()).compareTo(o1.getValue()));
        items.clear();

        for (Score score : scoreList) {
          // We add the current user if user == null, it means it's the current user's score
          if (score.getUser() == null) score.setUser(getCurrentUser());
        }

        Score first = scoreList.remove(0);
        setupPodium(1, first, avatarFirst, cardAvatarFirst, avatarEmptyFirst, txtNameFirst,
            txtRankingFirst, txtScoreFirst);

        Score second = null;
        if (scoreList.size() > 0) {
          second = scoreList.remove(0);
        }

        setupPodium(2, second, avatarSecond, cardAvatarSecond, avatarEmptySecond, txtNameSecond,
            txtRankingSecond, txtScoreSecond);

        Score third = null;
        if (scoreList.size() > 0) {
          third = scoreList.remove(0);
        }

        setupPodium(3, third, avatarThird, cardAvatarThird, avatarEmptyThird, txtNameThird,
            txtRankingThird, txtScoreThird);

        items.addAll(scoreList);

        if (items.size() == 0) {
          for (int i = 0; i < 10; i++) {
            Score score = new Score();
            score.setGame(game);
            items.add(score);
          }
        }

        adapter.setItems(items);

        showAvatars();
        showRecyclerView();

        subscriptions.add(Observable.timer((int) (DURATION * 0.3f), TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(aLong -> showRankings()));

        subscriptions.add(Observable.timer((int) (DURATION * 0.6f), TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(aLong -> showNames()));

        subscriptions.add(Observable.timer((int) (DURATION * 0.9f), TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(aLong -> showScores()));
      }

      @Override public Context context() {
        return GameLeaderboardActivity.this;
      }
    };
  }

  protected void initSubscriptions() {
    subscriptions = new CompositeSubscription();
  }

  private void initUI() {
    txtTitle.setText(game.getTitle());

    Glide.with(this).load(game.getBackground()).into(imgBackgroundLogo);

    GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.BL_TR, new int[] {
        Color.parseColor("#" + game.getPrimary_color()),
        Color.parseColor("#" + game.getSecondary_color())
    });

    ViewCompat.setBackground(imgBackgroundGradient, gd);

    recyclerView.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override public void onGlobalLayout() {
            recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            recyclerView.setTranslationY(recyclerView.getMeasuredHeight());
          }
        });
    layoutManager = new LeaderboardDetailsLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);
    adapter = new LeaderboardDetailsAdapter(this, recyclerView);
    recyclerView.setItemAnimator(null);
    recyclerView.setAdapter(adapter);
    recyclerView.addItemDecoration(
        new BaseListDividerDecoration(this, ContextCompat.getColor(this, R.color.white_opacity_10),
            screenUtils.dpToPx(0.25f)));

    adapter.setItems(items);

    gamePresenter.loadGameLeaderboard(gameId);

    appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
      float range = (float) -appBarLayout.getTotalScrollRange();
      layoutConstraint.setAlpha(1.0f - (float) verticalOffset / range);
    });
  }

  private void initDependencyInjector() {
    this.getApplicationComponent().inject(this);
  }

  private void setupPodium(int position, Score score, NewAvatarView avatar, CardView cardViewAvatar,
      ImageView avatarEmpty, TextViewFont txtName, TextViewRanking txtRanking,
      TextViewFont txtScore) {
    if (score == null) {
      avatarEmpty.setVisibility(View.VISIBLE);
      GradientDrawable drawable = new GradientDrawable();
      drawable.setShape(GradientDrawable.OVAL);
      drawable.setColor(Color.parseColor("#" + game.getPrimary_color()));
      avatarEmpty.setImageDrawable(drawable);
      cardViewAvatar.setVisibility(View.INVISIBLE);
      txtName.setBackgroundResource(R.drawable.bg_empty_podium_name);
      txtRanking.setVisibility(View.GONE);
      txtScore.setBackgroundResource(R.drawable.bg_empty_podium_score);
    } else {
      avatarEmpty.setVisibility(View.GONE);
      cardViewAvatar.setVisibility(View.VISIBLE);
      txtName.setBackground(null);
      txtRanking.setVisibility(View.VISIBLE);
      txtScore.setBackgroundResource(R.drawable.bg_pts);
      txtRanking.setTextColor(Color.parseColor("#" + game.getPrimary_color()));

      User user = score.getUser();
      avatar.load(user.getProfilePicture());
      txtName.setText(user.getDisplayName());
      txtRanking.setRanking(position);
      txtScore.setText("" + score.getValue());
    }
  }

  private void showAvatars() {
    ConstraintSet set = new ConstraintSet();
    set.clone(getApplicationContext(), R.layout.activity_game_leaderboards_final);
    animateLayoutWithConstraintSet(set, null);

    scaleUp(cardAvatarFirst);
    scaleUp(cardAvatarSecond);
    scaleUp(cardAvatarThird);
    scaleUp(avatarEmptyFirst);
    scaleUp(avatarEmptySecond);
    scaleUp(avatarEmptyThird);
  }

  private void showRankings() {
    scaleUp(txtRankingFirst);
    scaleUp(txtRankingSecond);
    scaleUp(txtRankingThird);
  }

  private void showNames() {
    scaleUp(txtNameFirst);
    scaleUp(txtNameSecond);
    scaleUp(txtNameThird);
  }

  private void showScores() {
    scaleUp(txtScoreFirst);
    scaleUp(txtScoreSecond);
    scaleUp(txtScoreThird);
  }

  private void showRecyclerView() {
    recyclerView.animate()
        .translationY(0)
        .setDuration(DURATION)
        .setInterpolator(new OvershootInterpolator(0.5f))
        .start();
  }

  private void scaleUp(View view) {
    view.setPivotX(view.getMeasuredWidth() >> 1);
    view.setPivotY(view.getMeasuredHeight() >> 1);

    view.animate()
        .scaleY(1)
        .scaleX(1)
        .setDuration(DURATION)
        .setInterpolator(new OvershootInterpolator(OVERSHOOT))
        .start();
  }

  private void animateLayoutWithConstraintSet(ConstraintSet constraintSet,
      Transition.TransitionListener transitionListener) {
    Transition transition = new ChangeBounds();
    transition.setDuration(DURATION);
    transition.setInterpolator(new OvershootInterpolator(OVERSHOOT));
    if (transitionListener != null) transition.addListener(transitionListener);
    TransitionManager.beginDelayedTransition(layoutConstraint, transition);
    constraintSet.applyTo(layoutConstraint);
  }

  /**
   * ONCLICK
   */

  @OnClick(R.id.btnBack) void back() {
    onBackPressed();
  }

  /**
   * PUBLIC
   */

  /**
   * OBSERVABLES
   */
}
