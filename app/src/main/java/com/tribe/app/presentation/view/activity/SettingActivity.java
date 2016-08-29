package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.ScrollView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.MessageSetting;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.view.component.SettingMessageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

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

    @BindView(R.id.scrollSetting)
    ScrollView scrollSetting;

    @BindView(R.id.messageSettingMemories)
    SettingMessageView messageSettingMemories;

    @BindView(R.id.messageSettingContext)
    SettingMessageView messageSettingContext;

    @BindView(R.id.messageSettingVoice)
    SettingMessageView messageSettingVoice;

    @BindView(R.id.messageSettingPreload)
    SettingMessageView messageSettingPreload;

    @BindView(R.id.messageSettingFahrenheit)
    SettingMessageView messageSettingFahrenheit;

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
        messageSettingMemories.setTitleBody(getString(R.string.settings_tribesave_title),
                getString(R.string.settings_tribesave_subtitle));
        messageSettingContext.setTitleBody(getString(R.string.settings_geolocation_title),
                getString(R.string.settings_geolocation_subtitle));
        messageSettingVoice.setTitleBody(getString(R.string.settings_audio_title),
                getString(R.string.settings_audio_subtitle));
        messageSettingPreload.setTitleBody(getString(R.string.settings_preload_title),
                getString(R.string.settings_preload_subtitle));
        messageSettingFahrenheit.setTitleBody(getString(R.string.settings_weatherunits_title),
                getString(R.string.settings_weatherunits_subtitle));
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
