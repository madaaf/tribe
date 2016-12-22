package com.tribe.app.presentation.view.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
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
import com.tribe.app.presentation.utils.preferences.LastMessageRequest;
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
    private static int DURATION_FAST = 150;

    private static final String COUNTRY_CODE = "COUNTRY_CODE";
    private static final String DEEP_LINK = "DEEP_LINK";

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
    @LastMessageRequest
    Preference<String> lastMessageRequest;

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
    private Uri deepLink;
    private Unbinder unbinder;
    private AuthenticationDialogFragment authenticationDialogFragment;
    private Pin pin;
    private ErrorLogin errorLogin;
    private LoginEntity loginEntity;
    private boolean countdownActive;

    // OBSERVABLES
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private Subscription countdownSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        unbinder = ButterKnife.bind(this);

        initDependencyInjector();
        init();

        lastMessageRequest.set("");
        lastUserRequest.set("");
        manageDeepLink(getIntent());
        tagManager.trackEvent(TagManagerConstants.ONBOARDING_START);
    }

    @Override
    protected void onPause() {
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
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Navigator.REQUEST_COUNTRY && resultCode == Activity.RESULT_OK && data.getStringExtra(Extras.COUNTRY_CODE) != null) {
            viewPhoneNumber.initWithCodeCountry(data.getStringExtra(Extras.COUNTRY_CODE));
        }
    }

    private void init() {
        viewBackground.setEnabled(false);
        btnPlay.setEnabled(false);

        layoutBottom.setTranslationY(screenUtils.getHeightPx());
        viewCode.setTranslationX(screenUtils.getWidthPx());

        subscriptions.add(Observable.timer(BuildConfig.DEBUG ? 0 : 5000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    AnimationUtils.fadeIn(btnSkip, DURATION);
                })
        );

        initViewPhoneNumber();
        initViewCode();
    }

    private void initViewPhoneNumber() {
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
    }

    private void initViewCode() {
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
                    }
                })
        );
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
        authVideoView.onPause(false);
        AnimationUtils.fadeOut(btnSkip, DURATION);
        AnimationUtils.fadeIn(viewBackground, DURATION);
        btnSkip.setEnabled(false);
        showLayoutBottom();
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
        authVideoView.play();
        viewPhoneNumber.hideKeyboard();
        AnimationUtils.fadeIn(btnSkip, DURATION);
        AnimationUtils.fadeOut(viewBackground, DURATION);
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

    private void showLayoutBottom() {
        layoutBottom
                .animate()
                .translationY(0)
                .setDuration(DURATION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        layoutBottom.animate().setListener(null).start();
                        viewPhoneNumber.openKeyboard(DURATION);
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

    private void hideViewPhoneNumber() {
        viewPhoneNumber
                .animate()
                .translationX(-screenUtils.getWidthPx())
                .setDuration(DURATION_MEDIUM)
                .start();
    }

    private void showViewPhoneNumber() {
        pin = null;
        txtMessage.setText(R.string.onboarding_step_phone);
        viewPhoneNumber.hideLoading();

        viewPhoneNumber
                .animate()
                .translationX(0)
                .setDuration(DURATION_MEDIUM)
                .start();
    }

    private void hideViewCode() {
        cleanCountdown();

        viewCode
            .animate()
            .translationX(screenUtils.getWidthPx())
            .setDuration(DURATION_MEDIUM)
            .start();
    }

    private void showViewCode() {
        AnimationUtils.fadeOut(btnPlay, DURATION_FAST);
        txtMessage.setText(R.string.onboarding_step_code);
        initCountdown();

        viewCode
            .animate()
            .translationX(0)
            .setDuration(DURATION_MEDIUM)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    viewCode.animate().setListener(null).start();
                    viewCode.openKeyboard(DURATION);
                }
            })
            .start();
    }

    private void backToPhoneNumber() {
        hideViewCode();
        showViewPhoneNumber();
        viewStatus.showDisclaimer();
    }

    private void goToCodeView() {
        showViewCode();
        hideViewPhoneNumber();
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

        authenticationDialogFragment.onDismiss(new DialogInterface() {
            @Override
            public void cancel() {
                viewStatus.showResend();
            }

            @Override
            public void dismiss() {
                viewStatus.showResend();
            }
        });

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
        viewStatus.showCodeSent(viewPhoneNumber.getPhoneNumberFormatted());

        goToCodeView();
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
        txtMessage.setVisibility(View.GONE);
        viewStatus.setVisibility(View.GONE);
        screenUtils.hideKeyboard(this);

        subscriptions.add(Observable.timer(DURATION, TimeUnit.MILLISECONDS)
                .onBackpressureDrop()
                .delay(DURATION, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(aLong -> {
                    viewCode.showConnected();
                })
                .delay(1700, TimeUnit.MILLISECONDS)
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