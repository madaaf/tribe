package com.tribe.app.presentation.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.f2prateek.rx.preferences.Preference;
import com.jakewharton.rxbinding.view.RxView;
import com.squareup.picasso.Picasso;
import com.tribe.app.R;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.scope.AudioDefault;
import com.tribe.app.presentation.internal.di.scope.LocationContext;
import com.tribe.app.presentation.internal.di.scope.Memories;
import com.tribe.app.presentation.internal.di.scope.Preload;
import com.tribe.app.presentation.internal.di.scope.WeatherUnits;
import com.tribe.app.presentation.mvp.presenter.SettingPresenter;
import com.tribe.app.presentation.mvp.view.SettingView;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.view.component.SettingSectionView;
import com.tribe.app.presentation.view.component.SettingItemView;
import com.tribe.app.presentation.view.fragment.ProfileInfoFragment;
import com.tribe.app.presentation.view.utils.Weather;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

/**
 * SettingActivity.java
 * Created by horatiothomas on 8/26/16.
 */
public class SettingActivity extends BaseActivity implements SettingView {

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, SettingActivity.class);
    }

    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();

//    @BindView(R.id.settingsPicture)
//    SettingItemView settingsPicture;
//
//    @BindView(R.id.settingsDisplayName)
//    SettingItemView settingsDisplayName;
//
//    @BindView(R.id.settingsUsername)
//    SettingItemView settingsUsername;

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

//    @BindView(R.id.profileSection)
//    SettingSectionView profileSection;

//    @BindView(R.id.messageSection)
//    SettingSectionView messageSection;
//
//    @BindView(R.id.supportSection)
//    SettingSectionView supportSection;
//
//    @BindView(R.id.exitSection)
//    SettingSectionView exitSection;

    @BindView(R.id.imgDone)
    ImageView imgDone;

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

    @Inject
    Picasso picasso;

    private static final int CAMERA_REQUEST = 6;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initUi();
        initDependencyInjector();
        initSettings();
        initPresenter();
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) unbinder.unbind();

        if (subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
            subscriptions.clear();
        }

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");


            String imageUri = Uri.fromFile(FileUtils.bitmapToFile(thumbnail, this)).toString();

            InputStream image_stream = null;
            try {
                image_stream = getContentResolver().openInputStream(Uri.parse(imageUri));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            thumbnail = BitmapFactory.decodeStream(image_stream );

//            settingsPicture.setPictureBitmap(thumbnail);
            settingPresenter.updateUser("picture", imageUri);
        }

    }




    private void initPresenter() {
        settingPresenter.attachView(this);
    }

    private void initSettings() {

//        subscriptions.add(RxView.clicks(settingsPicture).subscribe(aVoid -> {
//            // Get picture and set
//            navigator.getImageFromCamera(this, CAMERA_REQUEST);
//
//        }));
//
//        subscriptions.add(RxView.clicks(settingsDisplayName).subscribe(aVoid -> {
//            settingPresenter.updateUser("display_name", "Horatio 101");
//        }));
//
//        subscriptions.add(RxView.clicks(settingsUsername).subscribe(aVoid -> {
//            settingPresenter.updateUser("username", "Horatio T");
//        }));



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
            navigator.tweet(this, "@HeyTribe");
        }));

        subscriptions.add(RxView.clicks(settingsRateApp).subscribe(aVoid -> {
            navigator.rateApp(this);
        }));

        subscriptions.add(RxView.clicks(settingsEmail).subscribe(aVoid -> {
            String[] addresses = {getString(R.string.settings_email_address)};
            navigator.composeEmail(this, addresses, getString(R.string.settings_email_subject));
        }));

        subscriptions.add(RxView.clicks(imgDone).subscribe(aVoid -> {
            Intent resultIntent = new Intent();
            setResult(BaseActivity.RESULT_OK, resultIntent);
            finish();
        }));

        subscriptions.add(RxView.clicks(settingsLogOut).subscribe(aVoid -> {
            settingPresenter.logout();
        }));

    }

    private void initUi() {
        setContentView(R.layout.activity_setting);
        unbinder = ButterKnife.bind(this);

//        profileSection.setTitleIcon(R.string.settings_section_profile, R.drawable.picto_profile_icon);
//        messageSection.setTitleIcon(R.string.settings_section_messages, R.drawable.picto_setting_message_icon);
//        supportSection.setTitleIcon(R.string.settings_section_support, R.drawable.picto_setting_support_icon);
//        exitSection.setTitleIcon(R.string.settings_section_exit, R.drawable.picto_setting_exit_icon);

//        settingsPicture.setTitleBodyViewType(getString(R.string.settings_picture_title),
//                getString(R.string.settings_picture_subtitle),
//                SettingItemView.PICTURE);
//        settingsDisplayName.setTitleBodyViewType(getString(R.string.settings_displayname_title),
//                getString(R.string.settings_displayname_subtitle),
//                SettingItemView.NAME);
//        settingsUsername.setTitleBodyViewType(getString(R.string.settings_username_title),
//                getString(R.string.settings_username_subtitle),
//                SettingItemView.NAME);

        User user = getCurrentUser();

        settingsProfile.setTitleBodyViewType(getString(R.string.settings_profile_title),
                getString(R.string.settings_profile_subtitle),
                SettingItemView.MORE);

        settingsProfile.setPicture(user.getProfilePicture());

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


//        settingsUsername.setName(user.getUsername());
//        settingsDisplayName.setName(user.getDisplayName());

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


    @Override
    public void changeUsername(String username) {
//        settingsUsername.setName(username);
    }

    @Override
    public void changeDisplayName(String displayName) {
//        settingsDisplayName.setName(displayName);
    }

    @Override
    public void goToLauncher() {
        navigator.navigateToLauncher(this);
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
        return this;
    }
}
