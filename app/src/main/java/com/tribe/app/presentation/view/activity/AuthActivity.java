package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.AuthConfig;
import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;
import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.domain.entity.ErrorLogin;
import com.tribe.app.domain.entity.Pin;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.AuthPresenter;
import com.tribe.app.presentation.mvp.view.AuthMVPView;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.view.utils.PhoneUtils;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class AuthActivity extends BaseActivity implements AuthMVPView {

  public static Intent getCallingIntent(Context context) {
    Intent intent = new Intent(context, AuthActivity.class);
    return intent;
  }

  @Inject User currentUser;

  @Inject PhoneUtils phoneUtils;

  @Inject AuthPresenter authPresenter;

  private LoginEntity loginEntity;

  private AuthCallback authCallback;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initDependencyInjector();
  }

  @Override protected void onStart() {
    super.onStart();
    authPresenter.onViewAttached(this);
  }

  @Override protected void onDestroy() {
    authPresenter.onViewDetached();
    if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    super.onDestroy();
  }

  @Override protected void onResume() {
    super.onResume();
    Uri deepLink = getIntent().getData();
    AuthConfig.Builder builder = new AuthConfig.Builder();
    builder.withAuthCallBack(new AuthCallback() {
      @Override public void success(DigitsSession session, String phoneNumber) {
        tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_PinConfirmed);
        loginEntity = authPresenter.login(phoneNumber, null, null);
      }

      @Override public void failure(DigitsException error) {
        tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_PinFailed);
        Timber.e(error);
      }
    });

    AuthConfig authConfig = builder.build();
    Digits.authenticate(authConfig);
  }

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .applicationComponent(getApplicationComponent())
        .activityModule(getActivityModule())
        .build()
        .inject(this);
  }

  @Override protected void onNewIntent(Intent intent) {
    setIntent(intent);
  }

  @Override public void finish() {
    super.finish();
  }

  @Override public void goToCode(Pin pin) {
    Timber.e("SOEF goToCode");
  }

  @Override public void goToConnected(User user) {
    Timber.e("SOEF goToConnected");
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

  @Override public void loginError(ErrorLogin errorLogin) {
    Timber.e("SOEF loginError");
  }

  @Override public void pinError(ErrorLogin errorLogin) {
    Timber.e("SOEF pinError");
  }

  @Override public void showLoading() {
    Timber.e("SOEF showLoading");
  }

  @Override public void hideLoading() {
    Timber.e("SOEF hideLoading");
  }

  @Override public void showError(String message) {
    Timber.e("SOEF showError");
  }

  @Override public Context context() {
    return this;
  }
}