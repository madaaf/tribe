package com.tribe.app.presentation;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Pair;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.mvp.presenter.UserPresenter;
import com.tribe.app.presentation.mvp.view.adapter.UserMVPViewAdapter;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.service.BroadcastUtils;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.preferences.ChallengeNotifications;
import com.tribe.app.presentation.view.NotifView;
import com.tribe.app.presentation.view.NotificationModel;
import com.tribe.app.presentation.view.ShortcutUtil;
import com.tribe.app.presentation.view.activity.LiveActivity;
import com.tribe.app.presentation.view.notification.Alerter;
import com.tribe.app.presentation.view.notification.NotificationPayload;
import com.tribe.app.presentation.view.notification.NotificationUtils;
import com.tribe.app.presentation.view.popup.PopupManager;
import com.tribe.app.presentation.view.popup.view.PopupAskToJoin;
import com.tribe.app.presentation.view.utils.StateManager;
import com.tribe.app.presentation.view.widget.LiveNotificationView;
import com.tribe.app.presentation.view.widget.chat.ChatActivity;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
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
  @Inject UserPresenter userPresenter;
  @Inject StateManager stateManager;
  @Inject @ChallengeNotifications Preference<String> challengeNotificationsPref;

  private WeakReference<Activity> weakReferenceActivity;
  private UserMVPViewAdapter userMVPViewAdapter;

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

    userMVPViewAdapter = new UserMVPViewAdapter() {

      @Override public void onUserInfosList(List<User> users) {
        NotificationUtils.displayChallengeNotification(users, activity, stateManager,
            challengeNotificationsPref, user);
      }
    };

    userPresenter.onViewAttached(userMVPViewAdapter);
  }

  public void dispose() {
    if (userPresenter != null) userPresenter.onViewDetached();
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
        if (weakReferenceActivity == null || weakReferenceActivity.get() == null) return;

        List<NotificationModel> notificationModelList = new ArrayList<>();

        PopupAskToJoin popupAskToJoin = new PopupAskToJoin.Builder(context,
            notificationPayload.isAsking() ? PopupAskToJoin.ASK_TO_JOIN
                : PopupAskToJoin.INVITED_TO_JOIN).user(
            user.findUser(notificationPayload.getUserId()))
            .game(notificationPayload.getGame_id())
            .build();

        PopupManager popupManager = PopupManager.create(
            new PopupManager.Builder().activity(weakReferenceActivity.get())
                .dimBackground(false)
                .view(popupAskToJoin));

        notificationModelList.add(
            new NotificationModel.Builder().view(popupManager.getView()).build());
        NotifView notifView = new NotifView(context);
        notifView.show(weakReferenceActivity.get(), notificationModelList);

        //Shortcut shortcut = ShortcutUtil.getRecipientFromId(notificationPayload.getUserId(), user);
        //Room room = new Room(notificationPayload.getSessionId());
        //room.setShortcut(shortcut);
        //Invite invite = new Invite();
        //invite.setShortcut(shortcut);
        //invite.setRoom(room);
        //navigator.navigateToLive((Activity) context, invite,
        //    LiveActivity.SOURCE_IN_APP_NOTIFICATION, null, null);
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

    if (notificationPayload.getClickAction()
        .equals(NotificationPayload.CLICK_ACTION_USER_REGISTERED)) {
      liveNotificationView.getContainer().setOnClickListener(v -> {
        List<String> usersId = new ArrayList<>();
        usersId.add(notificationPayload.getUserId());
        userPresenter.getUsersInfoListById(usersId);
      });
    }

    if (liveNotificationView != null &&
        (weakReferenceActivity.get() != null &&
            (!(weakReferenceActivity.get() instanceof LiveActivity) ||
                notificationPayload.isLive()))) {
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
