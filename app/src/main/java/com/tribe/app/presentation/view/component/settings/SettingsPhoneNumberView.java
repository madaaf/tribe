package com.tribe.app.presentation.view.component.settings;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.view.utils.PhoneUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by remy on 31/07/2017.
 */

public class SettingsPhoneNumberView extends RelativeLayout {

    @Inject
    User user;

    @Inject
    Navigator navigator;

    @Inject
    PhoneUtils phoneUtils;

    @BindView(R.id.phoneNumber) TextViewFont phoneNumber;
    @BindView(R.id.country) TextViewFont country;

    @OnClick(R.id.changePhoneNumber) void changePhone() {
        onChangePhoneNumberClick.onNext(null);
    }

    // OBSERVABLES
    private CompositeSubscription subscriptions;

    // RX SUBSCRIPTIONS / SUBJECTS
    private final PublishSubject<Void> onChangePhoneNumberClick = PublishSubject.create();

    public SettingsPhoneNumberView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);

        initDependencyInjector();
        initSubscriptions();
        initUI();
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void onDestroy() {
        if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    }

    private void initSubscriptions() {
        subscriptions = new CompositeSubscription();
    }

    private void initUI() {

        if (user.getPhone() != null) {

            String countryCode = phoneUtils.getRegionCodeForNumber(user.getPhone());

            phoneNumber.setText(phoneUtils.formatMobileNumber(user.getPhone(), countryCode));
            country.setText(new Locale("", countryCode).getDisplayCountry());

        } else {
            phoneNumber.setText(null);
            country.setText(null);
        }
    }

    protected ApplicationComponent getApplicationComponent() {
        return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
    }

    protected ActivityModule getActivityModule() {
        return new ActivityModule(((Activity) getContext()));
    }

    private void initDependencyInjector() {
        DaggerUserComponent.builder()
                .activityModule(getActivityModule())
                .applicationComponent(getApplicationComponent())
                .build()
                .inject(this);
    }

    public Observable<Void> onChangePhoneNumberClick() {
        return onChangePhoneNumberClick.asObservable();
    }
}
