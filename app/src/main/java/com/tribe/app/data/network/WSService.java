package com.tribe.app.data.network;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import com.tribe.app.BuildConfig;
import com.tribe.app.R;
import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.data.cache.LiveCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.deserializer.JsonToModel;
import com.tribe.app.data.network.util.TribeApiUtils;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.tribelivesdk.back.WebSocketConnection;
import io.realm.RealmList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

@Singleton public class WSService extends Service {

  public static final String TYPE = "TYPE";
  public static final String ROOM_ID = "ROOM_ID";
  public static final String CHAT_IDS = "CHAT_IDS";

  public static final String CHAT_SUBSCRIBE = "CHAT_SUBSCRIBE";
  public static final String CALL_ROULETTE_TYPE = "CALL_ROULETTE_TYPE";
  public static final String CALL_ROOM_UPDATE_SUBSCRIBE_TYPE = "CALL_ROOM_UPDATE_TYPE";
  public static final String CALL_ROOM_UPDATE_UNSUBSCRIBE_TYPE =
      "CALL_ROOM_UPDATE_UNSUBSCRIBE_TYPE";

  public static final String USER_SUFFIX = "___u";
  public static final String INVITE_CREATED_SUFFIX = "___ic";
  public static final String INVITE_REMOVED_SUFFIX = "___ir";
  public static final String RANDOM_ROOM_ASSIGNED_SUFFIX = "___ra";
  public static final String ROOM_UDPATED_SUFFIX = "___ru";
  public static final String SHORTCUT_CREATED_SUFFIX = "___sc";
  public static final String SHORTCUT_UPDATED_SUFFIX = "___su";
  public static final String SHORTCUT_REMOVED_SUFFIX = "___sr";
  public static final String MESSAGE_CREATED_SUFFIX = "___mc";

  public static Intent getCallingIntent(Context context, String type, String usersFromatedIds) {
    Intent intent = new Intent(context, WSService.class);
    intent.putExtra(CHAT_IDS, usersFromatedIds);
    intent.putExtra(TYPE, type);
    return intent;
  }

  public static Intent getCallingIntentSubscribeRoom(Context context, String roomId) {
    Intent intent = new Intent(context, WSService.class);
    intent.putExtra(TYPE, CALL_ROOM_UPDATE_SUBSCRIBE_TYPE);
    intent.putExtra(ROOM_ID, roomId);
    return intent;
  }

  public static Intent getCallingIntentUnsubscribeRoom(Context context) {
    Intent intent = new Intent(context, WSService.class);
    intent.putExtra(TYPE, CALL_ROOM_UPDATE_UNSUBSCRIBE_TYPE);
    return intent;
  }

  @Inject User user;

  @Inject TribeApi tribeApi;

  @Inject UserCache userCache;

  @Inject LiveCache liveCache;

  @Inject ChatCache chatCache;

  @Inject AccessToken accessToken;

  @Inject JsonToModel jsonToModel;

  @Inject @Named("webSocketApi") WebSocketConnection webSocketConnection;

  // VARIABLES
  private Map<String, String> headers;
  private @WebSocketConnection.WebSocketState String webSocketState = WebSocketConnection.STATE_NEW;
  private boolean hasSubscribed = false;
  private String lastRoomSubscriptionId;

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

    String req = getApplicationContext().getString(R.string.subscription,
        getApplicationContext().getString(R.string.subscription_randomRoomAssigned,
            hash + RANDOM_ROOM_ASSIGNED_SUFFIX));

