package com.tribe.app.presentation.view.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.ContextThemeWrapper;

import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;

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

    public static Observable<Boolean> dialog(Context context, String title,
                                             String message, String positiveMessage,
                                             String negativeMessage) {
        return Observable.create((Subscriber<? super Boolean> subscriber) -> {

            ContextThemeWrapper themedContext;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                themedContext = new ContextThemeWrapper(context, android.R.style.Theme_Material_Light_Dialog_NoActionBar);
            } else {
                themedContext = new ContextThemeWrapper(context, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
            }

            final AlertDialog ad = new AlertDialog.Builder(themedContext)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(positiveMessage, (dialog, which) -> {
                        subscriber.onNext(true);
                        subscriber.onCompleted();
                    })
                    .setNegativeButton(negativeMessage, (dialog, which) -> {
                        subscriber.onNext(false);
                        subscriber.onCompleted();
                    })
                    .create();

            subscriber.add(Subscriptions.create(ad::dismiss));
            ad.show();
        });
    }
}