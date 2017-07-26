package com.tribe.app.presentation.view.activity;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;

import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.AuthConfig;
import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;
import com.f2prateek.rx.preferences.Preference;
import com.facebook.AccessToken;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;
import com.tribe.app.R;
import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.domain.entity.ErrorLogin;
import com.tribe.app.domain.entity.FacebookEntity;
import com.tribe.app.domain.entity.Pin;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.components.UserComponent;
import com.tribe.app.presentation.mvp.presenter.AuthPresenter;
import com.tribe.app.presentation.mvp.presenter.FacebookPresenter;
import com.tribe.app.presentation.mvp.view.AuthMVPView;
import com.tribe.app.presentation.mvp.view.FBInfoMVPView;
import com.tribe.app.presentation.utils.IntentUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.preferences.UserPhoneNumber;
import com.tribe.app.presentation.view.utils.PhoneUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.ShakeDetector;
import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class AuthActivity extends BaseActivity implements AuthMVPView, FBInfoMVPView, ViewTreeObserver.OnGlobalLayoutListener {
  private static String MOCKED_PHONE_NUMBER = "+15556787676";
  private static String DEEP_LINK = "DEEP_LINK";

  public static Intent getCallingIntent(Context context, Uri deepLink) {
    Intent intent = new Intent(context, AuthActivity.class);
    intent.putExtra(DEEP_LINK, deepLink);
    return intent;
  }

  @Inject User currentUser;

  @Inject PhoneUtils phoneUtils;

  @Inject ScreenUtils screenUtils;

  @Inject AuthPresenter authPresenter;
  @Inject FacebookPresenter facebookPresenter;

  @Inject @UserPhoneNumber Preference<String> userPhoneNumber;

  @BindView(R.id.logoView) View logoView;
  @BindView(R.id.buttonsView) View buttonsView;
  @BindView(R.id.imgLogo) View imgLogo;
  @BindView(R.id.baseline) View baseline;
  @BindView(R.id.loading_indicator) View loadingIndicator;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  // VARIABLES
  private LoginEntity loginEntity;
  private ShakeDetector shakeDetector;
  private SensorManager sensorManager;
  private Sensor accelerometer;
  private Uri deepLink = null;
  private AuthCallback authCallback;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initUi();
    initDependencyInjector();
    setSandboxBehavior();
    initRessource();
    deepLink = getIntent().getData();
    Timber.d("KPI_Onboarding_Start");
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (data != null) {
      if (data.hasExtra(LiveActivity.UNKNOWN_USER_FROM_DEEPLINK)) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          finishAndRemoveTask();
        } else {
          finish();
        }
        deepLink = null;
        userPhoneNumber.set(null);
        logout();
      }
    }
  }


  ////////////////
  //  PRIVATE   //
  ////////////////

  private void loginUser(String phoneNumber, AccessToken fbAccessToken) {
    if (getIntent().hasExtra(DEEP_LINK) && deepLink != null) {
      Timber.d("login from deeplink " + deepLink + ", phoneNumber :" + phoneNumber);
      loginEntity = authPresenter.login(null, null, null, null);
    } else if (phoneNumber != null) {
      if (phoneNumber.equals(MOCKED_PHONE_NUMBER)) {
        Timber.w("login with " + phoneNumber);
        digitAuth();
        return;
      }
      Timber.d("login with phoneNumber " + phoneNumber);
      loginEntity = authPresenter.login(phoneNumber, null, null, null);

    } else if (fbAccessToken != null) {
      Timber.d("login with fbAccessToken " + fbAccessToken);
      loginEntity = authPresenter.login(null, null, null, fbAccessToken);
    }
  }

  @OnClick(R.id.btnPhoneNumber) void digitAuth() {
    tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_Start);
    Timber.d("KPI_Onboarding_Start");

    authCallback = new AuthCallback() {
      @Override public void success(DigitsSession session, String phoneNumber) {
        tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_PinSucceeded);
        Timber.d("KPI_Onboarding_PinSucceeded");
        userPhoneNumber.set(phoneNumber);
        Timber.d("digit login success " + phoneNumber);
        loginUser(phoneNumber, null);
      }

      @Override public void failure(DigitsException error) {
        tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_PinFailed);
        Timber.d("KPI_Onboarding_PinFailed");
        userPhoneNumber.set(null);
      }
    };

    AuthConfig.Builder builder = new AuthConfig.Builder();
    builder.withAuthCallBack(authCallback);

    AuthConfig authConfig = builder.build();
    Digits.authenticate(authConfig);
  }

  @OnClick(R.id.btnFacebook) void facebookAuth() {
    tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_Start);
    Timber.d("KPI_Onboarding_Start");

    facebookPresenter.loginFacebook(true);
  }

  private void logout() {
    navigator.navigateToLogout(this);
    Timber.d("logout");
  }

  private void setSandboxBehavior() {
    sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    shakeDetector = new ShakeDetector(() -> {
      navigator.navigateToSandbox(this);
    });
  }

  private void initUi() {
    setContentView(R.layout.activity_auth);
    ButterKnife.bind(this);

    logoView.getViewTreeObserver().addOnGlobalLayoutListener(this);
  }



  private void initRessource() {
    authPresenter.onViewAttached(this);
    facebookPresenter.onViewAttached(this);
    sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI);

  }

  private void connectUser(User user) {
    this.currentUser.copy(user);
    Bundle properties = new Bundle();
    properties.putString(TagManagerUtils.TYPE, "signup");
    tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_AuthenticationSuccess, properties);
    Timber.d("KPI_Onboarding_AuthenticationSuccess");
    String countryCode = String.valueOf(phoneUtils.getCountryCode(loginEntity.getUsername()));
    if (deepLink != null) {
      Intent newIntent =
          IntentUtils.getLiveIntentFromURI(this, deepLink, LiveActivity.SOURCE_DEEPLINK);
      if (newIntent != null) {
        Timber.d("goToConnected from " + deepLink);
        navigator.navigateToIntent(this, newIntent);
        deepLink = null;
      }
    } else if (user == null || StringUtils.isEmpty(user.getProfilePicture()) || StringUtils.isEmpty(
        user.getUsername())) {
      Timber.d("goToConnected from new user");
      navigator.navigateToAuthProfile(this, null, loginEntity);
    } else {
      tagManager.updateUser(user);
      tagManager.setUserId(user.getId());
      Timber.d("goToConnected from " + user.getDisplayName());
      navigator.navigateToHomeFromLogin(this, countryCode, null);
    }
  }

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .applicationComponent(getApplicationComponent())
        .activityModule(getActivityModule())
        .build()
        .inject(this);
  }

  ////////////////
  //  OVERRIDE  //
  ////////////////


  @Override
  public void onGlobalLayout() {
    animate();
    imgLogo.getViewTreeObserver().removeOnGlobalLayoutListener(this);
  }

  @Override protected void onStart() {
    super.onStart();
  }

  @Override protected void onDestroy() {
    authPresenter.onViewDetached();
    facebookPresenter.onViewDetached();
    sensorManager.unregisterListener(shakeDetector);
    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    super.onDestroy();
  }

  @Override protected void onNewIntent(Intent intent) {
    setIntent(intent);
  }

  @Override public void finish() {
    super.finish();
  }

  @Override public void goToCode(Pin pin) {
    Timber.d("goToCode");
  }

  @Override public void goToConnected(User user) {
    connectUser(user);
  }

  @Override public void loginError(ErrorLogin errorLogin) {
    Timber.d("loginError");
    tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_AuthenticationError);
    Timber.d("KPI_Onboarding_AuthenticationError");
  }

  @Override public void pinError(ErrorLogin errorLogin) {
    Timber.d("errorLogin");
    userPhoneNumber.set(null);
    logout();
  }

  @Override public void showLoading() {
    loadingIndicator.setVisibility(View.VISIBLE);
  }

  @Override public void hideLoading() {
    loadingIndicator.setVisibility(View.GONE);
  }

  @Override public void showError(String message) {
    Timber.e("showError " + message);
  }

  @Override public Context context() {
    return this;
  }

  @Override public void errorFacebookLogin() {
    tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_PinFailed);
    Timber.d("KPI_Onboarding_PinFailed");
  }

  @Override public void loadFacebookInfos(FacebookEntity facebookEntity) { }

  @Override
  public void successFacebookLogin() {
    tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_PinSucceeded);
    Timber.d("KPI_Onboarding_PinSucceeded");
    userPhoneNumber.set(null);
    Timber.d("facebook login success " + FacebookUtils.accessToken().getToken());
    loginUser(null, FacebookUtils.accessToken());
  }

  /////////////////
  //  ANIMATION  //
  /////////////////

  private void animate() {

    SpringSystem springSystem = SpringSystem.create();
    SpringConfig config = SpringConfig.fromBouncinessAndSpeed(0f, 5f);

    int duration = 300;
    int delayStep1 = 1000;
    int delayStep2 = 2000;

    Spring springStep1 = springSystem.createSpring();
    Spring springStep2 = springSystem.createSpring();

    springStep1.setSpringConfig(config);
    springStep2.setSpringConfig(config);

    springStep1.addListener(new SimpleSpringListener() {

      @Override
      public void onSpringUpdate(Spring spring) {

        baseline.setTranslationY(imgLogo.getHeight() * 2/3 * (float)spring.getCurrentValue());
        imgLogo.setTranslationY(-imgLogo.getHeight() * 2/3 * (float)spring.getCurrentValue());
      }
    });

    springStep2.addListener(new SimpleSpringListener() {

      @Override
      public void onSpringUpdate(Spring spring) {

        buttonsView.setTranslationY(0 * (float)spring.getCurrentValue());
        logoView.setTranslationY(-buttonsView.getHeight() / 2 * (float)spring.getCurrentValue());
      }
    });

    buttonsView.setTranslationY(buttonsView.getHeight());

    baseline.animate()
            .alpha(1)
            .setStartDelay(delayStep1)
            .setListener(new Animator.AnimatorListener() {

              @Override
              public void onAnimationStart(Animator animator) {
                springStep1.setEndValue(1);
              }

              @Override public void onAnimationEnd(Animator animator) { }
              @Override public void onAnimationCancel(Animator animator) { }
              @Override public void onAnimationRepeat(Animator animator) { }
            })
            .setDuration(duration)
            .start();

    buttonsView.animate()
            .alpha(1)
            .setStartDelay(delayStep2)
            .setListener(new Animator.AnimatorListener() {

              @Override
              public void onAnimationStart(Animator animator) {
                springStep2.setEndValue(1);
              }

              @Override public void onAnimationEnd(Animator animator) { }
              @Override public void onAnimationCancel(Animator animator) { }
              @Override public void onAnimationRepeat(Animator animator) { }
            })
            .setDuration(duration)
            .start();
  }
}