package com.tribe.app.presentation.utils.analytics;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.tribe.app.BuildConfig;
import com.tribe.app.domain.entity.User;
import javax.inject.Singleton;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tiago on 22/09/2016.
 */
@Singleton public class MixpanelTagManager implements TagManager {

  private MixpanelAPI mixpanelAPI;

  public MixpanelTagManager(Context context, User user) {
    mixpanelAPI = MixpanelAPI.getInstance(context, BuildConfig.MIXPANEL_TOKEN);
  }

  @Override public void updateUser(User user) {

  }

  @Override public void alias(String userId) {
    mixpanelAPI.alias(userId, mixpanelAPI.getDistinctId());
  }

  @Override public void setUserId(String userId) {
    mixpanelAPI.identify(userId);
    mixpanelAPI.getPeople().identify(userId);
    mixpanelAPI.getPeople().initPushHandling(BuildConfig.MIXPANEL_GOOGLE_API);
  }

  @Override public void onStart(Activity activity) {

  }

  @Override public void onStop(Activity activity) {

  }

  @Override public void trackInstall() {

  }

  @Override public void trackEvent(String event) {
    mixpanelAPI.track(event);
  }

  @Override public void trackEvent(String event, Bundle properties) {
    mixpanelAPI.track(event, buildProperties(properties));
  }

  @Override public void setProperty(Bundle properties) {
    mixpanelAPI.getPeople().set(buildProperties(properties));
  }

  @Override public void setPropertyOnce(Bundle properties) {
    mixpanelAPI.getPeople().setOnce(buildProperties(properties));
  }

  @Override public void increment(String properties) {
    mixpanelAPI.getPeople().increment(properties, 1);
  }

  @Override public void increment(String properties, double value) {
    mixpanelAPI.getPeople().increment(properties, value);
  }

  @Override public void clear() {
    //mixpanelAPI.reset();
  }

  private JSONObject buildProperties(Bundle properties) {
    JSONObject eventProperties = new JSONObject();
    try {
      for (String key : properties.keySet()) {
        eventProperties.put(key, properties.get(key));
      }
    } catch (JSONException exception) {
      exception.printStackTrace();
    }

    return eventProperties;
  }

  private JSONObject buildProperty(String key, Object value) {
    JSONObject property = new JSONObject();
    try {
      property.put(key, value);
    } catch (JSONException exception) {
      exception.printStackTrace();
    }

    return property;
  }
}