    webSocketConnection.send(req);
  }

  public void subscribeChat(String userIds) {
    String req = getApplicationContext().getString(R.string.subscription,
        getApplicationContext().getString(R.string.subscription_messageCreated,
            MESSAGE_CREATED_SUFFIX, userIds));

    webSocketConnection.send(req);
  }

  public void subscribeRoomUpdate(String roomId) {
    lastRoomSubscriptionId = generateHash() + ROOM_UDPATED_SUFFIX;

    String req = getApplicationContext().getString(R.string.subscription,
        getApplicationContext().getString(R.string.subscription_roomUpdated, lastRoomSubscriptionId,
            roomId));

    webSocketConnection.send(req);
  }

  public void unsubscribeRoomUpdate() {
    String req = getApplicationContext().getString(R.string.subscription,
        getApplicationContext().getString(R.string.subscription_remove, lastRoomSubscriptionId));

    lastRoomSubscriptionId = null;

    webSocketConnection.send(req);
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null) {
      String type = intent.getStringExtra(TYPE);
      if (!StringUtils.isEmpty(type)) {
        if (type.equals(CALL_ROULETTE_TYPE)) {
          subscribeChatRoulette();
        } else if (type.equals(CALL_ROOM_UPDATE_SUBSCRIBE_TYPE)) {
          subscribeRoomUpdate(intent.getStringExtra(ROOM_ID));
        } else if (type.equals(CALL_ROOM_UPDATE_UNSUBSCRIBE_TYPE)) {
          unsubscribeRoomUpdate();
        } else if (type.equals(CHAT_SUBSCRIBE)) {
          String usersFromatedIds = intent.getStringExtra(CHAT_IDS);
          subscribeChat(usersFromatedIds);
        }
      }
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
    if (accessToken.isAnonymous()
        || StringUtils.isEmpty(accessToken.getTokenType())
        || StringUtils.isEmpty(accessToken.getAccessToken())) {

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
        // we wait to have a correct response with the live_users array
        // in the room correctly updated
        .delay(2000, TimeUnit.MILLISECONDS)
        .subscribeOn(Schedulers.from(Executors.newSingleThreadExecutor()))
        .filter(invite -> !liveCache.getInviteMap().containsKey(invite.getId()))
        .flatMap(invite -> this.tribeApi.invites(
            getApplicationContext().getString(R.string.invites_infos,
                getApplicationContext().getString(R.string.userfragment_infos),
                getApplicationContext().getString(R.string.roomFragment_infos))),
            (invite, invites) -> {
              if (invites != null) {
                for (Invite newInvite : invites) {
                  boolean shouldAdd = true;

                  if (newInvite.getRoom() == null) shouldAdd = false;

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
        .subscribe(invite -> liveCache.removeInvite(invite)));

    persistentSubscriptions.add(jsonToModel.onAddedLive().subscribe(s -> liveCache.putLive(s)));

    persistentSubscriptions.add(
        jsonToModel.onRemovedLive().subscribe(s -> liveCache.removeLive(s)));

    persistentSubscriptions.add(jsonToModel.onAddedOnline().subscribe(s -> liveCache.putOnline(s)));

    persistentSubscriptions.add(
        jsonToModel.onRemovedOnline().subscribe(s -> liveCache.removeOnline(s)));

    persistentSubscriptions.add(jsonToModel.onRandomRoomAssigned().subscribe(assignedRoomId -> {
      Timber.d("onRandomRoomAssigned assignedRoomId " + assignedRoomId);
      liveCache.putRandomRoomAssigned(assignedRoomId);
    }));

    persistentSubscriptions.add(jsonToModel.onMessageCreated().subscribe(messagRealm -> {

      RealmList<MessageRealm> messages = new RealmList<>();
      messages.add(messagRealm);
 /*     String[] array = new String[1] {messages.get()};
  *//*   ;
      ;*/
      chatCache.putMessages(messages, "\"ByFIVM7oW\"");


    }));

    persistentSubscriptions.add(
        jsonToModel.onFbIdUpdated().subscribe(userUpdated -> liveCache.onFbIdUpdated(userUpdated)));

    persistentSubscriptions.add(
        jsonToModel.onRoomUpdated().subscribe(room -> liveCache.onRoomUpdated(room)));

    persistentSubscriptions.add(jsonToModel.onUserListUpdated()
        .subscribe(userRealmList -> userCache.updateUserRealmList(userRealmList)));

    persistentSubscriptions.add(jsonToModel.onShortcutCreated()
        .subscribe(shortcutRealm -> userCache.addShortcut(shortcutRealm)));

    persistentSubscriptions.add(jsonToModel.onShortcutUpdated()
        .subscribe(shortcutRealm -> userCache.updateShortcut(shortcutRealm)));

    persistentSubscriptions.add(jsonToModel.onShortcutRemoved()
        .subscribe(shortcutRealm -> userCache.removeShortcut(shortcutRealm)));
  }

  private void initSubscriptions() {
    UserRealm userRealm = userCache.userInfosNoObs(accessToken.getUserId());

    String hash = generateHash();

    sendSubscription(getApplicationContext().getString(R.string.subscription_inviteCreated,
        hash + INVITE_CREATED_SUFFIX));

    sendSubscription(getApplicationContext().getString(R.string.subscription_inviteRemoved,
        hash + INVITE_REMOVED_SUFFIX));

    sendSubscription(getApplicationContext().getString(R.string.subscription_shortcutCreated,
        hash + SHORTCUT_CREATED_SUFFIX));

    sendSubscription(getApplicationContext().getString(R.string.subscription_shortcutUpdated,
        hash + SHORTCUT_UPDATED_SUFFIX));

    sendSubscription(getApplicationContext().getString(R.string.subscription_shortcutRemoved,
        hash + SHORTCUT_REMOVED_SUFFIX));

    tempSubscriptions.add(Observable.just(userRealm.getShortcuts()).doOnNext(shortcutList -> {
      int count = 0;

      for (ShortcutRealm shortcutRealm : shortcutList) {
        if (shortcutRealm.isSingle()) {
          sendSubscription(getApplicationContext().getString(R.string.subscription_userUpdated,
              hash + USER_SUFFIX + count, shortcutRealm.getSingleFriend().getId()));
        }

        count++;
      }
    }).subscribe());
  }

  private void sendSubscription(String body) {
    String userInfosFragment =
        (body.contains("UserInfos") ? "\n" + getApplicationContext().getString(
            R.string.userfragment_infos) : "");

    String req = getApplicationContext().getString(R.string.subscription, body) + userInfosFragment;

    webSocketConnection.send(req);
  }

  private String generateHash() {
    return "H" + UUID.randomUUID().toString().toUpperCase().replace("-", "");
  }

  private void initDependencyInjection() {
    ((AndroidApplication) getApplication()).getApplicationComponent().inject(this);
  }
}