package com.tribe.app.presentation.view.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.AuthConfig;
import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.solera.defrag.AnimationHandler;
import com.solera.defrag.TraversalAnimation;
import com.solera.defrag.TraversingOperation;
import com.solera.defrag.TraversingState;
import com.solera.defrag.ViewStack;
import com.tribe.app.BuildConfig;
import com.tribe.app.R;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.domain.entity.FacebookEntity;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.ProfilePresenter;
import com.tribe.app.presentation.mvp.view.ProfileMVPView;
import com.tribe.app.presentation.service.BroadcastUtils;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.view.component.profile.ProfileView;
import com.tribe.app.presentation.view.component.settings.SettingsBlockedFriendsView;
import com.tribe.app.presentation.view.component.settings.SettingsFacebookAccountView;
import com.tribe.app.presentation.view.component.settings.SettingsManageFriendshipsView;
import com.tribe.app.presentation.view.component.settings.SettingsPhoneNumberView;
import com.tribe.app.presentation.view.component.settings.SettingsProfileView;
import com.tribe.app.presentation.view.notification.Alerter;
import com.tribe.app.presentation.view.notification.NotificationPayload;
import com.tribe.app.presentation.view.notification.NotificationUtils;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.MissedCallManager;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.ViewStackHelper;
import com.tribe.app.presentation.view.widget.LiveNotificationView;
import com.tribe.app.presentation.view.widget.TextViewFont;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static android.view.View.GONE;

public class ProfileActivity extends BaseActivity implements ProfileMVPView {

  private static final int DURATION = 200;

  public static Intent getCallingIntent(Context context) {
    Intent intent = new Intent(context, ProfileActivity.class);
    return intent;
  }

  @Inject ScreenUtils screenUtils;

  @Inject ProfilePresenter profilePresenter;

  @Inject MissedCallManager missedCallManager;

  @BindView(R.id.txtTitle) TextViewFont txtTitle;

  @BindView(R.id.txtTitleTwo) TextViewFont txtTitleTwo;

  @BindView(R.id.txtAction) TextViewFont txtAction;

  @BindView(R.id.viewNavigatorStack) ViewStack viewStack;

  @BindView(R.id.progressView) CircularProgressView progressView;

  // VIEWS
  private ProfileView viewProfile;
  private SettingsProfileView viewSettingsProfile;
  private SettingsPhoneNumberView viewSettingsPhoneNumber;
  private SettingsFacebookAccountView viewSettingsFacebookAccount;
  private SettingsBlockedFriendsView viewSettingsBlockedFriends;
  private SettingsManageFriendshipsView viewSettingsManageFriendships;

  // VARIABLES
  private boolean disableUI = false;
  private ProgressDialog progressDialog;
  private NotificationReceiver notificationReceiver;
  private boolean receiverRegistered;
  private FirebaseRemoteConfig firebaseRemoteConfig;

