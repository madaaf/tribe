package com.tribe.app.presentation.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.tribe.app.R;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.utils.Extras;
import com.tribe.app.presentation.view.fragment.AccessFragment;
import com.tribe.app.presentation.view.fragment.IntroViewFragment;
import com.tribe.app.presentation.view.fragment.ProfileInfoFragment;
import com.tribe.app.presentation.view.widget.CustomViewPager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import rx.subscriptions.CompositeSubscription;

/**
 * IntroActivity.java
 * This is the first activity when app is launched for the first time.
 * Consists of a fragment view pager that is used throughout the onboarding process.
 * The first fragment in the view pager is IntroViewFragment.java
 */

// TODO: fix keyboard overlapping issue
public class IntroActivity extends BaseActivity {

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, IntroActivity.class);
    }

    /**
     * Globals
     */

    private static final int PAGE_INTRO = 0;
    private static final int PAGE_PROFILE_INFO = 1;
    private static final int PAGE_ACCESS = 2;

    private IntroViewFragment introViewFragment;
    private ProfileInfoFragment profileInfoFragment;
    private AccessFragment accessFragment;

    private IntroViewPagerAdapter introViewPagerAdapter;

    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @BindView(R.id.viewPager)
    CustomViewPager viewPager;

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

    // TODO: Fix this
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Navigator.REQUEST_COUNTRY && resultCode == Activity.RESULT_OK) {
            introViewFragment.initPhoneNumberViewWithCountryCode(data.getStringExtra(Extras.COUNTRY_CODE));
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
        introViewPagerAdapter = new IntroViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(introViewPagerAdapter);
        viewPager.setOffscreenPageLimit(4);
        viewPager.setScrollDurationFactor(2f);
        viewPager.setCurrentItem(PAGE_ACCESS);
        viewPager.setAllowedSwipeDirection(CustomViewPager.SWIPE_MODE_NONE);
        viewPager.setPageTransformer(false, new IntroPageTransformer());
        viewPager.setSwipeable(false);
    }

    /**
     * Navigation methods
     */

    public void goToProfileInfo() {
        viewPager.setCurrentItem(PAGE_PROFILE_INFO);
    }

    public void goToAccess() {
        viewPager.setCurrentItem(PAGE_ACCESS);
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