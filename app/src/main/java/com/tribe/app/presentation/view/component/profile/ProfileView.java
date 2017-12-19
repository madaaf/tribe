package com.tribe.app.presentation.view.component.profile;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Pair;
import android.widget.ImageView;
import android.widget.ScrollView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.BuildConfig;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManager;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.preferences.FullscreenNotifications;
import com.tribe.app.presentation.view.component.ActionView;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.widget.TextViewFont;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class ProfileView extends ScrollView {

  @Inject User user;

  @Inject TagManager tagManager;

  @Inject Navigator navigator;

  @Inject @FullscreenNotifications Preference<Boolean> fullScreenNotifications;

  @BindView(R.id.viewActionVideo) ActionView viewActionVideo;

  @BindView(R.id.viewActionProfile) ActionView viewActionProfile;

  @BindView(R.id.viewActionManageFriendships) ActionView viewActionManageFriendships;

  @BindView(R.id.viewActionFollow) ActionView viewActionFollow;

  @BindView(R.id.viewActionRateUs) ActionView viewActionRateUs;

  @BindView(R.id.viewActionChangePhoneNumber) ActionView viewActionChangePhoneNumber;

  @BindView(R.id.viewActionFacebookAccount) ActionView viewActionFacebookAccount;

  @BindView(R.id.viewActionLogout) ActionView viewActionLogout;

  @BindView(R.id.viewActionVisible) ActionView viewActionVisible;

  @BindView(R.id.viewActionPhoneIntegration) ActionView viewActionPhoneIntegration;

  @BindView(R.id.viewActionBlocked) ActionView viewActionBlocked;

  @BindView(R.id.txtVersion) TextViewFont txtVersion;

  @BindView(R.id.txtTimeInCall) TextViewFont txtTimeInCall;

  @BindView(R.id.imgLogo) ImageView imgLogo;

  // OBSERVABLES
  private CompositeSubscription subscriptions;
  private PublishSubject<Boolean> onChangeVisible = PublishSubject.create();
  private PublishSubject<Void> onDebugMode = PublishSubject.create();
  private PublishSubject<Void> onShare = PublishSubject.create();

  public ProfileView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.bind(this);

    initDependencyInjector();
    initUI();
    initSubscriptions();

    reloadUserUI(user);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
  }

  public void onDestroy() {
    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
  }

  public void reloadUserUI(User user) {
    long minutes = Math.round(user.getTimeInCall() / 60.0f);
    txtTimeInCall.setText(" " +
        getContext().getString(
            minutes > 1 ? R.string.profile_calls_length_mins : R.string.profile_calls_length_min,
            minutes));

    viewActionChangePhoneNumber.setWarning(!canOpenPhoneNumberView());
    viewActionFacebookAccount.setWarning(!canOpenFacebookView());
  }

  public boolean canOpenFacebookView() {
    return FacebookUtils.isLoggedIn();
  }

  public boolean canOpenPhoneNumberView() {
    return !StringUtils.isEmpty(user.getPhone());
  }

  /////////////
  // PRIVATE //
  /////////////

  private void initUI() {

    viewActionVisible.setValue(!user.isInvisibleMode());
    viewActionPhoneIntegration.setValue(fullScreenNotifications.get());

    txtVersion.setText(getContext().getString(R.string.settings_version, BuildConfig.VERSION_NAME,
        String.valueOf(BuildConfig.VERSION_CODE)));
  }

  private void initSubscriptions() {
    subscriptions = new CompositeSubscription();

    subscriptions.add(viewActionVisible.onChecked().flatMap(isChecked -> {
      if (!isChecked) {
        return DialogFactory.dialog(getContext(), EmojiParser.demojizedText(
            getContext().getString(R.string.profile_invisible_mode_enable_alert_title)),
            getContext().getString(R.string.profile_invisible_mode_enable_alert_msg),
            getContext().getString(R.string.profile_fullscreen_notifications_disable_alert_disable),
            getContext().getString(R.string.action_cancel));
      } else {
        return Observable.just(true);
      }
    }, (isChecked, proceed) -> Pair.create(isChecked, proceed)).filter(pair -> {
      boolean shouldContinue = true;

      if (!pair.second) {
        shouldContinue = false;
        viewActionVisible.setValue(true);
      }

      return shouldContinue;
    }).subscribe(pair -> {
      Bundle bundle = new Bundle();
      bundle.putBoolean(TagManagerUtils.USER_INVISIBLE_ENABLED, pair.first);
      tagManager.setProperty(bundle);
      onChangeVisible.onNext(!pair.first);
    }));

    subscriptions.add(viewActionPhoneIntegration.onChecked().flatMap(isChecked -> {
      if (!isChecked) {
        return DialogFactory.dialog(getContext(), EmojiParser.demojizedText(
            getContext().getString(R.string.profile_fullscreen_notifications_disable_alert_title)),
            getContext().getString(R.string.profile_fullscreen_notifications_disable_alert_msg),
            getContext().getString(R.string.profile_fullscreen_notifications_disable_alert_disable),
            getContext().getString(R.string.action_cancel));
      } else {
        return Observable.just(true);
      }
    }, (isChecked, proceed) -> Pair.create(isChecked, proceed)).filter(pair -> {
      if (!pair.second) {
        pair = Pair.create(true, true);
        viewActionPhoneIntegration.setValue(true);
      }
      return pair.second;
    }).subscribe(pair -> fullScreenNotifications.set(pair.first)));
  }

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) getContext()));
  }

  ///////////////
  //  ONCLICK  //
  ///////////////

  @OnClick(R.id.imgLogo) void clickLogo() {
    if (BuildConfig.DEBUG) {
      onDebugMode.onNext(null);
    }
  }

  /**
   * OBSERVABLES
   */

  public Observable<Void> onProfileClick() {
    return viewActionProfile.onClick();
  }

  public Observable<Void> onVideoClick() {
    return viewActionVideo.onClick();
  }

  public Observable<Void> onFollowClick() {
    return viewActionFollow.onClick();
  }

  public Observable<Void> onRateClick() {
    return viewActionRateUs.onClick();
  }

  public Observable<Void> onChangePhoneNumberClick() {
    return viewActionChangePhoneNumber.onClick();
  }

  public Observable<Void> onFacebookAccountClick() {
    return viewActionFacebookAccount.onClick();
  }

  public Observable<Void> onLogoutClick() {
    return viewActionLogout.onClick();
  }

  public Observable<Boolean> onChangeVisible() {
    return onChangeVisible;
  }

  public Observable<Void> onDebugMode() {
    return onDebugMode;
  }

  public Observable<Void> onShare() {
    return onShare;
  }

  public Observable<Void> onBlockedFriends() {
    return viewActionBlocked.onClick();
  }

  public Observable<Void> onManageFriends() {
    return viewActionManageFriendships.onClick();
  }
}
