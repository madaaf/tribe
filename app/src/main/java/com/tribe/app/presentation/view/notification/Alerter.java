package com.tribe.app.presentation.view.notification;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.R;
import com.tribe.app.presentation.view.widget.LiveNotificationView;
import java.lang.ref.WeakReference;

public final class Alerter {
  private static WeakReference<Activity> activityWeakReference;

  private LiveNotificationView liveNotificationView;

  private Alerter() {
    //Utility classes should not be instantiated
  }

  public static Alerter create(@NonNull final Activity activity,
      LiveNotificationView liveNotificationView) {
    if (activity == null) {
      throw new IllegalArgumentException("Activity cannot be null!");
    }

    final Alerter alerter = new Alerter();

    //Clear Current Alert, if one is Active
    Alerter.clearCurrent(activity);

    alerter.setActivity(activity);
    alerter.setLiveNotificationView(liveNotificationView);

    return alerter;
  }

  public LiveNotificationView show() {
    //This will get the Activity Window's DecorView
    if (getActivityWeakReference() != null) {
      getActivityWeakReference().get().runOnUiThread(new Runnable() {
        @Override public void run() {
          //Add the new Alert to the View Hierarchy
          final ViewGroup decorView = getActivityDecorView();
          if (decorView != null && getLiveNotificationView().getParent() == null) {
            decorView.addView(getLiveNotificationView());
          }
        }
      });
    }

    return getLiveNotificationView();
  }

  private LiveNotificationView getLiveNotificationView() {
    return liveNotificationView;
  }

  private void setLiveNotificationView(final LiveNotificationView liveNotificationView) {
    this.liveNotificationView = liveNotificationView;
  }

  @Nullable private WeakReference<Activity> getActivityWeakReference() {
    return activityWeakReference;
  }

  @Nullable private ViewGroup getActivityDecorView() {
    ViewGroup decorView = null;

    if (getActivityWeakReference() != null && getActivityWeakReference().get() != null) {
      decorView = (ViewGroup) getActivityWeakReference().get().getWindow().getDecorView();
    }

    return decorView;
  }

  private void setActivity(@NonNull final Activity activity) {
    activityWeakReference = new WeakReference<>(activity);
  }

  private static void clearCurrent(@NonNull final Activity activity) {
    if (activity == null) {
      return;
    }

    try {
      final View alertView =
          activity.getWindow().getDecorView().findViewById(R.id.view_live_notification_container);
      //Check if the Alert is added to the Window
      if (alertView == null || alertView.getWindowToken() == null) {
        Log.d(Alerter.class.getClass().getSimpleName(), "");
      } else {
        //Animate the Alpha
        alertView.animate().alpha(0).withEndAction(new Runnable() {
          @Override public void run() {
            //And remove the view for the parent layout
            ((ViewGroup) alertView.getParent()).removeView(alertView);
          }
        }).start();

        Log.d(Alerter.class.getClass().getSimpleName(), "");
      }
    } catch (Exception ex) {
      Log.e(Alerter.class.getClass().getSimpleName(), Log.getStackTraceString(ex));
    }
  }
}
