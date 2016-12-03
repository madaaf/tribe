package com.tribe.app.presentation.view.fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.tribe.app.presentation.internal.di.scope.DebugMode;
import com.tribe.app.presentation.internal.di.scope.LastSync;
import com.tribe.app.presentation.internal.di.scope.LocationContext;
import com.tribe.app.presentation.internal.di.scope.Preload;
import com.tribe.app.presentation.internal.di.scope.ShareProfile;
import com.tribe.app.presentation.internal.di.scope.WeatherUnits;
import com.tribe.app.presentation.mvp.presenter.SettingPresenter;
import com.tribe.app.presentation.mvp.view.SettingView;
import com.tribe.app.presentation.utils.PermissionUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerConstants;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.view.activity.SettingActivity;
import com.tribe.app.presentation.view.component.ActionView;
import com.tribe.app.presentation.view.component.SettingFilterView;
import com.tribe.app.presentation.view.component.SettingItemView;
import com.tribe.app.presentation.view.component.SettingThemeView;
import com.tribe.app.presentation.view.dialog_fragment.ShareDialogProfileFragment;
import com.tribe.app.presentation.view.utils.Constants;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.ScoreUtils;
import com.tribe.app.presentation.view.utils.Weather;
import com.tribe.app.presentation.view.widget.TextViewFont;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by horatiothomas on 9/6/16.
 */
public class SettingFragment extends BaseFragment implements SettingView {

    @BindView(R.id.settingsProfile)
    SettingItemView settingsProfile;

    @BindView(R.id.viewActionShareProfile)
    ActionView viewActionShareProfile;

    @BindView(R.id.viewActionPoints)
    ActionView viewActionPoints;

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

    @BindView(R.id.settingsDebugMode)
    SettingItemView settingsDebugMode;

    @BindView(R.id.layoutDebugMode)
    ViewGroup layoutDebugMode;

    @BindView(R.id.txtVersion)
    TextViewFont txtVersion;

    @Inject
    ReactiveLocationProvider reactiveLocationProvider;

    @Inject
    AccessToken accessToken;

    @Inject
    @ShareProfile
    Preference<Boolean> shareProfile;

    @Inject
    @WeatherUnits
    Preference<String> weatherUnits;

    @Inject
    @LocationContext
    Preference<Boolean> locationContext;

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
    @LastSync
    Preference<Long> lastSync;

    @Inject
    @DebugMode
    Preference<Boolean> debugMode;

    @Inject
    SettingPresenter settingPresenter;

    // VARIABLES
    private User user;
    Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private int debugModeCount = 0;

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
        loadContacts();

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

        subscriptions.add(viewActionPoints.onClick().subscribe(aVoid -> {
            navigator.navigateToScorePoints(getActivity());
        }));

        subscriptions.add(viewActionShareProfile.onClick().subscribe(aVoid -> {
            ShareDialogProfileFragment shareDialogProfileFragment = ShareDialogProfileFragment.newInstance();
            shareDialogProfileFragment.show(getFragmentManager(), ShareDialogProfileFragment.class.getName());

            if (!shareProfile.get()) {
                shareProfile.set(true);
                settingPresenter.updateScoreShare();
            }
        }));

        subscriptions.add(messageSettingMemories.checkedSwitch().subscribe(isChecked -> {
            Bundle bundle = new Bundle();
            bundle.putBoolean(TagManagerConstants.MEMORIES_ENABLED, isChecked);
            tagManager.setProperty(bundle);
            settingPresenter.updateUserTribeSave(isChecked);
        }));

        subscriptions.add(messageSettingContext.checkedSwitch().subscribe(isChecked -> {
            if (isChecked && !PermissionUtils.hasPermissionsLocation(getActivity())) {
                RxPermissions.getInstance(getActivity())
                        .request(PermissionUtils.PERMISSIONS_LOCATION)
                        .subscribe(granted -> {
                            if (granted) {
                                settingPresenter.updateScoreLocation();
                            } else {
                                messageSettingContext.setCheckedSwitch(false);
                            }
                        });
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
                settingPresenter.deleteFBContacts();
                Bundle bundle = new Bundle();
                bundle.putBoolean(TagManagerConstants.FACEBOOK_CONNECTED, false);
                tagManager.setProperty(bundle);
                settingPresenter.updateUserFacebook(null);
                FacebookUtils.logout();
                unsetFacebook();
            }
        }));

