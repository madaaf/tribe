package com.tribe.app.presentation.utils.analytics;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import com.f2prateek.rx.preferences.Preference;
import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.preferences.AddressBook;
import com.tribe.app.presentation.view.utils.DeviceUtils;
import io.branch.referral.Branch;
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
  private AppEventsLogger fbLogger;
  private Context context;

  @Inject public AnalyticsManager(Context context, @Named("userThreadSafe") User user) {
    this.context = context;
    this.mixpanel = new MixpanelTagManager(context, user);
    this.branch = new BranchTagManager(context);
    this.fbLogger = AppEventsLogger.newLogger(context);

    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);

    updateUser(user);
  }

  @Override public void updateUser(User user) {
    Bundle bundle = new Bundle();

    if (user != null) {
      bundle.putBoolean(TagManagerUtils.USER_INVISIBLE_ENABLED, user.isInvisibleMode());
      bundle.putString(TagManagerUtils.USER_USERNAME, user.getUsername());
      bundle.putString(TagManagerUtils.USER_DISPLAY_NAME, user.getDisplayName());
      bundle.putBoolean(TagManagerUtils.USER_FACEBOOK_CONNECTED, FacebookUtils.isLoggedIn());
      bundle.putBoolean(TagManagerUtils.USER_HAS_PROFILE_PICTURE,
          !StringUtils.isEmpty(user.getProfilePicture()));

      Bundle bundleMixpanel = new Bundle();
      bundleMixpanel.putString("$name", user.getDisplayName());
      if (!StringUtils.isEmpty(user.getPhone())) {
        bundleMixpanel.putString("$phone", user.getPhone());
      }

      mixpanel.setProperty(bundleMixpanel);
    }

    bundle.putString(TagManagerUtils.USER_LANGUAGE, DeviceUtils.getLanguage(context).toUpperCase());

    bundle.putBoolean(TagManagerUtils.USER_NOTIFICATIONS_ENABLED,
        true); // ALWAYS TRUE ON ANDROID FOR PERMISSIONS

    setProperty(bundle);
  }

  @Override public void alias(String userId) {
    mixpanel.alias(userId);
    branch.alias(userId);
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
    fbEvent(event, new Bundle());
  }

  @Override public void trackEvent(String event, Bundle properties) {
    mixpanel.trackEvent(event, properties);
    branch.trackEvent(event, properties);
    fbEvent(event, properties);
  }

  public void fbEvent(String event, Bundle properties) {
    Bundle bundle = new Bundle();

    if (event.equals(TagManagerUtils.Calls)) {
      if (properties.get(TagManagerUtils.STATE) != null) {
        String state = properties.getString(TagManagerUtils.STATE);
        bundle.putString(AppEventsConstants.EVENT_PARAM_DESCRIPTION, "Call_" + state + "");
        fbLogger.logEvent(AppEventsConstants.EVENT_NAME_UNLOCKED_ACHIEVEMENT, bundle);
      } else {
        bundle.putString(AppEventsConstants.EVENT_PARAM_DESCRIPTION, "Call");
        fbLogger.logEvent(AppEventsConstants.EVENT_NAME_UNLOCKED_ACHIEVEMENT, bundle);
      }
    } else if (event.equals(TagManagerUtils.KPI_Onboarding_AuthenticationSuccess)) {
      String platform = properties.getString(TagManagerUtils.PLATFORM);
      String type = properties.getString(TagManagerUtils.TYPE);

      if (!StringUtils.isEmpty(type) && type.equals(TagManagerUtils.SIGNUP)) {
        bundle.putString(AppEventsConstants.EVENT_PARAM_REGISTRATION_METHOD, platform);
        fbLogger.logEvent(AppEventsConstants.EVENT_NAME_COMPLETED_REGISTRATION, bundle);
      }
    } else if (event.equals(TagManagerUtils.KPI_Onboarding_WalkthroughCompleted)) {
      bundle.putBoolean(AppEventsConstants.EVENT_PARAM_SUCCESS, true);
      fbLogger.logEvent(AppEventsConstants.EVENT_NAME_COMPLETED_TUTORIAL, bundle);
    } else if (event.equals(TagManagerUtils.Searched)) {
      String contentType = properties.getString(TagManagerUtils.CONTENT_TYPE);
      String searchString = properties.getString(TagManagerUtils.SEARCH_STRING);
      bundle.putString(AppEventsConstants.EVENT_PARAM_CONTENT_TYPE, contentType);
      bundle.putString(AppEventsConstants.EVENT_PARAM_SEARCH_STRING, searchString);
      bundle.putBoolean(AppEventsConstants.EVENT_PARAM_SUCCESS, true);
      fbLogger.logEvent(AppEventsConstants.EVENT_NAME_SEARCHED, bundle);
    }
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

  @Override public void increment(String properties, double value) {
    mixpanel.increment(properties, value);
    branch.increment(properties, value);
  }

  @Override
  public void generateBranchLink(Context context, String link, String title, String description,
      String feature, String channel, Branch.BranchLinkCreateListener listener) {
    branch.generateBranchLink(context, link, title, description, feature, channel, listener);
  }

  @Override public void clear() {
    mixpanel.clear();
    branch.clear();
  }
}
