package com.tribe.app.presentation.view.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.solera.defrag.AnimationHandler;
import com.solera.defrag.TraversalAnimation;
import com.solera.defrag.TraversingOperation;
import com.solera.defrag.TraversingState;
import com.solera.defrag.ViewStack;
import com.tribe.app.R;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.view.component.games.GamesStoreView;
import com.tribe.app.presentation.view.component.profile.ProfileView;
import com.tribe.app.presentation.view.component.settings.SettingsBlockedFriendsView;
import com.tribe.app.presentation.view.component.settings.SettingsFacebookAccountView;
import com.tribe.app.presentation.view.component.settings.SettingsManageShortcutsView;
import com.tribe.app.presentation.view.component.settings.SettingsPhoneNumberView;
import com.tribe.app.presentation.view.component.settings.SettingsProfileView;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.ViewStackHelper;
import com.tribe.app.presentation.view.widget.TextViewFont;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

import static android.view.View.GONE;

public class NewGameActivity extends BaseActivity {

  private static final int DURATION = 200;

  public static Intent getCallingIntent(Context context) {
    Intent intent = new Intent(context, NewGameActivity.class);
    return intent;
  }

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.btnBack) ImageView btnBack;

  @BindView(R.id.txtTitle) TextViewFont txtTitle;

  @BindView(R.id.txtTitleTwo) TextViewFont txtTitleTwo;

  @BindView(R.id.txtAction) TextViewFont txtAction;

  @BindView(R.id.btnForward) ImageView btnForward;

  @BindView(R.id.viewNavigatorStack) ViewStack viewStack;

  // VIEWS
  private GamesStoreView viewGamesStore;

  // VARIABLES
  private boolean disableUI = false;

  // OBSERVABLES
  private Unbinder unbinder;
  private CompositeSubscription subscriptions = new CompositeSubscription();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_new_game);

    unbinder = ButterKnife.bind(this);

    initDependencyInjector();
    init(savedInstanceState);
  }

  @Override protected void onDestroy() {
    if (unbinder != null) unbinder.unbind();
    if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    super.onDestroy();
  }

  private void init(Bundle savedInstanceState) {
    txtTitle.setText(R.string.profile_title);

    txtTitleTwo.setTranslationX(screenUtils.getWidthPx());

    viewStack.setAnimationHandler(createCustomAnimationHandler());
    viewStack.addTraversingListener(
        traversingState -> disableUI = traversingState != TraversingState.IDLE);

    if (savedInstanceState == null) {
      setupMainView();
    }

    txtAction.setOnClickListener(v -> {
      if (viewStack.getTopView() instanceof SettingsProfileView) {

      }
    });
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
    overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
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
  }

  private void computeTitle(boolean forward, View to) {
    if (to instanceof GamesStoreView) {
      btnBack.setImageResource(R.drawable.picto_close);
    } else {
      btnBack.setImageResource(R.drawable.picto_back);
    }

    if (to instanceof ProfileView) {
      setupTitle(getString(R.string.profile_title), forward);
      txtAction.setVisibility(GONE);
    } else if (to instanceof SettingsProfileView) {
      setupTitle(getString(R.string.settings_profile_title), forward);
      txtAction.setVisibility(View.VISIBLE);
      txtAction.setText(getString(R.string.action_save));
    } else if (to instanceof SettingsBlockedFriendsView) {
      String title = EmojiParser.demojizedText(getString(R.string.profile_blocked_friends));
      setupTitle(title, forward);
      txtAction.setVisibility(GONE);
    } else if (to instanceof SettingsManageShortcutsView) {
      setupTitle(EmojiParser.demojizedText(getString(R.string.manage_friendships_title)), forward);
      txtAction.setVisibility(View.GONE);
    } else if (to instanceof SettingsPhoneNumberView) {
      setupTitle(getString(R.string.profile_change_phone_title), forward);
      txtAction.setVisibility(View.GONE);
    } else if (to instanceof SettingsFacebookAccountView) {
      setupTitle(getString(R.string.profile_facebook_account_title), forward);
      txtAction.setVisibility(View.GONE);
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
