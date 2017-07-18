package com.tribe.app.data.network;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import com.tribe.app.BuildConfig;
import com.tribe.app.R;
import com.tribe.app.data.cache.LiveCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.deserializer.JsonToModel;
import com.tribe.app.data.network.util.TribeApiUtils;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.MembershipRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.tribelivesdk.back.WebSocketConnection;
import java.util.HashMap;
import java.util.Map;
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

  public static final String TYPE = "TYPE";
  public static final String CALL_ROULETTE_TYPE = "CALL_ROULETTE_TYPE";

  public static final String USER_SUFFIX = "___u";
  public static final String GROUP_SUFFIX = "___g";
  public static final String FRIENDSHIP_CREATED_SUFFIX = "___fc";
  public static final String FRIENDSHIP_UDPATED_SUFFIX = "___fu";
  public static final String FRIENDSHIP_REMOVED_SUFFIX = "___fr";
  public static final String MEMBERSHIP_CREATED_SUFFIX = "___mc";
  public static final String MEMBERSHIP_REMOVED_SUFFIX = "___mr";
  public static final String INVITE_CREATED_SUFFIX = "___ic";
  public static final String INVITE_REMOVED_SUFFIX = "___ir";
  public static final String RANDOM_ROOM_ASSIGNED = "___ra";

  public static Intent getCallingIntent(Context context, String type) {
    Intent intent = new Intent(context, WSService.class);
    intent.putExtra(TYPE, type);
    return intent;
  }

  @Inject User user;

  @Inject TribeApi tribeApi;

  @Inject UserCache userCache;

  @Inject LiveCache liveCache;

  @Inject AccessToken accessToken;

  @Inject JsonToModel jsonToModel;

  @Inject @Named("webSocketApi") WebSocketConnection webSocketConnection;

  // VARIABLES
  private Map<String, String> headers;
  private @WebSocketConnection.WebSocketState String webSocketState = WebSocketConnection.STATE_NEW;
  private boolean hasSubscribed = false;

  // OBSERVABLES
  private CompositeSubscription persistentSubscriptions = new CompositeSubscription();
  private CompositeSubscription tempSubscriptions = new CompositeSubscription();

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }

  @Override public void onCreate() {
    super.onCreate();

    headers = new HashMap<>();

    initDependencyInjection();
    prepareHeaders();
  }

  @Override public void onDestroy() {
    if (tempSubscriptions != null) tempSubscriptions.clear();
    if (persistentSubscriptions != null) persistentSubscriptions.clear();
    handleStop();
    super.onDestroy();
  }

  public void subscribeChatRoulette() {
    String hash = generateHash();
    StringBuffer subscriptionsBuffer = new StringBuffer();
    append(subscriptionsBuffer,
        getApplicationContext().getString(R.string.subscription_randomRoomAssigned,
            hash + RANDOM_ROOM_ASSIGNED));

    String req =
        getApplicationContext().getString(R.string.subscription, subscriptionsBuffer.toString());

    webSocketConnection.send(req);
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null && intent.getStringExtra(TYPE) != null && intent.getStringExtra(TYPE)
        .equals(CALL_ROULETTE_TYPE)) {
      subscribeChatRoulette();
    }

    if (webSocketState != null && (webSocketState.equals(WebSocketConnection.STATE_CONNECTED)
        || webSocketConnection.equals(WebSocketConnection.STATE_CONNECTING))) {
      Timber.d("webSocketState connected or connecting, no need to reconnect");
      return Service.START_STICKY;
    }

    if (tempSubscriptions != null) tempSubscriptions.clear();
    if (persistentSubscriptions != null) persistentSubscriptions.clear();
    handleStart();
    return Service.START_STICKY;
  }

  private void handleStart() {
    initWebSocket();
    initModel();
  }

  private void handleStop() {
    if (webSocketConnection.getState().equals(WebSocketConnection.STATE_CONNECTED)) {
      webSocketConnection.disconnect(false);
    }
  }

  private void prepareHeaders() {
    if (StringUtils.isEmpty(accessToken.getTokenType()) || StringUtils.isEmpty(
        accessToken.getAccessToken())) {
      webSocketConnection.setShouldReconnect(false);
    } else {
      headers.put(WebSocketConnection.CONTENT_TYPE, "application/json");
      headers.put(WebSocketConnection.USER_AGENT,
          TribeApiUtils.getUserAgent(getApplicationContext()));
      headers.put(WebSocketConnection.AUTHORIZATION,
          accessToken.getTokenType() + " " + accessToken.getAccessToken());
      headers.put(WebSocketConnection.VERSION, "13");
      headers.put(WebSocketConnection.PROTOCOL, "graphql");
    }
  }

  private void initWebSocket() {
    hasSubscribed = false;
    webSocketConnection.setHeaders(headers);
    webSocketConnection.connect(BuildConfig.TRIBE_WSS);

    persistentSubscriptions.add(webSocketConnection.onStateChanged().subscribe(newState -> {
      webSocketState = newState;

      if (newState.equals(WebSocketConnection.STATE_CONNECTED)) {
        if (!hasSubscribed) {
          hasSubscribed = true;
          initSubscriptions();
        }
      } else if (newState.equals(WebSocketConnection.STATE_DISCONNECTED)) {
        hasSubscribed = false;
        if (tempSubscriptions != null) tempSubscriptions.clear();
      }
    }));

    persistentSubscriptions.add(webSocketConnection.onMessage().subscribe(message -> {
      Timber.d("onMessage : " + message);

      jsonToModel.convertToSubscriptionResponse(message);
    }));

    persistentSubscriptions.add(webSocketConnection.onConnectError().subscribe(s -> {
      Timber.d("onConnectError setting new headers : " + s);
      prepareHeaders();
      webSocketConnection.setHeaders(headers);
    }));
  }

  private void initModel() {
    persistentSubscriptions.add(jsonToModel.onInviteCreated()
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
                      if (friendship != null
                          && friendship.getFriend() != null
                          && friendship.getFriend().equals(user)) {
                        shouldAdd = false;
                      }
                    }
                  }

                  if (shouldAdd) {
                    if (!StringUtils.isEmpty(invite.getRoomName())) {
                      newInvite.setRoomName(
                          getApplicationContext().getString(R.string.grid_menu_call_placeholder));
                    }

                    liveCache.putInvite(newInvite);
                  }
                }
              }

              return null;
            })
        .subscribe());

    persistentSubscriptions.add(jsonToModel.onInviteRemoved()
        .subscribeOn(Schedulers.from(Executors.newSingleThreadExecutor()))
        .subscribe(invite -> {
          liveCache.removeInvite(invite);
        }));

    persistentSubscriptions.add(jsonToModel.onAddedLive().subscribe(s -> {
      liveCache.putLive(s);
    }));

    persistentSubscriptions.add(jsonToModel.onRemovedLive().subscribe(s -> {
      liveCache.removeLive(s);
    }));

    persistentSubscriptions.add(jsonToModel.onAddedOnline().subscribe(s -> {
      liveCache.putOnline(s);
    }));

    persistentSubscriptions.add(jsonToModel.onRemovedOnline().subscribe(s -> {
      liveCache.removeOnline(s);
    }));

    persistentSubscriptions.add(jsonToModel.onRandomRoomAssigned().subscribe(assignedRoomId -> {
      Timber.d("onRandomRoomAssigned assignedRoomId " + assignedRoomId);
      liveCache.putRandomRoomAssigned(assignedRoomId);
    }));

    persistentSubscriptions.add(jsonToModel.onFbIdUpdated().subscribe(userUpdated -> {
      liveCache.onFbIdUpdated(userUpdated);
    }));

    persistentSubscriptions.add(jsonToModel.onUserListUpdated().subscribe(userRealmList -> {
      userCache.updateUserRealmList(userRealmList);
    }));

    persistentSubscriptions.add(jsonToModel.onGroupListUpdated().subscribe(groupRealmList -> {
      userCache.updateGroupRealmList(groupRealmList);
    }));

    persistentSubscriptions.add(jsonToModel.onCreatedFriendship()
        .subscribeOn(Schedulers.from(Executors.newSingleThreadExecutor()))
        .flatMap(friendshipId -> {
          String ids = "\"" + friendshipId + "\"";
          final String requestFriendshipsInfos =
              getApplicationContext().getString(R.string.friendships_details, ids,
                  getApplicationContext().getString(R.string.friendshipfragment_info),
                  getApplicationContext().getString(R.string.userfragment_infos));
          return this.tribeApi.getUserInfos(requestFriendshipsInfos);
        }, (s, userRealmInfos) -> userRealmInfos)
        .subscribe(userRealmInfos -> {
          userCache.addFriendship(userRealmInfos.getFriendships().first());
        }));

    persistentSubscriptions.add(jsonToModel.onCreatedMembership()
        .subscribeOn(Schedulers.from(Executors.newSingleThreadExecutor()))
        .flatMap(membershipId -> {
          String ids = "\"" + membershipId + "\"";
          final String requestMembershipInfos =
              getApplicationContext().getString(R.string.membership_infos, ids,
                  getApplicationContext().getString(R.string.membershipfragment_info),
                  getApplicationContext().getString(R.string.groupfragment_info_members),
                  getApplicationContext().getString(R.string.userfragment_infos));
          return this.tribeApi.getUserInfos(requestMembershipInfos);
        }, (s, userRealmInfos) -> userRealmInfos)
        .subscribe(userRealmInfos -> {
          userCache.addMembership(userRealmInfos.getMemberships().first());
        }));

    persistentSubscriptions.add(jsonToModel.onRemovedFriendship().subscribe(friendshipRealm -> {
      userCache.removeFriendship(friendshipRealm);
    }));

    persistentSubscriptions.add(jsonToModel.onRemovedMembership().subscribe(membershipRealm -> {
      userCache.removeMembership(membershipRealm);
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

    tempSubscriptions.add(
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