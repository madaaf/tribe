package com.tribe.app.presentation.view.component.profile;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
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
import com.tribe.app.presentation.utils.analytics.TagManager;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.utils.preferences.FullscreenNotifications;
import com.tribe.app.presentation.view.component.ActionView;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class ProfileView extends ScrollView {

  @Inject User user;

  @Inject TagManager tagManager;

  @Inject Navigator navigator;

  @Inject @FullscreenNotifications Preference<Boolean> fullScreenNotifications;

  @BindView(R.id.avatar) AvatarView viewAvatar;

  @BindView(R.id.txtName) TextViewFont txtName;

  @BindView(R.id.txtUsername) TextViewFont txtUsername;

  @BindView(R.id.viewShareProfile) View viewShareProfile;

  @BindView(R.id.viewActionProfile) ActionView viewActionProfile;

  @BindView(R.id.viewActionFollow) ActionView viewActionFollow;

  @BindView(R.id.viewActionRateUs) ActionView viewActionRateUs;

  @BindView(R.id.viewActionLogout) ActionView viewActionLogout;

  @BindView(R.id.viewActionVisible) ActionView viewActionVisible;

  @BindView(R.id.viewActionPhoneIntegration) ActionView viewActionPhoneIntegration;

  @BindView(R.id.viewActionVideo) ActionView viewActionVideo;

  @BindView(R.id.viewActionBlocked) ActionView viewActionBlocked;

  @BindView(R.id.txtVersion) TextViewFont txtVersion;

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

  /////////////
  // PRIVATE //
  /////////////

  private void initUI() {
    txtName.setText(user.getDisplayName());
    txtUsername.setText("@" + user.getUsername());
    viewAvatar.load(user.getProfilePicture());

    viewActionVisible.setValue(!user.isInvisibleMode());
    viewActionPhoneIntegration.setValue(fullScreenNotifications.get());

    txtVersion.setText(getContext().getString(R.string.settings_version, BuildConfig.VERSION_NAME,
        String.valueOf(BuildConfig.VERSION_CODE)));
  }

  private void initSubscriptions() {
    subscriptions = new CompositeSubscription();

    subscriptions.add(viewActionVisible.onChecked().subscribe(isChecked -> {
      Bundle bundle = new Bundle();
      bundle.putBoolean(TagManagerUtils.USER_INVISIBLE_ENABLED, isChecked);
      tagManager.setProperty(bundle);
      onChangeVisible.onNext(!isChecked);
    }));

    subscriptions.add(viewActionPhoneIntegration.onChecked().flatMap(isChecked -> {
      if (!isChecked) {
        return DialogFactory.dialog(getContext(),
            getContext().getString(R.string.profile_fullscreen_notifications_disable_alert_title),
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

  @OnClick({ R.id.viewShareProfile, R.id.btnShare }) void clickShareProfile() {
    onShare.onNext(null);
  }

  /**
   * OBSERVABLES
   */

  public Observable<Void> onProfileClick() {
    return viewActionProfile.onClick();
  }

  public Observable<Void> onFollowClick() {
    return viewActionFollow.onClick();
  }

  public Observable<Void> onRateClick() {
    return viewActionRateUs.onClick();
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

  public Observable<Void> onVideo() {
    return viewActionVideo.onClick();
  }

  public Observable<Void> onShare() {
    return onShare;
  }

  public Observable<Void> onBlockedFriends() {
    return viewActionBlocked.onClick();
  }
}
