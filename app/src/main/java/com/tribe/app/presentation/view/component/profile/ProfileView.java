package com.tribe.app.presentation.view.component.profile;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.BuildConfig;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.analytics.TagManager;
import com.tribe.app.presentation.utils.analytics.TagManagerConstants;
import com.tribe.app.presentation.view.component.ActionView;
import com.tribe.app.presentation.view.widget.TextViewFont;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class ProfileView extends FrameLayout {

  @Inject User user;

  @Inject TagManager tagManager;

  @BindView(R.id.viewActionProfile) ActionView viewActionProfile;

  @BindView(R.id.viewActionFollow) ActionView viewActionFollow;

  @BindView(R.id.viewActionRateUs) ActionView viewActionRateUs;

  @BindView(R.id.viewActionLogout) ActionView viewActionLogout;

  @BindView(R.id.viewActionVisible) ActionView viewActionVisible;

  @BindView(R.id.txtVersion) TextViewFont txtVersion;

  // OBSERVABLES
  private CompositeSubscription subscriptions;
  private PublishSubject<Boolean> onChangeVisible = PublishSubject.create();

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

  private void initUI() {
    viewActionVisible.setValue(!user.isInvisibleMode());

    viewActionProfile.setTitle(
        EmojiParser.demojizedText(getContext().getString(R.string.profile_user_infos)));
    viewActionFollow.setTitle(
        EmojiParser.demojizedText(getContext().getString(R.string.profile_follow_us)));
    viewActionRateUs.setTitle(
        EmojiParser.demojizedText(getContext().getString(R.string.profile_rate_us)));
    viewActionLogout.setTitle(
        EmojiParser.demojizedText(getContext().getString(R.string.profile_logout)));
    viewActionVisible.setTitle(
        EmojiParser.demojizedText(getContext().getString(R.string.profile_invisible_mode)));

    txtVersion.setText(getContext().getString(R.string.settings_version, BuildConfig.VERSION_NAME,
        String.valueOf(BuildConfig.VERSION_CODE)));
  }

  private void initSubscriptions() {
    subscriptions = new CompositeSubscription();

    subscriptions.add(viewActionVisible.onChecked().subscribe(isChecked -> {
      Bundle bundle = new Bundle();
      bundle.putBoolean(TagManagerConstants.INVISIBLE_MODE_ENABLED, isChecked);
      tagManager.setProperty(bundle);
      onChangeVisible.onNext(!isChecked);
    }));
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
}
