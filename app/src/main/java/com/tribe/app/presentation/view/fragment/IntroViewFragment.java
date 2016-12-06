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
import com.tribe.app.presentation.mvp.view.IntroMVPView;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerConstants;
import com.tribe.app.presentation.view.activity.IntroActivity;
import com.tribe.app.presentation.view.component.CodeView;
import com.tribe.app.presentation.view.component.ConnectedView;
import com.tribe.app.presentation.view.component.PhoneNumberView;
import com.tribe.app.presentation.view.dialog_fragment.AuthenticationDialogFragment;
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
import rx.Subscription;
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
public class IntroViewFragment extends BaseFragment implements IntroMVPView {

    private static final String LOGIN_ENTITY = "LOGIN_ENTITY";
    private static final String PIN = "PIN";
    private static final String ERROR_LOGIN = "ERROR_LOGIN";
    private static final String PHONE_NUMBER = "PHONE_NUMBER";
    private static final String CODE = "CODE";
    private static final String COUNTDOWN = "COUNTDOWN";

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

    AuthenticationDialogFragment authenticationDialogFragment;

    // VARIABLES
    private LoginEntity loginEntity;
    private Pin pin;
    private ErrorLogin errorLogin;
    private String phoneNumber, code;
    private int currentCountdown = 0;
    private boolean countdownActive;

    public static final int PAGE_PHONE_NUMBER = 0,
            PAGE_CODE = 1,
            PAGE_CONNECTED = 2;

    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private Subscription countdownSubscription;

    /**
     * Lifecycle methods
     */


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        final View fragmentView = inflater.inflate(R.layout.fragment_intro_view, container, false);

        if (savedInstanceState != null) {
            if (savedInstanceState.get(LOGIN_ENTITY) != null) loginEntity = (LoginEntity) savedInstanceState.getSerializable(LOGIN_ENTITY);
            if (savedInstanceState.get(ERROR_LOGIN) != null) errorLogin = (ErrorLogin) savedInstanceState.getSerializable(ERROR_LOGIN);
            if (savedInstanceState.get(PIN) != null) pin = (Pin) savedInstanceState.getSerializable(PIN);
            if (savedInstanceState.get(CODE) != null) code = savedInstanceState.getString(CODE);
            if (savedInstanceState.get(PHONE_NUMBER) != null) phoneNumber = savedInstanceState.getString(PHONE_NUMBER);
            if (savedInstanceState.get(COUNTDOWN) != null) {
                currentCountdown = savedInstanceState.getInt(COUNTDOWN);
            }
        }

        initDependencyInjector();
        initUi(fragmentView);
        initViewPager();
        initPhoneNumberView();
        initPresenter();

        if (currentCountdown != 0) initCountdown(currentCountdown);

