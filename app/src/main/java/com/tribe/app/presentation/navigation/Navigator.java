package com.tribe.app.presentation.navigation;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.tribe.app.BuildConfig;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.utils.Extras;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.activity.ChatActivity;
import com.tribe.app.presentation.view.activity.CountryActivity;
import com.tribe.app.presentation.view.activity.GroupInfoActivity;
import com.tribe.app.presentation.view.activity.HomeActivity;
import com.tribe.app.presentation.view.activity.IntroActivity;
import com.tribe.app.presentation.view.activity.LauncherActivity;
import com.tribe.app.presentation.view.activity.PointsActivity;
import com.tribe.app.presentation.view.activity.ScoreActivity;
import com.tribe.app.presentation.view.activity.SettingActivity;
import com.tribe.app.presentation.view.activity.TribeActivity;

import java.io.File;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

/**
 * Class used to navigate through the application.
 */
public class Navigator {

    public static int REQUEST_COUNTRY = 1000;
    public static String SNAPCHAT = "com.snapchat.android";
    public static String INSTAGRAM = "com.instagram.android";
    public static String TWITTER = "com.twitter.android";

    @Inject
    public Navigator() {

    }

    public void navigateToLauncher(Context context) {
        if (context != null) {
            Intent intent = LauncherActivity.getCallingIntent(context);
            context.startActivity(intent);
        }
    }

    /**
     * Goes to the login.
     *
     * @param context A Context needed to open the destiny activity.
     */
    public void navigateToLogin(Context context, Uri deepLink) {
        if (context != null) {
            Intent intent = IntroActivity.getCallingIntent(context);
            intent.setData(deepLink);
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
     * @param activity An activity needed to open the destiny activity.
     */
    public void navigateToHome(Activity activity, boolean start, Uri uriDeepLink) {
        if (activity != null) {
            Intent intent = HomeActivity.getCallingIntent(activity);
            if (start) {
                if (uriDeepLink != null) {
                    intent.setData(uriDeepLink);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                }

                activity.startActivity(intent);
            } else {
                if (uriDeepLink != null) {
                    intent.setData(uriDeepLink);
                    intent.putExtra(Extras.IS_FROM_LOGIN, true);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activity.startActivity(intent);
//                activity.finish();
            }
        }
    }


    /**
     * Goes to the text chat screen.
     *
     * @param activity An activity needed to open the destiny activity.
     */
    public void navigateToChat(Activity activity, String recipientId, boolean isToGroup) {
        if (activity != null) {
            Intent intent = ChatActivity.getCallingIntent(activity, recipientId, isToGroup);
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
    public void navigateToTribe(Activity activity, int position, Recipient recipient, int result) {
        if (activity != null) {
            Intent intent = TribeActivity.getCallingIntent(activity, position, recipient);
            activity.startActivityForResult(intent, result);
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
     * @param activity activity needed to open the destiny activity.
     */
    public void navigateToSettings(Activity activity, int result) {
        if (activity != null) {
            Intent intent = SettingActivity.getCallingIntent(activity);
            activity.overridePendingTransition(R.anim.activity_in_from_right, R.anim.activity_out_scale_down);
            activity.startActivityForResult(intent, result);
        }
    }

    public void navigateToGroupInfo(Activity activity, String membershipId, boolean isCurrentUserAdmin, String groupId, String groupName, String groupPicture, String privateGroupLink, Date privateGroupLinkExpiresAt) {
        if (activity != null) {
            Intent intent = GroupInfoActivity.getCallingIntent(activity);
            Bundle bundle = new Bundle();
            bundle.putString("membershipId", membershipId);
            bundle.putBoolean("isCurrentUserAdmin", isCurrentUserAdmin);
            bundle.putString("groupId", groupId);
            bundle.putString("groupName", groupName);
            bundle.putString("groupPicture", groupPicture);
            bundle.putString("privateGroupLink", privateGroupLink);
            bundle.putLong("privateGroupLinkExpiresAt", privateGroupLinkExpiresAt.getTime());
            intent.putExtras(bundle);
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.activity_in_from_right, R.anim.activity_out_scale_down);
        }
    }

    /**
     * Logout -> new login
     * @param activity
     */
    public void navigateToLogout(Activity activity) {
        if (activity != null) {
            Intent i = new Intent(activity, IntroActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(i);
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

    public void shareGenericText(String body, Context context) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
        context.startActivity(Intent.createChooser(sharingIntent, "Share via..."));
    }

    public void sendText(String body, Context context) {
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.setData(Uri.parse("sms:"));
        sendIntent.putExtra("sms_body", body);
        context.startActivity(sendIntent);
    }

    public void shareHandle(String handle, Activity activity) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, activity.getString(R.string.share_add_friends_handle, "@" + handle, BuildConfig.TRIBE_URL + "/@" + handle));
        activity.startActivity(Intent.createChooser(sharingIntent, activity.getResources().getString(R.string.contacts_share_profile_button)));
    }

    public void invite(String phone, int nbFriends, Activity activity) {
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.setData(Uri.parse("sms:" + phone));
        sendIntent.putExtra("sms_body", activity.getResources().getString(R.string.share_add_friends_addressbook_suggestions, nbFriends, BuildConfig.TRIBE_URL));
        activity.startActivity(sendIntent);
    }

    public void openFacebookMessenger(String body, Context context) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, body);
        sendIntent.setType("text/plain");
        sendIntent.setPackage("com.facebook.orca");
        try {
            context.startActivity(sendIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            // TODO externalize this string
            Toast.makeText(context, "Facebook Messenger is not installed.", Toast.LENGTH_LONG).show();
        }
    }

    public void openWhatsApp(String body, Context context) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, body);
        sendIntent.setType("text/plain");
        sendIntent.setPackage("com.whatsapp");
        try {
            context.startActivity(sendIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            // TODO externalize this string
            Toast.makeText(context, "Whatsapp is not installed.", Toast.LENGTH_LONG).show();
        }
    }

    public void openSnapchat(String body, Activity activity) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, body);
        intent.setPackage(SNAPCHAT);
        activity.startActivity(Intent.createChooser(intent, "Open Snapchat"));
    }

    public void openSlack(String body, Activity activity) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, body);
        intent.setPackage("com.Slack");
        activity.startActivity(Intent.createChooser(intent, "Open Slack"));
    }

    public void openTelegram(String body, Activity activity) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, body);
        sendIntent.setType("text/plain");
        sendIntent.setPackage("org.telegram.messenger");
        activity.startActivity(sendIntent);
    }

    public void shareHandle(Context context, String handle, File file, String selectedPackage) {
        if (file != null) {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/jpeg");
            share.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_add_friends_handle, "@" + handle, BuildConfig.TRIBE_URL + "/@" + handle));

            Uri uri = Uri.fromFile(file);
            share.putExtra(Intent.EXTRA_STREAM, uri);

            if (StringUtils.isEmpty(selectedPackage)) {
                context.startActivity(Intent.createChooser(share, context.getString(R.string.contacts_share_profile_button)));
            } else {
                share.setPackage(selectedPackage);
                context.startActivity(share);
            }
        }
    }
}
