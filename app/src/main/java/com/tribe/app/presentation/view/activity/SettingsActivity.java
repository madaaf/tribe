package com.tribe.app.presentation.view.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.f2prateek.rx.preferences.Preference;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.solera.defrag.AnimationHandler;
import com.solera.defrag.TraversalAnimation;
import com.solera.defrag.TraversingOperation;
import com.solera.defrag.TraversingState;
import com.solera.defrag.ViewStack;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.SettingsPresenter;
import com.tribe.app.presentation.mvp.view.SettingsMVPView;
import com.tribe.app.presentation.utils.preferences.ShareProfile;
import com.tribe.app.presentation.view.component.settings.SettingsBlockedHiddenView;
import com.tribe.app.presentation.view.component.settings.SettingsFilterThemeView;
import com.tribe.app.presentation.view.component.settings.SettingsParametersView;
import com.tribe.app.presentation.view.component.settings.SettingsProfileView;
import com.tribe.app.presentation.view.component.settings.SettingsView;
import com.tribe.app.presentation.view.dialog_fragment.ShareDialogProfileFragment;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.ViewStackHelper;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

import static android.view.View.GONE;

public class SettingsActivity extends BaseActivity implements SettingsMVPView {

    private static final int DURATION = 200;

    public static Intent getCallingIntent(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        return intent;
    }

    @Inject
    User user;

    @Inject
    ScreenUtils screenUtils;

    @Inject
    SettingsPresenter settingsPresenter;

    @Inject
    @ShareProfile
    Preference<Boolean> shareProfile;

    @BindView(R.id.viewNavigatorStack)
    ViewStack viewStack;

    @BindView(R.id.txtTitle)
    TextViewFont txtTitle;

    @BindView(R.id.txtTitleTwo)
    TextViewFont txtTitleTwo;

    @BindView(R.id.txtAction)
    TextViewFont txtAction;

    @BindView(R.id.imgBack)
    ImageView imgBack;

    @BindView(R.id.progressView)
    CircularProgressView progressView;

    // VIEWS
    private SettingsView viewSettings;
    private SettingsProfileView viewSettingsProfile;
    private SettingsFilterThemeView viewSettingsFilterTheme;
    private SettingsParametersView viewSettingsParameters;
    private SettingsBlockedHiddenView viewSettingsBlockedHidden;

    // VARIABLES
    private boolean disableUI = false;
    private TextViewFont currentTitle;
    private List<Friendship> blockedFriendshipList;

    // OBSERVABLES
    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        unbinder = ButterKnife.bind(this);

