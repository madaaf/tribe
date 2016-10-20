package com.tribe.app.presentation.utils.analytics;

import android.app.Activity;
import android.os.Bundle;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by tiago on 19/10/2016.
 */
@Singleton
public class AnalyticsManager implements TagManager {

    private AmplitudeTagManager amplitude;
    private AppboyTagManager appboy;

    @Inject
    public AnalyticsManager(AmplitudeTagManager amplitudeTagManager, AppboyTagManager appboyTagManager) {
        this.amplitude = amplitudeTagManager;
        this.appboy = appboyTagManager;
    }

    @Override
    public void setUserId(String userId) {
        appboy.setUserId(userId);
        amplitude.setUserId(userId);
    }

    @Override
    public void onStart(Activity activity) {
        appboy.onStart(activity);
    }

    @Override
    public void onStop(Activity activity) {
        appboy.onStop(activity);
    }

    @Override
    public void trackInstall() {
        amplitude.trackInstall();
        appboy.trackInstall();
    }

    @Override
    public void trackEvent(String event) {
        amplitude.trackEvent(event);
        appboy.trackEvent(event);
    }

    @Override
    public void trackEvent(String event, Bundle properties) {
        amplitude.trackEvent(event, properties);
        appboy.trackEvent(event);
    }

    @Override
    public void setProperty(Bundle properties) {
        amplitude.setProperty(properties);
        appboy.setProperty(properties);
    }

    @Override
    public void setPropertyOnce(Bundle properties) {
        amplitude.setPropertyOnce(properties);
        appboy.setPropertyOnce(properties);
    }

    @Override
    public void increment(String properties) {
        amplitude.increment(properties);
        appboy.increment(properties);
    }

    @Override
    public void clear() {
        amplitude.clear();
        appboy.clear();
    }
}
