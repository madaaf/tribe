package com.tribe.app.presentation.view.utils;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Tiago Duarte
 */
public class Distance {

    @StringDef({MILES, METERS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DistanceUnits {
    }

    public static final String MILES = "miles";
    public static final String METERS = "meters";

    private Distance() {
    }
}
