package com.tribe.app.presentation.utils.analytics;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.appboy.Appboy;
import com.appboy.models.outgoing.AppboyProperties;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by tiago on 22/09/2016.
 */
@Singleton
public class AppboyTagManager implements TagManager {

    private Appboy appboy;

    @Inject
    public AppboyTagManager(Context context) {
        appboy = Appboy.getInstance(context);
    }

    @Override
    public void setUserId(String userId) {
        appboy.changeUser(userId);
    }

    @Override
    public void onStart(Activity activity) {
        appboy.openSession(activity);
    }

    @Override
    public void onStop(Activity activity) {
        appboy.closeSession(activity);
    }

    @Override
    public void trackInstall() {

    }

    @Override
    public void trackEvent(String event) {
        appboy.logCustomEvent(event);
    }

    @Override
    public void trackEvent(String event, Bundle properties) {
        appboy.logCustomEvent(event, buildProperties(properties));
    }

    @Override
    public void setProperty(Bundle properties) {
        for (String key : properties.keySet()) {
            Object o = properties.get(key);
            if (o instanceof String)
                appboy.getCurrentUser().setCustomUserAttribute(key, o.toString());
            else if (o instanceof Integer)
                appboy.getCurrentUser().setCustomUserAttribute(key, (Integer) o);
            else if (o instanceof Float)
                appboy.getCurrentUser().setCustomUserAttribute(key, (Float) o);
            else if (o instanceof Long)
                appboy.getCurrentUser().setCustomUserAttribute(key, (Long) o);
            else if (o instanceof Boolean)
                appboy.getCurrentUser().setCustomUserAttribute(key, (Boolean) o);
        }
    }

    @Override
    public void setPropertyOnce(Bundle properties) {
        setProperty(properties);
    }

    @Override
    public void increment(String properties) {
        appboy.getCurrentUser().incrementCustomUserAttribute(properties);
    }

    @Override
    public void clear() {
    }

    private AppboyProperties buildProperties(Bundle properties) {
        AppboyProperties eventProperties = new AppboyProperties();
        try {
            for (String key : properties.keySet()) {
                Object o = properties.get(key);
                if (o instanceof String)
                    eventProperties.addProperty(key, o.toString());
                else if (o instanceof Integer)
                    eventProperties.addProperty(key, (Integer) o);
                else if (o instanceof Double)
                    eventProperties.addProperty(key, (Double) o);
                else if (o instanceof Date)
                    eventProperties.addProperty(key, (Date) o);
                else if (o instanceof Boolean)
                    eventProperties.addProperty(key, (Boolean) o);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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
