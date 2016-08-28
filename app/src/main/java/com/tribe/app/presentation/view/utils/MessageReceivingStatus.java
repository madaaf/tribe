package com.tribe.app.presentation.view.utils;

import android.support.annotation.StringDef;

/**
 * Created by tiago on 10/07/2016.
 */
public class MessageReceivingStatus {

    public static final String STATUS_RECEIVED = "received";
    public static final String STATUS_NOT_SEEN = "not_seen";
    public static final String STATUS_SEEN = "seen";

    @StringDef({STATUS_NOT_SEEN, STATUS_SEEN, STATUS_RECEIVED})
    public @interface Status{}
}
