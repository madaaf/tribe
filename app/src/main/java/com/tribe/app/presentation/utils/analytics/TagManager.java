package com.tribe.app.presentation.utils.analytics;

import android.app.Activity;
import android.os.Bundle;
import com.tribe.app.domain.entity.User;

/**
 * Created by tiago on 22/09/2016.
 */
public interface TagManager {

  void updateUser(User user);

  void alias(String userId);

  void setUserId(String userId);

  void onStart(Activity activity);

  void onStop(Activity activity);

  void trackInstall();

  void trackEvent(String event);

  void trackEvent(String event, Bundle properties);

  void setProperty(Bundle properties);

  void setPropertyOnce(Bundle properties);

  void increment(String properties);

  void increment(String properties, double value);

  void clear();
}
