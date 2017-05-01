package com.tribe.app.presentation.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Toast;
import com.tribe.app.R;
import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.domain.entity.GroupMember;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.Extras;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.activity.AuthAccessActivity;
import com.tribe.app.presentation.view.activity.AuthActivity;
import com.tribe.app.presentation.view.activity.AuthProfileActivity;
import com.tribe.app.presentation.view.activity.CountryActivity;
import com.tribe.app.presentation.view.activity.DebugActivity;
import com.tribe.app.presentation.view.activity.GroupActivity;
import com.tribe.app.presentation.view.activity.HomeActivity;
import com.tribe.app.presentation.view.activity.LauncherActivity;
import com.tribe.app.presentation.view.activity.LiveActivity;
import com.tribe.app.presentation.view.activity.ProfileActivity;
import com.tribe.app.presentation.view.activity.VideoActivity;
import java.util.List;
import javax.inject.Inject;

/**
 * Class used to navigate through the application.
 */
public class Navigator {

  @Inject User user;

  public static int REQUEST_COUNTRY = 1000;
  public static int FROM_LIVE = 1001;
  public static String SNAPCHAT = "com.snapchat.android";
  public static String INSTAGRAM = "com.instagram.android";
  public static String TWITTER = "com.twitter.android";

  @Inject public Navigator(Context context) {
    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
  }

  public void navigateToLauncher(Context context) {
    if (context != null) {
      Intent intent = LauncherActivity.getCallingIntent(context);
      context.startActivity(intent);
    }
  }

  public void navigateToHomeAndFinishAffinity(Activity activity) {
    Intent mIntent = new Intent(activity, HomeActivity.class);
    activity.finishAffinity();
    activity.startActivity(mIntent);
  }

