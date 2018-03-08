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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import com.bumptech.glide.Glide;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Score;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.mvp.presenter.GamePresenter;
import com.tribe.app.presentation.mvp.presenter.MessagePresenter;
import com.tribe.app.presentation.mvp.view.adapter.GameMVPViewAdapter;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.preferences.PokeUserGame;
import com.tribe.app.presentation.view.ShortcutUtil;
import com.tribe.app.presentation.view.adapter.decorator.BaseListDividerDecoration;
import com.tribe.app.presentation.view.adapter.manager.LeaderboardDetailsLayoutManager;
import com.tribe.app.presentation.view.adapter.viewholder.LeaderboardDetailsAdapter;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.StateManager;
import com.tribe.app.presentation.view.widget.EmojiPoke;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.TextViewRanking;
import com.tribe.app.presentation.view.widget.TextViewScore;
import com.tribe.app.presentation.view.widget.avatar.NewAvatarView;
import com.tribe.app.presentation.view.widget.chat.model.MessagePoke;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class GameLeaderboardActivity extends BaseBroadcastReceiverActivity {

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

  @Inject MessagePresenter messagePresenter;

  @Inject StateManager stateManager;

  @BindView(R.id.imgBackgroundGradient) View imgBackgroundGradient;

  @BindView(R.id.imgBackgroundLogo) ImageView imgBackgroundLogo;

  @BindView(R.id.txtTitle) TextViewFont txtTitle;

  @BindView(R.id.recyclerViewLeaderboard) RecyclerView recyclerView;

  @BindView(R.id.appBar) AppBarLayout appBarLayout;

  @BindView(R.id.collapsingToolbar) CollapsingToolbarLayout collapsingToolbar;

  @BindView(R.id.layoutConstraint) ConstraintLayout layoutConstraint;

  @BindView(R.id.layoutContent) LinearLayout layoutContent;

  @BindView(R.id.container) FrameLayout container;

  @BindView(R.id.topBar) FrameLayout topBar;

  @BindView(R.id.cardAvatarFirst) CardView cardAvatarFirst;

  @BindView(R.id.avatarFirst) NewAvatarView avatarFirst;

  @BindView(R.id.avatarEmptyFirst) ImageView avatarEmptyFirst;

  @BindView(R.id.txtNameFirst) TextViewFont txtNameFirst;

  @BindView(R.id.txtRankingFirst) TextViewRanking txtRankingFirst;

  @BindView(R.id.txtScoreFirst) TextViewScore txtScoreFirst;

  @BindView(R.id.cardAvatarSecond) CardView cardAvatarSecond;

  @BindView(R.id.avatarSecond) NewAvatarView avatarSecond;

  @BindView(R.id.avatarEmptySecond) ImageView avatarEmptySecond;

  @BindView(R.id.txtNameSecond) TextViewFont txtNameSecond;

  @BindView(R.id.txtRankingSecond) TextViewRanking txtRankingSecond;

  @BindView(R.id.txtScoreSecond) TextViewScore txtScoreSecond;

  @BindView(R.id.cardAvatarThird) CardView cardAvatarThird;

  @BindView(R.id.avatarThird) NewAvatarView avatarThird;

  @BindView(R.id.avatarEmptyThird) ImageView avatarEmptyThird;

  @BindView(R.id.txtNameThird) TextViewFont txtNameThird;

  @BindView(R.id.txtRankingThird) TextViewRanking txtRankingThird;

  @BindView(R.id.txtScoreThird) TextViewScore txtScoreThird;

  @Inject @PokeUserGame Preference<String> pokeUserGame;

  // VARIABLES
  private LeaderboardDetailsLayoutManager layoutManager;
  private LeaderboardDetailsAdapter adapter;
  private List<Score> items;
  private GameManager gameManager;
  private GameMVPViewAdapter gameMVPViewAdapter;
  private String gameId;
  private Game game;
  private Subscription pokedSubscription = null;
  private long now;
  private boolean isPoked = false;
  private List<EmojiPoke> emojis = new ArrayList<>();

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

  private List<User> podiumList = new ArrayList<>();

  protected void initPresenter() {
    gameMVPViewAdapter = new GameMVPViewAdapter() {
      @Override public void onGameLeaderboard(List<Score> scoreList, boolean cloud) {
        if (scoreList == null) return;

        Collections.sort(scoreList, (o1, o2) -> ((Integer) o2.getValue()).compareTo(o1.getValue()));
        items.clear();

        ConstraintSet set = new ConstraintSet();
        set.clone(getApplicationContext(), R.layout.activity_game_leaderboards_final);
        animateLayoutWithConstraintSet(set, null);

        for (Score score : scoreList) {
          // We add the current user if user == null, it means it's the current user's score
          if (score.getUser() == null) score.setUser(getCurrentUser());
        }

        Score first = scoreList.remove(0);
        if (first != null) podiumList.add(first.getUser());
        setupPodium(1, first, avatarFirst, cardAvatarFirst, avatarEmptyFirst, txtNameFirst,
            txtRankingFirst, txtScoreFirst);

        Score second = null;

        if (scoreList.size() > 0) {
          second = scoreList.remove(0);
        }
        if (second != null) podiumList.add(second.getUser());

        setupPodium(2, second, avatarSecond, cardAvatarSecond, avatarEmptySecond, txtNameSecond,
            txtRankingSecond, txtScoreSecond);

        Score third = null;
        if (scoreList.size() > 0) {
          third = scoreList.remove(0);
        }

        if (third != null) podiumList.add(third.getUser());
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

  private EmojiPoke addPokeEmoji(View view, String emo, int x1, int y1, int transX, int transY,
      boolean anim, Animation animation) {
    EmojiPoke v =
        new EmojiPoke(this, emo, view.getWidth(), view.getHeight(), x1, y1, transX, transY, anim);
    container.addView(v);
    if (anim) {
      v.startAnimation(animation);
    }
    emojis.add(v);
    return v;
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
    adapter = new LeaderboardDetailsAdapter(this, recyclerView, stateManager);

    subscriptions.add(adapter.onClickPoke().subscribe(this::setClickPokeAnimation));

    recyclerView.setItemAnimator(null);
    recyclerView.setAdapter(adapter);

    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

      @Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        Timber.e("SOEF onScrollStateChanged");
        now = System.currentTimeMillis();
      }
    });

    if (pokedSubscription == null) {
      subscriptions.add(pokedSubscription = Observable.interval(100, TimeUnit.MILLISECONDS)
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe((Long aLong) -> {
            float interval = System.currentTimeMillis() - now;
            if (interval > 2000) {
              if (!isPoked) {
                adapter.onPoke(true);
              }
              isPoked = true;
            } else {
              if (isPoked) {
                adapter.onPoke(false);
              }
              isPoked = false;
            }
          }));
    }
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
      TextViewScore txtScore) {
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
      txtScore.setScore(score.getValue());
    }
  }

  private void showAvatars() {
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
    if (view.getVisibility() != View.VISIBLE) return;

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

  private void setClickPokeAnimation(Score score) {
    if (score.getUser().getId().equals(user.getId())) {
      navigator.shareGenericText(
          getString(R.string.poke_share_score, score.getGame().getId(), score.getRanking()), this);
      return;
    }

    String[] userIds = new String[1];
    userIds[0] = score.getUser().getId();

    String intent = (score.isAbove()) ? MessagePoke.INTENT_FUN : MessagePoke.INTENT_JEALOUS;
    messagePresenter.createPoke(userIds, score.getEmoticon(), score.getGame().getId(), intent);

    TextView view = score.getTextView();

    int[] locations = new int[2];
    view.getLocationOnScreen(locations);
    int x1 = locations[0];
    int y1 = locations[1];

    Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate_poke_emoji);
    Animation animation2 = AnimationUtils.loadAnimation(this, R.anim.rotate_hourglass_emoji);

    EmojiPoke v = addPokeEmoji(view, score.getEmoticon(), x1, y1, 0, 0, false, animation);
    addPokeEmoji(view, score.getEmoticon(), x1, y1, (view.getWidth() / 2), -view.getWidth(), true,
        animation);
    addPokeEmoji(view, score.getEmoticon(), x1, y1, -(view.getWidth() / 2), +view.getWidth(), true,
        animation);
    addPokeEmoji(view, score.getEmoticon(), x1, y1, view.getWidth(), 0, true, animation);
    addPokeEmoji(view, score.getEmoticon(), x1, y1, -view.getWidth(), 0, true, animation);
    addPokeEmoji(view, score.getEmoticon(), x1, y1, -(view.getWidth() / 2), -view.getWidth(), true,
        animation);
    addPokeEmoji(view, score.getEmoticon(), x1, y1, +(view.getWidth() / 2), +view.getWidth(), true,
        animation);

    v.animate().setInterpolator(new OvershootInterpolator()).withStartAction(() -> {
      v.setScaleX(0);
      v.setScaleY(0);
    }).withEndAction(() -> {
      view.setText(EmojiParser.demojizedText(getString(R.string.poke_emoji_disabled)));
      subscriptions.add(Observable.timer((2000), TimeUnit.MILLISECONDS)
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(aLong -> {
            v.animate().scaleX(1f).scaleY(1f).setDuration(300).withEndAction(() -> {
              for (EmojiPoke emo : emojis) {
                emo.clearAnimation();
                emo.animate()
                    .translationX(-emo.getTransX())
                    .translationY(-emo.getTransY())
                    .scaleX(0)
                    .scaleY(0)
                    .setDuration(300)
                    .withEndAction(() -> {
                      container.removeView(emo);
                      view.startAnimation(animation2);
                    })
                    .start();
              }
            }).start();
          }));
    }).scaleX(1.5f).scaleY(1.5f).setDuration(300).start();
  }

  @OnClick(R.id.btnBack) void back() {
    onBackPressed();
  }

  @OnClick(R.id.avatarFirst) void onClickAvatarFirst() {
    Recipient recipient = ShortcutUtil.getRecipientFromId(podiumList.get(0).getId(), user);
    navigator.navigateToChat(this, recipient, null, null, false);
  }

  @OnClick(R.id.avatarSecond) void onClickAvatarSecond() {
    Recipient recipient = ShortcutUtil.getRecipientFromId(podiumList.get(1).getId(), user);
    navigator.navigateToChat(this, recipient, null, null, false);
  }

  @OnClick(R.id.avatarThird) void onClickAvatarThird() {
    Recipient recipient = ShortcutUtil.getRecipientFromId(podiumList.get(2).getId(), user);
    navigator.navigateToChat(this, recipient, null, null, false);
  }

  @OnLongClick(R.id.avatarFirst) boolean onLongClickAvatarFirst() {
    Timber.e("SOEF OnLongClick onClickAvatarFirst");
    // setClickPokeAnimation(scor);
    return true;
  }

  @OnLongClick(R.id.avatarSecond) boolean onLongClickAvatarSecond() {
    Timber.e("SOEF OnLongClick onClickAvatarSecond");
    return true;
  }

  @OnLongClick(R.id.avatarThird) boolean onLongClickAvatarThird() {
    Timber.e("SOEF  OnLongClickonClickAvatarThird");
    return true;
  }
  /**
   * PUBLIC
   */

  /**
   * OBSERVABLES
   */

}
