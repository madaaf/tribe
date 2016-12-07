package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.ImageView;

import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.SettingsPresenter;
import com.tribe.app.presentation.mvp.view.SettingsMVPView;
import com.tribe.app.presentation.view.fragment.SettingBlockFragment;
import com.tribe.app.presentation.view.fragment.SettingFragment;
import com.tribe.app.presentation.view.fragment.SettingUpdateProfileFragment;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

/**
 * SettingActivity.java
 * Created by horatiothomas on 8/26/16.
 */
public class SettingActivity extends BaseActivity implements SettingsMVPView {

    private static final String SETTING_FRAGMENT = "settingFragment";
    private static final String SETTING_PROFILE_FRAGMENT = "settingUpdateProfileFragment";
    private static final String SETTING_BLOCK_FRAGMENT = "settingBlockFragment";

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, SettingActivity.class);
    }

    @BindView(R.id.imgBack)
    ImageView imgBack;

    @BindView(R.id.imgDone)
    ImageView imgDone;

    @BindView(R.id.txtTitle)
    TextViewFont txtTitle;

    // OBSERVABLES
    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    // VARIABLES
    private FragmentManager fragmentManager;
    private SettingFragment settingFragment;
    private SettingUpdateProfileFragment settingUpdateProfileFragment;
    private SettingBlockFragment settingBlockFragment;

    private int shortDuration = 150;

    @Inject
    SettingsPresenter settingsPresenter;

    @Inject
    ScreenUtils screenUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initUi(savedInstanceState);
        initDependencyInjector();
        initPresenter();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (settingUpdateProfileFragment != null && settingUpdateProfileFragment.isAdded()) {
            fragmentManager.putFragment(outState, SETTING_PROFILE_FRAGMENT, settingUpdateProfileFragment);
            fragmentManager.putFragment(outState, SETTING_FRAGMENT, settingFragment);
        }
    }

    @Override
    protected void onPause() {
        txtTitle.animate().setListener(null).start();

        super.onPause();
    }

    @Override
    protected void onStop() {
        settingsPresenter.onViewDetached();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) unbinder.unbind();
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_in_scale, R.anim.activity_out_to_right);
    }

    private void initUi(Bundle savedInstanceState) {
        setContentView(R.layout.activity_setting);
        unbinder = ButterKnife.bind(this);

        fragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            settingUpdateProfileFragment = SettingUpdateProfileFragment.newInstance();
            settingFragment = SettingFragment.newInstance();

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.layoutFragmentContainer, settingFragment);
            fragmentTransaction.commit();
        } else {
            settingUpdateProfileFragment = (SettingUpdateProfileFragment) fragmentManager.getFragment(savedInstanceState, SETTING_PROFILE_FRAGMENT);
            settingFragment = (SettingFragment) fragmentManager.getFragment(savedInstanceState, SETTING_FRAGMENT);
        }

        settingBlockFragment = SettingBlockFragment.newInstance();

        subscriptions.add(RxView.clicks(imgBack).subscribe(aVoid -> {
            goToMain();
        }));

        subscriptions.add(RxView.clicks(imgDone).subscribe(aVoid -> {
            Fragment fr = fragmentManager.findFragmentById(R.id.layoutFragmentContainer);

            if (fr instanceof SettingFragment) {
                Intent resultIntent = new Intent();
                setResult(BaseActivity.RESULT_OK, resultIntent);
                finish();
            }

            if (fr instanceof SettingUpdateProfileFragment) {
                settingsPresenter.updateUser(
                        settingUpdateProfileFragment.getUsername(),
                        settingUpdateProfileFragment.getDisplayName(),
                        settingUpdateProfileFragment.getImgUri(),
                        getCurrentUser().getFbid()
                );

                goToMain();
            }
        }));
    }

    private void updateAnim() {
        imgBack.animate()
                .alpha(1)
                .setStartDelay(0)
                .start();
    }

    public void setImgDoneEnabled(boolean isEnabled) {
        if (isEnabled) {
            imgDone.setClickable(true);
            imgDone.animate()
                    .alpha(1)
                    .setStartDelay(0)
                    .start();
        } else {
            imgDone.setClickable(false);
            imgDone.animate()
                    .alpha(.4f)
                    .setStartDelay(0)
                    .start();
        }
    }

    private void mainSettingAnim() {
        imgBack.animate()
                .alpha(0)
                .setStartDelay(0)
                .start();
    }

    public void goToMain() {
        screenUtils.hideKeyboard(this);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fragment_in_from_left, R.anim.out_from_right);
        fragmentTransaction.replace(R.id.layoutFragmentContainer, settingFragment);
        fragmentTransaction.commit();
        txtTitle.setText(getString(R.string.settings_title));
        imgDone.setAlpha(1f);
        mainSettingAnim();
    }

    public void goToUpdateProfile() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fragment_in_from_right, R.anim.out_from_left);
        fragmentTransaction.add(R.id.layoutFragmentContainer, settingUpdateProfileFragment);
        fragmentTransaction.addToBackStack("Update");
        fragmentTransaction.commit();
        txtTitle.setText(getString(R.string.settings_profile_title));
        updateAnim();

        subscriptions.add(settingUpdateProfileFragment.onUsernameSearch().subscribe(s -> {
            settingsPresenter.lookupUsername(s);
        }));
    }

    public void goToBlock() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fragment_in_from_right, R.anim.out_from_left);
        fragmentTransaction.add(R.id.layoutFragmentContainer, settingBlockFragment);
        fragmentTransaction.addToBackStack("Block");
        fragmentTransaction.commit();
        txtTitle.setText(getString(R.string.hiddenblocked_empty_title));
        imgDone.setAlpha(0f);
        updateAnim();
    }

    @Override
    public void goToLauncher() {
        navigator.navigateToLauncher(this);
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

    }

    @Override
    public void renderBlockedFriendshipList(List<Friendship> friendshipList) {

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

    @Override
    public void successUpdateUser(User user) {
        settingFragment.setPicture(user.getProfilePicture());
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
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.layoutFragmentContainer);
        if (fragment instanceof SettingUpdateProfileFragment) settingUpdateProfileFragment.setUsernameValid(usernameValid || settingUpdateProfileFragment.getUsername().equals(getCurrentUser().getUsername()));
    }

    private void initPresenter() {
        settingsPresenter.onViewAttached(this);
    }

    /**
     * Dagger Setup
     */

    private void initDependencyInjector() {
        DaggerUserComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build()
                .inject(this);
    }
}
