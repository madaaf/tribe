package com.tribe.app.presentation.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.presentation.view.activity.ChatActivity;
import com.tribe.app.presentation.view.activity.CountryActivity;
import com.tribe.app.presentation.view.activity.HomeActivity;
import com.tribe.app.presentation.view.activity.IntroActivity;
import com.tribe.app.presentation.view.activity.TribeActivity;

import javax.inject.Inject;

/**
 * Class used to navigate through the application.
 */
public class Navigator {

    public static int REQUEST_COUNTRY = 1000;

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
     * Opens the country list.
     *
     * @param activity An activity needed to open the destiny activity.
     */
    public void navigateToCountries(Activity activity) {
        if (activity != null) {
            Intent intent = CountryActivity.getCallingIntent(activity);
            activity.startActivityForResult(intent, REQUEST_COUNTRY);
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

    /**
     * Goes to the text chat screen.
     *
     * @param activity An activity needed to open the destiny activity.
     * @param position position of the friendship in the grid
     * @param friendship a friendship (user / group) to open the tribes
     */
    public void navigateToTribe(Activity activity, int position, Friendship friendship) {
        if (activity != null) {
            Intent intent = TribeActivity.getCallingIntent(activity, position, friendship);
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
        }
    }
}
