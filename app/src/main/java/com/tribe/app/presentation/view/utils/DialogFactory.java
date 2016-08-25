package com.tribe.app.presentation.view.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

public final class DialogFactory {

    public static Dialog createConfirmationDialog(Context context, String title,
                                                  String message, String positiveMessage, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveMessage, listener);
        return alertDialog.create();
    }
}