package com.tribe.app.presentation.utils.analytics;

import android.os.Bundle;

/**
 * Created by tiago on 22/09/2016.
 */
public interface TagManager {

    void trackInstall();
    void trackEvent(String event);
    void trackEvent(String event, Bundle properties);
    void setProperty(Bundle properties);
    void setPropertyOnce(Bundle properties);
    void increment(String properties);
    void clear();
}
