package com.tribe.app.presentation;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.service.BroadcastUtils;
import com.tribe.app.presentation.view.notification.Alerter;
import com.tribe.app.presentation.view.notification.NotificationPayload;
import com.tribe.app.presentation.view.notification.NotificationUtils;
import com.tribe.app.presentation.view.utils.MissedCallManager;
import com.tribe.app.presentation.view.widget.LiveNotificationView;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 27/09/2017.
 */

public class TribeBroadcastReceiver extends BroadcastReceiver {

  @Inject MissedCallManager missedCallManager;
  @Inject Navigator navigator;

  private WeakReference<Activity> weakReferenceActivity;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<String> onDeclineInvitation = PublishSubject.create();
  private PublishSubject<String> onCreateShortcut = PublishSubject.create();

  public TribeBroadcastReceiver(Activity activity) {
    weakReferenceActivity = new WeakReference<>(activity);
    ApplicationComponent applicationComponent =
        ((AndroidApplication) activity.getApplicationContext()).getApplicationComponent();
    applicationComponent.inject(this);
  }

  public void dispose() {
    subscriptions.clear();
    weakReferenceActivity.clear();
  }

  @Override public void onReceive(Context context, Intent intent) {
    NotificationPayload notificationPayload =
        (NotificationPayload) intent.getSerializableExtra(BroadcastUtils.NOTIFICATION_PAYLOAD);

    LiveNotificationView liveNotificationView =
        NotificationUtils.getNotificationViewFromPayload(context, notificationPayload,
            missedCallManager);

    if (liveNotificationView != null) {
      subscriptions.add(liveNotificationView.onClickAction()
          .delay(500, TimeUnit.MILLISECONDS)
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(action -> {
            if (weakReferenceActivity == null || weakReferenceActivity.get() == null) return;

            if (action.getId().equals(NotificationUtils.ACTION_DECLINE)) {
              onDeclineInvitation.onNext(action.getSessionId());
            } else if (action.getId().equals(NotificationUtils.ACTION_ADD_FRIEND)) {
              onCreateShortcut.onNext(action.getUserId());
            } else if (action.getIntent() != null) {
              navigator.navigateToIntent(weakReferenceActivity.get(), action.getIntent());
            }
          }));

      Alerter.create(weakReferenceActivity.get(), liveNotificationView).show();
    }
  }

  /**
   * OBSERVABLES
   */

  public Observable<String> onDeclineInvitation() {
    return onDeclineInvitation;
  }

  public Observable<String> onCreateShortcut() {
    return onCreateShortcut;
  }
}
