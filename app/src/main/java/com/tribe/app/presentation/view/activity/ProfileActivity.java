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

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.solera.defrag.AnimationHandler;
import com.solera.defrag.TraversalAnimation;
import com.solera.defrag.TraversingOperation;
import com.solera.defrag.TraversingState;
import com.solera.defrag.ViewStack;
import com.tribe.app.R;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.ProfilePresenter;
import com.tribe.app.presentation.mvp.view.ProfileMVPView;
import com.tribe.app.presentation.view.component.profile.ProfileView;
import com.tribe.app.presentation.view.component.settings.SettingsProfileView;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.ViewStackHelper;
import com.tribe.app.presentation.view.widget.TextViewFont;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

import static android.view.View.GONE;


public class ProfileActivity extends BaseActivity implements ProfileMVPView {

    private static final int DURATION = 200;

    public static Intent getCallingIntent(Context context) {
        Intent intent = new Intent(context, ProfileActivity.class);
        return intent;
    }

    @Inject
    ScreenUtils screenUtils;

    @Inject
    ProfilePresenter profilePresenter;

    @BindView(R.id.txtTitle)
    TextViewFont txtTitle;

    @BindView(R.id.txtTitleTwo)
    TextViewFont txtTitleTwo;

    @BindView(R.id.txtAction)
    TextViewFont txtAction;

    @BindView(R.id.viewNavigatorStack)
    ViewStack viewStack;

    @BindView(R.id.progressView)
    CircularProgressView progressView;

    // VIEWS
    private ProfileView viewProfile;
    private SettingsProfileView viewSettingsProfile;

    // VARIABLES
    private boolean disableUI = false;

    // OBSERVABLES
    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        unbinder = ButterKnife.bind(this);

        initDependencyInjector();
        init(savedInstanceState);
        initPresenter();
    }

    @Override
    protected void onStop() {
        profilePresenter.onViewDetached();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) unbinder.unbind();
        if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
        if (viewSettingsProfile != null) viewSettingsProfile.onDestroy();
        if (viewProfile != null) viewProfile.onDestroy();
        super.onDestroy();
    }

    private void init(Bundle savedInstanceState) {
        txtTitle.setText(R.string.profile_title);

        txtTitleTwo.setTranslationX(screenUtils.getWidthPx());

        viewStack.setAnimationHandler(createCustomAnimationHandler());
        viewStack.addTraversingListener(traversingState -> disableUI = traversingState != TraversingState.IDLE);

        if (savedInstanceState == null) {
            setupMainView();
        }

        txtAction.setOnClickListener(v -> {
            if (viewStack.getTopView() instanceof SettingsProfileView) {
                screenUtils.hideKeyboard(this);
                profilePresenter.updateUser(
                        viewSettingsProfile.getUsername(),
                        viewSettingsProfile.getDisplayName(),
                        viewSettingsProfile.getImgUri(),
                        getCurrentUser().getFbid()
                );
            }
        });


    }


    private void initPresenter() {
        profilePresenter.onViewAttached(this);
    }

    private void initDependencyInjector() {
        DaggerUserComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build()
                .inject(this);
    }

    @OnClick(R.id.txtBack)
    void clickBack() {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
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

    private void setupMainView() {
        viewProfile = (ProfileView) viewStack.push(R.layout.view_profile);

        subscriptions.add(viewProfile.onProfileClick()
                .subscribe(aVoid -> {
                    setupProfileDetailView();
                }));

        subscriptions.add(viewProfile.onFollowClick()
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

        subscriptions.add(viewProfile.onRateClick()
                .subscribe(aVoid -> {
                    navigator.rateApp(this);
                }));

        subscriptions.add(viewProfile.onLogoutClick()
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
                    profilePresenter.logout();
                }));

        subscriptions.add(viewProfile.onChangeVisible()
                .subscribe(aBoolean -> profilePresenter.updateUserInvisibleMode(aBoolean)));

    }

    private void setupProfileDetailView() {
        viewSettingsProfile = (SettingsProfileView) viewStack.push(R.layout.view_settings_profile);

        subscriptions.add(viewSettingsProfile.onUsernameInput().subscribe(s -> {
            profilePresenter.lookupUsername(s);
        }));

        subscriptions.add(viewSettingsProfile.onInfoValid().subscribe(b -> {
            txtAction.setEnabled(b);
        }));
    }

    private void computeTitle(boolean forward, View to) {
        if (to instanceof ProfileView) {
            setupTitle(getString(R.string.profile_title), forward);
            txtAction.setVisibility(GONE);
        } else if (to instanceof SettingsProfileView) {
            setupTitle(getString(R.string.settings_title), forward);
            txtAction.setVisibility(View.VISIBLE);
            txtAction.setText(getString(R.string.action_save));
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
        ((AndroidApplication) getApplication()).logoutUser();
        navigator.navigateToLogout(this);
        finish();
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
