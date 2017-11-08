package com.tribe.app.presentation;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Pair;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.service.BroadcastUtils;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.view.activity.LiveActivity;
import com.tribe.app.presentation.view.notification.Alerter;
import com.tribe.app.presentation.view.notification.NotificationPayload;
import com.tribe.app.presentation.view.notification.NotificationUtils;
import com.tribe.app.presentation.view.utils.ListUtils;
import com.tribe.app.presentation.view.widget.LiveNotificationView;
import com.tribe.app.presentation.view.widget.chat.ChatActivity;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 27/09/2017.
 */

public class TribeBroadcastReceiver extends BroadcastReceiver {

  @Inject Navigator navigator;
  @Inject User user;

  private WeakReference<Activity> weakReferenceActivity;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<String> onDeclineInvitation = PublishSubject.create();
  private PublishSubject<String> onCreateShortcut = PublishSubject.create();
  private PublishSubject<String> onDisplayNotification = PublishSubject.create();
  private PublishSubject<Void> onCallDeclined = PublishSubject.create();
  private PublishSubject<Pair<NotificationPayload, LiveNotificationView>> onShowNotificationLive =
      PublishSubject.create();

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
        NotificationUtils.getNotificationViewFromPayload(context, notificationPayload);

    if (notificationPayload.getClickAction().equals(NotificationPayload.CLICK_ACTION_MESSAGE)) {
      if (!showMessageNotification(context, liveNotificationView, notificationPayload)) {
        return;
      }
    }

    if (notificationPayload.getClickAction().equals(NotificationPayload.CLICK_ACTION_DECLINE)) {
      onDisplayNotification.onNext(EmojiParser.demojizedText(
          context.getString(R.string.live_notification_guest_declined,
              notificationPayload.getUserDisplayName())));
      onDisplayNotification.onNext(null);
    }

    if (liveNotificationView != null && (weakReferenceActivity.get() != null
        && !(weakReferenceActivity.get() instanceof LiveActivity))) {
      Alerter.create(weakReferenceActivity.get(), liveNotificationView).show();
    } else if (liveNotificationView != null) {
      onShowNotificationLive.onNext(Pair.create(notificationPayload, liveNotificationView));
    }
  }

  /**
   * PRIVATE METHODE
   */

  private boolean showMessageNotification(Context context,
      LiveNotificationView liveNotificationView, NotificationPayload notificationPayload) {
    if (liveNotificationView.getContainer() != null) {

      List<String> myList =
          new ArrayList<>(Arrays.asList(notificationPayload.getUsers_ids().split(",")));
      List<String> idslist = new ArrayList<>();
      idslist.add(user.getId());
      Shortcut notificationShortcut = null;
      for (Recipient recipient : user.getRecipientList()) {
        if (recipient instanceof Shortcut) {
          for (User member : ((Shortcut) recipient).getMembers()) {
            idslist.add(member.getId());
          }

          if (ListUtils.equalLists(myList, idslist)) {
            notificationShortcut = (Shortcut) recipient;
            myList.clear();
            idslist.clear();
            break;
          }
        }
        idslist.clear();
      }

      if (notificationShortcut != null) {
        if (context instanceof ChatActivity) {
          if (((ChatActivity) context).getShortcut().getId().equals(notificationShortcut.getId())) {
            return false;
          }
        } else if (context instanceof LiveActivity) {
          String shortcutId = ((LiveActivity) context).getShortcutId();
          if (shortcutId != null && shortcutId.equals(notificationShortcut.getId())) {
            ((LiveActivity) context).notififyNewMessage();
            return false;
          }
        }
      }

      Shortcut finalNotificationShortcut = notificationShortcut;
      liveNotificationView.getContainer().setOnClickListener(view -> {
        if (finalNotificationShortcut != null) {
          if (context instanceof ChatActivity) {
            if (!((ChatActivity) context).getShortcut()
                .getId()
                .equals(finalNotificationShortcut.getId())) {
              navigator.navigateToChat((Activity) context, finalNotificationShortcut, null, null,
                  null, false);
            }
          } else if (context instanceof LiveActivity) {
            // TODO
          } else {
            navigator.navigateToChat((Activity) context, finalNotificationShortcut, null, null,
                null, false);
          }
        }
      });
    }
    return true;
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

  public Observable<Pair<NotificationPayload, LiveNotificationView>> onShowNotificationLive() {
    return onShowNotificationLive;
  }
}
