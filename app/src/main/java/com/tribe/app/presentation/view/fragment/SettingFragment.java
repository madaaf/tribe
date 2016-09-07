package com.tribe.app.presentation.view.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.f2prateek.rx.preferences.Preference;
import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.internal.di.scope.AudioDefault;
import com.tribe.app.presentation.internal.di.scope.LocationContext;
import com.tribe.app.presentation.internal.di.scope.Memories;
import com.tribe.app.presentation.internal.di.scope.Preload;
import com.tribe.app.presentation.internal.di.scope.WeatherUnits;
import com.tribe.app.presentation.mvp.presenter.SettingPresenter;
import com.tribe.app.presentation.mvp.view.SettingView;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.view.activity.BaseActivity;
import com.tribe.app.presentation.view.activity.SettingActivity;
import com.tribe.app.presentation.view.component.SettingItemView;
import com.tribe.app.presentation.view.utils.Weather;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by horatiothomas on 9/6/16.
 */
public class SettingFragment extends BaseFragment {


    public static SettingFragment newInstance() {

        Bundle args = new Bundle();

        SettingFragment fragment = new SettingFragment();
        fragment.setArguments(args);
        return fragment;
    }

    User user;
    Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @BindView(R.id.settingsProfile)
    SettingItemView settingsProfile;
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


    @Inject
    @WeatherUnits
    Preference<String> weatherUnits;
    @Inject
    @Memories
    Preference<Boolean> memories;
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
    Navigator navigator;
    @Inject
    SettingPresenter settingPresenter;

    /**
     * Lifecycle methods
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        final View fragmentView = inflater.inflate(R.layout.fragment_setting, container, false);

        unbinder = ButterKnife.bind(this, fragmentView);

        initUi();
        initSettings();
        initDependencyInjector();

        return fragmentView;
    }

    @Override
    public void onDestroy() {
        unbinder.unbind();

        if (subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
            subscriptions.clear();
        }

        super.onDestroy();
    }

    private void initSettings() {
        subscriptions.add(RxView.clicks(settingsProfile).subscribe(aVoid -> {
            ((SettingActivity) getActivity()).goToUpdateProfile();
        }));

        subscriptions.add(messageSettingMemories.checkedSwitch().subscribe(isChecked -> {
            if (isChecked) memories.set(true);
            else memories.set(false);
        }));

        subscriptions.add(messageSettingContext.checkedSwitch().subscribe(isChecked -> {
            if (isChecked) locationContext.set(true);
            else locationContext.set(false);
        }));

        subscriptions.add(messageSettingVoice.checkedSwitch().subscribe(isChecked -> {
            if (isChecked) audioDefault.set(true);
            else audioDefault.set(false);
        }));

        subscriptions.add(messageSettingPreload.checkedSwitch().subscribe(isChecked -> {
            if (isChecked) preload.set(true);
            else preload.set(false);
        }));

        subscriptions.add(messageSettingFahrenheit.checkedSwitch().subscribe(isChecked -> {
            if (isChecked) weatherUnits.set(Weather.FAHRENHEIT);
            else weatherUnits.set(Weather.CELSIUS);
        }));

        subscriptions.add(RxView.clicks(settingsTweet).subscribe(aVoid -> {
            navigator.tweet(getActivity(), "@HeyTribe");
        }));

        subscriptions.add(RxView.clicks(settingsRateApp).subscribe(aVoid -> {
            navigator.rateApp(getActivity());
        }));

        subscriptions.add(RxView.clicks(settingsEmail).subscribe(aVoid -> {
            String[] addresses = {getString(R.string.settings_email_address)};
            navigator.composeEmail(getActivity(), addresses, getString(R.string.settings_email_subject));
        }));

        subscriptions.add(RxView.clicks(settingsLogOut).subscribe(aVoid -> {
            settingPresenter.logout();
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
        messageSettingContext.setTitleBodyViewType(getString(R.string.settings_geolocation_title),
                getString(R.string.settings_geolocation_subtitle),
                SettingItemView.SWITCH);
        messageSettingVoice.setTitleBodyViewType(getString(R.string.settings_audio_title),
                getString(R.string.settings_audio_subtitle),
                SettingItemView.SWITCH);
        messageSettingPreload.setTitleBodyViewType(getString(R.string.settings_preload_title),
                getString(R.string.settings_preload_subtitle),
                SettingItemView.SWITCH);
        messageSettingFahrenheit.setTitleBodyViewType(getString(R.string.settings_weatherunits_title),
                getString(R.string.settings_weatherunits_subtitle),
                SettingItemView.SWITCH);

        // TODO: setup based on sync status
        settingsFacebook.setTitleBodyViewType(getString(R.string.settings_facebook_sync_title),
                getString(R.string.settings_facebook_not_synced_description),
                SettingItemView.SWITCH);

        settingsFacebook.setSyncUp(R.color.red_circle, R.drawable.picto_black_facebook_icon);

        settingsAddress.setTitleBodyViewType(getString(R.string.settings_addressbook_sync_title),
                getString(R.string.contacts_section_addressbook_sync_description),
                SettingItemView.SIMPLE);

        settingsAddress.setSyncUp(R.color.blue_text, R.drawable.picto_phone_icon);


        settingsInvisible.setTitleBodyViewType(getString(R.string.settings_invisible_title),
                getString(R.string.settings_invisible_subtitle),
                SettingItemView.SWITCH);

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

    }

    public void setPicture(String profilePicUrl) {
        settingsProfile.setPicture(profilePicUrl);
    }

    protected ApplicationComponent getApplicationComponent() {
        return ((AndroidApplication) getActivity().getApplication()).getApplicationComponent();
    }

    protected ActivityModule getActivityModule() {
        return new ActivityModule(getActivity());
    }

    private void initDependencyInjector() {
        DaggerUserComponent.builder()
                .activityModule(getActivityModule())
                .applicationComponent(getApplicationComponent())
                .build().inject(this);
    }
}
