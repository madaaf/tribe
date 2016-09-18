package com.tribe.app.presentation.view.utils;

import android.content.Context;
import android.os.Build;
import android.widget.TextView;

/**
 * Created by tiago on 17/09/2016.
 */
public class TextViewUtils {

    public static void setTextAppearence(Context context, TextView textView, int resId) {
        if (Build.VERSION.SDK_INT < 23) {
            textView.setTextAppearance(context, resId);
        } else {
            textView.setTextAppearance(resId);
        }
    }
}
