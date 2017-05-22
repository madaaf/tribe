package com.tribe.app.presentation.view.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
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
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.view.utils.PhoneUtils;
import com.tribe.app.presentation.view.utils.ShakeDetector;
import javax.inject.Inject;
import timber.log.Timber;

public class AuthActivity extends BaseActivity implements AuthMVPView {
  public static Intent getCallingIntent(Context context) {
    Intent intent = new Intent(context, AuthActivity.class);
    return intent;
  }

  @Inject User currentUser;

  @Inject PhoneUtils phoneUtils;

  @Inject AuthPresenter authPresenter;

  @BindView(R.id.progressView) CircularProgressView progressView;

  // VARIABLES
  private Unbinder unbinder;
  private LoginEntity loginEntity;
  private ShakeDetector mShakeDetector;
  private SensorManager mSensorManager;
  private Sensor mAccelerometer;
  private Boolean enableSandbox = false;
  private AuthCallback authCallback;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    unbinder = ButterKnife.bind(this);
    initDependencyInjector();
    setSandboxBehavior();
    initRessource();
    digitAuth();
  }

  ////////////////
  //  PRIVATE   //
  ////////////////

  private void digitAuth() {
    authCallback = new AuthCallback() {
      @Override public void success(DigitsSession session, String phoneNumber) {
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

      @Override public void failure(DigitsException error) {
        tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_PinFailed);
        Timber.e(error);
      }
    };

    AuthConfig.Builder builder = new AuthConfig.Builder();
    builder.withAuthCallBack(authCallback);
    AuthConfig authConfig = builder.build();
    Digits.authenticate(authConfig);
  }
  
  private void logout() {
    FacebookUtils.logout();
    Digits.logout();
    Intent intent = new Intent(this, HomeActivity.class);
    intent.putExtra(IntentUtils.FINISH, true);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);

    Intent intentLauncher = new Intent(this, LauncherActivity.class);
    intentLauncher.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    intentLauncher.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
    intentLauncher.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
    int pendingIntentId = 123456; // FAKE ID
    PendingIntent mPendingIntent = PendingIntent.getActivity(this, pendingIntentId, intentLauncher,
        PendingIntent.FLAG_CANCEL_CURRENT);
    AlarmManager mgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
    System.exit(0);
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

  private void finishActivity() {
    if (unbinder != null) unbinder.unbind();
    authPresenter.onViewDetached();
    mSensorManager.unregisterListener(mShakeDetector);
  }

  private void connectUser(User user) {
    Timber.d("goToConnected");
    this.currentUser.copy(user);
    tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_PinSucceeded);
    String countryCode = String.valueOf(phoneUtils.getCountryCode(loginEntity.getUsername()));
    if (user == null || StringUtils.isEmpty(user.getProfilePicture()) || StringUtils.isEmpty(
        user.getUsername())) {
      navigator.navigateToAuthProfile(this, null, loginEntity);
    } else {
      tagManager.updateUser(user);
      tagManager.setUserId(user.getId());
      navigator.navigateToHomeFromLogin(this, null, countryCode, true);
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
  }

  @Override protected void onStart() {
    super.onStart();
    initRessource();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    finishActivity();
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