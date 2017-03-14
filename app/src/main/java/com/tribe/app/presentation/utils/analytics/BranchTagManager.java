package com.tribe.app.presentation.utils.analytics;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import com.tribe.app.domain.entity.User;
import io.branch.referral.Branch;
import javax.inject.Singleton;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by tiago on 22/09/2016.
 */
@Singleton public class BranchTagManager implements TagManager {

  private Branch branch;

  public BranchTagManager(Context context) {
    branch = branch.getInstance(context);
  }

  @Override public void updateUser(User user) {

  }

  @Override public void alias(String userId) {

  }

  @Override public void setUserId(String userId) {
    branch.setIdentity(userId);
  }

  @Override public void onStart(Activity activity) {

  }

  @Override public void onStop(Activity activity) {

  }

  @Override public void trackInstall() {

  }

  @Override public void trackEvent(String event) {
    branch.userCompletedAction(event);
  }

  @Override public void trackEvent(String event, Bundle properties) {
    branch.userCompletedAction(event, buildProperties(properties));
  }

  @Override public void setProperty(Bundle properties) {
  }

  @Override public void setPropertyOnce(Bundle properties) {

  }

  @Override public void increment(String properties) {

  }

  @Override public void increment(String properties, double value) {

  }

  @Override public void clear() {
    branch.logout();
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
