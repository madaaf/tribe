package com.tribe.app.presentation.view.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.solera.defrag.AnimationHandler;
import com.solera.defrag.TraversalAnimation;
import com.solera.defrag.TraversingOperation;
import com.solera.defrag.TraversingState;
import com.solera.defrag.ViewStack;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.view.ShortcutUtil;
import com.tribe.app.presentation.view.component.games.GamesMembersView;
import com.tribe.app.presentation.view.component.games.GamesStoreView;
import com.tribe.app.presentation.view.utils.GlideUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.ViewStackHelper;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.tribelivesdk.game.Game;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

import static android.view.View.GONE;

public class NewGameActivity extends BaseActivity {

  public static final String SHORTCUT = "shortcut";
  public static final String CALL_ROULETTE = "call_roulette";
  public static final String GAME_ID = "game_id";
  public static final String FROM_HOME = "from_home";
  public static final String SOURCE = "source";

  private static final int DURATION = 200;

  public static Intent getCallingIntent(Activity activity, String source) {
    Intent intent = new Intent(activity, NewGameActivity.class);
    intent.putExtra(FROM_HOME, activity instanceof HomeActivity);
    intent.putExtra(SOURCE, source);
    return intent;
  }

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.btnBack) ImageView btnBack;

  @BindView(R.id.txtTitle) TextViewFont txtTitle;

  @BindView(R.id.txtTitleTwo) TextViewFont txtTitleTwo;

  @BindView(R.id.txtAction) TextViewFont txtAction;

  @BindView(R.id.btnForward) ImageView btnForward;

  @BindView(R.id.progressView) CircularProgressView progressView;

  @BindView(R.id.viewNavigatorStack) ViewStack viewStack;

  // VIEWS
  private GamesStoreView viewGamesStore;
  private GamesMembersView viewGamesMembers;

  // VARIABLES
  private boolean disableUI = false, isFromHome = false;
  private Game selectedGame = null;
  private String source;

  // OBSERVABLES
  private Unbinder unbinder;
  private CompositeSubscription subscriptions = new CompositeSubscription();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_new_game);

    if (getIntent() != null && getIntent().getExtras() != null) {
      isFromHome = getIntent().getBooleanExtra(FROM_HOME, false);
      source = getIntent().getStringExtra(SOURCE);
    }

    unbinder = ButterKnife.bind(this);

    initDependencyInjector();
    init(savedInstanceState);
  }

  @Override protected void onPause() {
    super.onPause();
    screenUtils.hideKeyboard(this);
  }

  @Override protected void onDestroy() {
    if (unbinder != null) unbinder.unbind();
    if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    super.onDestroy();
  }

  @OnClick(R.id.supportBtn) void onClickSupport() {
    Shortcut s = ShortcutUtil.createShortcutSupport();
    s.setTypeSupport(Shortcut.SUPPORT_SUGGEST);
    navigator.navigateToChat(this, s, null, null, null, false);
  }

  private void init(Bundle savedInstanceState) {
    txtTitle.setText(R.string.new_game_title);

    txtTitleTwo.setTranslationX(screenUtils.getWidthPx());

    viewStack.setAnimationHandler(createCustomAnimationHandler());
    viewStack.addTraversingListener(
        traversingState -> disableUI = traversingState != TraversingState.IDLE);

    if (savedInstanceState == null) {
      setupMainView();
    }

    txtAction.setOnClickListener(v -> {
      if (viewStack.getTopView() instanceof GamesMembersView) {
        progressView.setVisibility(View.VISIBLE);
        txtAction.setVisibility(View.GONE);
        viewGamesMembers.create();

        if (selectedGame != null) {
          Bundle bundle = new Bundle();
          bundle.putString(TagManagerUtils.ACTION, TagManagerUtils.GAME);
          bundle.putString(TagManagerUtils.SOURCE, source);
          bundle.putString(TagManagerUtils.GAME, selectedGame.getId());
          tagManager.trackEvent(TagManagerUtils.Game, bundle);
        }
      }
    });

    btnForward.setOnClickListener(v -> {
      if (viewStack.getTopView() instanceof GamesStoreView) {
        setupMembersView(null);
      }
    });

    if (!isFromHome) {
      txtAction.setVisibility(View.GONE);
      btnForward.setVisibility(View.GONE);
    }
  }

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .applicationComponent(getApplicationComponent())
        .activityModule(getActivityModule())
        .build()
        .inject(this);
  }

  @OnClick(R.id.btnBack) void clickBack() {
    onBackPressed();
  }

  @Override public void onBackPressed() {
    if (viewStack.getTopView() instanceof GamesStoreView) {
      Bundle bundle = new Bundle();
      bundle.putString(TagManagerUtils.ACTION, TagManagerUtils.CANCEL);
      bundle.putString(TagManagerUtils.SOURCE, source);
      tagManager.trackEvent(TagManagerUtils.Game, bundle);
    }

    screenUtils.hideKeyboard(this);

    if (disableUI) {
      return;
    }

    if (!viewStack.pop()) {
      super.onBackPressed();
    }
  }

  @Override public void finish() {
    super.finish();
    overridePendingTransition(R.anim.activity_in_scale, R.anim.slide_out_down);
  }

  @Override public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
    return disableUI || super.dispatchTouchEvent(ev);
  }

  @Override public Object getSystemService(@NonNull String name) {
    if (ViewStackHelper.matchesServiceName(name)) {
      return viewStack;
    }

    return super.getSystemService(name);
  }

  @NonNull private AnimationHandler createCustomAnimationHandler() {
    return (from, to, operation) -> {
      boolean forward = operation != TraversingOperation.POP;

      AnimatorSet set = new AnimatorSet();

      set.setDuration(DURATION);
      set.setInterpolator(new DecelerateInterpolator());

      final int width = from.getWidth();

      computeTitle(forward, to);

      if (forward) {
        to.setTranslationX(width);
        set.play(ObjectAnimator.ofFloat(from, View.TRANSLATION_X, 0 - (width)));
        set.play(ObjectAnimator.ofFloat(to, View.TRANSLATION_X, 0));
      } else {
        to.setTranslationX(0 - (width));
        set.play(ObjectAnimator.ofFloat(from, View.TRANSLATION_X, width));
        set.play(ObjectAnimator.ofFloat(to, View.TRANSLATION_X, 0));
      }

      return TraversalAnimation.newInstance(set,
          forward ? TraversalAnimation.ABOVE : TraversalAnimation.BELOW);
    };
  }

  private void setupMainView() {
    viewGamesStore = (GamesStoreView) viewStack.push(R.layout.view_game_store);

    subscriptions.add(viewGamesStore.onGameClick().subscribe(game -> {
      if (isFromHome) {
        setupMembersView(game);
      } else {
        Intent intent = new Intent();
        if (game != null) intent.putExtra(GAME_ID, game.getId());
        setResult(RESULT_OK, intent);
        finish();
      }
    }));
  }

  private void setupMembersView(Game game) {
    selectedGame = game;

    viewGamesMembers = (GamesMembersView) viewStack.push(R.layout.view_game_members);
    viewGamesMembers.setGame(selectedGame);

    subscriptions.add(viewGamesMembers.onFinish().subscribe(shortcut -> {
      if (shortcut == null) {
        finish();
      } else {
        Intent intent = new Intent();
        intent.putExtra(SHORTCUT, shortcut);
        if (game != null) intent.putExtra(GAME_ID, selectedGame.getId());
        setResult(RESULT_OK, intent);
        finish();
      }
    }));

    subscriptions.add(
        viewGamesMembers.onHasMembers().subscribe(aBoolean -> txtAction.setEnabled(aBoolean)));

    subscriptions.add(viewGamesMembers.onCallRouletteSelected().subscribe(aVoid -> {
      Intent intent = new Intent();
      intent.putExtra(CALL_ROULETTE, true);
      intent.putExtra(GAME_ID, selectedGame.getId());
      setResult(RESULT_OK, intent);
      finish();
    }));
  }

  private void computeTitle(boolean forward, View to) {
    if (to instanceof GamesStoreView) {
      ViewCompat.setElevation(btnBack, 0);
      btnBack.setImageResource(R.drawable.picto_btn_close);
    } else if (to instanceof GamesMembersView) {
      if (selectedGame == null) {
        ViewCompat.setElevation(btnBack, 0);
        btnBack.setImageResource(R.drawable.picto_btn_back);
      } else {
        new GlideUtils.GameImageBuilder(this, screenUtils).url(selectedGame.getIcon())
            .hasBorder(false)
            .hasPlaceholder(true)
            .rounded(true)
            .target(btnBack)
            .load();

        ViewCompat.setElevation(btnBack, screenUtils.dpToPx(5));
      }
    }

    if (to instanceof GamesStoreView) {
      setupTitle(getString(R.string.new_game_title), forward);
      txtAction.setVisibility(GONE);
      btnForward.setVisibility(View.VISIBLE);
    } else if (to instanceof GamesMembersView) {
      if (selectedGame == null) {
        setupTitle(getString(R.string.home_action_new_chat), forward);
      } else {
        setupTitle(selectedGame.getTitle(), forward);
      }

      btnForward.setVisibility(View.GONE);
      txtAction.setVisibility(View.VISIBLE);
      txtAction.setText(getString(R.string.action_create));
    }
  }

  private void setupTitle(String title, boolean forward) {
    if (txtTitle.getTranslationX() == 0) {
      txtTitleTwo.setText(title);
      hideTitle(txtTitle, forward);
      showTitle(txtTitleTwo, forward);
    } else {
      txtTitle.setText(title);
      hideTitle(txtTitleTwo, forward);
      showTitle(txtTitle, forward);
    }
  }

  private void hideTitle(View view, boolean forward) {
    if (forward) {
      view.animate()
          .translationX(-(screenUtils.getWidthPx() / 3))
          .alpha(0)
          .setDuration(DURATION)
          .start();
    } else {
      view.animate().translationX(screenUtils.getWidthPx()).setDuration(DURATION).start();
    }
  }

  private void showTitle(View view, boolean forward) {
    if (forward) {
      view.setTranslationX(screenUtils.getWidthPx());
      view.setAlpha(1);
    } else {
      view.setTranslationX(-(screenUtils.getWidthPx() / 3));
      view.setAlpha(0);
    }

    view.animate().translationX(0).alpha(1).setDuration(DURATION).start();
  }
}
