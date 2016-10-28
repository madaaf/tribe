package com.tribe.app.presentation.view.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.ContextThemeWrapper;

public final class DialogFactory {

    public static Dialog createConfirmationDialog(Context context, String title,
                                                  String message, String positiveMessage, String negativeMessage,
                                                  DialogInterface.OnClickListener positiveListener,
                                                  DialogInterface.OnClickListener negativeListener) {
        ContextThemeWrapper themedContext;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            themedContext = new ContextThemeWrapper(context, android.R.style.Theme_Material_Light_Dialog_NoActionBar);
        } else {
            themedContext = new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
        }

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(themedContext)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveMessage, positiveListener)
                .setNegativeButton(negativeMessage, negativeListener);
        return alertDialog.create();
    }

    public static ProgressDialog createProgressDialog(Context context, int title) {
        int themedContext;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            themedContext = android.R.style.Theme_Material_Light_Dialog_NoActionBar;
        } else {
            themedContext = android.R.style.Theme_Holo_Light_Dialog_NoActionBar;
        }

        ProgressDialog pd = new ProgressDialog(context, themedContext);
        pd.setTitle(title);
        return pd;
    }
}