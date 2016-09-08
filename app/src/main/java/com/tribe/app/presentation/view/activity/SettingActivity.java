package com.tribe.app.presentation.view.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;

import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.SettingPresenter;
import com.tribe.app.presentation.mvp.view.SettingView;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.view.component.ProfileInfoView;
import com.tribe.app.presentation.view.fragment.SettingBlockFragment;
import com.tribe.app.presentation.view.fragment.SettingFragment;
import com.tribe.app.presentation.view.fragment.SettingUpdateProfileFragment;
import com.tribe.app.presentation.view.widget.CustomViewPager;
import com.tribe.app.presentation.view.widget.TextViewFont;

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

    @BindView(R.id.imgBack)
    ImageView imgBack;

    @BindView(R.id.imgDone)
    ImageView imgDone;

    @BindView(R.id.txtTitle)
    TextViewFont txtTitle;

    private SettingFragment settingFragment;
    private SettingUpdateProfileFragment settingUpdateProfileFragment;
    private SettingBlockFragment settingBlockFragment;

    private final static int PAGE_MAIN = 0, PAGE_UPDATE = 1, PAGE_BLOCK = 2;

    private int shortDuration = 150;

    @BindView(R.id.viewPager)
    CustomViewPager viewPager;

    @Inject
    SettingPresenter settingPresenter;

    String pictureUri = null;

    private static final int CAMERA_REQUEST = 6;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initUi();
        initViewPager();
        initDependencyInjector();
        initPresenter();
    }

    @Override
    protected void onPause() {
        txtTitle.animate().setListener(null).start();

        super.onPause();
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

            settingUpdateProfileFragment.setImgProfilePic(thumbnail);

            String imageUri = Uri.fromFile(FileUtils.bitmapToFile(thumbnail, this)).toString();

            pictureUri = imageUri;
        }

        // 2. Get image from Gallery
        if (requestCode == ProfileInfoView.RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            pictureUri = selectedImage.toString();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            Bitmap thumbnail = BitmapFactory.decodeFile(picturePath);

            if (thumbnail != null) {
                settingUpdateProfileFragment.setImgProfilePic(thumbnail);
            }
        }
    }


    private void initUi() {
        setContentView(R.layout.activity_setting);
        unbinder = ButterKnife.bind(this);

        subscriptions.add(RxView.clicks(imgBack).subscribe(aVoid -> {
            goToMain();
        }));

        subscriptions.add(RxView.clicks(imgDone).subscribe(aVoid -> {
            if (viewPager.getCurrentItem() == PAGE_MAIN) {
                Intent resultIntent = new Intent();
                setResult(BaseActivity.RESULT_OK, resultIntent);
                finish();
            }
            if (viewPager.getCurrentItem() == PAGE_UPDATE) {
                settingPresenter.updateUser(settingUpdateProfileFragment.getUsername(), settingUpdateProfileFragment.getDisplayName(), pictureUri);
                goToMain();
            }
        }));
    }

    private void updateAnim() {
        txtTitle.animate()
                .alpha(0)
                .setStartDelay(0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        txtTitle.animate()
                                .alpha(1)
                                .setStartDelay(0)
                                .setDuration(shortDuration)
                                .translationX(10)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        txtTitle.animate()
                                                .translationX(0)
                                                .setDuration(shortDuration)
                                                .start();
                                    }
                                })
                                .start();
                    }
                })
                .start();

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
        txtTitle.animate()
                .alpha(0)
                .setStartDelay(0)
                .setDuration(shortDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        txtTitle.animate()
                                .alpha(1)
                                .setStartDelay(0)
                                .setDuration(shortDuration)
                                .start();
                    }
                })
                .start();

        imgBack.animate()
                .alpha(0)
                .setStartDelay(0)
                .start();
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

    public void goToMain() {
        viewPager.setCurrentItem(PAGE_MAIN);
        txtTitle.setText(getString(R.string.settings_title));
        imgDone.setAlpha(1f);
        mainSettingAnim();
    }

    public void goToUpdateProfile() {
        viewPager.setCurrentItem(PAGE_UPDATE);
        txtTitle.setText(getString(R.string.settings_profile_title));
        updateAnim();
    }

    public void goToBlock() {
        viewPager.setCurrentItem(PAGE_BLOCK);
        txtTitle.setText(getString(R.string.hiddenblocked_empty_title));
        imgDone.setAlpha(0f);
        updateAnim();
    }


    private class SetttingPageTransformer implements ViewPager.PageTransformer {

        @Override
        public void transformPage(View page, float position) {

        }
    }


    @Override
    public void updateUser(String username, String displayName, String pictureUri) {

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
        return null;
    }


    @Override
    public void setProfilePic(String profilePicUrl) {
        settingFragment.setPicture(profilePicUrl);
    }

    private void initPresenter() {
        settingPresenter.attachView(this);
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
