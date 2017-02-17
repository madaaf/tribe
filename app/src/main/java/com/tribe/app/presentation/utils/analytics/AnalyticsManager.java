package com.tribe.app.presentation.utils.analytics;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.PermissionUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.preferences.AddressBook;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Created by tiago on 19/10/2016.
 */
@Singleton public class AnalyticsManager implements TagManager {

  @Inject @AddressBook Preference<Boolean> addressBook;

  private MixpanelTagManager mixpanel;
  private BranchTagManager branch;

  @Inject public AnalyticsManager(Context context, @Named("userThreadSafe") User user) {
    this.mixpanel = new MixpanelTagManager(context, user);
    this.branch = new BranchTagManager(context);

    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);

    Bundle bundle = new Bundle();

    if (user != null) {
      bundle.putBoolean(TagManagerConstants.user_invisible_enabled, user.isInvisibleMode());
      bundle.putString(TagManagerConstants.user_username, user.getUsername());
      bundle.putString(TagManagerConstants.user_display_name, user.getUsername());
      bundle.putString(TagManagerConstants.user_username, user.getUsername());
      bundle.putBoolean(TagManagerConstants.user_facebook_connected, FacebookUtils.isLoggedIn());
      bundle.putBoolean(TagManagerConstants.user_has_profile_picture,
          !StringUtils.isEmpty(user.getProfilePicture()));
    }

    bundle.putBoolean(TagManagerConstants.user_address_book_enabled,
        addressBook.get() && PermissionUtils.hasPermissionsContact(context));
    bundle.putBoolean(TagManagerConstants.user_camera_enabled,
        PermissionUtils.hasPermissionsCamera(context));
    bundle.putBoolean(TagManagerConstants.user_microphone_enabled,
        PermissionUtils.hasPermissionsCamera(context));
    bundle.putBoolean(TagManagerConstants.user_notifications_enabled,
        true); // ALWAYS TRUE ON ANDROID FOR PERMISSIONS

    setProperty(bundle);
  }

  @Override public void alias(String userId) {

  }

  @Override public void setUserId(String userId) {
    mixpanel.setUserId(userId);
    branch.setUserId(userId);
  }

  @Override public void onStart(Activity activity) {
    mixpanel.onStart(activity);
    branch.onStart(activity);
  }

  @Override public void onStop(Activity activity) {
    mixpanel.onStop(activity);
    branch.onStop(activity);
  }

  @Override public void trackInstall() {
    mixpanel.trackInstall();
    branch.trackInstall();
  }

  @Override public void trackEvent(String event) {
    mixpanel.trackEvent(event);
    branch.trackEvent(event);
  }

  @Override public void trackEvent(String event, Bundle properties) {
    mixpanel.trackEvent(event, properties);
    branch.trackEvent(event, properties);
  }

  @Override public void setProperty(Bundle properties) {
    mixpanel.setProperty(properties);
    branch.setProperty(properties);
  }

  @Override public void setPropertyOnce(Bundle properties) {
    mixpanel.setPropertyOnce(properties);
    branch.setPropertyOnce(properties);
  }

  @Override public void increment(String properties) {
    mixpanel.increment(properties);
    branch.increment(properties);
  }

  @Override public void clear() {
    mixpanel.clear();
    branch.clear();
  }
}