  public void navigateToSettingApp(Context context) {
    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.getPackageName(), null));
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(intent);
  }

  /**
   * Goes to the login.
   *
   * @param context A Context needed to open the destiny activity.
   */
  public void navigateToLogin(Context context, Uri deepLink) {
    if (context != null) {
      Intent intent = AuthActivity.getCallingIntent(context);
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
      activity.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
    }
  }

  public void navigateToAuthProfile(Activity activity, Uri deepLink, LoginEntity loginEntity) {
    if (activity != null) {
      Intent intent = AuthProfileActivity.getCallingIntent(activity, loginEntity);
      intent.setData(deepLink);
      activity.startActivity(intent);
      activity.overridePendingTransition(R.anim.in_from_right, R.anim.out_from_left);
      activity.finish();
    }
  }

  public void navigateToAuthAccess(Activity activity, Uri deepLink, String countryCode) {
    if (activity != null) {
      Intent intent = AuthAccessActivity.getCallingIntent(activity, countryCode);
      intent.setData(deepLink);
      activity.startActivity(intent);
      activity.overridePendingTransition(R.anim.in_from_right, R.anim.out_from_left);
      activity.finish();
    }
  }

  /**
   * Goes to the main grid.
   *
   * @param activity An activity needed to open the destiny activity.
   */
  public void navigateToHomeFromStart(Activity activity, Uri uriDeepLink) {
    if (activity != null) {
      Intent intent = HomeActivity.getCallingIntent(activity);
      if (uriDeepLink != null) {
        intent.setData(uriDeepLink);
      }
      activity.startActivity(intent);
    }
  }

  /**
   * Goes to the main grid.
   *
   * @param activity An activity needed to open the destiny activity.
   */
  public void navigateToHomeFromLogin(Activity activity, Uri uriDeepLink, String countryCode) {
    if (activity != null) {
      Intent intent = HomeActivity.getCallingIntent(activity);
      intent.putExtra(Extras.IS_FROM_LOGIN, true);
      intent.putExtra(Extras.COUNTRY_CODE, countryCode);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
          | Intent.FLAG_ACTIVITY_CLEAR_TASK
          | Intent.FLAG_ACTIVITY_SINGLE_TOP);
      activity.startActivity(intent);
      activity.overridePendingTransition(R.anim.in_from_right, R.anim.out_from_left);
    }
  }

  public void navigateToProfile(Activity activity) {
    if (activity != null) {
      Intent intent = ProfileActivity.getCallingIntent(activity);
      activity.startActivity(intent);
      activity.overridePendingTransition(R.anim.in_from_right, R.anim.activity_out_scale_down);
    }
  }

  public void navigateToVideo(Activity activity) {
    if (activity != null) {
      Intent intent = VideoActivity.getCallingIntent(activity);
      activity.startActivity(intent);
      activity.overridePendingTransition(R.anim.in_from_right, R.anim.activity_out_scale_down);
    }
  }

  /**
   * Logout -> new login
   */
  public void navigateToLogout(Activity activity) {

  }

  /**
   * Goes to the debug screen.
   *
   * @param activity activity needed to open the destiny activity.
   */
  public void navigateToDebugMode(Activity activity) {
    if (activity != null) {
      Intent intent = DebugActivity.getCallingIntent(activity);
      activity.startActivity(intent);
      activity.overridePendingTransition(R.anim.in_from_right, R.anim.activity_out_scale_down);
    }
  }

  /**
   * Goes to the group screen.
   *
   * @param activity activity needed to open the destiny activity.
   */
  public void navigateToCreateGroup(Activity activity) {
    if (activity != null) {
      Intent intent = GroupActivity.getCallingIntent(activity, null);
      activity.startActivityForResult(intent, 0);
      activity.overridePendingTransition(R.anim.in_from_right, R.anim.activity_out_scale_down);
    }
  }

  public void navigateToPrefilledCreationGroup(Activity activity,
      List<GroupMember> prefilledGrpMembers, boolean createGrpDirectly) {
    if (activity != null) {
      Intent intent = GroupActivity.getCallingIntentWithMembers(activity, prefilledGrpMembers,
          createGrpDirectly);
      activity.startActivityForResult(intent, 0);
      activity.overridePendingTransition(R.anim.in_from_right, R.anim.activity_out_scale_down);
    }
  }

  /**
   * Goes to the group screen.
   *
   * @param activity activity needed to open the destiny activity.
   * @param membership membership to detail
   */
  public void navigateToGroupDetails(Activity activity, Membership membership) {
    if (activity != null) {
      Intent intent = GroupActivity.getCallingIntent(activity, membership);
      activity.startActivity(intent);
      activity.overridePendingTransition(R.anim.in_from_right, R.anim.activity_out_scale_down);
    }
  }

  /**
   * Goes to the live screen.
   *
   * @param activity activity needed to open the destiny activity.
   * @param recipient recipient to go live with
   * @param color the color of the tile
   */
  public void navigateToLive(Activity activity, Recipient recipient, int color,
      @LiveActivity.Source String source) {
    if (activity != null) {
      Intent intent = LiveActivity.getCallingIntent(activity, recipient, color, source);
      activity.startActivityForResult(intent, FROM_LIVE);
      activity.overridePendingTransition(R.anim.in_from_right, R.anim.activity_out_scale_down);
    }
  }

  public void navigateToIntent(Activity activity, Intent intent) {
    if (activity != null) {
      if (activity instanceof LiveActivity) {
        activity.startActivity(intent);
      } else {
        activity.startActivityForResult(intent, FROM_LIVE);
        activity.overridePendingTransition(R.anim.in_from_right, R.anim.activity_out_scale_down);
      }
    }
  }

  public void navigateToLive(Activity activity, String linkId, String url,
      @LiveActivity.Source String source) {
    if (activity != null) {
      Intent intent = LiveActivity.getCallingIntent(activity, linkId, url, source);
      activity.startActivityForResult(intent, FROM_LIVE);
      activity.overridePendingTransition(R.anim.in_from_right, R.anim.activity_out_scale_down);
    }
  }

  public void navigateToNewCall(Activity activity) {
    if (activity != null) {
      Intent intent = LiveActivity.getCallingIntent(activity, LiveActivity.SOURCE_NEW_CALL);
      activity.startActivityForResult(intent, FROM_LIVE);
      activity.overridePendingTransition(R.anim.in_from_right, R.anim.activity_out_scale_down);
    }
  }

  /**
   * Goes to the app page in the playstore so the user may rate the app.
   *
   * @param context context needed to open the destiny activity.
   */
  public void rateApp(Context context) {
    final String appPackageName = "com.tribe.app";

    try {
      context.startActivity(
          new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
    } catch (android.content.ActivityNotFoundException anfe) {
      context.startActivity(new Intent(Intent.ACTION_VIEW,
          Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
    }
  }

  /**
   * Composes a tweet to post and opens Twitter app.
   *
   * @param context context needed to open the intent.
   * @param tweet the pre-filled tweet.
   */

  public void tweet(Context context, String tweet) {
    // http://stackoverflow.com/questions/21088250/android-launch-twitter-intent
    Intent tweetIntent = new Intent(Intent.ACTION_SEND);
    tweetIntent.putExtra(Intent.EXTRA_TEXT, tweet);
    tweetIntent.setType("text/plain");

    PackageManager packManager = context.getPackageManager();
    List<ResolveInfo> resolvedInfoList =
        packManager.queryIntentActivities(tweetIntent, PackageManager.MATCH_DEFAULT_ONLY);

    boolean resolved = false;

    for (ResolveInfo resolveInfo : resolvedInfoList) {
      if (resolveInfo.activityInfo.packageName.startsWith("com.twitter.android")) {
        tweetIntent.setClassName(resolveInfo.activityInfo.packageName,
            resolveInfo.activityInfo.name);
        resolved = true;
        break;
      }
    }

    if (resolved) {
      context.startActivity(tweetIntent);
    } else {
      context.startActivity(new Intent(Intent.ACTION_VIEW,
          Uri.parse("https://twitter.com/intent/tweet?text=@heytribe")));
    }
  }

  public void navigateToUrl(Context context, String url) {
    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
  }

  /**
   * Intent to send an email
   *
   * @param context context to start activity
   * @param addresses addresses to send email to
   * @param subject the subject of the email
   */

  public void composeEmail(Context context, String[] addresses, String subject) {
    Intent intent = new Intent(Intent.ACTION_SENDTO);
    intent.setData(Uri.parse("mailto:"));
    intent.putExtra(Intent.EXTRA_EMAIL, addresses);
    intent.putExtra(Intent.EXTRA_SUBJECT, subject);
    if (intent.resolveActivity(context.getPackageManager()) != null) {
      context.startActivity(intent);
    }
  }

  public void shareGenericText(String body, Activity activity) {
    Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
    sharingIntent.setType("text/plain");
    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
    activity.startActivity(sharingIntent);
    activity.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
  }

  public void sendText(String body, Context context) {
    Intent sendIntent = new Intent(Intent.ACTION_VIEW);
    sendIntent.setData(Uri.parse("sms:"));
    sendIntent.putExtra("sms_body", body);
    context.startActivity(sendIntent);
  }

  public void openSms(String body, Activity activity) {
    Intent sendIntent = new Intent(Intent.ACTION_VIEW);
    sendIntent.setData(Uri.parse("sms:"));
    sendIntent.putExtra("sms_body", body);
    activity.startActivity(sendIntent);
    activity.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
  }

  public void openSmsForInvite(Activity activity, String phoneNumber) {
    String text = EmojiParser.demojizedText(
        activity.getString(R.string.share_invite, user.getUsername(),
            activity.getString(R.string.share_messenger_url)));

    if (StringUtils.isEmpty(phoneNumber)) {
      shareGenericText(text, activity);
    } else {
      Intent sendIntent = new Intent(Intent.ACTION_VIEW);
      sendIntent.setData(Uri.parse("sms:" + phoneNumber));
      sendIntent.putExtra("sms_body", text);
      activity.startActivity(sendIntent);
      activity.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
    }
  }

  public void inviteToRoom(Activity activity, String roomLink) {
    String text = EmojiParser.demojizedText(activity.getString(R.string.share_live, roomLink));
    shareGenericText(text, activity);
  }

  public void openFacebookMessenger(String body, Activity activity) {
    Intent sendIntent = new Intent();
    sendIntent.setAction(Intent.ACTION_SEND);
    sendIntent.putExtra(Intent.EXTRA_TEXT, body);
    sendIntent.setType("text/plain");
    sendIntent.setPackage("com.facebook.orca");
    try {
      activity.startActivity(sendIntent);
    } catch (android.content.ActivityNotFoundException ex) {
      // TODO externalize this string
      Toast.makeText(activity, "Facebook Messenger is not installed.", Toast.LENGTH_LONG).show();
    }

    activity.overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
  }

  public void openWhatsApp(String body, Activity activity) {
    Intent sendIntent = new Intent();
    sendIntent.setAction(Intent.ACTION_SEND);
    sendIntent.putExtra(Intent.EXTRA_TEXT, body);
    sendIntent.setType("text/plain");
    sendIntent.setPackage("com.whatsapp");
    try {
      activity.startActivity(sendIntent);
    } catch (android.content.ActivityNotFoundException ex) {
      // TODO externalize this string
      Toast.makeText(activity, "Whatsapp is not installed.", Toast.LENGTH_LONG).show();
    }
  }
}
