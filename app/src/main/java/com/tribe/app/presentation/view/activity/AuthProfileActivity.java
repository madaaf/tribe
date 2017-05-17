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
import com.tribe.app.domain.entity.Group;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.AccessPresenter;
import com.tribe.app.presentation.mvp.presenter.ProfileInfoPresenter;
import com.tribe.app.presentation.mvp.view.AccessMVPView;
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
import com.tribe.app.presentation.utils.preferences.LastSync;
import com.tribe.app.presentation.view.component.ProfileInfoView;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.PhoneUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.FacebookView;
import com.tribe.app.presentation.view.widget.TextViewFont;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.tribe.app.presentation.utils.Extras.COUNTRY_CODE;

public class AuthProfileActivity extends BaseActivity implements ProfileInfoMVPView, AccessMVPView {

  private static final String LOGIN_ENTITY = "LOGIN_ENTITY";
  private static final String FACEBOOK_ENTITY = "FACEBOOK_ENTITY";
  private static final String DEEPLINK = "DEEPLINK";
  private static final String URI_PICTURE = "URI_PICTURE";

  public static Intent getCallingIntent(Context context, LoginEntity loginEntity,
      String countryCode) {
    Intent intent = new Intent(context, AuthProfileActivity.class);
    intent.putExtra(LOGIN_ENTITY, loginEntity);
    intent.putExtra(COUNTRY_CODE, countryCode);
    return intent;
  }

  @Inject User user;

  @Inject RxImagePicker rxImagePicker;

  @Inject RxFacebook rxFacebook;

  @Inject ScreenUtils screenUtils;

  @Inject PhoneUtils phoneUtils;

  @Inject @AddressBook Preference<Boolean> addressBook;

  @Inject @LastSync Preference<Long> lastSync;

  @Inject ProfileInfoPresenter profileInfoPresenter;

  @Inject AccessPresenter accessPresenter;

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
  private int totalTimeSynchro;
  private int nbFriends = 0;
  private long timeSyncStart = 0;
  private RxPermissions rxPermissions;
  private String countryCode = null;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private Subscription lookupSubscription;
  private Subscription startSubscription;
  private Subscription endSubscription;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_auth_profile);

    unbinder = ButterKnife.bind(this);
    initIntent(getIntent());

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
    initResources();
    initParams(getIntent());
    init();
  }

  private void initIntent(Intent intent) {
    if (intent != null) countryCode = intent.getStringExtra(COUNTRY_CODE);
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
    accessPresenter.onViewAttached(this);
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
    accessPresenter.onViewDetached();
    super.onStop();
  }

  @Override protected void onDestroy() {
    if (unbinder != null) unbinder.unbind();
    if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    if (lookupSubscription != null) lookupSubscription.unsubscribe();
    if (startSubscription != null) startSubscription.unsubscribe();
    if (endSubscription != null) endSubscription.unsubscribe();
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
    }

    manageDeepLink(intent);
  }

  private void init() {

    rxPermissions = new RxPermissions(this);

    startSubscription = Observable.timer(0, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> start());

    tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_FindFriendsStart);

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

      if (deepLink != null && !StringUtils.isEmpty(deepLink.getPath())) {
        if (deepLink.getPath().startsWith("/g/")) {
          accessPresenter.lookupGroupInfos(StringUtils.getLastBitFromUrl(deepLink.toString()));
        }
      }
    }
  }

  private void initResources() {
    totalTimeSynchro = getResources().getInteger(R.integer.time_synchro);
  }

  private void lookupContacts() {
    rxPermissions.request(PermissionUtils.PERMISSIONS_CONTACTS).subscribe(hasPermission -> {
      Bundle bundle = new Bundle();
      bundle.putBoolean(TagManagerUtils.USER_ADDRESS_BOOK_ENABLED, hasPermission);
      tagManager.setProperty(bundle);

      Bundle bundleBis = new Bundle();
      bundleBis.putBoolean(TagManagerUtils.ACCEPTED, true);
      tagManager.trackEvent(TagManagerUtils.KPI_Onboarding_SystemContacts, bundleBis);

      if (hasPermission) {
        addressBook.set(true);
        timeSyncStart = System.currentTimeMillis();
        accessPresenter.lookupContacts();
      } else {
        renderFriendList(new ArrayList<>());
      }
    });
  }

  private void start() {
    startSubscription.unsubscribe();
    lookupContacts();
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
      profileInfoPresenter.updateUser(profileInfoView.getUsername(),
          profileInfoView.getDisplayName(), profileInfoView.getImgUri(),
          facebookEntity != null && !StringUtils.isEmpty(facebookEntity.getId())
              ? facebookEntity.getId() : null);
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

    profileInfoPresenter.updateUser(user.getUsername(), user.getDisplayName(),
        profileInfoView.getImgUri(),
        facebookEntity != null && !StringUtils.isEmpty(facebookEntity.getId())
            ? facebookEntity.getId() : null);
  }

  @Override public void renderFriendList(List<User> userList) {
/*    lastSync.set(System.currentTimeMillis());

    Map<String, Object> relationsInApp = new HashMap<>();

    for (User user : userList) {
      relationsInApp.put(user.getId(), user);
    }

    if (relationsInApp.values() != null && relationsInApp.values().size() > 0) {

      lookupSubscription = Observable.zip(Observable.from(relationsInApp.values()),
          Observable.interval(0, totalTimeSynchro / relationsInApp.values().size(),
              TimeUnit.MILLISECONDS).onBackpressureDrop(), (contact, aLong) -> contact)
          .subscribeOn(Schedulers.newThread())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(relation -> {
            nbFriends++;

            if (nbFriends == relationsInApp.values().size()) {
              subscriptions.add(Observable.timer(750, TimeUnit.MILLISECONDS)
                  .onBackpressureDrop()
                  .subscribeOn(Schedulers.newThread())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(time -> showCongrats()));
            }
          });
    } else {
      showCongrats();
    }*/
  }

/*  private void showCongrats() {
    endSubscription = Observable.timer(0, TimeUnit.MILLISECONDS).subscribe(aLong -> {
      //navigator.navigateToHomeFromLogin(this, deepLink, countryCode);
      Timber.e("SOEF showCongrats");
    });
  }*/

  @Override public void groupInfosFailed() {

  }

  @Override public void groupInfosSuccess(Group group) {

  }

  @Override public void successUpdateUser(User user) {//MADA
    this.user.copy(user);
    Timber.e("SOEF successUpdateUser");

    subscriptions.add(DialogFactory.dialog(this,
        EmojiParser.demojizedText(getString(R.string.onboarding_user_alert_call_link_title)),
        getString(R.string.onboarding_user_alert_call_link_msg),
        getString(R.string.onboarding_user_alert_call_link_sms), null)
        .filter(x -> x == true)
        .subscribe(a -> {
          navigator.navigateToHomeFromLogin(this, deepLink, countryCode, false);
          String linkId = StringUtils.generateLinkId();
          String url = StringUtils.getUrlFromLinkId(this, linkId);
          navigator.openSMSDefaultApp(this, EmojiParser.demojizedText(
              getString(R.string.onboarding_user_alert_call_link_content, url)));
        }));
  }

  @Override public void successFacebookLogin() {
    getInfoFromFacebook();
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

  @Override public void onRegisterFail() {
    this.accessToken = null;
  }

  @Override public void usernameResult(Boolean available) {
    boolean usernameValid = available;
    profileInfoView.setUsernameValid(usernameValid || (user != null
        && !StringUtils.isEmpty(profileInfoView.getUsername())
        && profileInfoView.getUsername().equals(user.getUsername())));
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