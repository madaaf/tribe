package com.tribe.app.presentation.utils;

import android.support.annotation.StringDef;

/**
 * Created by tiago on 10/07/2016.
 */
public class MessageStatus {

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_SENT = "sent";
    public static final String STATUS_OPENED = "opened";
    public static final String STATUS_DELIVERED = "delivered";
    public static final String STATUS_SENDING = "sending";
    public static final String STATUS_LOADING = "loading";

    @StringDef({STATUS_PENDING})
    public @interface Status{}
}
