package com.tribe.app.presentation.view.activity;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewTreeObserver;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import com.f2prateek.rx.preferences.Preference;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.PhoneNumber;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.tribe.app.R;
import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.domain.entity.ErrorLogin;
import com.tribe.app.domain.entity.FacebookEntity;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.Pin;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.AuthPresenter;
import com.tribe.app.presentation.mvp.presenter.FacebookPresenter;
import com.tribe.app.presentation.mvp.view.AuthMVPView;
import com.tribe.app.presentation.mvp.view.FBInfoMVPView;
import com.tribe.app.presentation.utils.IntentUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.preferences.UserPhoneNumber;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.PhoneUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class AuthActivity extends BaseActivity
    implements AuthMVPView, FBInfoMVPView, ViewTreeObserver.OnGlobalLayoutListener {

  public static int APP_REQUEST_CODE = 99;
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
  @BindView(R.id.btnPhoneNumber) View btnPhoneNumber;
  @BindView(R.id.btnFacebook) View btnFacebook;
  @BindView(R.id.logoView) View logoView;
  @BindView(R.id.buttonsView) View buttonsView;
  @BindView(R.id.imgLogo) View imgLogo;
  @BindView(R.id.baseline) View baseline;

  @BindView(R.id.loading_indicator) View loadingIndicator;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  // VARIABLES
  private LoginEntity loginEntity;

  private Uri deepLink = null;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initUi();
    initDependencyInjector();
    initRessource();
    loginFromDeepLink();
  }

  @Override protected void onResume() {
    super.onResume();
  }

  ////////////////

  private void loginFromDeepLink() {

    deepLink = getIntent().getData();
    if (getIntent().hasExtra(DEEP_LINK) && deepLink != null) {
      Timber.d("login from deeplink " + deepLink);
      loginEntity = authPresenter.login(null, null, null, null, null);
    }
  }

  public void phoneLogin() {
    final Intent intent = new Intent(this, AccountKitActivity.class);
    AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
        new AccountKitConfiguration.AccountKitConfigurationBuilder(LoginType.PHONE,
            AccountKitActivity.ResponseType.TOKEN); // or .ResponseType.TOKEN
    // ... perform additional configuration ...
    intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
        configurationBuilder.build());
    startActivityForResult(intent, APP_REQUEST_CODE);
  }

  private void getAccount(String accessToken) {
    AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
      @Override public void onSuccess(final Account account) {
        // Get phone number
        PhoneNumber phoneNumber = account.getPhoneNumber();
        String phoneNumberString = phoneNumber.toString();

        tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_PinSucceeded);
        Timber.d("KPI_Onboarding_PinSucceeded");
        Timber.d("digit login success " + phoneNumberString);
        userPhoneNumber.set(phoneNumberString);
        loginEntity = authPresenter.login(phoneNumberString, null, null, null, accessToken);
      }

      @Override public void onError(final AccountKitError error) {
        // Handle Error
        tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_PinFailed);
        Timber.d("KPI_Onboarding_PinFailed");
        userPhoneNumber.set(null);
      }
    });
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
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

    if (requestCode == APP_REQUEST_CODE) { // confirm that this response matches your request
      AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
      if (loginResult.getError() != null) {
        Timber.e("login error " + loginResult.getError());
      } else {
        if (loginResult.getAccessToken() != null) {
          getAccount(loginResult.getAccessToken().getToken());
        }
      }
    }
  }

  @OnClick(R.id.btnPhoneNumber) void auth() {
    tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_Phone_Button);
    tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_Start);
    Timber.d("KPI_Onboarding_Start");
    phoneLogin();
  }

  private void alternativeAuth(boolean shouldCall) {

    subscriptions.add(
        DialogFactory.inputDialog(this, getString(R.string.onboarding_step_phone), null,
            getString(R.string.action_start), getString(R.string.action_cancel),
            InputType.TYPE_CLASS_PHONE).subscribe(phoneNumber -> {

          userPhoneNumber.set(phoneNumber);
          authPresenter.requestCode(phoneNumber, shouldCall);
        }));
  }

  @OnLongClick(R.id.btnPhoneNumber) boolean menuPhoneNumber() {
    subscriptions.add(DialogFactory.showBottomSheetForPhoneNumberAuth(this).subscribe(type -> {

      if (type != null) {
        switch (type.getTypeDef()) {

          case LabelType.LOGIN:
            auth();
            break;

          case LabelType.LOGIN_ALTERNATIVE:
            alternativeAuth(false);
            break;

          case LabelType.LOGIN_CALL:
            alternativeAuth(true);
            break;
        }
      }
    }));

    return true;
  }

  @OnLongClick(R.id.btnFacebook) boolean menuFacebook() {
    subscriptions.add(DialogFactory.showBottomSheetForFacebookAuth(this).subscribe(type -> {

      if (type != null) {
        switch (type.getTypeDef()) {

          case LabelType.LOGIN:
            facebookAuth();
            break;

          case LabelType.FORCE_LOGOUT:
            FacebookUtils.logout();
            AccountKit.logOut();
            facebookAuth();
            break;
        }
      }
    }));

    return true;
  }

  @OnClick(R.id.btnFacebook) void facebookAuth() {
    tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_Facebook_Button);
    tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_Start);
    Timber.d("KPI_Onboarding_Start");

    facebookPresenter.loginFacebook();
  }

  private void logout() {
    navigator.navigateToLogout(this);
    Timber.d("logout");
  }

  private void initUi() {
    setContentView(R.layout.activity_auth);
    ButterKnife.bind(this);

    logoView.getViewTreeObserver().addOnGlobalLayoutListener(this);
  }

  private void initRessource() {
    authPresenter.onViewAttached(this);
    facebookPresenter.onViewAttached(this);
  }

  private void tagLogin() {
    Bundle properties = new Bundle();
    properties.putString(TagManagerUtils.TYPE, "login");
    tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_AuthenticationSuccess, properties);
  }

  private void connectUser(User user) {
    this.currentUser.copy(user);
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
    } else if (user == null ||
        StringUtils.isEmpty(user.getProfilePicture()) ||
        StringUtils.isEmpty(user.getUsername())) {
      Timber.d("goToConnected from new user");

      navigator.navigateToAuthProfile(this, null, loginEntity);
    } else {
      tagManager.updateUser(user);
      tagManager.setUserId(user.getId());
      tagLogin();
      Timber.d("goToConnected from " + user.getDisplayName());
      navigator.navigateToGameStoreLogin(this, deepLink, false);
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

  @Override public void onGlobalLayout() {
    animate();
    imgLogo.getViewTreeObserver().removeOnGlobalLayoutListener(this);
  }

  @Override protected void onStart() {
    super.onStart();
  }

  @Override protected void onDestroy() {
    authPresenter.onViewDetached();
    facebookPresenter.onViewDetached();
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

    subscriptions.add(
        DialogFactory.inputDialog(context(), getString(R.string.onboarding_step_code), null,
            getString(R.string.action_enter), getString(R.string.action_cancel),
            InputType.TYPE_CLASS_NUMBER).subscribe(code -> {

          Timber.d("login with phoneNumber (alternative) " + userPhoneNumber.get());
          loginEntity =
              authPresenter.login(userPhoneNumber.get(), code, pin.getPinId(), null, null);
        }));
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
  }

  @Override public void loadFacebookInfos(FacebookEntity facebookEntity) {
  }

  @Override public void successFacebookLogin() {
    userPhoneNumber.set(null);

    String token = FacebookUtils.accessToken().getToken();
    Timber.d("facebook login success " + token);
    loginEntity = authPresenter.login(null, null, null, token, null);
  }

  /////////////////
  //  ANIMATION  //
  /////////////////

  private void animate() {

    float buttonsViewHeight = buttonsView.getHeight();
    buttonsView.setTranslationY(buttonsViewHeight);

    btnFacebook.setTranslationY(btnFacebook.getHeight() * 6);
    btnPhoneNumber.setTranslationY(btnPhoneNumber.getHeight() * 3);

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

      @Override public void onSpringUpdate(Spring spring) {

        baseline.setTranslationY(imgLogo.getHeight() * 2 / 3 * (float) spring.getCurrentValue());
        imgLogo.setTranslationY(-imgLogo.getHeight() * 2 / 3 * (float) spring.getCurrentValue());
      }
    });

    springStep2.addListener(new SimpleSpringListener() {

      @Override public void onSpringUpdate(Spring spring) {

        buttonsView.setTranslationY(buttonsViewHeight * (1 - (float) spring.getCurrentValue()));
        logoView.setTranslationY(-buttonsViewHeight / 2 * (float) spring.getCurrentValue());
        btnFacebook.setTranslationY(
            (btnFacebook.getHeight() * 6) * (1 - (float) spring.getCurrentValue()));
        btnPhoneNumber.setTranslationY(
            (btnPhoneNumber.getHeight() * 3) * (1 - (float) spring.getCurrentValue()));
      }
    });

    baseline.animate()
        .alpha(1)
        .setStartDelay(delayStep1)
        .setListener(new Animator.AnimatorListener() {

          @Override public void onAnimationStart(Animator animator) {
            springStep1.setEndValue(1);
          }

          @Override public void onAnimationEnd(Animator animator) {
          }

          @Override public void onAnimationCancel(Animator animator) {
          }

          @Override public void onAnimationRepeat(Animator animator) {
          }
        })
        .setDuration(duration)
        .start();

    buttonsView.animate()
        .alpha(1)
        .setStartDelay(delayStep2)
        .setListener(new Animator.AnimatorListener() {

          @Override public void onAnimationStart(Animator animator) {
            springStep2.setEndValue(1);
          }

          @Override public void onAnimationEnd(Animator animator) {
          }

          @Override public void onAnimationCancel(Animator animator) {
          }

          @Override public void onAnimationRepeat(Animator animator) {
          }
        })
        .setDuration(duration)
        .start();
  }
}