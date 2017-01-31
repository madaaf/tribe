package com.tribe.app.presentation.view.component.settings;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.widget.ScrollView;

import com.f2prateek.rx.preferences.Preference;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.utils.PermissionUtils;
import com.tribe.app.presentation.utils.analytics.TagManager;
import com.tribe.app.presentation.utils.analytics.TagManagerConstants;
import com.tribe.app.presentation.utils.preferences.AudioDefault;
import com.tribe.app.presentation.utils.preferences.LocationContext;
import com.tribe.app.presentation.utils.preferences.UISounds;
import com.tribe.app.presentation.utils.preferences.WeatherUnits;
import com.tribe.app.presentation.view.component.ActionView;
import com.tribe.app.presentation.view.utils.Weather;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 12/6/2016.
 */

public class SettingsParametersView extends ScrollView {

  @Inject User user;

  @Inject TagManager tagManager;

  @Inject ReactiveLocationProvider reactiveLocationProvider;

  @Inject @LocationContext Preference<Boolean> locationContext;

  @Inject @AudioDefault Preference<Boolean> audioDefault;

  @Inject @UISounds Preference<Boolean> uiSounds;

  @Inject @WeatherUnits Preference<String> weatherUnits;

  @BindView(R.id.viewActionNotifications) ActionView viewActionNotifications;

  @BindView(R.id.viewActionLocation) ActionView viewActionLocation;

  @BindView(R.id.viewActionInvisible) ActionView viewActionInvisible;

  @BindView(R.id.viewActionAudioDefaults) ActionView viewActionAudioDefaults;

  @BindView(R.id.viewActionMemories) ActionView viewActionMemories;

  @BindView(R.id.viewActionUISounds) ActionView viewActionUISounds;

  @BindView(R.id.viewActionWeatherUnits) ActionView viewActionWeatherUnits;

  // VARIABLES

  // OBSERVABLES
  private CompositeSubscription subscriptions;
  private PublishSubject<Boolean> onChangeMemories = PublishSubject.create();
  private PublishSubject<Boolean> onChangeInvisible = PublishSubject.create();
  private PublishSubject<Boolean> onChangeLocation = PublishSubject.create();
  private PublishSubject<Boolean> onChangeNotifications = PublishSubject.create();

  public SettingsParametersView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();
    ButterKnife.bind(this);

    initDependencyInjector();
    initUI();
    initSubscriptions();
  }

  public void onDestroy() {
    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
  }

  private void initUI() {
    viewActionNotifications.setValue(user.isPushNotif());
    viewActionMemories.setValue(user.isTribeSave());
    viewActionLocation.setValue(locationContext.get());
    viewActionAudioDefaults.setValue(audioDefault.get());
    viewActionWeatherUnits.setValue(weatherUnits.get().equals(Weather.FAHRENHEIT));
    viewActionInvisible.setValue(user.isInvisibleMode());
    viewActionUISounds.setValue(uiSounds.get());
  }

  private void initSubscriptions() {
    subscriptions = new CompositeSubscription();

    subscriptions.add(viewActionNotifications.onChecked().subscribe(isChecked -> {
      Bundle bundle = new Bundle();
      bundle.putBoolean(TagManagerConstants.PUSH_ENABLED, isChecked);
      tagManager.setProperty(bundle);
      onChangeNotifications.onNext(isChecked);
    }));

    subscriptions.add(viewActionMemories.onChecked().subscribe(isChecked -> {
      Bundle bundle = new Bundle();
      bundle.putBoolean(TagManagerConstants.MEMORIES_ENABLED, isChecked);
      tagManager.setProperty(bundle);
      onChangeMemories.onNext(isChecked);
    }));

    subscriptions.add(viewActionLocation.onChecked().subscribe(isChecked -> {
      if (isChecked && !PermissionUtils.hasPermissionsLocation(getContext())) {
        RxPermissions.getInstance(getContext())
            .request(PermissionUtils.PERMISSIONS_LOCATION)
            .subscribe(granted -> {
              if (granted) {
                onChangeLocation.onNext(isChecked);
              } else {
                viewActionLocation.setValue(false);
              }
            });
      }

      Bundle bundle = new Bundle();
      bundle.putBoolean(TagManagerConstants.LOCATION_ENABLED, isChecked);
      tagManager.setProperty(bundle);
      locationContext.set(isChecked);
    }));

    subscriptions.add(viewActionAudioDefaults.onChecked().subscribe(isChecked -> {
      Bundle bundle = new Bundle();
      bundle.putBoolean(TagManagerConstants.AUDIO_ONLY_ENABLED, isChecked);
      tagManager.setProperty(bundle);
      audioDefault.set(isChecked);
    }));

    subscriptions.add(viewActionWeatherUnits.onChecked().subscribe(isChecked -> {
      if (isChecked) {
        weatherUnits.set(Weather.FAHRENHEIT);
      } else {
        weatherUnits.set(Weather.CELSIUS);
      }
    }));

    subscriptions.add(viewActionInvisible.onChecked().subscribe(isChecked -> {
      Bundle bundle = new Bundle();
      bundle.putBoolean(TagManagerConstants.INVISIBLE_MODE_ENABLED, isChecked);
      tagManager.setProperty(bundle);
      onChangeInvisible.onNext(isChecked);
    }));

    subscriptions.add(viewActionUISounds.onChecked().subscribe(isChecked -> {
      uiSounds.set(isChecked);
    }));
  }

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) getContext()));
  }

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  /**
   * OBSERVABLES
   */

  public Observable<Boolean> onChangeMemories() {
    return onChangeMemories;
  }

  public Observable<Boolean> onChangeInvisible() {
    return onChangeInvisible;
  }

  public Observable<Boolean> onChangeLocation() {
    return onChangeLocation;
  }

  public Observable<Boolean> onChangeNotifications() {
    return onChangeNotifications;
  }
}
