package com.tribe.app.presentation.view.activity;

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
import android.view.ViewGroup;

import com.tribe.app.R;
import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.utils.Extras;
import com.tribe.app.presentation.view.component.ProfileInfoView;
import com.tribe.app.presentation.view.fragment.AccessFragment;
import com.tribe.app.presentation.view.fragment.IntroViewFragment;
import com.tribe.app.presentation.view.fragment.ProfileInfoFragment;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.CustomViewPager;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * IntroActivity.java
 * This is the first activity when app is launched for the first time.
 * Consists of a fragment view pager that is used throughout the onboarding process.
 * The first fragment in the view pager is IntroViewFragment.java
 */

public class IntroActivity extends BaseActivity {

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, IntroActivity.class);
    }

    /**
     * Globals
     */

    private static final int PAGE_INTRO = 0,
            PAGE_PROFILE_INFO = 1,
            PAGE_ACCESS = 2;

    private IntroViewFragment introViewFragment;
    private ProfileInfoFragment profileInfoFragment;
    private AccessFragment accessFragment;

    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    // for ui testing
    public static final boolean uiOnlyMode = false;

    @Inject
    ScreenUtils screenUtils;

    @BindView(R.id.viewPager)
    CustomViewPager viewPager;

    @BindView(R.id.introActivityRoot)
    ViewGroup container;

    /**
     * Lifecycle methods
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUi();
        initViewPager();
        initDependencyInjector();
    }

    @Override
    protected void onDestroy() {
        unbinder.unbind();

        if (subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
            subscriptions.clear();
        }

        super.onDestroy();
    }

    /**
     * onActivityResult here handles four different scenarios:
     * 1. Country code selector
     *    In IntroviewFragment.java, when a user changes their country they are brought to a new activity
     *    and then returned to the fragment, where the results are handled here.
     *
     * 2. Get image from Gallery
     *    In ProfileInfoFragment.java, when a user chooses a photo from their camera for a profile picture,
     *    their picture is setup here
     *
     * 3. Capture image
     *    In ProfileInfoFragment.java a user can take a picture for their profile picture.
     *    The result is handled here.
     *
     * 4. Facebook Login
     *    This handles the Facebook login in ProfileInfoFragment.java.
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 1. Country code selector
        if (requestCode == Navigator.REQUEST_COUNTRY && resultCode == Activity.RESULT_OK) {
            introViewFragment.initPhoneNumberViewWithCountryCode(data.getStringExtra(Extras.COUNTRY_CODE));
        }

        // Load image into profile info

        // 2. Get image from Gallery
        if (requestCode == ProfileInfoView.RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            Bitmap thumbnail = BitmapFactory.decodeFile(picturePath);

            if (thumbnail != null) {
                profileInfoFragment.setImgProfilePic(thumbnail);
            }
        }

        // 3. Capture image
        if (requestCode == ProfileInfoView.CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
            profileInfoFragment.setImgProfilePic(thumbnail);
        }
    }

    /**
     * View Initialization methods
     */

    private void initUi() {
        setContentView(R.layout.activity_intro);
        unbinder = ButterKnife.bind(this);
    }

    private void initViewPager() {
        IntroViewPagerAdapter introViewPagerAdapter = new IntroViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(introViewPagerAdapter);
        viewPager.setOffscreenPageLimit(4);
        viewPager.setScrollDurationFactor(2f);
        viewPager.setCurrentItem(PAGE_INTRO);
        viewPager.setAllowedSwipeDirection(CustomViewPager.SWIPE_MODE_NONE);
        viewPager.setPageTransformer(false, new IntroPageTransformer());
        viewPager.setSwipeable(false);
    }

    /**
     * Navigation methods
     */

    public void goToProfileInfo(LoginEntity loginEntity) {
        profileInfoFragment.setLoginEntity(loginEntity);
        viewPager.setCurrentItem(PAGE_PROFILE_INFO);
    }

    public void goToAccess() {
        accessFragment.fadeBigLockIn();
        Observable.timer(250, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(time -> {
                    viewPager.setCurrentItem(PAGE_ACCESS);
                });
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
                    introViewFragment = IntroViewFragment.newInstance();
                    return introViewFragment;
                case 1:
                    profileInfoFragment = ProfileInfoFragment.newInstance();
                    return profileInfoFragment;
                case 2:
                    accessFragment = AccessFragment.newInstance();
                    return accessFragment;
                default:
                    introViewFragment = IntroViewFragment.newInstance();
                    return introViewFragment;
            }
        }

    }

    private class IntroPageTransformer implements ViewPager.PageTransformer {

        @Override
        public void transformPage(View page, float position) {

        }
    }

    /**
     * Dagger setup
     */
    private void initDependencyInjector() {
        DaggerUserComponent.builder()
                .activityModule(getActivityModule())
                .applicationComponent(getApplicationComponent())
                .build().inject(this);
    }
}