package com.tribe.app.presentation;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Pair;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.service.BroadcastUtils;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.view.ShortcutUtil;
import com.tribe.app.presentation.view.activity.LiveActivity;
import com.tribe.app.presentation.view.notification.Alerter;
import com.tribe.app.presentation.view.notification.NotificationPayload;
import com.tribe.app.presentation.view.notification.NotificationUtils;
import com.tribe.app.presentation.view.widget.LiveNotificationView;
import com.tribe.app.presentation.view.widget.chat.ChatActivity;
import java.lang.ref.WeakReference;
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

    computeNotificationPayload(context, notificationPayload);
  }

  public void notifiyStaticNotifSupport(Context context) {
    NotificationPayload payload = NotificationPayload.createSupportNotificationPayload();
    computeNotificationPayload(context, payload);
  }

  private boolean showMessageNotification(Context context,
      LiveNotificationView liveNotificationView, NotificationPayload notificationPayload) {
    if (liveNotificationView.getContainer() != null) {

      Shortcut notificationShortcut =
          ShortcutUtil.getRecipientFromId(notificationPayload.getUsers_ids(), user);

      if (notificationShortcut != null) {
        if (context instanceof ChatActivity) {
          List<User> memberInChat = null;
          if (((ChatActivity) context).getShortcut() != null &&
              ((ChatActivity) context).getShortcut().getMembers() != null) {
            memberInChat = ((ChatActivity) context).getShortcut().getMembers();
          }
          boolean isSameChat =
              ShortcutUtil.equalShortcutMembers(memberInChat, notificationShortcut.getMembers(),
                  user);
          if (isSameChat) {
            return false;
          }
        } else if (context instanceof LiveActivity) {
          List<User> memberInlive = null;
          if (((LiveActivity) context).getShortcut() != null &&
              ((LiveActivity) context).getShortcut().getMembers() != null) {
            memberInlive = ((LiveActivity) context).getShortcut().getMembers();
          }
          boolean isSameChat =
              ShortcutUtil.equalShortcutMembers(memberInlive, notificationShortcut.getMembers(),
                  user);
          if (isSameChat) {
            ((LiveActivity) context).notififyNewMessage();
            return false;
          }
        }
      }

      Shortcut finalNotificationShortcut = notificationShortcut;
      liveNotificationView.getContainer().setOnClickListener(view -> {
        liveNotificationView.hide();
        if (finalNotificationShortcut != null) {
          if (context instanceof ChatActivity) {
            if (!((ChatActivity) context).getShortcut()
                .getId()
                .equals(finalNotificationShortcut.getId())) {
              navigator.navigateToChat((Activity) context, finalNotificationShortcut, null, null,
                  false);
            }
          } else if (context instanceof LiveActivity) {
            // TODO
          } else {
            navigator.navigateToChat((Activity) context, finalNotificationShortcut, null, null,
                false);
          }
        }
      });
    }
    return true;
  }

  /**
   * PUBLIC
   */

  public void computeNotificationPayload(Context context, NotificationPayload notificationPayload) {
    LiveNotificationView liveNotificationView =
        NotificationUtils.getNotificationViewFromPayload(context, notificationPayload);

    if (notificationPayload.isLive()) {
      liveNotificationView.getContainer().setOnClickListener(view -> {
        Shortcut shortcut = ShortcutUtil.getRecipientFromId(notificationPayload.getUserId(), user);
        Room room = new Room(notificationPayload.getSessionId());
        room.setShortcut(shortcut);
        Invite invite = new Invite();
        invite.setShortcut(shortcut);
        invite.setRoom(room);
        navigator.navigateToLive((Activity) context, invite,
            LiveActivity.SOURCE_IN_APP_NOTIFICATION, null, null);
      });
    }

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

    if (liveNotificationView != null &&
        (weakReferenceActivity.get() != null &&
            !(weakReferenceActivity.get() instanceof LiveActivity))) {
      Alerter.create(weakReferenceActivity.get(), liveNotificationView).show();
    } else if (liveNotificationView != null) {
      onShowNotificationLive.onNext(Pair.create(notificationPayload, liveNotificationView));
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

  public Observable<Pair<NotificationPayload, LiveNotificationView>> onShowNotificationLive() {
    return onShowNotificationLive;
  }
}
