package com.tribe.app.presentation.view.fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.f2prateek.rx.preferences.Preference;
import com.jakewharton.rxbinding.view.RxView;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.BuildConfig;
import com.tribe.app.R;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.scope.AddressBook;
import com.tribe.app.presentation.internal.di.scope.AudioDefault;
import com.tribe.app.presentation.internal.di.scope.LocationContext;
import com.tribe.app.presentation.internal.di.scope.LocationPopup;
import com.tribe.app.presentation.internal.di.scope.Preload;
import com.tribe.app.presentation.internal.di.scope.WeatherUnits;
import com.tribe.app.presentation.mvp.presenter.SettingPresenter;
import com.tribe.app.presentation.mvp.view.SettingView;
import com.tribe.app.presentation.utils.PermissionUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerConstants;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.view.activity.SettingActivity;
import com.tribe.app.presentation.view.component.SettingFilterView;
import com.tribe.app.presentation.view.component.SettingItemView;
import com.tribe.app.presentation.view.component.SettingThemeView;
import com.tribe.app.presentation.view.dialog_fragment.LocationDialogFragment;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.Weather;
import com.tribe.app.presentation.view.widget.TextViewFont;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by horatiothomas on 9/6/16.
 */
public class SettingFragment extends BaseFragment implements SettingView {

    @BindView(R.id.settingsProfile)
    SettingItemView settingsProfile;

    @BindView(R.id.settingsFilter)
    SettingFilterView settingFilterView;

    @BindView(R.id.settingsTheme)
    SettingThemeView settingThemeView;

    @BindView(R.id.messageSettingMemories)
    SettingItemView messageSettingMemories;

    @BindView(R.id.messageSettingContext)
    SettingItemView messageSettingContext;

    @BindView(R.id.messageSettingVoice)
    SettingItemView messageSettingVoice;

    @BindView(R.id.messageSettingPreload)
    SettingItemView messageSettingPreload;

    @BindView(R.id.messageSettingFahrenheit)
    SettingItemView messageSettingFahrenheit;

    @BindView(R.id.settingsFacebook)
    SettingItemView settingsFacebook;

    @BindView(R.id.settingsAddress)
    SettingItemView settingsAddress;

    @BindView(R.id.settingsInvisible)
    SettingItemView settingsInvisible;

    @BindView(R.id.settingsTweet)
    SettingItemView settingsTweet;

    @BindView(R.id.settingsEmail)
    SettingItemView settingsEmail;

    @BindView(R.id.settingsRateApp)
    SettingItemView settingsRateApp;

    @BindView(R.id.settingsBlocked)
    SettingItemView settingsBlocked;

    @BindView(R.id.settingsLogOut)
    SettingItemView settingsLogOut;

    @BindView(R.id.txtVersion)
    TextViewFont txtVersion;

    @Inject
    ReactiveLocationProvider reactiveLocationProvider;

    @Inject
    AccessToken accessToken;

    @Inject
    @WeatherUnits
    Preference<String> weatherUnits;

    @Inject
    @LocationContext
    Preference<Boolean> locationContext;

    @Inject
    @LocationPopup
    Preference<Boolean> locationPopup;

    @Inject
    @AudioDefault
    Preference<Boolean> audioDefault;

    @Inject
    @Preload
    Preference<Boolean> preload;

    @Inject
    @AddressBook
    Preference<Boolean> addressBook;

    @Inject
    SettingPresenter settingPresenter;

    // VARIABLES
    private User user;
    Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    public static SettingFragment newInstance() {

        Bundle args = new Bundle();

        SettingFragment fragment = new SettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Lifecycle methods
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        final View fragmentView = inflater.inflate(R.layout.fragment_setting, container, false);

        unbinder = ButterKnife.bind(this, fragmentView);

        initDependencyInjector();
        initUi();
        initSettings();

        settingPresenter.attachView(this);

        return fragmentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        settingFilterView.onResume();
    }

    @Override
    public void onPause() {
        settingFilterView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        unbinder.unbind();

        if (subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
            subscriptions.clear();
        }

        if (settingPresenter != null) {
            settingPresenter.onDestroy();
        }

        super.onDestroy();
    }

