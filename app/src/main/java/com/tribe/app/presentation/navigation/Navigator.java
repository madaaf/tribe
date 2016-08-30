package com.tribe.app.presentation.navigation;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.widget.Toast;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.view.activity.ChatActivity;
import com.tribe.app.presentation.view.activity.CountryActivity;
import com.tribe.app.presentation.view.activity.HomeActivity;
import com.tribe.app.presentation.view.activity.IntroActivity;
import com.tribe.app.presentation.view.activity.PointsActivity;
import com.tribe.app.presentation.view.activity.ScoreActivity;
import com.tribe.app.presentation.view.activity.SettingActivity;
import com.tribe.app.presentation.view.activity.TribeActivity;

import java.util.List;

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
     * @param activity An activity needed to open the destiny activity.
     */
    public void navigateToChat(Activity activity, Recipient recipient) {
        if (activity != null) {
            Intent intent = ChatActivity.getCallingIntent(activity, recipient);
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.activity_in_from_right, R.anim.activity_out_scale_down);
        }
    }

    /**
     * Goes to the text chat screen.
     *
     * @param activity An activity needed to open the destiny activity.
     * @param position position of the friendship in the grid
     * @param recipient a recipient (friendship with user / group) to open the tribes
     */
    public void navigateToTribe(Activity activity, int position, Recipient recipient) {
        if (activity != null) {
            Intent intent = TribeActivity.getCallingIntent(activity, position, recipient);
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
        }
    }

    /**
     * Goes to the score screen.
     *
     * @param activity An activity needed to open the destiny activity.
     */
    public void navigateToScorePoints(Activity activity) {
        if (activity != null) {
            Intent intent = ScoreActivity.getCallingIntent(activity);
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
        }
    }

    /**
     * Goes to the points screen.
     *
     * @param activity An activity needed to open the destiny activity.
     */
    public void navigateToPoints(Activity activity) {
        if (activity != null) {
            Intent intent = PointsActivity.getCallingIntent(activity);
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.activity_in_from_right, R.anim.activity_out_scale_down);
        }
    }

    /**
     * Goes to the settings screen.
     *
     * @param context context needed to open the destiny activity.
     */
    public void navigateToSettings(Context context) {
        if (context != null) {
            Intent intent = SettingActivity.getCallingIntent(context);
            context.startActivity(intent);
        }
    }

    /**
     * Goes to the app page in the playstore so the user may rate the app.
     *
     * @param context context needed to open the destiny activity.
     */
    public void rateApp(Context context) {

        String appPackage = "com.tribe.app";
        // http://stackoverflow.com/questions/10816757/rate-this-app-link-in-google-play-store-app-on-the-phone
        Uri uri = Uri.parse("market://details?id=" + appPackage);
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + appPackage)));
        }

    }

    /**
     * Composes a tweet to post and opens Twitter app.
     * @param context context needed to open the intent.
     * @param tweet the pre-filled tweet.
     */

    public void tweet(Context context, String tweet) {
        // http://stackoverflow.com/questions/21088250/android-launch-twitter-intent
        Intent tweetIntent = new Intent(Intent.ACTION_SEND);
        tweetIntent.putExtra(Intent.EXTRA_TEXT, tweet);
        tweetIntent.setType("text/plain");

        PackageManager packManager =  context.getPackageManager();
        List<ResolveInfo> resolvedInfoList = packManager.queryIntentActivities(tweetIntent,  PackageManager.MATCH_DEFAULT_ONLY);

        boolean resolved = false;
        for(ResolveInfo resolveInfo: resolvedInfoList){
            if(resolveInfo.activityInfo.packageName.startsWith("com.twitter.android")){
                tweetIntent.setClassName(
                        resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name );
                resolved = true;
                break;
            }
        }
        if(resolved){
           context.startActivity(tweetIntent);
        }else{
            Toast.makeText(context, "Twitter app not found", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Intent to send an email
     * @param context context to start activity
     * @param addresses addresses to send email to
     * @param subject the subject of the email
     */

    public void composeEmail(Context context, String[] addresses, String subject) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }


}
