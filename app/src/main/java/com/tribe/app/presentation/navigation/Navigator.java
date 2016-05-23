package com.tribe.app.presentation.navigation;

import android.content.Context;
import android.content.Intent;

import com.tribe.app.presentation.view.activity.TextActivity;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Class used to navigate through the application.
 */
public class Navigator {

    @Inject
    public Navigator() {

    }

    /**
     * Goes to the main grid.
     *
     * @param context A Context needed to open the destiny activity.
     */
    public void navigateToHome(Context context) {
        if (context != null) {

        }
    }

    /**
     * Goes to the text chat screen.
     *
     * @param context A Context needed to open the destiny activity.
     */
    public void navigateToChat(Context context, int id) {
        if (context != null) {
            Intent intent = TextActivity.getCallingIntent(context, id);
            context.startActivity(intent);
        }
    }
}
