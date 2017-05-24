package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.AuthConfig;
import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;
import com.f2prateek.rx.preferences.Preference;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.tribe.app.R;
import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.domain.entity.ErrorLogin;
import com.tribe.app.domain.entity.Pin;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.AuthPresenter;
import com.tribe.app.presentation.mvp.view.AuthMVPView;
import com.tribe.app.presentation.utils.IntentUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.utils.preferences.UserPhoneNumber;
import com.tribe.app.presentation.view.utils.PhoneUtils;
import com.tribe.app.presentation.view.utils.ShakeDetector;
import javax.inject.Inject;
import timber.log.Timber;

public class AuthActivity extends BaseActivity implements AuthMVPView {
  private static String MOCKED_PHONE_NUMBER = "+15556787676";
  private static String DEEP_LINK = "DEEP_LINK";

  public static Intent getCallingIntent(Context context, Uri deepLink) {
    Intent intent = new Intent(context, AuthActivity.class);
    intent.putExtra(DEEP_LINK, deepLink);
    return intent;
  }

  @Inject User currentUser;

  @Inject PhoneUtils phoneUtils;

  @Inject AuthPresenter authPresenter;

  @Inject @UserPhoneNumber Preference<String> userPhoneNumber;

  @BindView(R.id.progressView) CircularProgressView progressView;

  // VARIABLES
  private Unbinder unbinder;
  private LoginEntity loginEntity;
  private ShakeDetector mShakeDetector;
  private SensorManager mSensorManager;
  private Sensor mAccelerometer;
  private Boolean enableSandbox = false;
  private Uri deepLink = null;
  private AuthCallback authCallback;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    unbinder = ButterKnife.bind(this);

    initDependencyInjector();
    setSandboxBehavior();
    initRessource();
    deepLink = getIntent().getData();
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
      }
    }
  }
  ////////////////
  //  PRIVATE   //
  ////////////////

  private void authentifyUser(String phoneNumber) {
    if (phoneNumber.equals(MOCKED_PHONE_NUMBER)) return;
    tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_PinConfirmed);
    if (!enableSandbox) {
      loginEntity = authPresenter.login(phoneNumber, null, null);
    } else if (phoneNumber.startsWith("+8502121")) {
      loginEntity = authPresenter.login(phoneNumber, null, null);
    } else {
      Toast toast = Toast.makeText(getApplicationContext(), "PIN ERROR", Toast.LENGTH_SHORT);
      toast.show();
      logout();
    }
  }

  private void digitAuth() {
    authCallback = new AuthCallback() {
      @Override public void success(DigitsSession session, String phoneNumber) {
        userPhoneNumber.set(phoneNumber);
        authentifyUser(phoneNumber);
      }

      @Override public void failure(DigitsException error) {
        tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_PinFailed);
        Timber.e(error);
        logout();
      }
    };

    AuthConfig.Builder builder = new AuthConfig.Builder();
    builder.withAuthCallBack(authCallback);

    AuthConfig authConfig = builder.build();
    Digits.authenticate(authConfig);
  }

  private void logout() {
    navigator.navigateToLogout(this);
  }

  private void setSandboxBehavior() {
    mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    mShakeDetector = new ShakeDetector(() -> {
      Toast toast;
      if (!enableSandbox) {
        toast = Toast.makeText(getApplicationContext(), "enable Sandbox", Toast.LENGTH_SHORT);
        Digits.enableSandbox();
      } else {
        toast = Toast.makeText(getApplicationContext(), "disable Sandbox", Toast.LENGTH_SHORT);
        Digits.disableSandbox();
      }
      enableSandbox = !enableSandbox;
      toast.show();
    });
  }

  private void initRessource() {
    authPresenter.onViewAttached(this);
    mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
  }

  private void connectUser(User user) {
    Timber.d("goToConnected");
/*    String phoneNumber = (user != null) ? user.getPhone() : null;
    userPhoneNumber.set(phoneNumber);*/
    this.currentUser.copy(user);
    tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_PinSucceeded);
    String countryCode = String.valueOf(phoneUtils.getCountryCode(loginEntity.getUsername()));
    if (deepLink != null) {
      Intent newIntent =
          IntentUtils.getLiveIntentFromURI(this, deepLink, LiveActivity.SOURCE_DEEPLINK);
      if (newIntent != null) {
        navigator.navigateToIntent(this, newIntent);
        deepLink = null;
      }
    } else if (user == null || StringUtils.isEmpty(user.getProfilePicture()) || StringUtils.isEmpty(
        user.getUsername())) {
      navigator.navigateToAuthProfile(this, null, loginEntity);
    } else {
      tagManager.updateUser(user);
      tagManager.setUserId(user.getId());
      navigator.navigateToHomeFromLogin(this, null, countryCode, null);
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

  @Override protected void onResume() {
    super.onResume();
    if (getIntent().hasExtra(DEEP_LINK) && deepLink != null) {
      loginEntity = authPresenter.login(null, null, null);
    } else {
      if (userPhoneNumber.get() != null) {
        authentifyUser(userPhoneNumber.get());
      } else {
        digitAuth();
      }
    }
  }

  @Override protected void onStart() {
    super.onStart();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    if (unbinder != null) unbinder.unbind();
    authPresenter.onViewDetached();
    mSensorManager.unregisterListener(mShakeDetector);
  }

  @Override protected void onNewIntent(Intent intent) {
    setIntent(intent);
  }

  @Override public void finish() {
    super.finish();
  }

  @Override public void goToCode(Pin pin) {
  }

  @Override public void goToConnected(User user) {
    connectUser(user);
  }

  @Override public void loginError(ErrorLogin errorLogin) {
    Timber.d("loginError");
  }

  @Override public void pinError(ErrorLogin errorLogin) {
    Timber.d("errorLogin");
  }

  @Override public void showLoading() {
    progressView.setVisibility(View.VISIBLE);
  }

  @Override public void hideLoading() {
    progressView.setVisibility(View.INVISIBLE);
  }

  @Override public void showError(String message) {
    Timber.e("showError " + message);
  }

  @Override public Context context() {
    return this;
  }
}