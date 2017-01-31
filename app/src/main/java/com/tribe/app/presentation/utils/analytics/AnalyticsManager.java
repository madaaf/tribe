package com.tribe.app.presentation.utils.analytics;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.preferences.AddressBook;
import com.tribe.app.presentation.utils.preferences.AudioDefault;
import com.tribe.app.presentation.utils.preferences.Filter;
import com.tribe.app.presentation.utils.preferences.LocationContext;
import com.tribe.app.presentation.utils.PermissionUtils;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Created by tiago on 19/10/2016.
 */
@Singleton public class AnalyticsManager implements TagManager {

  @Inject @Filter Preference<Integer> filter;

  @Inject @LocationContext Preference<Boolean> locationContext;

  @Inject @AudioDefault Preference<Boolean> audioDefault;

  @Inject @AddressBook Preference<Boolean> addressBook;

  private AmplitudeTagManager amplitude;
  private BranchTagManager branch;

  @Inject public AnalyticsManager(Context context, @Named("userThreadSafe") User user) {
    this.amplitude = new AmplitudeTagManager(context, user);
    this.branch = new BranchTagManager(context);

    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);

    Bundle bundle = new Bundle();

    switch (filter.get()) {
      case 1:
        bundle.putString(TagManagerConstants.FILTER_ENABLED, TagManagerConstants.FILTER_TAN);
        break;
      case 2:
        bundle.putString(TagManagerConstants.FILTER_ENABLED,
            TagManagerConstants.FILTER_BLACK_WHITE);
        break;
      case 3:
        bundle.putString(TagManagerConstants.FILTER_ENABLED, TagManagerConstants.FILTER_PIXEL);
        break;
      default:
        bundle.putString(TagManagerConstants.FILTER_ENABLED, TagManagerConstants.FILTER_NONE);
        break;
    }

    bundle.putBoolean(TagManagerConstants.LOCATION_ENABLED, locationContext.get());
    bundle.putBoolean(TagManagerConstants.AUDIO_ONLY_ENABLED, audioDefault.get());
    bundle.putBoolean(TagManagerConstants.FACEBOOK_CONNECTED, FacebookUtils.isLoggedIn());
    bundle.putBoolean(TagManagerConstants.ADDRESS_BOOK_ENABLED,
        addressBook.get() && PermissionUtils.hasPermissionsContact(context));
    if (user != null) {
      bundle.putBoolean(TagManagerConstants.INVISIBLE_MODE_ENABLED, user.isInvisibleMode());
      bundle.putBoolean(TagManagerConstants.MEMORIES_ENABLED, user.isTribeSave());
    }
    bundle.putBoolean(TagManagerConstants.CAMERA_ENABLED,
        PermissionUtils.hasPermissionsCamera(context));
    bundle.putBoolean(TagManagerConstants.MICROPHONE_ENABLED,
        PermissionUtils.hasPermissionsCamera(context));
    bundle.putBoolean(TagManagerConstants.PUSH_ENABLED,
        user.isPushNotif()); // ALWAYS TRUE ON ANDROID FOR PERMISSIONS

    if (user != null) bundle.putString(TagManagerConstants.USERNAME, user.getUsername());

    setProperty(bundle);
  }

  @Override public void setUserId(String userId) {
    amplitude.setUserId(userId);
    branch.setUserId(userId);
  }

  @Override public void onStart(Activity activity) {
    branch.onStart(activity);
  }

  @Override public void onStop(Activity activity) {
    branch.onStop(activity);
  }

  @Override public void trackInstall() {
    amplitude.trackInstall();
    branch.trackInstall();
  }

  @Override public void trackEvent(String event) {
    amplitude.trackEvent(event);
    branch.trackEvent(event);
  }

  @Override public void trackEvent(String event, Bundle properties) {
    amplitude.trackEvent(event, properties);
    branch.trackEvent(event, properties);
  }

  @Override public void setProperty(Bundle properties) {
    amplitude.setProperty(properties);
    branch.setProperty(properties);
  }

  @Override public void setPropertyOnce(Bundle properties) {
    amplitude.setPropertyOnce(properties);
    branch.setPropertyOnce(properties);
  }

  @Override public void increment(String properties) {
    amplitude.increment(properties);
    branch.increment(properties);
  }

  @Override public void clear() {
    amplitude.clear();
    branch.clear();
  }
}
