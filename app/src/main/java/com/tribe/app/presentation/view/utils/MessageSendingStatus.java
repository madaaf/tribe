package com.tribe.app.presentation.view.utils;

import android.content.Context;
import android.support.annotation.StringDef;

import com.tribe.app.R;

/**
 * Created by tiago on 10/07/2016.
 */
public class MessageSendingStatus {

    public static final String STATUS_NONE = "none";
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_SENDING = "sending";
    public static final String STATUS_SENT = "sent";
    public static final String STATUS_OPENED_PARTLY = "opened_partly";
    public static final String STATUS_OPENED = "opened";
    public static final String STATUS_ERROR = "error";

    @StringDef({STATUS_NONE, STATUS_PENDING, STATUS_SENDING, STATUS_SENT, STATUS_OPENED_PARTLY, STATUS_OPENED, STATUS_ERROR})
    public @interface Status{}

    public static String getStrRes(Context ctx, @MessageSendingStatus.Status String status) {
        String res = "";

        if (status.equals(STATUS_PENDING)) {
            res = ctx.getString(R.string.grid_friendship_status_sending);
        } else if (status.equals(STATUS_SENT)) {
            res = ctx.getString(R.string.grid_friendship_status_sent);
        } else if (status.equals(STATUS_OPENED)) {
            res = ctx.getString(R.string.grid_friendship_status_opened);
        } else if (status.equals(STATUS_OPENED_PARTLY)) {
            res = ctx.getString(R.string.grid_friendship_status_opened);
        } else if (status.equals(STATUS_SENT)) {
            res = ctx.getString(R.string.grid_friendship_status_sent);
        } else {
            res = ctx.getString(R.string.grid_friendship_status_default);
        }

        return res;
    }

    public static int getIconRes(@MessageSendingStatus.Status String status) {
        int res = -1;

        if (status.equals(STATUS_PENDING) || status.equals(STATUS_SENDING)) {
            res = R.drawable.picto_sending;
        } else if (status.equals(STATUS_SENT)) {
            res = R.drawable.picto_sent;
        } else if (status.equals(STATUS_OPENED) || status.equals(STATUS_OPENED_PARTLY)) {
            res = R.drawable.picto_opened;
        } else if (status.equals(STATUS_ERROR)) {
            res = R.drawable.picto_error_tribe;
        } else {
            res = R.drawable.picto_tap_to_view;
        }

        return res;
    }
}
