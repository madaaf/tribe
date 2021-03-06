package com.tribe.app.presentation;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Pair;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.R;
import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.mvp.presenter.MessagePresenter;
import com.tribe.app.presentation.mvp.presenter.UserPresenter;
import com.tribe.app.presentation.mvp.view.adapter.MessageMVPViewAdapter;
import com.tribe.app.presentation.mvp.view.adapter.UserMVPViewAdapter;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.service.BroadcastUtils;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.utils.preferences.ChallengeNotifications;
import com.tribe.app.presentation.view.NotifView;
import com.tribe.app.presentation.view.NotificationModel;
import com.tribe.app.presentation.view.ShortcutUtil;
import com.tribe.app.presentation.view.activity.LiveActivity;
import com.tribe.app.presentation.view.notification.Alerter;
import com.tribe.app.presentation.view.notification.NotificationPayload;
import com.tribe.app.presentation.view.notification.NotificationUtils;
import com.tribe.app.presentation.view.popup.PopupManager;
import com.tribe.app.presentation.view.popup.listener.PopupAskToJoinListenerAdapter;
import com.tribe.app.presentation.view.popup.view.PopupAskToJoin;
import com.tribe.app.presentation.view.popup.view.PopupParentView;
import com.tribe.app.presentation.view.utils.DialogFactory;
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
  @Inject MessagePresenter messagePresenter;
  @Inject StateManager stateManager;
  @Inject @ChallengeNotifications Preference<String> challengeNotificationsPref;

  private WeakReference<Activity> weakReferenceActivity;
  private UserMVPViewAdapter userMVPViewAdapter;
  private MessageMVPViewAdapter messageMVPViewAdapter;
  private String userIdAsk;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<String> onDeclineInvitation = PublishSubject.create();
  private PublishSubject<String> onCreateShortcut = PublishSubject.create();
  private PublishSubject<String> onDisplayNotification = PublishSubject.create();
  private PublishSubject<NotificationPayload> onCallDeclined = PublishSubject.create();
  private PublishSubject<String> onCreateInvite = PublishSubject.create();
  private PublishSubject<Pair<NotificationPayload, LiveNotificationView>> onShowNotificationLive =
      PublishSubject.create();
  private PublishSubject<NotificationPayload> onGetRoomMembersLive = PublishSubject.create();

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

    messageMVPViewAdapter = new MessageMVPViewAdapter(activity) {
    };
    messagePresenter.onViewAttached(messageMVPViewAdapter);
  }

  public void dispose() {
    userPresenter.onViewDetached();
    messagePresenter.onViewDetached();
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

  public void setWaitingToJoin(String userIdAsk) {
    this.userIdAsk = userIdAsk;
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
      if (notificationPayload.getUserId().equals(userIdAsk)) {
        userIdAsk = null;
        goToLive(context, notificationPayload);
        return;
      }

      liveNotificationView.getContainer().setOnClickListener(view -> {
        if (weakReferenceActivity == null || weakReferenceActivity.get() == null) return;
        if (notificationPayload.isAsking()) {
          displayJoinPopup(context, notificationPayload, null);
        } else {
          onGetRoomMembersLive.onNext(notificationPayload);
        }
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

  public void displayJoinPopup(Context context, NotificationPayload notificationPayload,
      List<User> members) {
    List<NotificationModel> notificationModelList = new ArrayList<>();

    PopupAskToJoin.Builder builder = new PopupAskToJoin.Builder(context,
        notificationPayload.isAsking() ? PopupAskToJoin.ASK_TO_JOIN
            : PopupAskToJoin.INVITED_TO_JOIN).user(user.findUser(notificationPayload.getUserId()))
        .game(notificationPayload.getGame_id());

    if (members != null) {
      builder.members(members);
    }

    PopupAskToJoin popupAskToJoin = builder.build();

    NotifView notifView = new NotifView(context);

    PopupParentView popupParentView = PopupManager.create(
        new PopupManager.Builder().activity(weakReferenceActivity.get())
            .dimBackground(false)
            .listener(new PopupAskToJoinListenerAdapter() {
              @Override public void accept() {
                if (!notificationPayload.isAsking()) {
                  goToLive(context, notificationPayload);
                  if (notifView != null) notifView.dispose();
                } else {
                  onCreateInvite.onNext(notificationPayload.getUserId());
                  if (notifView != null) notifView.hideView();
                }
              }

              @Override public void decline() {
                onDeclineInvitation.onNext(notificationPayload.getSessionId());
                subscriptions.add(DialogFactory.dialogMultipleChoices(context,
                    EmojiParser.demojizedText(context.getString(R.string.decline_menu_title,
                        notificationPayload.getUserDisplayName())),
                    EmojiParser.demojizedText(context.getString(R.string.decline_menu_message)),
                    EmojiParser.demojizedText(context.getString(R.string.decline_menu_answer1)),
                    EmojiParser.demojizedText(context.getString(R.string.decline_menu_answer2)),
                    EmojiParser.demojizedText(context.getString(R.string.decline_menu_custom)))
                    .subscribe(integer -> {
                      String[] array = new String[] { notificationPayload.getUserId() };
                      switch (integer) {
                        case 0:
                          messagePresenter.createMessage(array,
                              EmojiParser.demojizedText("Let's catch up after! :dizzy:"),
                              MessageRealm.TEXT, 0);
                          break;

                        case 1:
                          messagePresenter.createMessage(array,
                              EmojiParser.demojizedText("Text me in the chat! :speech_balloon:"),
                              MessageRealm.TEXT, 0);
                          break;
                        case 2:
                          Shortcut shortcut =
                              ShortcutUtil.getRecipientFromId(notificationPayload.getUserId(),
                                  user);
                          if (weakReferenceActivity.get() != null) {
                            navigator.navigateToChat(weakReferenceActivity.get(), shortcut,
                                shortcut, TagManagerUtils.SECTION_ONLINE, false);
                          }
                          break;
                      }
                    }));
                if (notifView != null) notifView.hideView();
              }

              @Override public void later() {
                if (notifView != null) notifView.hideView();
              }
            })
            .view(popupAskToJoin));

    notificationModelList.add(new NotificationModel.Builder().view(popupParentView).build());

    notifView.show(weakReferenceActivity.get(), notificationModelList);

    subscriptions.add(notifView.onDismiss()
        .filter(aVoid -> !StringUtils.isEmpty(notificationPayload.getSessionId()))
        .subscribe(aVoid -> onDeclineInvitation.onNext(notificationPayload.getSessionId())));
  }

  private void goToLive(Context context, NotificationPayload notificationPayload) {
    Shortcut shortcut = ShortcutUtil.getRecipientFromId(notificationPayload.getUserId(), user);
    Room room = new Room(notificationPayload.getSessionId());
    room.setShortcut(shortcut);
    Invite invite = new Invite();
    invite.setShortcut(shortcut);
    invite.setRoom(room);
    navigator.navigateToLive((Activity) context, invite, LiveActivity.SOURCE_IN_APP_NOTIFICATION,
        null, null);
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

  public Observable<String> onCreateInvite() {
    return onCreateInvite;
  }

  public Observable<NotificationPayload> onGetRoomMembersLive() {
    return onGetRoomMembersLive;
  }
}
