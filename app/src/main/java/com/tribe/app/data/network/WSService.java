package com.tribe.app.data.network;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import com.tribe.app.R;
import com.tribe.app.data.cache.LiveCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.deserializer.JsonToModel;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.MembershipRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.tribelivesdk.back.WebSocketConnection;
import java.util.UUID;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

@Singleton public class WSService extends Service {

  public static final String USER_SUFFIX = "___u";
  public static final String GROUP_SUFFIX = "___g";
  public static final String FRIENDSHIP_CREATED_SUFFIX = "___fc";
  public static final String FRIENDSHIP_UDPATED_SUFFIX = "___fu";
  public static final String FRIENDSHIP_REMOVED_SUFFIX = "___fr";
  public static final String MEMBERSHIP_CREATED_SUFFIX = "___mc";
  public static final String MEMBERSHIP_REMOVED_SUFFIX = "___mr";
  public static final String INVITE_CREATED_SUFFIX = "___ic";
  public static final String INVITE_REMOVED_SUFFIX = "___ir";

  public static Intent getCallingIntent(Context context) {
    Intent intent = new Intent(context, WSService.class);
    return intent;
  }

  @Inject User user;

  @Inject TribeApi tribeApi;

  @Inject UserCache userCache;

  @Inject LiveCache liveCache;

  @Inject AccessToken accessToken;

  @Inject JsonToModel jsonToModel;

