package com.tribe.app.presentation.view.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.BuildConfig;
import com.tribe.app.R;
import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.domain.entity.ErrorLogin;
import com.tribe.app.domain.entity.Pin;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.AuthPresenter;
import com.tribe.app.presentation.mvp.view.AuthMVPView;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.utils.Extras;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerConstants;
import com.tribe.app.presentation.utils.preferences.LastUserRequest;
import com.tribe.app.presentation.view.component.onboarding.AuthVideoView;
import com.tribe.app.presentation.view.component.onboarding.CodeView;
import com.tribe.app.presentation.view.component.onboarding.PhoneNumberView;
import com.tribe.app.presentation.view.component.onboarding.StatusView;
import com.tribe.app.presentation.view.dialog_fragment.AuthenticationDialogFragment;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class AuthActivity extends BaseActivity implements AuthMVPView {

    private static int DURATION = 300;
    private static int DURATION_MEDIUM = 400;
    private static int DURATION_LONG = 600;
    private static int DURATION_FAST = 150;

    private static final String COUNTRY_CODE = "COUNTRY_CODE";
    private static final String DEEP_LINK = "DEEP_LINK";
    private static final String LOGIN_ENTITY = "LOGIN_ENTITY";
    private static final String PIN = "PIN";
    private static final String ERROR_LOGIN = "ERROR_LOGIN";
    private static final String PHONE_NUMBER = "PHONE_NUMBER";
    private static final String CODE = "CODE";
    private static final String COUNTDOWN = "COUNTDOWN";
    private static final String IS_PAUSED = "IS_PAUSED";

    public static Intent getCallingIntent(Context context) {
        Intent intent = new Intent(context, AuthActivity.class);
        return intent;
    }

    @Inject
    User user;

    @Inject
    ScreenUtils screenUtils;

    @Inject
    AuthPresenter authPresenter;

    @Inject
    @LastUserRequest
    Preference<String> lastUserRequest;

    @BindView(R.id.viewVideoAuth)
    AuthVideoView authVideoView;

    @BindView(R.id.btnSkip)
    ImageView btnSkip;

    @BindView(R.id.btnPlay)
    ImageView btnPlay;

    @BindView(R.id.viewBackground)
    View viewBackground;

    @BindView(R.id.viewRoot)
    ViewGroup viewRoot;

    @BindView(R.id.layoutBottom)
    ViewGroup layoutBottom;

    @BindView(R.id.viewPhoneNumber)
    PhoneNumberView viewPhoneNumber;

    @BindView(R.id.viewCode)
    CodeView viewCode;

    @BindView(R.id.txtMessage)
    TextViewFont txtMessage;

    @BindView(R.id.viewStatus)
    StatusView viewStatus;

    // VARIABLES
    private Unbinder unbinder;
    private Uri deepLink;
    private AuthenticationDialogFragment authenticationDialogFragment;
    private Pin pin;
    private ErrorLogin errorLogin;
    private LoginEntity loginEntity;
    private boolean countdownActive;
    private String countryCode;
    private String phoneNumber;
    private String code;
    private int currentCountdown;
    private boolean shouldPauseOnRestore = false;

    // OBSERVABLES
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private Subscription countdownSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            if (savedInstanceState.getParcelable(DEEP_LINK) != null)
                deepLink = savedInstanceState.getParcelable(DEEP_LINK);
            if (savedInstanceState.getString(COUNTRY_CODE) != null)
                countryCode = savedInstanceState.getString(COUNTRY_CODE);
            if (savedInstanceState.get(LOGIN_ENTITY) != null)
                loginEntity = (LoginEntity) savedInstanceState.getSerializable(LOGIN_ENTITY);
            if (savedInstanceState.get(ERROR_LOGIN) != null)
                errorLogin = (ErrorLogin) savedInstanceState.getSerializable(ERROR_LOGIN);
            if (savedInstanceState.get(PIN) != null)
                pin = (Pin) savedInstanceState.getSerializable(PIN);
            if (savedInstanceState.get(CODE) != null) code = savedInstanceState.getString(CODE);
            if (savedInstanceState.get(PHONE_NUMBER) != null)
                phoneNumber = savedInstanceState.getString(PHONE_NUMBER);
            if (savedInstanceState.get(COUNTDOWN) != null) {
                currentCountdown = savedInstanceState.getInt(COUNTDOWN);
            }
            if (savedInstanceState.get(IS_PAUSED) != null)
                shouldPauseOnRestore = savedInstanceState.getBoolean(IS_PAUSED);
        }

        setContentView(R.layout.activity_auth);

        unbinder = ButterKnife.bind(this);

        initDependencyInjector();
        init();

        screenUtils.hideKeyboard(this);

        lastUserRequest.set("");
        manageDeepLink(getIntent());
        tagManager.trackEvent(TagManagerConstants.ONBOARDING_START);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        shouldPauseOnRestore = true;
        screenUtils.hideKeyboard(this);
        authVideoView.onPause(true);
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        authPresenter.onViewAttached(this);
    }

    @Override
    protected void onStop() {
        authPresenter.onViewDetached();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) unbinder.unbind();
        if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
        if (countdownSubscription != null) countdownSubscription.unsubscribe();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!StringUtils.isEmpty(countryCode)) outState.putString(COUNTRY_CODE, countryCode);
        if (deepLink != null) outState.putParcelable(DEEP_LINK, deepLink);
        if (loginEntity != null) outState.putSerializable(LOGIN_ENTITY, loginEntity);
        if (errorLogin != null) outState.putSerializable(ERROR_LOGIN, errorLogin);
        if (pin != null) outState.putSerializable(PIN, pin);
        if (!StringUtils.isEmpty(viewPhoneNumber.getPhoneNumberInput()))
            outState.putString(PHONE_NUMBER, viewPhoneNumber.getPhoneNumberInput());
        if (!StringUtils.isEmpty(code)) outState.putString(CODE, code);
        if (countdownActive) outState.putInt(COUNTDOWN, viewCode.getCurrentCountdown());
        if (shouldPauseOnRestore) {
            outState.putBoolean(IS_PAUSED, shouldPauseOnRestore);
        } else {
            outState.remove(IS_PAUSED);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Navigator.REQUEST_COUNTRY && resultCode == Activity.RESULT_OK && data.getStringExtra(Extras.COUNTRY_CODE) != null) {
            countryCode = data.getStringExtra(Extras.COUNTRY_CODE);
            viewPhoneNumber.initWithCodeCountry(countryCode);
        }
    }

    private void init() {
        viewBackground.setEnabled(false);
        btnPlay.setEnabled(false);

        layoutBottom.setTranslationY(screenUtils.getHeightPx());
        viewCode.setTranslationX(screenUtils.getWidthPx());

        subscriptions.add(Observable.timer(BuildConfig.DEBUG ? 5000 : 5000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .filter(aLong -> viewBackground.getVisibility() == View.GONE)
                .subscribe(aLong -> AnimationUtils.fadeIn(btnSkip, DURATION))
        );

        subscriptions.add(authVideoView.videoCompleted()
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> showPhoneInput(true))
        );

        subscriptions.add(authVideoView.videoStarted()
                .delay(50, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                            if (shouldPauseOnRestore) {
                                System.out.println("PAUSE");
                                authVideoView.onPause(true);
                            }
                        }
                )
        );

        initViewPhoneNumber();
        initViewCode();
    }

    private void initViewPhoneNumber() {
        if (!StringUtils.isEmpty(countryCode)) viewPhoneNumber.initWithCodeCountry(countryCode);
        if (!StringUtils.isEmpty(phoneNumber)) viewPhoneNumber.setPhoneNumber(phoneNumber);

        subscriptions.add(viewPhoneNumber.phoneNumberValid()
                .subscribe(isValid -> {
                    viewPhoneNumber.setNextEnabled(isValid);
                })
        );

        subscriptions.add(viewPhoneNumber.countryClick()
                .subscribe(aVoid -> {
                    viewPhoneNumber.hideKeyboard();
                    navigator.navigateToCountries(this);
                })
        );

        subscriptions.add(viewPhoneNumber.nextClick()
                .subscribe(aVoid -> {
                    confirmPhoneNumber();
                }));

        if (shouldPauseOnRestore && pin == null) showPhoneInput(false);
    }

    private void initViewCode() {
        if (!StringUtils.isEmpty(code)) viewCode.setCode(code);

        subscriptions.add(viewCode.backClicked()
                .subscribe(aVoid -> {
                    backToPhoneNumber();
                })
        );

        subscriptions.add(viewCode.codeValid()
                .subscribe(isValid -> {
                    if (isValid) {
                        cleanCountdown();
                        loginEntity = authPresenter.login(viewPhoneNumber.getPhoneNumberFormatted(), viewCode.getCode(), pin.getPinId());
                        //loginEntity = authPresenter.login(viewPhoneNumber.getPhoneNumberFormatted(), viewCode.getCode(), "");
                    }
                })
        );

        if (pin != null) {
            showPhoneInput(false);
            goToCodeView(false);
        }
    }

    private void initDependencyInjector() {
        DaggerUserComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build()
                .inject(this);
    }

    private void manageDeepLink(Intent intent) {
        if (intent != null && intent.getData() != null) {
            deepLink = intent.getData();
        }
    }

    @OnClick(R.id.btnSkip)
    void skip() {
        showPhoneInput(true);
    }

    @OnClick(R.id.viewVideoAuth)
    void endVideo() {
        showPhoneInput(true);
    }

    private void showPhoneInput(boolean animate) {
        authVideoView.onPause(false);
        AnimationUtils.fadeOut(btnSkip, animate ? DURATION : 0);
        viewBackground.setAlpha(0);
        viewBackground.setVisibility(View.VISIBLE);
        AnimationUtils.fadeIn(viewBackground, animate ? DURATION : 0);
        btnSkip.setEnabled(false);
        showLayoutBottom(animate);
    }

    @OnClick(R.id.viewBackground)
    void showPlay() {
        if (pin == null) {
            btnPlay.postDelayed(() -> AnimationUtils.fadeIn(btnPlay, DURATION), DURATION_FAST);
            viewBackground.setEnabled(false);
            btnPlay.setEnabled(true);
            viewPhoneNumber.hideKeyboard();
        }
    }

    @OnClick(R.id.btnPlay)
    void play() {
        shouldPauseOnRestore = false;
        authVideoView.play();
        viewPhoneNumber.hideKeyboard();
        AnimationUtils.fadeIn(btnSkip, DURATION);
        AnimationUtils.fadeOut(viewBackground, DURATION, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                viewBackground.setVisibility(View.GONE);
                viewBackground.animate().setListener(null).start();
            }
        });
        AnimationUtils.fadeOut(btnPlay, DURATION);
        btnPlay.setEnabled(false);
        btnSkip.setEnabled(true);
        viewBackground.setEnabled(false);
        hideLayoutBottom();
    }

    @OnClick(R.id.viewStatus)
    void clickResendStatus() {
        if (viewStatus.getStatus() == StatusView.RESEND) {
            requestCode();
        }
    }

    private void showLayoutBottom(boolean animate) {
        layoutBottom
                .animate()
                .translationY(0)
                .setDuration(animate ? DURATION : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        layoutBottom.animate().setListener(null).start();
                        viewPhoneNumber.openKeyboard(animate ? DURATION : 0);
                        viewBackground.setEnabled(true);
                    }
                })
                .start();
    }

    private void hideLayoutBottom() {
        layoutBottom
                .animate()
                .translationY(screenUtils.getHeightPx())
                .setDuration(DURATION)
                .start();
    }

    private void hideViewPhoneNumber(boolean animate) {
        viewPhoneNumber
                .animate()
                .translationX(-screenUtils.getWidthPx())
                .setDuration(animate ? DURATION : 0)
                .start();
    }

    private void showViewPhoneNumber() {
        pin = null;
        txtMessage.setText(R.string.onboarding_step_phone);
        viewPhoneNumber.hideLoading();

        viewPhoneNumber
                .animate()
                .translationX(0)
                .setDuration(DURATION)
                .start();
    }

    private void hideViewCode() {
        cleanCountdown();

        viewCode
                .animate()
                .translationX(screenUtils.getWidthPx())
                .setDuration(DURATION)
                .start();
    }

    private void showViewCode(boolean animate) {
        AnimationUtils.fadeOut(btnPlay, animate ? DURATION_FAST : 0);
        txtMessage.setText(R.string.onboarding_step_code);
        initCountdown();

        viewCode
                .animate()
                .translationX(0)
                .setDuration(animate ? DURATION : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        viewCode.animate().setListener(null).start();
                        viewCode.openKeyboard(animate ? DURATION : 0);
                    }
                })
                .start();
    }

    private void backToPhoneNumber() {
        hideViewCode();
        showViewPhoneNumber();
        viewStatus.showDisclaimer();
    }

    private void goToCodeView(boolean animate) {
        viewStatus.showCodeSent(viewPhoneNumber.getPhoneNumberFormatted());
        showViewCode(animate);
        hideViewPhoneNumber(animate);
    }

    private void confirmPhoneNumber() {
        authenticationDialogFragment = AuthenticationDialogFragment.newInstance(
                getApplicationComponent().phoneUtils().formatPhoneNumberForView(
                        viewPhoneNumber.getPhoneNumberFormatted(),
                        viewPhoneNumber.getCountryCode()
                ),
                false);
        authenticationDialogFragment.show(getSupportFragmentManager(), AuthenticationDialogFragment.class.getName());
        subscriptions.add(authenticationDialogFragment.confirmClicked().subscribe(aVoid -> {
            authenticationDialogFragment.dismiss();
            requestCode();
        }));

        subscriptions.add(authenticationDialogFragment.cancelClicked().subscribe(aVoid -> {
            authenticationDialogFragment.dismiss();
        }));
    }

    private void requestCode() {
        viewStatus.showSendingCode();
        authPresenter.requestCode(viewPhoneNumber.getPhoneNumberFormatted());
    }

    private void initCountdown() {
        if (!countdownActive) {
            countdownActive = true;
            viewCode.startCountdown();
            countdownSubscription = viewCode.countdownExpired().subscribe(aVoid -> {
                resend();
                cleanCountdown();
            });
        }
    }

    private void resend() {
        viewStatus.showSendingCode();

        tagManager.trackEvent(TagManagerConstants.ONBOARDING_SMS_NOT_RECEIVED);
        authenticationDialogFragment = AuthenticationDialogFragment.newInstance(
                getApplicationComponent().phoneUtils().formatPhoneNumberForView(
                        viewPhoneNumber.getPhoneNumberFormatted(),
                        viewPhoneNumber.getCountryCode()
                ),
                true
        );

        authenticationDialogFragment.show(getSupportFragmentManager(), AuthenticationDialogFragment.class.getName());
        subscriptions.add(authenticationDialogFragment.confirmClicked().subscribe(aVoid -> {
            tagManager.trackEvent(TagManagerConstants.ONBOARDING_RESEND_PIN);
            authenticationDialogFragment.dismiss();
            requestCode();
        }));

        subscriptions.add(authenticationDialogFragment.cancelClicked().subscribe(aVoid -> {
            authenticationDialogFragment.dismiss();
            viewStatus.showResend();
        }));
    }

    private void cleanCountdown() {
        countdownActive = false;
        if (countdownSubscription != null) countdownSubscription.unsubscribe();
        viewCode.removeCountdown();
    }

    @Override
    public void goToCode(Pin pin) {
        tagManager.trackEvent(TagManagerConstants.ONBOARDING_SEND_PIN);
        this.pin = pin;

        goToCodeView(true);
    }

    @Override
    public void goToHome() {
        navigator.navigateToHome(this, false, null);
    }

    @Override
    public void goToConnected(User user) {
        if (user != null && !StringUtils.isEmpty(user.getId())) tagManager.setUserId(user.getId());

        this.user.copy(user);

        viewCode.removeCountdown();

        subscriptions.add(Observable.timer(DURATION, TimeUnit.MILLISECONDS)
                .onBackpressureDrop()
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(aLong -> viewCode.showConnected())
                .delay(DURATION_LONG, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(aLong -> {
                    txtMessage.setVisibility(View.GONE);
                    viewStatus.setVisibility(View.GONE);
                    screenUtils.hideKeyboard(this);
                    viewCode.showConnectedEnd();
                })
                .delay(1400, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(time -> {
                    if (user == null || StringUtils.isEmpty(user.getProfilePicture()) || StringUtils.isEmpty(user.getUsername())) {
                        navigator.navigateToAuthProfile(this, deepLink, loginEntity);
                    } else {
                        tagManager.trackEvent(TagManagerConstants.ONBOARDING_CONNECTION);
                        navigator.navigateToAuthAccess(this, deepLink);
                    }
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

    @Override
    public void showLoading() {
        if (pin == null) viewPhoneNumber.showLoading();
        else viewCode.showLoading();
    }

    @Override
    public void hideLoading() {
        if (pin == null) viewPhoneNumber.hideLoading();
        else viewCode.hideLoading();
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public Context context() {
        return this;
    }
}