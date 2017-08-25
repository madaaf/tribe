package com.mixpanel.android.mpmetrics;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.tribe.app.domain.entity.Live;
import com.tribe.app.presentation.view.activity.LiveActivity;

public class TribeGCMReceiver extends GCMReceiver {

    public static final String EXTRA_ACTION = "action";

    public static final String ACTION_CALLROULETTE = "callroulette";

    @Override
    NotificationData readInboundIntent(Context context, Intent inboundIntent, ResourceIds iconIds) {

        NotificationData data = super.readInboundIntent(context, inboundIntent, iconIds);
        if (data != null) {

            data.intent.putExtra(EXTRA_ACTION,
                    inboundIntent.getStringExtra(EXTRA_ACTION));
        }

        return data;
    }
}