        initDependencyInjector();
        init(savedInstanceState);
        initPresenter();
    }

    @Override
    protected void onStop() {
        settingsPresenter.onViewDetached();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) unbinder.unbind();
        if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
        if (viewSettingsFilterTheme != null) viewSettingsFilterTheme.onDestroy();
        if (viewSettingsParameters != null) viewSettingsParameters.onDestroy();
        if (viewSettingsProfile != null) viewSettingsProfile.onDestroy();
        if (viewSettingsBlockedHidden != null) viewSettingsBlockedHidden.onDestroy();
        if (viewSettings != null) viewSettings.onDestroy();
        super.onDestroy();
    }

    private void init(Bundle savedInstanceState) {
        blockedFriendshipList = new ArrayList<>();

        txtTitle.setText(R.string.settings_title);

        txtTitleTwo.setTranslationX(screenUtils.getWidthPx());

        viewStack.setAnimationHandler(createCustomAnimationHandler());
        viewStack.addTraversingListener(traversingState -> disableUI = traversingState != TraversingState.IDLE);

        if (savedInstanceState == null) {
            setupSettingsView();
        }

        txtAction.setOnClickListener(v -> {
            if (viewStack.getTopView() instanceof SettingsProfileView) {
                screenUtils.hideKeyboard(this);
                settingsPresenter.updateUser(
                        viewSettingsProfile.getUsername(),
                        viewSettingsProfile.getDisplayName(),
                        viewSettingsProfile.getImgUri(),
                        getCurrentUser().getFbid()
                );
            }
        });
    }

    private void initPresenter() {
        settingsPresenter.onViewAttached(this);
    }

    private void initDependencyInjector() {
        DaggerUserComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build()
                .inject(this);
    }

    @OnClick(R.id.imgBack)
    void clickBack() {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        hideLoading();
        screenUtils.hideKeyboard(this);

        if (disableUI) {
            return;
        }

        if (!viewStack.pop()) {
            super.onBackPressed();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_in_scale, R.anim.activity_out_to_right);
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        return disableUI || super.dispatchTouchEvent(ev);
    }

    @Override
    public Object getSystemService(@NonNull String name) {
        if (ViewStackHelper.matchesServiceName(name)) {
            return viewStack;
        }

        return super.getSystemService(name);
    }

    @NonNull
    private AnimationHandler createCustomAnimationHandler() {
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

    private void setupSettingsView() {
        viewSettings = (SettingsView) viewStack.push(R.layout.view_settings);

        subscriptions.add(viewSettings.onPointsClick()
                .subscribe(aVoid -> {
                    navigator.navigateToScorePoints(this);
                })
        );

        subscriptions.add(viewSettings.onShareProfileClick()
                .subscribe(aVoid -> {
                    ShareDialogProfileFragment shareDialogProfileFragment = ShareDialogProfileFragment.newInstance();
                    shareDialogProfileFragment.show(getSupportFragmentManager(), ShareDialogProfileFragment.class.getName());

                    if (!shareProfile.get()) {
                        shareProfile.set(true);
                        settingsPresenter.updateScoreShare();
                    }
                })
        );

        subscriptions.add(viewSettings.onProfileClick()
                .subscribe(aVoid -> {
                    setupProfileView();
                }));


        subscriptions.add(viewSettings.onFiltersClick()
                .subscribe(aVoid -> {
                    setupFiltersView();
                }));

        subscriptions.add(viewSettings.onParametersClick()
                .subscribe(aVoid -> {
                    setupParametersView();
                }));

        subscriptions.add(viewSettings.onContactClick()
                .flatMap(aVoid -> DialogFactory.showBottomSheetForContact(this),
                        ((aVoid, labelType) -> {
                            if (labelType.getTypeDef().equals(LabelType.TRIBE)) {
                                //navigator.navigateToUrl(this, getString(R.string.settings_follow_instagram_url));
                            } else if (labelType.getTypeDef().equals(LabelType.EMAIL)) {
                                String[] addresses = {getString(R.string.settings_email_address)};
                                navigator.composeEmail(this, addresses, getString(R.string.settings_email_subject));
                            } else if (labelType.getTypeDef().equals(LabelType.TWITTER)) {
                                navigator.tweet(this, "");
                            }

                            return null;
                        }))
                .subscribe()
        );

        subscriptions.add(viewSettings.onFollowClick()
                .flatMap(aVoid -> DialogFactory.showBottomSheetForFollow(this),
                        ((aVoid, labelType) -> {
                            if (labelType.getTypeDef().equals(LabelType.INSTAGRAM)) {
                                navigator.navigateToUrl(this, getString(R.string.settings_follow_instagram_url));
                            } else if (labelType.getTypeDef().equals(LabelType.SNAPCHAT)) {
                                navigator.navigateToUrl(this, getString(R.string.settings_follow_snapchat_url));
                            } else if (labelType.getTypeDef().equals(LabelType.TWITTER)) {
                                navigator.navigateToUrl(this, getString(R.string.settings_follow_twitter_url));
                            }

                            return null;
                        }))
                .subscribe());

        subscriptions.add(viewSettings.onBlockedHiddenClick()
                .subscribe(aVoid -> {
                    setupBlockedHiddenView();
                }));

        subscriptions.add(viewSettings.onRateClick()
                .subscribe(aVoid -> {
                    navigator.rateApp(this);
                    settingsPresenter.updateScoreRateApp();
                }));

        subscriptions.add(viewSettings.onLogoutClick()
                .flatMap(aVoid -> DialogFactory.dialog(
                        this,
                        getString(R.string.settings_logout_title),
                        getString(R.string.settings_logout_confirm_message),
                        getString(R.string.settings_logout_title),
                        getString(R.string.action_cancel)))
                .filter(x -> x == true)
                .subscribe(aVoid -> {
                    ProgressDialog pd = new ProgressDialog(this);
                    pd.setTitle(R.string.settings_logout_wait);
                    pd.show();
                    settingsPresenter.logout();
                }));
    }

    private void setupProfileView() {
        viewSettingsProfile = (SettingsProfileView) viewStack.push(R.layout.view_settings_profile);

        subscriptions.add(viewSettingsProfile.onUsernameInput().subscribe(s -> {
            settingsPresenter.lookupUsername(s);
        }));

        subscriptions.add(viewSettingsProfile.onInfoValid().subscribe(b -> {
            txtAction.setEnabled(b);
        }));
    }

    private void setupFiltersView() {
        viewSettingsFilterTheme = (SettingsFilterThemeView) viewStack.push(R.layout.view_settings_filter_theme);
    }

    private void setupParametersView() {
        viewSettingsParameters = (SettingsParametersView) viewStack.push(R.layout.view_settings_parameters);

        subscriptions.add(viewSettingsParameters.onChangeInvisible()
                .subscribe(aBoolean -> settingsPresenter.updateUserInvisibleMode(aBoolean))
        );

        subscriptions.add(viewSettingsParameters.onChangeLocation()
                .subscribe(aBoolean -> settingsPresenter.updateScoreLocation())
        );

        subscriptions.add(viewSettingsParameters.onChangeMemories()
                .subscribe(aBoolean -> settingsPresenter.updateUserTribeSave(aBoolean))
        );

        subscriptions.add(viewSettingsParameters.onChangeNotifications()
                .subscribe(aBoolean -> settingsPresenter.updateUserNotifications(aBoolean))
        );
    }

    private void setupBlockedHiddenView() {
        viewSettingsBlockedHidden = (SettingsBlockedHiddenView) viewStack.push(R.layout.view_settings_blocked_hidden);
        viewSettingsBlockedHidden.renderBlockedFriendshipList(blockedFriendshipList);

        subscriptions.add(viewSettingsBlockedHidden
                .onUpdateFriendship()
                .subscribe(id -> settingsPresenter.updateFriendship(id))
        );
    }

    private void computeTitle(boolean forward, View to) {
        if (to instanceof SettingsView) {
            setupTitle(getString(R.string.settings_title), forward);
            txtAction.setVisibility(GONE);
        } else if (to instanceof SettingsProfileView) {
            setupTitle(getString(R.string.settings_profile_title), forward);
            txtAction.setVisibility(View.VISIBLE);
            txtAction.setText(getString(R.string.action_save));
        } else if (to instanceof SettingsFilterThemeView) {
            setupTitle(getString(R.string.settings_filter_title), forward);
            txtAction.setVisibility(View.GONE);
        } else if (to instanceof SettingsParametersView) {
            setupTitle(getString(R.string.settings_parameters_title), forward);
            txtAction.setVisibility(View.GONE);
        } else if (to instanceof SettingsBlockedHiddenView) {
            setupTitle(getString(R.string.settings_hidden_blocked_title), forward);
            txtAction.setVisibility(View.GONE);
        }
    }

    private void setupTitle(String title, boolean forward) {
        if (txtTitle.getTranslationX() == 0) {
            currentTitle = txtTitleTwo;
            txtTitleTwo.setText(title);
            hideTitle(txtTitle, forward);
            showTitle(txtTitleTwo, forward);
        } else {
            currentTitle = txtTitle;
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
            view.animate()
                    .translationX(screenUtils.getWidthPx())
                    .setDuration(DURATION)
                    .start();
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

        view.animate()
                .translationX(0)
                .alpha(1)
                .setDuration(DURATION)
                .start();
    }

    @Override
    public void goToLauncher() {

    }

    @Override
    public void onFBContactsSync(int count) {

    }

    @Override
    public void onAddressBookContactSync(int count) {

    }

    @Override
    public void onSuccessSync() {

    }

    @Override
    public void friendshipUpdated(Friendship friendship) {
        if (viewSettingsBlockedHidden != null) viewSettingsBlockedHidden.friendshipUpdated(friendship);
    }

    @Override
    public void renderBlockedFriendshipList(List<Friendship> friendshipList) {
        blockedFriendshipList.clear();
        blockedFriendshipList.addAll(friendshipList);

        if (viewSettingsBlockedHidden != null) viewSettingsBlockedHidden.renderBlockedFriendshipList(friendshipList);
    }

    @Override
    public void successUpdateUser(User user) {

    }

    @Override
    public void successFacebookLogin() {

    }

    @Override
    public void errorFacebookLogin() {

    }

    @Override
    public void usernameResult(Boolean available) {
        boolean usernameValid = available;
        if (viewStack.getTopView() instanceof SettingsProfileView) {
            viewSettingsProfile.setUsernameValid(
                    usernameValid || viewSettingsProfile.getUsername().equals(getCurrentUser().getUsername())
            );
        }
    }

    @Override
    public void showLoading() {
        txtAction.setVisibility(GONE);
        progressView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        txtAction.setVisibility(View.VISIBLE);
        progressView.setVisibility(GONE);
    }

    @Override
    public void showError(String message) {

    }

    @Override
    public Context context() {
        return this;
    }
}