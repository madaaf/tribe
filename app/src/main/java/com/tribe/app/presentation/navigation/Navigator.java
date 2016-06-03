package com.tribe.app.presentation.navigation;

import android.content.Context;
import android.content.Intent;

import com.tribe.app.presentation.view.activity.ChatActivity;
import com.tribe.app.presentation.view.activity.HomeActivity;
import com.tribe.app.presentation.view.activity.IntroActivity;

import javax.inject.Inject;

/**
 * Class used to navigate through the application.
 */
public class Navigator {

    @Inject
    public Navigator() {

    }

    /**
     * Goes to the login.
     *
     * @param context A Context needed to open the destiny activity.
     */
    public void navigateToLogin(Context context) {
        if (context != null) {
            Intent intent = IntroActivity.getCallingIntent(context);
            context.startActivity(intent);
        }
    }

    /**
     * Goes to the main grid.
     *
     * @param context A Context needed to open the destiny activity.
     */
    public void navigateToHome(Context context) {
        if (context != null) {
            Intent intent = HomeActivity.getCallingIntent(context);
            context.startActivity(intent);
        }
    }

    /**
     * Goes to the text chat screen.
     *
     * @param context A Context needed to open the destiny activity.
     */
    public void navigateToChat(Context context, String id) {
        if (context != null) {
            Intent intent = ChatActivity.getCallingIntent(context, id);
            context.startActivity(intent);
        }
    }
}
