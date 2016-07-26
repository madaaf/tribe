package com.tribe.app.presentation.view.utils;

import android.content.Context;
import android.support.annotation.StringDef;

import com.tribe.app.R;

/**
 * Created by tiago on 10/07/2016.
 */
public class MessageStatus {

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_SENT = "sent";
    public static final String STATUS_OPENED = "opened";
    public static final String STATUS_DELIVERED = "delivered";
    public static final String STATUS_RECEIVED = "received";
    public static final String STATUS_LOADING = "loading";
    public static final String STATUS_READY = "ready";
    public static final String STATUS_ERROR = "error";
    public static final String STATUS_NONE = "none";

    @StringDef({STATUS_PENDING, STATUS_SENT, STATUS_OPENED, STATUS_DELIVERED, STATUS_RECEIVED, STATUS_LOADING, STATUS_READY, STATUS_ERROR, STATUS_NONE})
    public @interface Status{}

    public static String getStrRes(Context ctx, @MessageStatus.Status String status) {
        String res = "";

        if (status.equals(STATUS_PENDING)) {
            res = ctx.getString(R.string.grid_friendship_status_sending);
        } else if (status.equals(STATUS_SENT)) {
            res = ctx.getString(R.string.grid_friendship_status_sent);
        } else if (status.equals(STATUS_OPENED)) {
            res = ctx.getString(R.string.grid_friendship_status_opened);
        } else if (status.equals(STATUS_DELIVERED)) {
            res = ctx.getString(R.string.grid_friendship_status_sent);
        } else if (status.equals(STATUS_RECEIVED)) {
            res = ctx.getString(R.string.grid_friendship_status_new_messages);
        } else if (status.equals(STATUS_LOADING)) {
            res = ctx.getString(R.string.grid_friendship_status_loading);
        } else if (status.equals(STATUS_READY)) {
            res = ctx.getString(R.string.grid_friendship_status_new_messages);
        } else {
            res = ctx.getString(R.string.grid_friendship_status_default);
        }

        return res;
    }

    public static int getIconRes(@MessageStatus.Status String status) {
        int res = -1;

        if (status.equals(STATUS_PENDING)) {
            res = R.drawable.picto_sending;
        } else if (status.equals(STATUS_SENT)) {
            res = R.drawable.picto_sent;
        } else if (status.equals(STATUS_OPENED)) {
            res = R.drawable.picto_opened;
        } else if (status.equals(STATUS_DELIVERED)) {
            res = R.drawable.picto_delivered;
        } else if (status.equals(STATUS_LOADING)) {
            res = R.drawable.picto_loading;
        } else if (status.equals(STATUS_READY) || status.equals(STATUS_RECEIVED)) {
            res = R.drawable.picto_tap_to_view;
        } else if (status.equals(STATUS_ERROR)) {
            res = R.drawable.picto_error_tribe;
        } else {
            res = R.drawable.picto_tap_to_view;
        }

        return res;
    }
}