        return fragmentView;
    }

    @Override
    public void onPause() {
        videoViewIntro.releasePlayer();
        currentCountdown = viewCode.getCurrentCountdown();
        viewCode.removeCountdown();
        if (countdownSubscription != null) countdownSubscription.unsubscribe();

        super.onPause();
    }

    @Override
    public void onResume() {
        initPlayerView();
        if (countdownActive) initCountdown(currentCountdown);

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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (loginEntity != null) outState.putSerializable(LOGIN_ENTITY, loginEntity);
        if (errorLogin != null) outState.putSerializable(ERROR_LOGIN, errorLogin);
        if (pin != null) outState.putSerializable(PIN, pin);
        if (!StringUtils.isEmpty(phoneNumber)) outState.putString(PHONE_NUMBER, phoneNumber);
        if (!StringUtils.isEmpty(code)) outState.putString(CODE, code);
        if (countdownActive) outState.putInt(COUNTDOWN, viewCode.getCurrentCountdown());
    }

    /**
     * MVPView Initialization methods
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

        subscriptions.add(viewPhoneNumber.nextClick().subscribe(aVoid -> {
            confirmPhoneNumber();
        }));

        subscriptions.add(viewCode.backClicked().subscribe(aVoid -> {
            backToPhoneNumber();
        }));

        subscriptions.add(viewCode.codeValid().subscribe(isValid -> {
            if (isValid && isResumed() && getUserVisibleHint()) {
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
        if (!StringUtils.isEmpty(phoneNumber)) viewPhoneNumber.setPhoneNumber(phoneNumber);

        subscriptions.add(viewPhoneNumber.phoneNumberValid().subscribe(isValid -> {
            if (viewPager.getCurrentItem() == PAGE_PHONE_NUMBER) {
                this.phoneNumber = viewPhoneNumber.getPhoneNumberFormatted();
                viewPhoneNumber.setNextEnabled(isValid);
            }
        }));

        subscriptions.add(viewPhoneNumber.countryClick().subscribe(aVoid -> {
            navigator.navigateToCountries(getActivity());
        }));
    }

    public void initPhoneNumberViewWithCountryCode(String countryCode) {
        viewPhoneNumber.initWithCodeCountry(countryCode);
    }

    private void initPresenter() {
        introPresenter.onViewAttached(this);
    }

    /**
     * Initialize MVPView Pager Adapter
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

    private void backToPhoneNumber() {
        viewCode.fadeBackOut();
        viewPhoneNumber.nextIconVisisble();
        viewPager.setCurrentItem(PAGE_PHONE_NUMBER, true);
        viewPhoneNumber.openKeyboard();
    }

    private void requestCode() {
        viewPhoneNumber.fadeOutNext();
        viewCode.setImgBackIconVisible();
        introPresenter.requestCode(phoneNumber);
    }

    private void requestCodeInResend() {
        introPresenter.requestCode(phoneNumber);
        initCountdown(0);
    }

    private void confirmPhoneNumber() {
        authenticationDialogFragment = AuthenticationDialogFragment.newInstance(getApplicationComponent().phoneUtils().formatPhoneNumberForView(phoneNumber, viewPhoneNumber.getCountryCode()), false);
        authenticationDialogFragment.show(getFragmentManager(), AuthenticationDialogFragment.class.getName());
        subscriptions.add(authenticationDialogFragment.confirmClicked().subscribe(aVoid -> {
            authenticationDialogFragment.dismiss();
            requestCode();
        }));

        subscriptions.add(authenticationDialogFragment.cancelClicked().subscribe(aVoid -> {
            viewPhoneNumber.openKeyboard();
        }));
    }

    private void resend() {
        if (countdownSubscription != null) countdownSubscription.unsubscribe();

        tagManager.trackEvent(TagManagerConstants.ONBOARDING_SMS_NOT_RECEIVED);
        authenticationDialogFragment = AuthenticationDialogFragment.newInstance(getApplicationComponent().phoneUtils().formatPhoneNumberForView(phoneNumber, viewPhoneNumber.getCountryCode()), true);
        authenticationDialogFragment.show(getFragmentManager(), AuthenticationDialogFragment.class.getName());
        subscriptions.add(authenticationDialogFragment.confirmClicked().subscribe(aVoid -> {
            tagManager.trackEvent(TagManagerConstants.ONBOARDING_RESEND_PIN);
            authenticationDialogFragment.dismiss();
            requestCodeInResend();
        }));
        subscriptions.add(authenticationDialogFragment.cancelClicked().subscribe(aVoid -> {
            authenticationDialogFragment.dismiss();
            backToPhoneNumber();
        }));
    }

    private void initCountdown(int currentCountdown) {
        if (!countdownActive) {
            countdownActive = true;
            viewCode.startCountdown(currentCountdown);
            countdownSubscription = viewCode.countdownExpired().subscribe(aVoid -> {
                resend();
                countdownActive = false;
            });
            subscriptions.add(countdownSubscription);
        }
    }

    @Override
    public void goToCode(Pin pin) {
        tagManager.trackEvent(TagManagerConstants.ONBOARDING_SEND_PIN);
        this.pin = pin;
        viewPhoneNumber.fadeOutNext();
        txtIntroMessage.setText(getString(R.string.onboarding_step_code));
        viewPager.setCurrentItem(PAGE_CODE, true);
        initCountdown(0);
        viewCode.openKeyboard();
    }


    @Override
    public void goToHome() {
        navigator.navigateToHome(getActivity(), false, null);
    }

    @Override
    public void goToConnected(User user) {
        viewCode.removeCountdown();
        if (countdownSubscription != null) countdownSubscription.unsubscribe();
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

    @Override
    public void pinError(ErrorLogin errorLogin) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(TagManagerConstants.TYPE_ERROR_TECHNICAL, true);
        tagManager.trackEvent(TagManagerConstants.ONBOARDING_PIN_ERROR, bundle);
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
        if (viewPhoneNumber != null) {
            viewPhoneNumber.setNextVisible(true);
            viewPhoneNumber.setNextEnabled(true);
            viewPhoneNumber.progressViewVisible(false);
        }
        if (viewCode != null) viewCode.progressViewVisible(false);
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
}