  @Inject @Named("webSocketApi") WebSocketConnection webSocketConnection;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }

  @Override public void onCreate() {
    super.onCreate();

    initDependencyInjection();
  }

  @Override public void onDestroy() {
    if (subscriptions != null) subscriptions.clear();
    handleStop();
    super.onDestroy();
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    if (subscriptions != null) subscriptions.clear();
    handleStart();
    return Service.START_STICKY;
  }

  private void handleStart() {
    initWebSocket();
  }

  private void handleStop() {
    if (webSocketConnection.getState().equals(WebSocketConnection.STATE_CONNECTED)) {
      webSocketConnection.disconnect(false);
    }
  }

  private void initWebSocket() {
    webSocketConnection.connect("wss://api.tribedev.pm");

    subscriptions.add(webSocketConnection.onStateChanged().subscribe(newState -> {
      if (newState.equals(WebSocketConnection.STATE_CONNECTED)) {
        initSubscriptions();
      }
    }));

    subscriptions.add(webSocketConnection.onMessage().subscribe(message -> {
      Timber.d("onMessage : " + message);

      jsonToModel.convertToSubscriptionResponse(message);
    }));
  }

  private void initSubscriptions() {
    UserRealm userRealm = userCache.userInfosNoObs(accessToken.getUserId());

    StringBuffer subscriptionsBuffer = new StringBuffer();
    String hash = generateHash();

    append(subscriptionsBuffer,
        getApplicationContext().getString(R.string.subscription_friendshipCreated,
            hash + FRIENDSHIP_CREATED_SUFFIX));

    append(subscriptionsBuffer,
        getApplicationContext().getString(R.string.subscription_friendshipRemoved,
            hash + FRIENDSHIP_REMOVED_SUFFIX));

    append(subscriptionsBuffer,
        getApplicationContext().getString(R.string.subscription_membershipCreated,
            hash + MEMBERSHIP_CREATED_SUFFIX));

    append(subscriptionsBuffer,
        getApplicationContext().getString(R.string.subscription_membershipRemoved,
            hash + MEMBERSHIP_REMOVED_SUFFIX));

    append(subscriptionsBuffer,
        getApplicationContext().getString(R.string.subscription_inviteCreated,
            hash + INVITE_CREATED_SUFFIX));

    append(subscriptionsBuffer,
        getApplicationContext().getString(R.string.subscription_inviteRemoved,
            hash + INVITE_REMOVED_SUFFIX));

    Observable.zip(Observable.just(userRealm.getFriendships()).doOnNext(friendshipList -> {
      int count = 0;

      for (FriendshipRealm friendshipRealm : friendshipList) {
        if (!friendshipRealm.getSubId().equals(accessToken.getUserId())) {
          append(subscriptionsBuffer,
              getApplicationContext().getString(R.string.subscription_userUpdated,
                  hash + USER_SUFFIX + count, friendshipRealm.getSubId()));

          append(subscriptionsBuffer,
              getApplicationContext().getString(R.string.subscription_friendshipUpdated,
                  hash + FRIENDSHIP_UDPATED_SUFFIX + count, friendshipRealm.getId()));

          count++;
        }
      }
    }), Observable.just(userRealm.getMemberships()).doOnNext(membershipList -> {
      int count = 0;

      for (MembershipRealm membershipRealm : membershipList) {
        append(subscriptionsBuffer,
            getApplicationContext().getString(R.string.subscription_groupUpdated,
                hash + GROUP_SUFFIX + count, membershipRealm.getGroup().getId()));

        count++;
      }
    }), (friendshipRealmRealmList, membershipRealms) -> subscriptionsBuffer)
        .subscribe(stringBuffer -> {
          String body = subscriptionsBuffer.toString();

          String userInfosFragment =
              (body.contains("UserInfos") ? "\n" + getApplicationContext().getString(
                  R.string.userfragment_infos) : "");

          String groupInfosFragment =
              (body.contains("GroupInfos") ? "\n" + getApplicationContext().getString(
                  R.string.groupfragment_info_members) : "");

          String req = getApplicationContext().getString(R.string.subscription,
              subscriptionsBuffer.toString()) + userInfosFragment + groupInfosFragment;

          webSocketConnection.send(req);
        });

    subscriptions.add(jsonToModel.onInviteCreated()
        .subscribeOn(Schedulers.from(Executors.newSingleThreadExecutor()))
        .filter(invite -> !liveCache.getInviteMap().containsKey(invite.getRoomId()))
        .flatMap(invite -> this.tribeApi.invites(
            getApplicationContext().getString(R.string.invites_infos,
                getApplicationContext().getString(R.string.userfragment_infos),
                getApplicationContext().getString(R.string.groupfragment_info_members),
                getApplicationContext().getString(R.string.friendshipfragment_info))),
            (invite, invites) -> {
              if (invites != null) {
                for (Invite newInvite : invites) {
                  boolean shouldAdd = true;
                  if (newInvite.getFriendships() != null) {
                    for (Friendship friendship : newInvite.getFriendships()) {
                      if (friendship.getFriend().equals(user)) {
                        shouldAdd = false;
                      }
                    }
                  }

                  if (shouldAdd) {
                    liveCache.putInvite(newInvite);
                  }
                }
              }

              return null;
            })
        .subscribe());

    subscriptions.add(jsonToModel.onInviteRemoved()
        .subscribeOn(Schedulers.from(Executors.newSingleThreadExecutor()))
        .subscribe(invite -> {
          liveCache.removeInvite(invite);
        }));

    subscriptions.add(jsonToModel.onAddedLive().subscribe(s -> {
      liveCache.putLive(s);
    }));

    subscriptions.add(jsonToModel.onRemovedLive().subscribe(s -> {
      liveCache.removeLive(s);
    }));

    subscriptions.add(jsonToModel.onAddedOnline().subscribe(s -> {
      liveCache.putOnline(s);
    }));

    subscriptions.add(jsonToModel.onRemovedOnline().subscribe(s -> {
      liveCache.removeOnline(s);
    }));

    subscriptions.add(jsonToModel.onUserListUpdated().subscribe(userRealmList -> {
      userCache.updateUserRealmList(userRealmList);
    }));

    subscriptions.add(jsonToModel.onGroupListUpdated().subscribe(groupRealmList -> {
      userCache.updateGroupRealmList(groupRealmList);
    }));

    subscriptions.add(jsonToModel.onCreatedFriendship().subscribe(friendshipRealm -> {
      userCache.addFriendship(friendshipRealm);
    }));

    subscriptions.add(jsonToModel.onCreatedMembership()
        .subscribeOn(Schedulers.from(Executors.newSingleThreadExecutor()))
        .flatMap(groupId -> {
          final String requestCreateMembership =
              getApplicationContext().getString(R.string.create_membership, groupId,
                  getApplicationContext().getString(R.string.membershipfragment_info),
                  getApplicationContext().getString(R.string.groupfragment_info_members),
                  getApplicationContext().getString(R.string.userfragment_infos));
          return this.tribeApi.createMembership(requestCreateMembership);
        }, (s, membershipRealm) -> membershipRealm)
        .subscribe(membershipRealm -> {
          userCache.addMembership(membershipRealm);
        }));

    subscriptions.add(jsonToModel.onRemovedFriendship().subscribe(friendshipRealm -> {
      userCache.removeFriendship(friendshipRealm);
    }));

    subscriptions.add(jsonToModel.onRemovedMembership().subscribe(membershipRealm -> {
      userCache.removeMembership(membershipRealm);
    }));
  }

  private void append(StringBuffer buffer, String str) {
    buffer.append(str);
    buffer.append(System.getProperty("line.separator"));
  }

  private String generateHash() {
    return "H" + UUID.randomUUID().toString().toUpperCase().replace("-", "");
  }

  private void initDependencyInjection() {
    ((AndroidApplication) getApplication()).getApplicationComponent().inject(this);
  }
}