package com.tribe.app.presentation.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.domain.entity.ErrorLogin;
import com.tribe.app.domain.entity.Pin;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.mvp.presenter.IntroPresenter;
import com.tribe.app.presentation.mvp.view.IntroView;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerConstants;
import com.tribe.app.presentation.view.activity.IntroActivity;
import com.tribe.app.presentation.view.component.CodeView;
import com.tribe.app.presentation.view.component.ConnectedView;
import com.tribe.app.presentation.view.component.PhoneNumberView;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.CustomViewPager;
import com.tribe.app.presentation.view.widget.IntroVideoView;
import com.tribe.app.presentation.view.widget.TextViewFont;

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
 * IntroViewFragment.java
 * Created by horatiothomas on 8/17/16.
 * The first fragment of the view pager from IntroActivity.java.
 * This fragment contains the intro video and the view pager that goes through
 * the process of capturing the users phone number, sending, and validating their pin code.
 * Next fragment in onboarding view pager is ProfileInfoFragment.
 */
public class IntroViewFragment extends BaseFragment implements IntroView {

    public static IntroViewFragment newInstance() {
        Bundle args = new Bundle();

        IntroViewFragment fragment = new IntroViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Globals
     */
    @Inject
    User currentUser;

    @Inject
    IntroPresenter introPresenter;

    @Inject
    ScreenUtils screenUtils;

    @BindView(R.id.viewPager)
    CustomViewPager viewPager;

    @BindView(R.id.viewShadow)
    View viewShadow;

    @BindView(R.id.viewPhoneNumber)
    PhoneNumberView viewPhoneNumber;

    @BindView(R.id.viewCode)
    CodeView viewCode;

    @BindView(R.id.viewConnected)
    ConnectedView viewConnected;

    @BindView(R.id.videoViewIntro)
    IntroVideoView videoViewIntro;

    @BindView(R.id.txtIntroMessage)
    TextViewFont txtIntroMessage;

    // VARIABLES
    private LoginEntity loginEntity;
    private Pin pin;
    private ErrorLogin errorLogin;
    private String phoneNumber, code;

    public static final int PAGE_PHONE_NUMBER = 0,
            PAGE_CODE = 1,
            PAGE_CONNECTED = 2;

    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    /**
     * Lifecycle methods
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        final View fragmentView = inflater.inflate(R.layout.fragment_intro_view, container, false);

        initDependencyInjector();
        initUi(fragmentView);
        initViewPager();
        initPhoneNumberView();
        initPresenter();
        return fragmentView;
    }

    @Override
    public void onPause() {
        videoViewIntro.releasePlayer();
        super.onPause();
    }

    @Override
    public void onResume() {
        initPlayerView();
        super.onResume();
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

    /**
     * View Initialization methods
     */

    private void initUi(View view) {
        unbinder = ButterKnife.bind(this, view);

        viewPhoneNumber.setNextEnabled(false);

        viewShadow.setTranslationY(screenUtils.getHeightPx() >> 1);
        viewShadow.animate().translationY(0).setDuration(300).setStartDelay(1000).setInterpolator(new DecelerateInterpolator()).start();
        viewPhoneNumber.setTranslationY(screenUtils.getHeightPx() >> 1);
        viewPhoneNumber.animate().translationY(0).setDuration(300).setStartDelay(1000).setInterpolator(new DecelerateInterpolator()).start();
        txtIntroMessage.setAlpha(0);
        txtIntroMessage.animate().alpha(1).setDuration(300).setStartDelay(1000).setInterpolator(new DecelerateInterpolator()).start();

        subscriptions.add(RxView.clicks(viewPhoneNumber.getImageViewNextIcon()).subscribe(aVoid -> {
            viewPhoneNumber.fadeOutNext();
            viewCode.setImgBackIconVisible();
            introPresenter.requestCode(phoneNumber);
        }));

        subscriptions.add(viewCode.backClicked().subscribe(aVoid -> {
            viewCode.fadeBackOut();
            viewPhoneNumber.nextIconVisisble();
            viewPager.setCurrentItem(PAGE_PHONE_NUMBER, true);
        }));

        subscriptions.add(viewCode.codeValid().subscribe(isValid -> {
            if (isValid) {
                if (IntroActivity.uiOnlyMode) {
                    loginEntity = introPresenter.login("", "", "");
                } else {
                    this.code = viewCode.getCode();
                    loginEntity = introPresenter.login(phoneNumber, code, pin.getPinId());
                }
            }
        }));
    }

    private void initViewPager() {
        IntroViewFragmentPagerAdapter introViewFragmentPagerAdapter = new IntroViewFragmentPagerAdapter();
        viewPager.setAdapter(introViewFragmentPagerAdapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setScrollDurationFactor(3.5f);
        viewPager.setCurrentItem(PAGE_PHONE_NUMBER);
        viewPager.setAllowedSwipeDirection(CustomViewPager.SWIPE_MODE_NONE);
        viewPager.setPageTransformer(false, new IntroPageTransformer());
        viewPager.setSwipeable(false);
    }

    private void initPlayerView() {
        videoViewIntro.createPlayer("asset:///video/onboarding_video.mp4");
    }

    private void initPhoneNumberView() {
        viewPhoneNumber.setPhoneUtils(getApplicationComponent().phoneUtils());

        subscriptions.add(viewPhoneNumber.phoneNumberValid().subscribe(isValid -> {
            this.phoneNumber = viewPhoneNumber.getPhoneNumberFormatted();
            viewPhoneNumber.setNextEnabled(isValid);
        }));

        subscriptions.add(viewPhoneNumber.countryClick().subscribe(aVoid -> {
            navigator.navigateToCountries(getActivity());
        }));
    }

    public void initPhoneNumberViewWithCountryCode(String countryCode) {
        viewPhoneNumber.initWithCodeCountry(countryCode);
    }

    private void initPresenter() {
        introPresenter.attachView(this);
    }

    /**
     * Initialize View Pager Adapter
     * Used for bottom bar navigation
     */

    private class IntroViewFragmentPagerAdapter extends PagerAdapter {

        public static final int NUM_ITEMS = 3;

        public Object instantiateItem(View container, int position) {

            int resId = 0;
            switch (position) {
                case 0:
                    resId = R.id.viewPhoneNumber;
                    break;

                case 1:
                    resId = R.id.viewCode;
                    break;
                case 2:
                    resId = R.id.viewConnected;
                    break;
            }

            return container.findViewById(resId);

        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == ((View) obj);
        }
    }

    private class IntroPageTransformer implements ViewPager.PageTransformer {

        @Override
        public void transformPage(View page, float position) {

        }
    }

    /**
     * Navigation methods
     */
    @Override
    public void goToCode(Pin pin) {
        tagManager.trackEvent(TagManagerConstants.ONBOARDING_SEND_PIN);
        this.pin = pin;
        viewPhoneNumber.fadeOutNext();
        txtIntroMessage.setText(getString(R.string.onboarding_step_code));
        viewPager.setCurrentItem(PAGE_CODE, true);
    }


    @Override
    public void goToHome() {
        navigator.navigateToHome(getActivity(), false, null);
    }

    @Override
    public void goToConnected(User user) {
        currentUser.copy(user);
        txtIntroMessage.setText("");
        screenUtils.hideKeyboard(getActivity());

        subscriptions.add(Observable.timer(300, TimeUnit.MILLISECONDS)
                .onBackpressureDrop()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(time -> {
                    viewCode.animateConnectedIcon();

                    subscriptions.add(Observable.timer(300, TimeUnit.MILLISECONDS)
                            .onBackpressureDrop()
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(time1 -> {
                                viewCode.fadeConnectedOut();
                                viewPager.setCurrentItem(PAGE_CONNECTED, true);
                                viewConnected.animateConnected();
                            }));

                    subscriptions.add(Observable.timer(2000, TimeUnit.MILLISECONDS)
                            .onBackpressureDrop()
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(time2 -> {
                                if (user == null || StringUtils.isEmpty(user.getProfilePicture()) || StringUtils.isEmpty(user.getUsername())) {
                                    ((IntroActivity) getActivity()).goToProfileInfo(user, loginEntity);
                                } else {
                                    tagManager.trackEvent(TagManagerConstants.ONBOARDING_CONNECTION);
                                    ((IntroActivity) getActivity()).goToAccess(user);
                                }
                            }));
                }));
    }

    @Override
    public void loginError(ErrorLogin errorLogin) {
        this.errorLogin = errorLogin;

        if (errorLogin != null && !errorLogin.isVerified()) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(TagManagerConstants.TYPE_ERROR_TECHNICAL, true);
            tagManager.trackEvent(TagManagerConstants.ONBOARDING_SMS_ERROR, bundle);
        }
    }

    /**
     * Manage view pager transition methods
     */
    @Override
    public void showLoading() {
        if (viewPager.getCurrentItem() == PAGE_PHONE_NUMBER) {
            viewPhoneNumber.setNextVisible(false);
            viewPhoneNumber.progressViewVisible(true);
        } else {
            viewCode.progressViewVisible(true);
        }
    }

    @Override
    public void hideLoading() {
        viewPhoneNumber.setNextVisible(true);
        viewPhoneNumber.setNextEnabled(true);
        viewPhoneNumber.progressViewVisible(false);
        viewCode.progressViewVisible(false);
    }

    @Override
    public void showRetry() {

    }

    @Override
    public void hideRetry() {

    }

    @Override
    public void showError(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public Context context() {
        return getActivity();
    }

    /**
     * Begin Dagger setup
     */

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

    /**
     * Util methods
     */

}
