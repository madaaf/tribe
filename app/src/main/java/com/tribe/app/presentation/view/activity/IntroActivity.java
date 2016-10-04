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
import android.view.ViewGroup;
import android.view.WindowManager;

import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.R;
import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.scope.LastMessageRequest;
import com.tribe.app.presentation.internal.di.scope.LastUserRequest;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.utils.Extras;
import com.tribe.app.presentation.utils.StringUtils;
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

    private static final int PAGE_INTRO = 0,
            PAGE_PROFILE_INFO = 1,
            PAGE_ACCESS = 2;

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, IntroActivity.class);
    }

    /**
     * Globals
     */

    private IntroViewFragment introViewFragment;
    private ProfileInfoFragment profileInfoFragment;
    private AccessFragment accessFragment;

    // OBSERVABLES
    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    // for ui testing
    public static final boolean uiOnlyMode = false;

    @Inject
    ScreenUtils screenUtils;

    @Inject
    User currentUser;

    @Inject
    @LastMessageRequest
    Preference<String> lastMessageRequest;

    @Inject
    @LastUserRequest
    Preference<String> lastUserRequest;

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
        initDependencyInjector();
        initViewPager();

        lastMessageRequest.set("");
        lastUserRequest.set("");
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

        if (currentUser == null || StringUtils.isEmpty(currentUser.getUsername())) {
            viewPager.setCurrentItem(PAGE_INTRO);
        } else if (currentUser.getFriendshipList().size() == 0) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            viewPager.setCurrentItem(PAGE_PROFILE_INFO);
        }

        viewPager.setAllowedSwipeDirection(CustomViewPager.SWIPE_MODE_NONE);
        viewPager.setPageTransformer(false, new IntroPageTransformer());
        viewPager.setSwipeable(false);
    }

    /**
     * Navigation methods
     */

    public void goToProfileInfo(LoginEntity loginEntity) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        profileInfoFragment.setLoginEntity(loginEntity);
        viewPager.setCurrentItem(PAGE_PROFILE_INFO);
    }

    public void goToAccess(User user) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        accessFragment.fadeBigLockIn();
        accessFragment.setUser(user);
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
                    accessFragment.setUser(currentUser);
                    return accessFragment;
                default:
                    introViewFragment = IntroViewFragment.newInstance();
                    return introViewFragment;
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            switch (position) {
                case 0:
                    introViewFragment = (IntroViewFragment) super.instantiateItem(container, position);
                    return introViewFragment;
                case 1:
                    profileInfoFragment = (ProfileInfoFragment) super.instantiateItem(container, position);
                    return profileInfoFragment;
                case 2:
                    accessFragment = (AccessFragment) super.instantiateItem(container, position);
                    accessFragment.setUser(currentUser);
                    return accessFragment;
                default:
                    introViewFragment = (IntroViewFragment) super.instantiateItem(container, position);
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