    private void initSettings() {
        subscriptions.add(RxView.clicks(settingsProfile).subscribe(aVoid -> {
            ((SettingActivity) getActivity()).goToUpdateProfile();
        }));

        subscriptions.add(messageSettingMemories.checkedSwitch().subscribe(isChecked -> {
            Bundle bundle = new Bundle();
            bundle.putBoolean(TagManagerConstants.MEMORIES_ENABLED, isChecked);
            tagManager.setProperty(bundle);
            settingPresenter.updateUserTribeSave(isChecked);
        }));

        subscriptions.add(messageSettingContext.checkedSwitch().subscribe(isChecked -> {
            if (isChecked && !locationPopup.get() && !PermissionUtils.hasPermissionsLocation(getActivity())) {
                LocationDialogFragment locationDialogFragment = LocationDialogFragment.newInstance();
                locationDialogFragment.show(getFragmentManager(), LocationDialogFragment.class.getName());
                subscriptions.add(locationDialogFragment.onClickYes().subscribe(aVoid -> {
                    locationPopup.set(true);
                    settingPresenter.updateScoreLocation();
                }));

                subscriptions.add(locationDialogFragment.onClickNo().subscribe(aVoid -> {
                    messageSettingContext.setCheckedSwitch(false);
                }));
            }

            Bundle bundle = new Bundle();
            bundle.putBoolean(TagManagerConstants.LOCATION_ENABLED, isChecked);
            tagManager.setProperty(bundle);
            locationContext.set(isChecked);
        }));

        subscriptions.add(messageSettingVoice.checkedSwitch().subscribe(isChecked -> {
            Bundle bundle = new Bundle();
            bundle.putBoolean(TagManagerConstants.AUDIO_ONLY_ENABLED, isChecked);
            tagManager.setProperty(bundle);
            audioDefault.set(isChecked);
        }));

        subscriptions.add(messageSettingPreload.checkedSwitch().subscribe(isChecked -> {
            Bundle bundle = new Bundle();
            bundle.putBoolean(TagManagerConstants.PRELOAD_ENABLED, isChecked);
            tagManager.setProperty(bundle);
            preload.set(isChecked);
        }));

        subscriptions.add(messageSettingFahrenheit.checkedSwitch().subscribe(isChecked -> {
            if (isChecked) weatherUnits.set(Weather.FAHRENHEIT);
            else weatherUnits.set(Weather.CELSIUS);
        }));

        subscriptions.add(settingsFacebook.checkedSwitch().subscribe(isChecked -> {
            if (isChecked)
                settingPresenter.loginFacebook();
            else {
                Bundle bundle = new Bundle();
                bundle.putBoolean(TagManagerConstants.FACEBOOK_CONNECTED, false);
                tagManager.setProperty(bundle);
                settingPresenter.updateUserFacebook(null);
                FacebookUtils.logout();
            }
        }));

        subscriptions.add(RxView.clicks(settingsAddress).subscribe(aVoid -> {
            RxPermissions.getInstance(getContext())
                    .request(Manifest.permission.READ_CONTACTS)
                    .subscribe(hasPermission -> {
                        if (hasPermission) {
                            settingPresenter.lookupContacts();
                        }
                    });
        }));

        subscriptions.add(settingsInvisible.checkedSwitch().subscribe(isChecked -> {
            Bundle bundle = new Bundle();
            bundle.putBoolean(TagManagerConstants.INVISIBLE_MODE_ENABLED, isChecked);
            tagManager.setProperty(bundle);
            settingPresenter.updateUserInvisibleMode(isChecked);
        }));

        subscriptions.add(RxView.clicks(settingsTweet).subscribe(aVoid -> {
            navigator.tweet(getActivity(), "@HeyTribe");
        }));

        subscriptions.add(RxView.clicks(settingsRateApp).subscribe(aVoid -> {
            navigator.rateApp(getActivity());
            settingPresenter.updateScoreRateApp();
        }));

        subscriptions.add(RxView.clicks(settingsEmail).subscribe(aVoid -> {
            String[] addresses = {getString(R.string.settings_email_address)};
            navigator.composeEmail(getActivity(), addresses, getString(R.string.settings_email_subject));
        }));

        subscriptions.add(RxView.clicks(settingsBlocked).subscribe(aVoid -> {
            ((SettingActivity) getActivity()).goToBlock();
        }));

        subscriptions.add(RxView.clicks(settingsLogOut).subscribe(aVoid -> {
            DialogFactory.createConfirmationDialog(getContext(),
                    getString(R.string.settings_logout_title), getString(R.string.settings_logout_confirm_message), getString(R.string.settings_logout_title),
                    (dialog, which) -> {
                        ProgressDialog pd = new ProgressDialog(getContext());
                        pd.setTitle(R.string.settings_logout_wait);
                        pd.show();
                        settingPresenter.logout();
                    }).show();
        }));
    }

