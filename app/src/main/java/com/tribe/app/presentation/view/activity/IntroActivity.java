package com.tribe.app.presentation.view.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.domain.entity.Pin;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.IntroPresenter;
import com.tribe.app.presentation.mvp.view.IntroView;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.view.component.CodeView;
import com.tribe.app.presentation.view.component.PhoneNumberView;
import com.tribe.app.presentation.view.utils.Extras;
import com.tribe.app.presentation.view.widget.CustomViewPager;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

public class IntroActivity extends BaseActivity implements IntroView {

    public static final int PAGE_PHONE_NUMBER = 0;
    public static final int PAGE_CODE = 1;

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

    @BindView(R.id.btnNext)
    View btnNext;

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
        initCodeView();
        initPresenter();
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

        btnNext.setEnabled(false);

        subscriptions.add(RxView.clicks(btnNext).subscribe(aVoid -> {
            if (viewPager.getCurrentItem() == PAGE_PHONE_NUMBER) introPresenter.requestCode(phoneNumber);
            else introPresenter.login(phoneNumber, code, pin.getPinId());
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
        viewPager.setPagingEnabled(false);
        viewPager.setPageTransformer(false, new IntroPageTransformer());
    }

    private void initPhoneNumberView() {
        viewPhoneNumber.setPhoneUtils(getApplicationComponent().phoneUtils());
        subscriptions.add(viewPhoneNumber.phoneNumberValid().subscribe(isValid -> {
            this.phoneNumber = viewPhoneNumber.getPhoneNumberFormatted();
            btnNext.setEnabled(isValid);
        }));
        subscriptions.add(viewPhoneNumber.countryClick().subscribe(aVoid -> navigator.navigateToCountries(this)));
    }

    private void initCodeView() {
        subscriptions.add(viewCode.codeValid().subscribe(isValid -> {
            this.code = viewCode.getCode();
            btnNext.setEnabled(isValid);
        }));
    }

    private void initPresenter() {
        introPresenter.attachView(this);
    }

    @Override
    public void goToCode(Pin pin) {
        this.pin = pin;
        btnNext.setEnabled(false);
        viewPager.setCurrentItem(PAGE_CODE, true);
    }

    @Override
    public void goToHome(AccessToken token) {
        navigator.navigateToHome(context());
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
            }

            return findViewById(resId);
        }

        @Override
        public int getCount() {
            return 2;
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
}