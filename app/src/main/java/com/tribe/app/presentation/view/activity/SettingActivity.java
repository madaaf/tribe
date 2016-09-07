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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
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
import com.tribe.app.presentation.view.fragment.AccessFragment;
import com.tribe.app.presentation.view.fragment.IntroViewFragment;
import com.tribe.app.presentation.view.fragment.ProfileInfoFragment;
import com.tribe.app.presentation.view.fragment.SettingBlockFragment;
import com.tribe.app.presentation.view.fragment.SettingFragment;
import com.tribe.app.presentation.view.fragment.SettingUpdateProfileFragment;
import com.tribe.app.presentation.view.utils.Weather;
import com.tribe.app.presentation.view.widget.CustomViewPager;

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
public class SettingActivity extends BaseActivity {

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, SettingActivity.class);
    }

    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();


    @BindView(R.id.imgDone)
    ImageView imgDone;

    private SettingFragment settingFragment;
    private SettingUpdateProfileFragment settingUpdateProfileFragment;
    private SettingBlockFragment settingBlockFragment;

    private final static int PAGE_MAIN = 0, PAGE_UPDATE = 1, PAGE_BLOCK = 2;

    @BindView(R.id.viewPager)
    CustomViewPager viewPager;

    @Inject
    Picasso picasso;

    private static final int CAMERA_REQUEST = 6;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initUi();
        initViewPager();
        initDependencyInjector();
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) unbinder.unbind();

        super.onDestroy();
    }

    private void initViewPager() {
        IntroViewPagerAdapter introViewPagerAdapter = new IntroViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(introViewPagerAdapter);
        viewPager.setOffscreenPageLimit(4);
        viewPager.setScrollDurationFactor(2f);
        viewPager.setCurrentItem(PAGE_MAIN);
        viewPager.setAllowedSwipeDirection(CustomViewPager.SWIPE_MODE_NONE);
        viewPager.setPageTransformer(false, new SetttingPageTransformer());
        viewPager.setSwipeable(false);
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
//            settingPresenter.updateUser("picture", imageUri);
        }

    }


    private void initUi() {
        setContentView(R.layout.activity_setting);
        unbinder = ButterKnife.bind(this);

        subscriptions.add(RxView.clicks(imgDone).subscribe(aVoid -> {
            Intent resultIntent = new Intent();
            setResult(BaseActivity.RESULT_OK, resultIntent);
            finish();
        }));

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



//        settingsUsername.setName(user.getUsername());
//        settingsDisplayName.setName(user.getDisplayName());

    }


    /**
     * Initialize fragment view pager adapter
     */

    private class IntroViewPagerAdapter extends FragmentPagerAdapter {

        private static final int NUM_ITEMS = 3;

        public IntroViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    settingFragment = SettingFragment.newInstance();
                    return settingFragment;
                case 1:
                    settingUpdateProfileFragment = SettingUpdateProfileFragment.newInstance();
                    return settingUpdateProfileFragment;
                case 2:
                    settingBlockFragment = SettingBlockFragment.newInstance();
                    return settingBlockFragment;
                default:
                    settingFragment = SettingFragment.newInstance();
                    return settingFragment;
            }
        }

    }

    private class SetttingPageTransformer implements ViewPager.PageTransformer {

        @Override
        public void transformPage(View page, float position) {

        }
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