    private void initUi() {
        user = getCurrentUser();

        settingsProfile.setPicture(user.getProfilePicture());
        settingsProfile.setTitleBodyViewType(getString(R.string.settings_profile_title),
                getString(R.string.settings_profile_subtitle),
                SettingItemView.MORE);

        messageSettingMemories.setTitleBodyViewType(getString(R.string.settings_tribesave_title),
                getString(R.string.settings_tribesave_subtitle),
                SettingItemView.SWITCH);
        messageSettingMemories.setCheckedSwitch(user.isTribeSave());

        messageSettingContext.setTitleBodyViewType(getString(R.string.settings_geolocation_title),
                getString(R.string.settings_geolocation_subtitle),
                SettingItemView.SWITCH);
        messageSettingContext.setCheckedSwitch(locationContext.get());

        messageSettingVoice.setTitleBodyViewType(getString(R.string.settings_audio_title),
                getString(R.string.settings_audio_subtitle),
                SettingItemView.SWITCH);
        messageSettingVoice.setCheckedSwitch(audioDefault.get());

        messageSettingPreload.setTitleBodyViewType(getString(R.string.settings_preload_title),
                getString(R.string.settings_preload_subtitle),
                SettingItemView.SWITCH);
        messageSettingPreload.setCheckedSwitch(preload.get());

        messageSettingFahrenheit.setTitleBodyViewType(getString(R.string.settings_weatherunits_title),
                getString(R.string.settings_weatherunits_subtitle),
                SettingItemView.SWITCH);

        if (weatherUnits.get().equals(Weather.FAHRENHEIT)) messageSettingFahrenheit.setCheckedSwitch(true);

        else messageSettingFahrenheit.setCheckedSwitch(false);

        settingsFacebook.setTitleBodyViewType(getString(R.string.settings_facebook_sync_title),
                getString(R.string.settings_facebook_not_synced_description),
                SettingItemView.SWITCH);
        settingsFacebook.setIcon(R.drawable.picto_black_facebook_icon);
        settingsFacebook.setSyncUp(FacebookUtils.isLoggedIn() ? R.color.blue_text : R.color.red_circle);
        settingsFacebook.setCheckedSwitch(FacebookUtils.isLoggedIn());

        settingsAddress.setTitleBodyViewType(getString(R.string.settings_addressbook_sync_title),
                getString(R.string.contacts_section_addressbook_sync_description),
                SettingItemView.SWITCH);
        settingsAddress.setSyncUp(addressBook.get() ? R.color.blue_text : R.color.red_circle);
        settingsAddress.setIcon(R.drawable.picto_phone_icon);
        settingsAddress.setCheckedSwitch(addressBook.get());

        settingsInvisible.setTitleBodyViewType(getString(R.string.settings_invisible_title),
                getString(R.string.settings_invisible_subtitle),
                SettingItemView.SWITCH);
        settingsInvisible.setCheckedSwitch(user.isInvisibleMode());
        settingsInvisible.setIcon(R.drawable.picto_ghost_black);

        settingsTweet.setTitleBodyViewType(getString(R.string.settings_tweet_title),
                getString(R.string.settings_tweet_subtitle),
                SettingItemView.SIMPLE);

        settingsEmail.setTitleBodyViewType(getString(R.string.settings_email_title),
                getString(R.string.settings_email_subtitle),
                SettingItemView.SIMPLE);

        settingsRateApp.setTitleBodyViewType(getString(R.string.settings_rate_title),
                getString(R.string.settings_rate_subtitle),
                SettingItemView.SIMPLE);

        settingsBlocked.setTitleBodyViewType(getString(R.string.settings_blocked_title),
                getString(R.string.settings_blocked_subtitle),
                SettingItemView.MORE);

        settingsLogOut.setTitleBodyViewType(getString(R.string.settings_logout_title),
                getString(R.string.settings_logout_subtitle),
                SettingItemView.SIMPLE);

        txtVersion.setText(getString(R.string.settings_version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
    }

    public void setPicture(String profilePicUrl) {
        settingsProfile.setPicture(profilePicUrl);
    }

    private void initDependencyInjector() {
        DaggerUserComponent.builder()
                .activityModule(getActivityModule())
                .applicationComponent(getApplicationComponent())
                .build().inject(this);
    }

    @Override
    public void goToLauncher() {
        getApplication().logoutUser();
        navigator.navigateToLogout(getActivity());
        getActivity().finish();
    }

    @Override
    public void successUpdateUser(User user) {

    }

    @Override
    public void successFacebookLogin() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(TagManagerConstants.FACEBOOK_CONNECTED, true);
        tagManager.setProperty(bundle);
        settingPresenter.updateUserFacebook(com.facebook.AccessToken.getCurrentAccessToken().getUserId());
    }

    @Override
    public void errorFacebookLogin() {
        settingsFacebook.setCheckedSwitch(false);
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
    public void showRetry() {

    }

    @Override
    public void hideRetry() {

    }

    @Override
    public void showError(String message) {

    }

    @Override
    public Context context() {
        return null;
    }
}
