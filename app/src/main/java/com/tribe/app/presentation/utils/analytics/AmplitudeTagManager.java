package com.tribe.app.presentation.utils.analytics;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import com.amplitude.api.Amplitude;
import com.amplitude.api.AmplitudeClient;
import com.amplitude.api.Identify;
import com.tribe.app.BuildConfig;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Singleton;

/**
 * Created by tiago on 22/09/2016.
 */
@Singleton
public class AmplitudeTagManager implements TagManager {

    private AmplitudeClient amplitude;

    public AmplitudeTagManager(Context context, User user) {
        if (user != null && StringUtils.isEmpty(user.getId())) {
            amplitude = Amplitude.getInstance().initialize(context, BuildConfig.AMPLITUDE_KEY)
                    .enableForegroundTracking((Application) context.getApplicationContext());
        } else {
            amplitude = Amplitude.getInstance().initialize(context, BuildConfig.AMPLITUDE_KEY, user.getId())
                    .enableForegroundTracking((Application) context.getApplicationContext());
        }

        amplitude.trackSessionEvents(true);
    }

    @Override
    public void setUserId(String userId) {

    }

    @Override
    public void onStart(Activity activity) {

    }

    @Override
    public void onStop(Activity activity) {

    }

    @Override
    public void trackInstall() {

    }

    @Override
    public void trackEvent(String event) {
        amplitude.logEvent(event);
    }

    @Override
    public void trackEvent(String event, Bundle properties) {
        amplitude.logEvent(event, buildProperties(properties));
    }

    @Override
    public void setProperty(Bundle properties) {
        amplitude.setUserProperties(buildProperties(properties));
    }

    @Override
    public void setPropertyOnce(Bundle properties) {
        Identify identify = new Identify();

        for (String key : properties.keySet()) {
            buildProperty(key, properties.get(key));
        }

        amplitude.identify(identify);
    }

    @Override
    public void increment(String properties) {
        Identify identify = new Identify().add(properties, 1);
        amplitude.identify(identify);
    }

    @Override
    public void clear() {
        amplitude.clearUserProperties();
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
