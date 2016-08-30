package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.ScrollView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.MessageSetting;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.view.component.SettingSectionView;
import com.tribe.app.presentation.view.component.SettingView;

import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

/**
 * SettingActivity.java
 * Created by horatiothomas on 8/26/16.
 */
public class SettingActivity extends BaseActivity {

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, SettingActivity.class);
    }

//    @Inject
//    Preference<Boolean> shareLocation;


    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @BindView(R.id.settingsPicture)
    SettingView settingsPicture;

    @BindView(R.id.settingsDisplayName)
    SettingView settingsDisplayName;

    @BindView(R.id.settingsUsername)
    SettingView settingsUsername;

    @BindView(R.id.messageSettingMemories)
    SettingView messageSettingMemories;

    @BindView(R.id.messageSettingContext)
    SettingView messageSettingContext;

    @BindView(R.id.messageSettingVoice)
    SettingView messageSettingVoice;

    @BindView(R.id.messageSettingPreload)
    SettingView messageSettingPreload;

    @BindView(R.id.messageSettingFahrenheit)
    SettingView messageSettingFahrenheit;

    @BindView(R.id.settingsTweet)
    SettingView settingsTweet;

    @BindView(R.id.settingsEmail)
    SettingView settingsEmail;

    @BindView(R.id.settingsRateApp)
    SettingView settingsRateApp;

    @BindView(R.id.settingsLogOut)
    SettingView settingsLogOut;

    @BindView(R.id.settingsRemove)
    SettingView settingsRemove;

    @BindView(R.id.profileSection)
    SettingSectionView profileSection;

    @BindView(R.id.messageSection)
    SettingSectionView messageSection;

    @BindView(R.id.supportSection)
    SettingSectionView supportSection;

    @BindView(R.id.exitSection)
    SettingSectionView exitSection;

    private LinearLayoutManager linearLayoutManager;

    private List<MessageSetting> listMessageSetting;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initUi();
        initDependencyInjector();
        initMessageSettings();
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) unbinder.unbind();

        super.onDestroy();
    }

    private void initUi() {
        setContentView(R.layout.activity_setting);
        unbinder = ButterKnife.bind(this);
    }

    private void initMessageSettings() {

        profileSection.setTitleIcon(R.string.settings_section_profile, R.drawable.picto_profile_icon);
        messageSection.setTitleIcon(R.string.settings_section_messages, R.drawable.picto_setting_message_icon);
        supportSection.setTitleIcon(R.string.settings_section_support, R.drawable.picto_setting_support_icon);
        exitSection.setTitleIcon(R.string.settings_section_exit, R.drawable.picto_setting_exit_icon);

        settingsPicture.setTitleBodyViewType(getString(R.string.settings_picture_title),
                getString(R.string.settings_picture_subtitle),
                SettingView.PICTURE);
        settingsDisplayName.setTitleBodyViewType(getString(R.string.settings_displayname_title),
                getString(R.string.settings_displayname_subtitle),
                SettingView.NAME);
        settingsUsername.setTitleBodyViewType(getString(R.string.settings_username_title),
                getString(R.string.settings_username_subtitle),
                SettingView.NAME);

        messageSettingMemories.setTitleBodyViewType(getString(R.string.settings_tribesave_title),
                getString(R.string.settings_tribesave_subtitle),
                SettingView.SWITCH);
        messageSettingContext.setTitleBodyViewType(getString(R.string.settings_geolocation_title),
                getString(R.string.settings_geolocation_subtitle),
                SettingView.SWITCH);
        messageSettingVoice.setTitleBodyViewType(getString(R.string.settings_audio_title),
                getString(R.string.settings_audio_subtitle),
                SettingView.SWITCH);
        messageSettingPreload.setTitleBodyViewType(getString(R.string.settings_preload_title),
                getString(R.string.settings_preload_subtitle),
                SettingView.SWITCH);
        messageSettingFahrenheit.setTitleBodyViewType(getString(R.string.settings_weatherunits_title),
                getString(R.string.settings_weatherunits_subtitle),
                SettingView.SWITCH);

        settingsTweet.setTitleBodyViewType(getString(R.string.settings_tweet_title),
                getString(R.string.settings_tweet_subtitle),
                SettingView.SIMPLE);
        settingsEmail.setTitleBodyViewType(getString(R.string.settings_email_title),
                getString(R.string.settings_email_subtitle),
                SettingView.SIMPLE);
        settingsRateApp.setTitleBodyViewType(getString(R.string.settings_rate_title),
                getString(R.string.settings_rate_subtitle),
                SettingView.SIMPLE);
        settingsLogOut.setTitleBodyViewType(getString(R.string.settings_logout_title),
                getString(R.string.settings_logout_subtitle),
                SettingView.SIMPLE);
        settingsRemove.setTitleBodyViewType(getString(R.string.settings_logout_title),
                getString(R.string.settings_logout_subtitle),
                SettingView.DELETE);
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
