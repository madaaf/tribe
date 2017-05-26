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
    Timber.e("SOEF ON CREATE");
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
        deepLink = null;
        Timber.e("SOEF ON ACTIVITY RESULT FINISH");
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
    if (phoneNumber.equals(MOCKED_PHONE_NUMBER)) {
      digitAuth();
      Timber.e("SOEF 1 AUTHIFY_USER MOCKED_PHONE_NUMBER ");
      return;
    }
    tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_PinConfirmed);
    if (!enableSandbox) {
      Timber.d("SOEF 2 AUTHIFY_USER LOGIN NORMAL");
      loginEntity = authPresenter.login(phoneNumber, null, null);
    } else if (phoneNumber.startsWith("+8502121")) {
      Timber.d("SOEF 3 AUTHIFY_USER LOGIN +8050");
      loginEntity = authPresenter.login(phoneNumber, null, null);
    } else {
      Timber.e("SOEF 4 AUTHIFY_USER LOGIN PIN ERROR");
      Toast toast = Toast.makeText(getApplicationContext(), "PIN ERROR", Toast.LENGTH_SHORT);
      toast.show();
      logout();
    }
  }

  private void digitAuth() {
    Timber.e("SOEF DIGIT AUTH");
    authCallback = new AuthCallback() {
      @Override public void success(DigitsSession session, String phoneNumber) {
        Timber.e("SOEF DIGIT AUTH SUCCESS");
        userPhoneNumber.set(phoneNumber);
        authentifyUser(phoneNumber);
      }

      @Override public void failure(DigitsException error) {
        Timber.e("SOEF DIGIT FAILURE");
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
    Timber.i("SOEF NAVIGATE LOGOUT");
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
        Timber.e("SOEF ENABLE SENDBOX");
      } else {
        toast = Toast.makeText(getApplicationContext(), "disable Sandbox", Toast.LENGTH_SHORT);
        Digits.disableSandbox();
        Timber.e("SOEF DISABLE SENDBOX");
      }
      enableSandbox = !enableSandbox;
      toast.show();
    });
  }

  private void initRessource() {
    Timber.e("SOEF INIT RESSOURCE");
    authPresenter.onViewAttached(this);
    mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
  }

  private void connectUser(User user) {
    Timber.d("goToConnected");
    this.currentUser.copy(user);
    tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_PinSucceeded);
    String countryCode = String.valueOf(phoneUtils.getCountryCode(loginEntity.getUsername()));
    if (deepLink != null) {
      Intent newIntent =
          IntentUtils.getLiveIntentFromURI(this, deepLink, LiveActivity.SOURCE_DEEPLINK);
      if (newIntent != null) {
        Timber.i("SOEF 1 CONNECT_USER NAVIGATE DEEPLINK");
        navigator.navigateToIntent(this, newIntent);
        deepLink = null;
      }
    } else if (user == null || StringUtils.isEmpty(user.getProfilePicture()) || StringUtils.isEmpty(
        user.getUsername())) {
      Timber.i("SOEF 2 CONNECT_USER NAVIGATE AUTH");
      navigator.navigateToAuthProfile(this, null, loginEntity);
    } else {
      Timber.i("SOEF 3 CONNECT_USER NAVIGATE HOME");
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
      Timber.d("SOEF 1 ON_RESUME LOGIN DEEPLINK");
      loginEntity = authPresenter.login(null, null, null);
    } else {
      if (userPhoneNumber.get() != null) {
        Timber.e("SOEF 2 ON_RESUME AUTH USER");
        authentifyUser(userPhoneNumber.get());
      } else {
        Timber.e("SOEF 3 ON_RESUME DIGIT AUTH");
        digitAuth();
      }
    }
  }

  @Override protected void onStart() {
    super.onStart();
    Timber.e("SOEF ON_START");
  }

  @Override protected void onDestroy() {
    if (unbinder != null) unbinder.unbind();
    authPresenter.onViewDetached();
    mSensorManager.unregisterListener(mShakeDetector);
    super.onDestroy();
    Timber.e("SOEF ON_DESTROY");
  }

  @Override protected void onNewIntent(Intent intent) {
    setIntent(intent);
  }

  @Override public void finish() {
    Timber.e("SOEF ON_FINISH");
    super.finish();
  }

  @Override public void goToCode(Pin pin) {
    Timber.d("goToCode");
    Timber.e("SOEF goToCode");
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