        subscriptions.add(settingsAddress.checkedSwitch().subscribe(isChecked -> {
            RxPermissions.getInstance(getContext())
                    .request(Manifest.permission.READ_CONTACTS)
                    .subscribe(hasPermission -> {
                        Bundle bundle = new Bundle();
                        bundle.putBoolean(TagManagerConstants.ADDRESS_BOOK_ENABLED, hasPermission);
                        tagManager.setProperty(bundle);

                        if (hasPermission && isChecked) {
                            addressBook.set(true);
                            sync();
                        } else {
                            settingPresenter.deleteABContacts();
                            addressBook.set(false);
                            unsetAddressBook();
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
                    getString(R.string.action_cancel),
                    (dialog, which) -> {
                        ProgressDialog pd = new ProgressDialog(getContext());
                        pd.setTitle(R.string.settings_logout_wait);
                        pd.show();
                        settingPresenter.logout();
                    },
                    null)

                    .show();
        }));

        subscriptions.add(RxView.clicks(settingsDebugMode).subscribe(aVoid -> {
            navigator.navigateToDebugMode(getActivity());
        }));
    }

    private void initUi() {
        user = getCurrentUser();

        ScoreUtils.Level level = ScoreUtils.getLevelForScore(user.getScore());
        viewActionPoints.setTitle(getString(level.getStringId()));

        viewActionShareProfile.setTitle(getString(R.string.settings_profile_share_title, "@" + user.getUsername()));
        viewActionShareProfile.setBody(getString(R.string.settings_profile_share_title, BuildConfig.TRIBE_URL + "/@" + user.getUsername()));

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
        messageSettingFahrenheit.setCheckedSwitch(weatherUnits.get().equals(Weather.FAHRENHEIT));

        settingsFacebook.setTitleBodyViewType(getString(R.string.settings_facebook_sync_title),
                getString(R.string.settings_facebook_not_synced_description),
                SettingItemView.SWITCH);
        settingsFacebook.setIcon(R.drawable.picto_black_facebook_icon);
        settingsFacebook.setSyncUp(FacebookUtils.isLoggedIn() ? R.color.blue_text : R.color.red_circle);
        settingsFacebook.setCheckedSwitch(FacebookUtils.isLoggedIn());

        settingsAddress.setTitleBodyViewType(getString(R.string.settings_addressbook_sync_title),
                getString(R.string.settings_addressbook_not_synced_description),
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

        settingsDebugMode.setTitleBodyViewType("Access debug mode",
                "Only for OGs",
                SettingItemView.SIMPLE);

        txtVersion.setText(getString(R.string.settings_version, BuildConfig.VERSION_NAME, String.valueOf(BuildConfig.VERSION_CODE)));

        if (debugMode.get()) layoutDebugMode.setVisibility(View.VISIBLE);
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

    private void sync() {
        if (addressBook.get()) {
            settingsAddress.setSyncUp(R.color.grey_light);
            settingsAddress.setTitleBodyViewType(getString(R.string.settings_addressbook_sync_title),
                    getString(R.string.contacts_section_addressbook_syncing),
                    SettingItemView.SWITCH);
        }

        if (FacebookUtils.isLoggedIn()) {
            settingsFacebook.setSyncUp(R.color.grey_light);
            settingsFacebook.setTitleBodyViewType(getString(R.string.settings_facebook_sync_title),
                    getString(R.string.contacts_section_facebook_syncing),
                    SettingItemView.SWITCH);
        }

        lastSync.set(System.currentTimeMillis());
        settingPresenter.lookupContacts();
    }

    private void loadContacts() {
        this.settingPresenter.loadContactsFB();
        this.settingPresenter.loadContactsAddressBook();
    }

    private void unsetFacebook() {
        settingsFacebook.setTitleBodyViewType(getString(R.string.settings_facebook_sync_title),
                getString(R.string.settings_facebook_not_synced_description),
                SettingItemView.SWITCH);
        settingsFacebook.setIcon(R.drawable.picto_black_facebook_icon);
        settingsFacebook.setSyncUp(FacebookUtils.isLoggedIn() ? R.color.blue_text : R.color.red_circle);
        settingsFacebook.setCheckedSwitch(FacebookUtils.isLoggedIn());
    }

    private void unsetAddressBook() {
        settingsAddress.setTitleBodyViewType(getString(R.string.settings_addressbook_sync_title),
                getString(R.string.settings_addressbook_not_synced_description),
                SettingItemView.SWITCH);
        settingsAddress.setSyncUp(addressBook.get() ? R.color.blue_text : R.color.red_circle);
        settingsAddress.setIcon(R.drawable.picto_phone_icon);
        settingsAddress.setCheckedSwitch(addressBook.get());
    }

    @Override
    public void goToLauncher() {
        getApplication().logoutUser();
        navigator.navigateToLogout(getActivity());
        getActivity().finish();
    }

    @Override
    public void onFBContactsSync(int count) {
        if (FacebookUtils.isLoggedIn()) {
            settingsFacebook.setTitleBodyViewType(getString(R.string.settings_facebook_sync_title),
                    getString(R.string.settings_facebook_synced_description, count),
                    SettingItemView.SWITCH);
            settingsFacebook.setSyncUp(R.color.blue_text);
        }
    }

    @Override
    public void onAddressBookContactSync(int count) {
        if (addressBook.get()) {
            settingsAddress.setTitleBodyViewType(getString(R.string.settings_addressbook_sync_title),
                    getString(R.string.settings_addressbook_synced_description, count),
                    SettingItemView.SWITCH);
            settingsAddress.setSyncUp(R.color.blue_text);
        }
    }

    @Override
    public void onSuccessSync() {

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
        sync();
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

    @OnClick(R.id.txtVersion)
    void clickVersion(View v) {
        debugModeCount++;

        if (debugModeCount == Constants.DEBUG_MODE) {
            debugMode.set(true);
            layoutDebugMode.setVisibility(View.VISIBLE);
        } else if (debugModeCount >= 3) {
            Toast.makeText(getActivity(),
                    (Constants.DEBUG_MODE - debugModeCount) + " taps to go before DEBUG MODE", Toast.LENGTH_SHORT).show();
        }
    }
}