  // OBSERVABLES
  private Unbinder unbinder;
  private CompositeSubscription subscriptions = new CompositeSubscription();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_profile);

    unbinder = ButterKnife.bind(this);

    initDependencyInjector();
    init(savedInstanceState);
    initPresenter();
    initRemoteConfig();
  }

  @Override protected void onStart() {
    super.onStart();
    profilePresenter.onViewAttached(this);
  }

  @Override protected void onResume() {
    super.onResume();
    if (!receiverRegistered) {
      if (notificationReceiver == null) notificationReceiver = new NotificationReceiver();

      registerReceiver(notificationReceiver,
          new IntentFilter(BroadcastUtils.BROADCAST_NOTIFICATIONS));
      receiverRegistered = true;
    }
  }

  @Override protected void onPause() {
    if (receiverRegistered) {
      unregisterReceiver(notificationReceiver);
      receiverRegistered = false;
    }

    super.onPause();
  }

  @Override protected void onStop() {
    profilePresenter.onViewDetached();
    super.onStop();
  }

  @Override protected void onDestroy() {
    if (unbinder != null) unbinder.unbind();
    if (subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    if (viewSettingsProfile != null) viewSettingsProfile.onDestroy();
    if (viewSettingsPhoneNumber != null) viewSettingsPhoneNumber.onDestroy();
    if (viewSettingsFacebookAccount != null) viewSettingsFacebookAccount.onDestroy();
    if (viewSettingsBlockedFriends != null) viewSettingsBlockedFriends.onDestroy();
    if (viewSettingsManageFriendships != null) viewSettingsManageFriendships.onDestroy();
    if (viewProfile != null) viewProfile.onDestroy();
    if (progressDialog != null) progressDialog.dismiss();
    super.onDestroy();
  }

  private void init(Bundle savedInstanceState) {
    txtTitle.setText(R.string.profile_title);

    txtTitleTwo.setTranslationX(screenUtils.getWidthPx());

    viewStack.setAnimationHandler(createCustomAnimationHandler());
    viewStack.addTraversingListener(
        traversingState -> disableUI = traversingState != TraversingState.IDLE);

    if (savedInstanceState == null) {
      setupMainView();
    }

    txtAction.setOnClickListener(v -> {
      if (viewStack.getTopView() instanceof SettingsProfileView) {
        screenUtils.hideKeyboard(this);
        profilePresenter.updateUser(getCurrentUser().getId(), viewSettingsProfile.getUsername(),
            viewSettingsProfile.getDisplayName(), viewSettingsProfile.getImgUri(),
            FacebookUtils.accessToken());
      }
    });
  }

  private void initPresenter() {
    profilePresenter.onViewAttached(this);
  }

  private void initRemoteConfig() {
    firebaseRemoteConfig = firebaseRemoteConfig.getInstance();
    FirebaseRemoteConfigSettings configSettings =
        new FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(BuildConfig.DEBUG)
            .build();
    firebaseRemoteConfig.setConfigSettings(configSettings);

    firebaseRemoteConfig.fetch().addOnCompleteListener(task -> {
      if (task.isSuccessful()) {
        firebaseRemoteConfig.activateFetched();
      }
    });
  }

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .applicationComponent(getApplicationComponent())
        .activityModule(getActivityModule())
        .build()
        .inject(this);
  }

  @OnClick(R.id.imgBack) void clickBack() {
    onBackPressed();
  }

  @Override public void onBackPressed() {
    screenUtils.hideKeyboard(this);

    if (disableUI) {
      return;
    }

    if (!viewStack.pop()) {
      super.onBackPressed();
    }
  }

  @Override public void finish() {
    super.finish();
    overridePendingTransition(R.anim.activity_in_scale, R.anim.activity_out_to_right);
  }

  @Override public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
    return disableUI || super.dispatchTouchEvent(ev);
  }

  @Override public Object getSystemService(@NonNull String name) {
    if (ViewStackHelper.matchesServiceName(name)) {
      return viewStack;
    }

    return super.getSystemService(name);
  }

  @NonNull private AnimationHandler createCustomAnimationHandler() {
    return (from, to, operation) -> {
      boolean forward = operation != TraversingOperation.POP;

      AnimatorSet set = new AnimatorSet();

      set.setDuration(DURATION);
      set.setInterpolator(new DecelerateInterpolator());

      final int width = from.getWidth();

      computeTitle(forward, to);

      if (forward) {
        to.setTranslationX(width);
        set.play(ObjectAnimator.ofFloat(from, View.TRANSLATION_X, 0 - (width)));
        set.play(ObjectAnimator.ofFloat(to, View.TRANSLATION_X, 0));
      } else {
        to.setTranslationX(0 - (width));
        set.play(ObjectAnimator.ofFloat(from, View.TRANSLATION_X, width));
        set.play(ObjectAnimator.ofFloat(to, View.TRANSLATION_X, 0));
      }

      return TraversalAnimation.newInstance(set,
          forward ? TraversalAnimation.ABOVE : TraversalAnimation.BELOW);
    };
  }

  private void setupMainView() {
    viewProfile = (ProfileView) viewStack.push(R.layout.view_profile);

    subscriptions.add(viewProfile.onShare().subscribe(aVoid -> {
      String linkId =
          navigator.sendInviteToCall(this, firebaseRemoteConfig, TagManagerUtils.PROFILE, null,
              null, false);
      profilePresenter.bookRoomLink(linkId);
    }));

    subscriptions.add(viewProfile.onProfileClick().subscribe(aVoid -> setupProfileDetailView()));

    subscriptions.add(viewProfile.onFollowClick()
        .flatMap(aVoid -> DialogFactory.showBottomSheetForFollow(this), ((aVoid, labelType) -> {
          if (labelType.getTypeDef().equals(LabelType.INSTAGRAM)) {
            navigator.navigateToUrl(this, getString(R.string.settings_follow_instagram_url));
          } else if (labelType.getTypeDef().equals(LabelType.SNAPCHAT)) {
            navigator.navigateToUrl(this, getString(R.string.settings_follow_snapchat_url));
          } else if (labelType.getTypeDef().equals(LabelType.TWITTER)) {
            navigator.navigateToUrl(this, getString(R.string.settings_follow_twitter_url));
          }

          return null;
        }))
        .subscribe());

    subscriptions.add(viewProfile.onRateClick().subscribe(aVoid -> navigator.rateApp(this)));

    subscriptions.add(viewProfile.onChangePhoneNumberClick().subscribe(aVoid -> {

      if (viewProfile.canOpenPhoneNumberView()) {
        setupPhoneNumberView();
      } else {
        changeMyPhoneNumber();
      }
    }));

    subscriptions.add(viewProfile.onFacebookAccountClick().subscribe(aVoid -> {

      if (viewProfile.canOpenFacebookView()) {
        setupFacebookAccountView();
      } else {
        profilePresenter.loginFacebook();
      }
    }));

    subscriptions.add(viewProfile.onLogoutClick()
        .flatMap(aVoid -> DialogFactory.dialog(this, getString(R.string.settings_logout_title),
            getString(R.string.settings_logout_confirm_message),
            getString(R.string.settings_logout_title), getString(R.string.action_cancel)))
        .filter(x -> x == true)
        .doOnNext(aBoolean -> {
          tagManager.trackEvent(TagManagerUtils.Logout);
          progressDialog = DialogFactory.createProgressDialog(this, R.string.settings_logout_wait);
          progressDialog.show();
          profilePresenter.logout();
        })
        .delay(300, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(aBoolean -> ((AndroidApplication) getApplication()).logoutUser())
        .delay(2000, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(aBoolean -> finish())
        .subscribe(aVoid -> {

        }));

    subscriptions.add(viewProfile.onChangeVisible()
        .filter(aBoolean -> getCurrentUser().isInvisibleMode() != aBoolean)
        .subscribe(aBoolean -> profilePresenter.updateUserInvisibleMode(aBoolean)));

    subscriptions.add(
        viewProfile.onDebugMode().subscribe(aVoid -> navigator.navigateToDebugMode(this)));

    subscriptions.add(viewProfile.onBlockedFriends().subscribe(aVoid -> setupBlockedFriendsView()));

    subscriptions.add(
        viewProfile.onManageFriends().subscribe(aVoid -> setupManageFriendshipsView()));
  }

  private void setupProfileDetailView() {
    viewSettingsProfile = (SettingsProfileView) viewStack.push(R.layout.view_settings_profile);

    subscriptions.add(
            viewSettingsProfile.onUsernameInput().subscribe(s -> profilePresenter.lookupUsername(s)));

    subscriptions.add(viewSettingsProfile.onInfoValid().subscribe(b -> txtAction.setEnabled(b)));
  }

  private void setupPhoneNumberView() {
    viewSettingsPhoneNumber = (SettingsPhoneNumberView) viewStack.push(R.layout.view_settings_phone_number);

    subscriptions.add(viewSettingsPhoneNumber.onChangePhoneNumberClick().subscribe(aVoid -> {
      changeMyPhoneNumber();
    }));
  }

  private void changeMyPhoneNumber() {

    AuthConfig.Builder builder = new AuthConfig.Builder();
    builder.withAuthCallBack(new AuthCallback() {

      @Override public void success(DigitsSession session, String phoneNumber) {
        profilePresenter.updatePhoneNumber(getCurrentUser().getId(), session);
      }

      @Override public void failure(DigitsException error) {
        showError(error.getMessage());
      }
    });

    AuthConfig authConfig = builder.build();

    Digits.logout(); // Force logout
    Digits.authenticate(authConfig);
  }

  private void setupFacebookAccountView() {
    viewSettingsFacebookAccount = (SettingsFacebookAccountView) viewStack.push(R.layout.view_settings_facebook_account);
    profilePresenter.loadFacebookInfos();

    subscriptions.add(viewSettingsFacebookAccount.onChecked().subscribe(aBool -> {

      if (!aBool) {
        if (viewProfile.canOpenPhoneNumberView()) {
          subscriptions.add(DialogFactory.dialog(this, EmojiParser.demojizedText(getString(R.string.linked_friends_notifications_disable_fb_alert_title)),
                  getString(R.string.linked_friends_notifications_disable_fb_alert_msg),
                  getString(R.string.action_cancel),
                  getString(R.string.linked_friends_notifications_disable_fb_alert_disable))
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(shouldCancel -> {

                    if (shouldCancel) {
                      viewSettingsFacebookAccount.setChecked(true);

                    } else {
                      FacebookUtils.logout();
                      profilePresenter.disconnectFromFacebook(getCurrentUser().getId());
                    }
                  }));

        } else {
          showToastMessage(getString(R.string.linked_friends_unlink_error_unable_to_unlink));
          viewSettingsFacebookAccount.setChecked(true);
        }

      } else {
        profilePresenter.loginFacebook();
      }
    }));
  }

  private void setupBlockedFriendsView() {
    viewSettingsBlockedFriends =
        (SettingsBlockedFriendsView) viewStack.push(R.layout.view_settings_blocked_friends);

    subscriptions.add(viewSettingsBlockedFriends.onUnblock().subscribe(recipient -> {
      if (recipient instanceof Friendship) {
        Friendship fr = (Friendship) recipient;
        profilePresenter.updateFriendship(fr.getId(), fr.isMute(), FriendshipRealm.DEFAULT);
      }
    }));

    subscriptions.add(viewSettingsBlockedFriends.onHangLive()
        .subscribe(recipient -> navigator.navigateToLive(this, recipient, PaletteGrid.get(0),
            LiveActivity.SOURCE_FRIENDS)));

    profilePresenter.loadBlockedFriendshipList();
  }

  private void setupManageFriendshipsView() {
    viewSettingsManageFriendships =
        (SettingsManageFriendshipsView) viewStack.push(R.layout.view_settings_manage_friendships);

    subscriptions.add(viewSettingsManageFriendships.onClickRemove()
        .flatMap(recipient -> DialogFactory.showBottomSheetForRecipient(this, recipient),
            ((recipient, labelType) -> {
              if (labelType != null) {
                if (labelType.getTypeDef().equals(LabelType.HIDE) || labelType.getTypeDef()
                    .equals(LabelType.BLOCK_HIDE)) {
                  Friendship friendship = (Friendship) recipient;
                  profilePresenter.updateFriendship(friendship.getId(), friendship.isMute(),
                      labelType.getTypeDef().equals(LabelType.BLOCK_HIDE) ? FriendshipRealm.BLOCKED
                          : FriendshipRealm.HIDDEN);
                }
              }

              return recipient;
            }))
        .subscribe(recipient -> viewSettingsManageFriendships.remove((Friendship) recipient)));

    subscriptions.add(viewSettingsManageFriendships.onClickMute().doOnNext(friendship -> {
      friendship.setMute(!friendship.isMute());
      profilePresenter.updateFriendship(friendship.getId(), friendship.isMute(),
          friendship.getStatus());
    }).subscribe());

    profilePresenter.loadUnblockedFriendshipList();
  }

  private void computeTitle(boolean forward, View to) {
    if (to instanceof ProfileView) {
      setupTitle(getString(R.string.profile_title), forward);
      txtAction.setVisibility(GONE);
    } else if (to instanceof SettingsProfileView) {
      setupTitle(getString(R.string.settings_profile_title), forward);
      txtAction.setVisibility(View.VISIBLE);
      txtAction.setText(getString(R.string.action_save));
    } else if (to instanceof SettingsBlockedFriendsView) {
      setupTitle(getString(R.string.profile_blocked_friends), forward);
      txtAction.setVisibility(GONE);
    } else if (to instanceof SettingsManageFriendshipsView) {
      setupTitle(getString(R.string.manage_friendships_title), forward);
      txtAction.setVisibility(View.GONE);
    } else if (to instanceof SettingsPhoneNumberView) {
      setupTitle(getString(R.string.profile_change_phone_title), forward);
      txtAction.setVisibility(View.GONE);
    } else if (to instanceof SettingsFacebookAccountView) {
      setupTitle(getString(R.string.profile_facebook_account_title), forward);
      txtAction.setVisibility(View.GONE);
    }
  }

  private void setupTitle(String title, boolean forward) {
    if (txtTitle.getTranslationX() == 0) {
      txtTitleTwo.setText(title);
      hideTitle(txtTitle, forward);
      showTitle(txtTitleTwo, forward);
    } else {
      txtTitle.setText(title);
      hideTitle(txtTitleTwo, forward);
      showTitle(txtTitle, forward);
    }
  }

  private void hideTitle(View view, boolean forward) {
    if (forward) {
      view.animate()
          .translationX(-(screenUtils.getWidthPx() / 3))
          .alpha(0)
          .setDuration(DURATION)
          .start();
    } else {
      view.animate().translationX(screenUtils.getWidthPx()).setDuration(DURATION).start();
    }
  }

  private void showTitle(View view, boolean forward) {
    if (forward) {
      view.setTranslationX(screenUtils.getWidthPx());
      view.setAlpha(1);
    } else {
      view.setTranslationX(-(screenUtils.getWidthPx() / 3));
      view.setAlpha(0);
    }

    view.animate().translationX(0).alpha(1).setDuration(DURATION).start();
  }

  private void declineInvitation(String sessionId) {
    profilePresenter.declineInvite(sessionId);
  }

  @Override public void goToLauncher() {
  }

  @Override public void renderBlockedFriendshipList(List<Friendship> friendshipList) {
    if (viewSettingsBlockedFriends != null) {
      viewSettingsBlockedFriends.renderBlockedFriendshipList(friendshipList);
    }
  }

  @Override public void renderUnblockedFriendshipList(List<Friendship> friendshipList) {
    if (viewSettingsManageFriendships != null) {
      viewSettingsManageFriendships.renderUnblockedFriendshipList(friendshipList);
    }
  }

  @Override public void successUpdateUser(User user) {
    viewProfile.reloadUserUI();
    this.clickBack();
  }

  @Override
  public void loadFacebookInfos(FacebookEntity facebookEntity) {

    if (viewSettingsFacebookAccount != null) {
      viewSettingsFacebookAccount.reloadUserUI(facebookEntity);
    }
  }

  @Override public void successFacebookLogin() {
    profilePresenter.connectToFacebook(getCurrentUser().getId(), FacebookUtils.accessToken().getToken());
    profilePresenter.loadFacebookInfos();
  }

  @Override public void errorFacebookLogin() {

  }

  @Override public void usernameResult(Boolean available) {
    boolean usernameValid = available;
    if (viewStack.getTopView() instanceof SettingsProfileView) {
      viewSettingsProfile.setUsernameValid(usernameValid || viewSettingsProfile.getUsername()
          .equals(getCurrentUser().getUsername()));
    }
  }

  @Override public void showLoading() {
    txtAction.setVisibility(GONE);
    progressView.setVisibility(View.VISIBLE);
  }

  @Override public void hideLoading() {
    txtAction.setVisibility(View.VISIBLE);
    progressView.setVisibility(GONE);
  }

  @Override
  public void successUpdateFacebook(User user) {
    viewProfile.reloadUserUI();

    if (viewSettingsFacebookAccount != null) {
      if (FacebookUtils.isLoggedIn()) {
        profilePresenter.loadFacebookInfos();
        showToastMessage(getString(R.string.linked_friends_link_success_fb));

      } else {
        viewSettingsFacebookAccount.reloadUserUI(null);
        showToastMessage(getString(R.string.linked_friends_unlink_success_fb));
      }
    }
  }

  @Override
  public void successUpdatePhoneNumber(User user) {
    viewProfile.reloadUserUI();

    if (viewSettingsPhoneNumber != null) {
      viewSettingsPhoneNumber.reloadUserUI();
    }

    showToastMessage(getString(R.string.linked_friends_link_success_phone));

    if (StringUtils.isEmpty(user.getPhone())) {
      onBackPressed();
    }
  }

  @Override public void showError(String message) {

  }

  @Override public Context context() {
    return this;
  }

  class NotificationReceiver extends BroadcastReceiver {

    @Override public void onReceive(Context context, Intent intent) {
      NotificationPayload notificationPayload =
          (NotificationPayload) intent.getSerializableExtra(BroadcastUtils.NOTIFICATION_PAYLOAD);

      LiveNotificationView liveNotificationView =
          NotificationUtils.getNotificationViewFromPayload(context, notificationPayload, null);

      if (liveNotificationView != null) {
        subscriptions.add(liveNotificationView.onClickAction()
            .delay(500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(action -> {
              if (action.getId().equals(NotificationUtils.ACTION_DECLINE)) {
                declineInvitation(action.getSessionId());
              } else if (action.getIntent() != null) {
                navigator.navigateToIntent(ProfileActivity.this, action.getIntent());
              }
            }));

        Alerter.create(ProfileActivity.this, liveNotificationView).show();
      }
    }
  }
}
