package com.tribe.app.presentation.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.MediaController;
import android.widget.VideoView;

import com.google.android.gms.common.data.DataBufferObserver;
import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.domain.entity.Pin;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.IntroPresenter;
import com.tribe.app.presentation.mvp.view.IntroView;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.view.component.CodeView;
import com.tribe.app.presentation.view.component.ConnectedView;
import com.tribe.app.presentation.view.component.PhoneNumberView;
import com.tribe.app.presentation.utils.Extras;
import com.tribe.app.presentation.view.widget.CustomViewPager;
import com.tribe.app.presentation.view.widget.IntroVideoView;
import com.tribe.app.presentation.view.widget.PlayerView;
import com.tribe.app.presentation.view.widget.TextViewFont;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

public class IntroActivity extends BaseActivity implements IntroView {

    public static final int PAGE_PHONE_NUMBER = 0;
    public static final int PAGE_CODE = 1;
    public static final int PAGE_CONNECTED = 2;

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, IntroActivity.class);
    }

    @Inject
    IntroPresenter introPresenter;

    @BindView(R.id.viewPager)
    CustomViewPager viewPager;

    @BindView(R.id.viewPhoneNumber)
    PhoneNumberView viewPhoneNumber;

    @BindView(R.id.viewCode)
    CodeView viewCode;

    @BindView(R.id.viewConnected)
    ConnectedView viewConnected;

    @BindView(R.id.videoViewIntro)
    IntroVideoView videoViewIntro;

    // TODO: add to strings.xml
    @BindView(R.id.txtIntroMessage)
    TextViewFont txtIntroMessage;

    private IntroViewPagerAdapter introViewPagerAdapter;

    private Pin pin;
    private String phoneNumber;
    private String code;

    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUi();
        initDependencyInjector();
        initViewPager();
        initPhoneNumberView();
        initPresenter();
        initPlayerView();

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Navigator.REQUEST_COUNTRY && resultCode == Activity.RESULT_OK) {
            viewPhoneNumber.initWithCodeCountry(data.getStringExtra(Extras.COUNTRY_CODE));
        }
    }

    private void initUi() {
        setContentView(R.layout.activity_intro);
        unbinder = ButterKnife.bind(this);

        viewPhoneNumber.setNextEnabled(false);

        subscriptions.add(RxView.clicks(viewPhoneNumber.getImageViewNextIcon()).subscribe(aVoid -> {
            introPresenter.requestCode(phoneNumber);
        }));

        subscriptions.add(RxView.clicks(viewCode.getBackIcon()).subscribe(aVoid -> {
            viewPager.setCurrentItem(PAGE_PHONE_NUMBER, true);
        }));

        subscriptions.add(viewCode.codeValid().subscribe(isValid -> {
            if (isValid) {
//                this.code = viewCode.getCode();
//                introPresenter.login(phoneNumber, code, pin.getPinId());
//                introPresenter.login("", "", "");
                goToConnected();
            }
        }));
    }

    private void initDependencyInjector() {
        DaggerUserComponent.builder()
                .activityModule(getActivityModule())
                .applicationComponent(getApplicationComponent())
                .build().inject(this);
    }

    private void initViewPager() {
        introViewPagerAdapter = new IntroViewPagerAdapter();
        viewPager.setAdapter(introViewPagerAdapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setScrollDurationFactor(2f);
        viewPager.setCurrentItem(PAGE_PHONE_NUMBER);
        viewPager.setAllowedSwipeDirection(CustomViewPager.SWIPE_MODE_NONE);
        viewPager.setPageTransformer(false, new IntroPageTransformer());
        viewPager.setSwipeable(false);
    }

    private void initPhoneNumberView() {
        viewPhoneNumber.setPhoneUtils(getApplicationComponent().phoneUtils());
        subscriptions.add(viewPhoneNumber.phoneNumberValid().subscribe(isValid -> {
            this.phoneNumber = viewPhoneNumber.getPhoneNumberFormatted();
            viewPhoneNumber.setNextEnabled(isValid);
        }));
        subscriptions.add(viewPhoneNumber.countryClick().subscribe(aVoid -> navigator.navigateToCountries(this)));
    }

    private void initPresenter() {
        introPresenter.attachView(this);
    }

    private void initPlayerView() {
        videoViewIntro.createPlayer("android.resource://" + getPackageName() +"/"+R.raw.onboarding_video);
    }

    @Override
    public void goToCode() {
//        this.pin = pin;
        viewPhoneNumber.setNextEnabled(false);
        //TODO: add to strings.xml and add animation
        txtIntroMessage.setText("We just texted you a code to verify it.");
        viewPager.setCurrentItem(PAGE_CODE, true);
    }


    @Override
    public void goToHome() {
        navigator.navigateToHome(context());
    }


    @Override
    public void goToConnected() {
        txtIntroMessage.setText("");
        hideKeyboard();
        viewPager.setCurrentItem(PAGE_CONNECTED, true);
    }

    @Override
    public void goToProfileInfo() {
        navigator.navigateToProfileInfo(context());
    }

    @Override
    public void goToAccess() {

    }

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
        viewPhoneNumber.progressViewVisible(false);
        viewPhoneNumber.setNextVisible(true);
        viewPhoneNumber.setNextEnabled(true);
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

    }

    @Override
    public Context context() {
        return this;
    }

    public class IntroViewPagerAdapter extends PagerAdapter {

        public Object instantiateItem(View collection, int position) {

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
            }

            return findViewById(resId);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == ((View) obj);
        }
    }

    public class IntroPageTransformer implements ViewPager.PageTransformer {

        @Override
        public void transformPage(View page, float position) {

        }
    }

    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}