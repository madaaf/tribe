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

import com.f2prateek.rx.preferences.Preference;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.solera.defrag.AnimationHandler;
import com.solera.defrag.TraversalAnimation;
import com.solera.defrag.TraversingOperation;
import com.solera.defrag.TraversingState;
import com.solera.defrag.ViewStack;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.scope.ShareProfile;
import com.tribe.app.presentation.mvp.presenter.SettingsPresenter;
import com.tribe.app.presentation.mvp.view.SettingMVPView;
import com.tribe.app.presentation.view.component.settings.SettingsProfileView;
import com.tribe.app.presentation.view.component.settings.SettingsView;
import com.tribe.app.presentation.view.dialog_fragment.ShareDialogProfileFragment;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.ViewStackHelper;
import com.tribe.app.presentation.view.widget.TextViewFont;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

public class SettingsActivity extends BaseActivity implements SettingMVPView {

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

    // VARIABLES
    private boolean disableUI = false;
    private TextViewFont currentTitle;

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
        super.onDestroy();
    }

    private void init(Bundle savedInstanceState) {
        txtTitle.setText(R.string.settings_title);

        txtTitleTwo.setTranslationX(screenUtils.getWidthPx());

        viewStack.setAnimationHandler(createCustomAnimationHandler());
        viewStack.addTraversingListener(traversingState -> disableUI = traversingState != TraversingState.IDLE);

        if (savedInstanceState == null) {
            setupSettingsView();
        }
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
    }

    private void setupProfileView() {
        viewSettingsProfile = (SettingsProfileView) viewStack.push(R.layout.view_settings_profile);
    }

    private void computeTitle(boolean forward, View to) {
        if (to instanceof SettingsView) {
            setupTitle(getString(R.string.settings_title), forward);
            txtAction.setVisibility(View.GONE);
        } else if (to instanceof SettingsProfileView) {
            setupTitle(getString(R.string.settings_profile_title), forward);
            txtAction.setVisibility(View.VISIBLE);
            txtAction.setText(getString(R.string.action_save));
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

    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void showError(String message) {

    }

    @Override
    public Context context() {
        return this;
    }
}