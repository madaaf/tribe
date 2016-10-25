package com.tribe.app.presentation.view.utils;

import android.support.annotation.StringDef;

import com.tribe.app.R;
import com.tribe.app.presentation.view.component.TileView;

/**
 * Created by tiago on 10/07/2016.
 */
public class MessageSendingStatus {

    public static final String STATUS_NONE = "none";
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_SENDING = "sending";
    public static final String STATUS_CONFIRMED = "confirmed";
    public static final String STATUS_SENT = "sent";
    public static final String STATUS_OPENED_PARTLY = "opened_partly";
    public static final String STATUS_OPENED = "opened";
    public static final String STATUS_ERROR = "error";

    @StringDef({STATUS_NONE, STATUS_PENDING, STATUS_CONFIRMED, STATUS_SENDING, STATUS_SENT, STATUS_OPENED_PARTLY, STATUS_OPENED, STATUS_ERROR})
    public @interface Status{}

    public static int getStrRes(@MessageSendingStatus.Status String status, int type) {
        int res = 0;

        if (status.equals(STATUS_PENDING) || status.equals(STATUS_SENDING)) {
            res = type == TileView.TYPE_SUPPORT ? R.string.grid_support_status_sending
                    : R.string.grid_friendship_status_sending;
        } else if (status.equals(STATUS_CONFIRMED)) {
            res = type == TileView.TYPE_SUPPORT ? R.string.grid_support_status_confirmed
                    : R.string.grid_friendship_status_confirmed;
        } else if (status.equals(STATUS_SENT)) {
            res = type == TileView.TYPE_SUPPORT ? R.string.grid_support_status_sent
                    : R.string.grid_friendship_status_sent;
        } else if (status.equals(STATUS_OPENED)) {
            res = type == TileView.TYPE_SUPPORT ? R.string.grid_support_status_opened
                    : R.string.grid_friendship_status_opened;
        } else if (status.equals(STATUS_OPENED_PARTLY)) {
            res = type == TileView.TYPE_SUPPORT ? R.string.grid_support_status_opened
                    : R.string.grid_friendship_status_opened;
        } else {
            res = type == TileView.TYPE_SUPPORT ? R.string.grid_support_status_default
                    : R.string.grid_friendship_status_default;
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
