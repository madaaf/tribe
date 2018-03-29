package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.TextViewCompat;
import android.view.View;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.f2prateek.rx.preferences.Preference;
import com.facebook.login.LoginResult;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.jakewharton.rxbinding.view.RxView;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.R;
import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.domain.entity.FacebookEntity;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.ProfileInfoPresenter;
import com.tribe.app.presentation.mvp.view.ProfileInfoMVPView;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.FontUtils;
import com.tribe.app.presentation.utils.PermissionUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.facebook.RxFacebook;
import com.tribe.app.presentation.utils.mediapicker.RxImagePicker;
import com.tribe.app.presentation.utils.preferences.AddressBook;
import com.tribe.app.presentation.view.component.ProfileInfoView;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.PhoneUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.StateManager;
import com.tribe.app.presentation.view.widget.FacebookView;
import com.tribe.app.presentation.view.widget.TextViewFont;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class AuthProfileActivity extends BaseActivity implements ProfileInfoMVPView {

  private static final String LOGIN_ENTITY = "LOGIN_ENTITY";
  private static final String FACEBOOK_ENTITY = "FACEBOOK_ENTITY";
  private static final String DEEPLINK = "DEEPLINK";
  private static final String URI_PICTURE = "URI_PICTURE";

  public static Intent getCallingIntent(Context context, LoginEntity loginEntity) {
    Intent intent = new Intent(context, AuthProfileActivity.class);
    intent.putExtra(LOGIN_ENTITY, loginEntity);
    return intent;
  }

  @Inject User user;

  @Inject RxImagePicker rxImagePicker;

  @Inject RxFacebook rxFacebook;

  @Inject ScreenUtils screenUtils;

  @Inject PhoneUtils phoneUtils;

  @Inject User recipient;

  @Inject StateManager stateManager;

  @Inject ProfileInfoPresenter profileInfoPresenter;

  @Inject @AddressBook Preference<Boolean> addressBook;

  @BindView(R.id.profileInfoView) ProfileInfoView profileInfoView;

  @BindView(R.id.txtAction) TextViewFont txtAction;

  @BindView(R.id.progressView) CircularProgressView progressView;

  @BindView(R.id.facebookView) FacebookView facebookView;

  // VARIABLES
  private Uri deepLink;
  private Unbinder unbinder;
  private LoginEntity loginEntity;
  private FacebookEntity facebookEntity;
  private Uri uriPicture;
  private AccessToken accessToken;
  private RxPermissions rxPermissions;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_auth_profile);

    unbinder = ButterKnife.bind(this);

    if (savedInstanceState != null) {
      if (savedInstanceState.get(LOGIN_ENTITY) != null) {
        loginEntity = (LoginEntity) savedInstanceState.getSerializable(LOGIN_ENTITY);
      }
      if (savedInstanceState.get(FACEBOOK_ENTITY) != null) {
        facebookEntity = (FacebookEntity) savedInstanceState.getSerializable(FACEBOOK_ENTITY);
      }
      if (savedInstanceState.get(DEEPLINK) != null) {
        deepLink = savedInstanceState.getParcelable(DEEPLINK);
      }
      if (savedInstanceState.get(URI_PICTURE) != null) {
        uriPicture = savedInstanceState.getParcelable(URI_PICTURE);
      }
    }

    initDependencyInjector();
    initParams(getIntent());
    init();
  }

  private void askPermissionAccessContact() {
    rxPermissions.request(PermissionUtils.PERMISSIONS_CONTACTS).subscribe(hasPermission -> {
      Bundle bundle = new Bundle();
      bundle.putBoolean(TagManagerUtils.USER_ADDRESS_BOOK_ENABLED, hasPermission);
      tagManager.setProperty(bundle);

      Bundle bundleBis = new Bundle();
      bundleBis.putBoolean(TagManagerUtils.ACCEPTED, hasPermission);
      tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_SystemContacts, bundleBis);
      addressBook.set(hasPermission);
    });
  }

  private void askAccessFacebookContact() {
    if (!FacebookUtils.isLoggedIn()) {
      subscriptions.add(DialogFactory.dialog(context(),
          EmojiParser.demojizedText(context().getString(R.string.permission_facebook_popup_title)),
          EmojiParser.demojizedText(
              context().getString(R.string.permission_facebook_popup_message)),
          context().getString(R.string.permission_facebook_popup_ok),
          context().getString(R.string.permission_facebook_popup_ko))
          .filter(x -> x == true)
          .subscribe(a -> {
            profileInfoPresenter.loginFacebook();
          }));
    }
  }

  @Override protected void onPostCreate(@Nullable Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

    subscriptions.add(Observable.timer(500, TimeUnit.MILLISECONDS)
        .onBackpressureDrop()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          uriPicture = rxImagePicker.getUri();

          if (uriPicture != null) {
            profileInfoView.loadUri(uriPicture);
          }
        }));

    if (profileInfoView != null && uriPicture != null) profileInfoView.loadUri(uriPicture);
  }

  @Override protected void onStart() {
    super.onStart();
    profileInfoPresenter.onViewAttached(this);
  }

  @Override public void onResume() {
    super.onResume();

    subscriptions.add(Observable.timer(2000, TimeUnit.MILLISECONDS)
        .onBackpressureDrop()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          LoginResult loginResult = rxFacebook.getLoginResult();

          if (loginResult != null && FacebookUtils.isLoggedIn()) {
            successFacebookLogin();
          }
        }));
  }

  @Override protected void onStop() {
    profileInfoPresenter.onViewDetached();
    super.onStop();
  }

  @Override protected void onDestroy() {
    if (unbinder != null) unbinder.unbind();
    if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    super.onDestroy();
  }

  @Override protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    if (loginEntity != null) outState.putSerializable(LOGIN_ENTITY, loginEntity);
    if (facebookEntity != null) outState.putSerializable(FACEBOOK_ENTITY, facebookEntity);
    if (deepLink != null) outState.putParcelable(DEEPLINK, deepLink);
    if (uriPicture != null) outState.putParcelable(URI_PICTURE, uriPicture);
  }

  private void initParams(Intent intent) {
    if (intent != null && intent.hasExtra(LOGIN_ENTITY)) {
      loginEntity = phoneUtils.prepareLoginForRegister(
          (LoginEntity) intent.getSerializableExtra(LOGIN_ENTITY));

      if (FacebookUtils.isLoggedIn()) {
        facebookView.setVisibility(View.INVISIBLE);
        getInfoFromFacebook();
      }
    }

    manageDeepLink(intent);
  }

  private void init() {
    rxPermissions = new RxPermissions(this);
    askPermissionAccessContact();
    askAccessFacebookContact();

    subscriptions.add(profileInfoView.onInfoValid().subscribe(b -> {
      if (b) {
        TextViewCompat.setTextAppearance(txtAction, R.style.Title_2_BlueNew);
      } else {
        TextViewCompat.setTextAppearance(txtAction, R.style.Title_2_Grey);
      }
      txtAction.setCustomFont(this, FontUtils.PROXIMA_BOLD);
    }));

    subscriptions.add(RxView.clicks(facebookView).subscribe(aVoid -> {
      if (isReady()) {
        nextStep();
      } else {
        tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_ProfileFilledWithFacebook);
        if (FacebookUtils.isLoggedIn()) {
          getInfoFromFacebook();
        } else {
          profileInfoPresenter.loginFacebook();
        }
      }
    }));

    subscriptions.add(profileInfoView.onUsernameInput().subscribe(s -> {
      profileInfoPresenter.lookupUsername(s);
    }));
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

  private void getInfoFromFacebook() {
    profileInfoPresenter.loadFacebookInfos();
  }

  @OnClick(R.id.txtAction) void onClickAction() {
    if (isReady()) {
      nextStep();
    } else if (!profileInfoView.isDisplayNameSelected()) {
      profileInfoView.shakeDisplayName();
    } else if (!profileInfoView.isUsernameSelected()) {
      profileInfoView.shakeUsername();
    }
  }

  private boolean isReady() {
    return profileInfoView.isUsernameSelected() && profileInfoView.isDisplayNameSelected();
  }

  private void nextStep() {
    screenUtils.hideKeyboard(this);

    if (StringUtils.isEmpty(user.getId()) && accessToken == null) {
      profileInfoPresenter.register(profileInfoView.getDisplayName(), profileInfoView.getUsername(),
          loginEntity);
    } else if (accessToken != null && StringUtils.isEmpty(user.getId())) {
      showLoading();
      profileInfoPresenter.getUserInfo();
    } else if (!StringUtils.isEmpty(user.getId())) {
      showLoading();
      profileInfoPresenter.updateUser(user.getId(), profileInfoView.getUsername(),
          profileInfoView.getDisplayName(), profileInfoView.getImgUri(),
          FacebookUtils.accessToken());
    }
  }

  @Override public void userInfos(User user) {
    this.user.copy(user);

    Bundle bundle = new Bundle();
    bundle.putBoolean(TagManagerUtils.SUCCESS, true);
    tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_ProfileConfigured, bundle);

    Bundle bundleUser = new Bundle();
    bundleUser.putString(TagManagerUtils.USER_USERNAME, this.user.getUsername());
    tagManager.setProperty(bundleUser);
    tagManager.setUserId(this.user.getId());
    tagManager.updateUser(this.user);

    profileInfoPresenter.updateUser(user.getId(), user.getUsername(), user.getDisplayName(),
        profileInfoView.getImgUri(), FacebookUtils.accessToken());
  }

  @Override public void successUpdateUser(User user) {
    this.user.copy(user);
    String linkId = StringUtils.generateLinkId();

    if (loginEntity.getFbAccessToken() != null) {
      tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_OpenNewCallFacebook);
    } else {
      tagSignUp();
    }

    navigator.navigateToGameStoreLogin(this, deepLink, true);
    finish();
  }

  private void tagSignUp() {
    Bundle properties = new Bundle();
    properties.putString(TagManagerUtils.TYPE, TagManagerUtils.SIGNUP);
    properties.putString(TagManagerUtils.PLATFORM,
        loginEntity.getFbAccessToken() != null ? TagManagerUtils.PLATFORM_FACEBOOK
            : TagManagerUtils.PLATFORM_PHONE);
    tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_AuthenticationSuccess, properties);
  }

  @Override public void successFacebookLogin() {
    getInfoFromFacebook();
    facebookView.setVisibility(View.INVISIBLE);
  }

  @Override public void errorFacebookLogin() {

  }

  @Override public void loadFacebookInfos(FacebookEntity facebookEntity) {
    this.facebookEntity = facebookEntity;
    profileInfoView.setInfoFromFacebook(facebookEntity);
  }

  @Override public void onRegisterSuccess(AccessToken accessToken) {
    this.accessToken = accessToken;
    if (accessToken != null) {
      tagManager.alias(accessToken.getUserId());
      tagManager.setUserId(accessToken.getUserId());
    }
  }

  @Override public void successUpdateFacebook(User user) {

  }

  @Override public void successUpdatePhoneNumber(User user) {

  }

  @Override public void errorUpdatePhoneNumber() {

  }

  @Override public void onRegisterFail() {
    this.accessToken = null;
  }

  @Override public void usernameResult(Boolean available) {
    boolean usernameValid = available;
    profileInfoView.setUsernameValid(usernameValid ||
        (user != null &&
            !StringUtils.isEmpty(profileInfoView.getUsername()) &&
            profileInfoView.getUsername().equals(user.getUsername())));
  }

  @Override public void showLoading() {
    txtAction.setVisibility(View.GONE);
    progressView.setVisibility(View.VISIBLE);
  }

  @Override public void hideLoading() {
    txtAction.setVisibility(View.VISIBLE);
    progressView.setVisibility(View.GONE);
  }

  @Override public void showError(String message) {

  }

  @Override public Context context() {
    return this;
  }
}