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
import com.tribe.app.data.realm.mapper.UserRealmDataMapper;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.tribelivesdk.back.WebSocketConnection;
import io.realm.RealmList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

@Singleton public class WSService extends Service {

  private static final int TIMER_IM_ONLINE = 30000; // 30 SECS
  private static final long TWENTY_FOUR_HOURS = 86400000;

  public static final String TYPE = "TYPE";
  public static final String ROOM_ID = "ROOM_ID";
  public static final String CHAT_IDS = "CHAT_IDS";

  public static final String CHAT_SUBSCRIBE = "CHAT_SUBSCRIBE";
  public static final String CHAT_SUBSCRIBE_IMTYPING = "CHAT_SUBSCRIBE_IMTYPING";
  public static final String CHAT_SUBSCRIBE_IMTALKING = "CHAT_SUBSCRIBE_IMTALKING";
  public static final String CHAT_SUBSCRIBE_IMREADING = "CHAT_SUBSCRIBE_IMREADING";
  public static final String CHAT_UNSUBSCRIBE = "CHAT_UNSUBSCRIBE";
  public static final String CALL_ROULETTE_TYPE = "CALL_ROULETTE_TYPE";
  public static final String CALL_ROOM_UPDATE_SUBSCRIBE_TYPE = "CALL_ROOM_UPDATE_TYPE";
  public static final String CALL_ROOM_UPDATE_UNSUBSCRIBE_TYPE =
      "CALL_ROOM_UPDATE_UNSUBSCRIBE_TYPE";
  public static final String CALL_ROOM_CANCEL_IM_ONLINE_TYPE = "CALL_ROOM_CANCEL_IM_ONLINE_TYPE";

  public static final String USER_SUFFIX = "___u";
  public static final String INVITE_CREATED_SUFFIX = "___ic";
  public static final String INVITE_REMOVED_SUFFIX = "___ir";
  public static final String RANDOM_ROOM_ASSIGNED_SUFFIX = "___ra";
  public static final String ROOM_UDPATED_SUFFIX = "___ru";
  public static final String SHORTCUT_CREATED_SUFFIX = "___sc";
  public static final String SHORTCUT_UPDATED_SUFFIX = "___su";
  public static final String SHORTCUT_REMOVED_SUFFIX = "___sr";
  public static final String MESSAGE_CREATED_SUFFIX = "___mc";
  public static final String MESSAGE_IS_TYPING_SUFFIX = "___mit";
  public static final String MESSAGE_IS_TALKING_SUFFIX = "___mtalking";
  public static final String MESSAGE_IS_READING_SUFFIX = "___mReading";

  public static Intent getCallingSubscribeChat(Context context, String type,
      String usersFromatedIds) {
    Intent intent = new Intent(context, WSService.class);
    intent.putExtra(CHAT_IDS, usersFromatedIds);
    intent.putExtra(TYPE, type);
    return intent;
  }

  public static Intent getCallingUnSubscribeChat(Context context, String usersFromatedIds) {
    Intent intent = new Intent(context, WSService.class);
    intent.putExtra(CHAT_IDS, usersFromatedIds);
    intent.putExtra(TYPE, CHAT_UNSUBSCRIBE);
    return intent;
  }

  public static Intent getCallingIntent(Context context, String type, String usersFromatedIds) {
    Intent intent = new Intent(context, WSService.class);
    intent.putExtra(TYPE, type);
    return intent;
  }

  public static Intent getCallingIntentSubscribeRoom(Context context, String roomId) {
    Intent intent = new Intent(context, WSService.class);
    intent.putExtra(TYPE, CALL_ROOM_UPDATE_SUBSCRIBE_TYPE);
    intent.putExtra(ROOM_ID, roomId);
    return intent;
  }

  public static Intent getCallingIntentUnsubscribeRoom(Context context, String roomId) {
    Intent intent = new Intent(context, WSService.class);
    intent.putExtra(TYPE, CALL_ROOM_UPDATE_UNSUBSCRIBE_TYPE);
    intent.putExtra(ROOM_ID, roomId);
    return intent;
  }

  @Inject User user;

  @Inject TribeApi tribeApi;

  @Inject UserCache userCache;

  @Inject LiveCache liveCache;

  @Inject ChatCache chatCache;

  @Inject AccessToken accessToken;

  @Inject JsonToModel jsonToModel;

  @Inject UserRealmDataMapper userRealmDataMapper;

  @Inject @Named("webSocketApi") WebSocketConnection webSocketConnection;

  // VARIABLES
  private Map<String, String> headers;
  private @WebSocketConnection.WebSocketState String webSocketState = WebSocketConnection.STATE_NEW;
  private boolean hasSubscribed = false;
  private Map<String, String> roomSubscriptions;
  private Map<String, String> chatSubscriptions;
  private Set<String> userSubscribed;

  // OBSERVABLES
  private CompositeSubscription persistentSubscriptions = new CompositeSubscription();
  private CompositeSubscription tempSubscriptions = new CompositeSubscription();

  @Nullable @Override public IBinder onBind(Intent intent) {
    return null;
  }

  @Override public void onCreate() {
    super.onCreate();

    headers = new HashMap<>();
    userSubscribed = new HashSet<>();
    roomSubscriptions = new HashMap<>();
    chatSubscriptions = new HashMap<>();

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
   /* if (chatSubscriptions.get(userIds + MESSAGE_CREATED_SUFFIX) != null
        && chatSubscriptions.get(userIds + MESSAGE_IS_TYPING_SUFFIX) != null
        && chatSubscriptions.get(userIds + MESSAGE_IS_TALKING_SUFFIX) != null
        && chatSubscriptions.get(userIds + MESSAGE_IS_READING_SUFFIX) != null) {
      Timber.i("SOEF already subscribe");
      return;
    }*/
    if (!chatSubscriptions.isEmpty()) {
      unsubscribeChat(userIds);
    }

    String suffix = generateHash() + MESSAGE_CREATED_SUFFIX;
    chatSubscriptions.put(userIds + MESSAGE_CREATED_SUFFIX, suffix);

    String req = getApplicationContext().getString(R.string.subscription,
        getApplicationContext().getString(R.string.subscription_messageCreated, suffix, userIds));

    Timber.i("SOEF " + req);
    webSocketConnection.send(req);

    String suffix2 = generateHash() + MESSAGE_IS_TYPING_SUFFIX;
    chatSubscriptions.put(userIds + MESSAGE_IS_TYPING_SUFFIX, suffix2);
    String req2 = getApplicationContext().getString(R.string.subscription,
        getApplicationContext().getString(R.string.subscription_isTyping, suffix2, userIds));

    Timber.i("SOEF " + req2);
    webSocketConnection.send(req2);

    String suffix3 = generateHash() + MESSAGE_IS_TALKING_SUFFIX;
    chatSubscriptions.put(userIds + MESSAGE_IS_TALKING_SUFFIX, suffix3);
    String req3 = getApplicationContext().getString(R.string.subscription,
        getApplicationContext().getString(R.string.subscription_isTalking, suffix3, userIds));
    Timber.i("SOEF " + req3);

    String suffix4 = generateHash() + MESSAGE_IS_READING_SUFFIX;
    chatSubscriptions.put(userIds + MESSAGE_IS_READING_SUFFIX, suffix4);
    String req4 = getApplicationContext().getString(R.string.subscription,
        getApplicationContext().getString(R.string.subscription_isReading, suffix4, userIds));

    Timber.i("SOEF " + req4);
    webSocketConnection.send(req4);
  }

  public void subscribeImTyping(String userIds) {
    String req = getApplicationContext().getString(R.string.mutation,
        getApplicationContext().getString(R.string.imTyping, userIds));
    Timber.i("SOEF " + req);
    webSocketConnection.send(req);
  }

  public void subscribeImTalking(String userIds) {
    String req = getApplicationContext().getString(R.string.mutation,
        getApplicationContext().getString(R.string.imTalking, userIds));
    Timber.i("SOEF " + req);
    webSocketConnection.send(req);
  }

  public void subscribeImReading(String userIds) {
    String req = getApplicationContext().getString(R.string.mutation,
        getApplicationContext().getString(R.string.imReading, userIds));
    Timber.i("SOEF " + req);
    webSocketConnection.send(req);
  }

  public void subscribeRoomUpdate(String roomId) {
    if (roomSubscriptions.containsKey(roomId)) return;

    String subscriptionId = generateHash() + ROOM_UDPATED_SUFFIX;
    roomSubscriptions.put(roomId, subscriptionId);

    String req = getApplicationContext().getString(R.string.subscription,
        getApplicationContext().getString(R.string.subscription_roomUpdated, subscriptionId,
            roomId));

    webSocketConnection.send(req);
  }

  public void unsubscribeRoomUpdate(String roomId) {
    String req = getApplicationContext().getString(R.string.subscription,
        getApplicationContext().getString(R.string.subscription_remove,
            roomSubscriptions.get(roomId)));

    roomSubscriptions.remove(roomId);

    webSocketConnection.send(req);
  }

  public void unsubscribeChat(String userIds) {
    Timber.i("unsubscibe chat " + chatSubscriptions.size());
    List<String> valueToRemove = new ArrayList<>();
    for (String key : chatSubscriptions.keySet()) {
      String req = getApplicationContext().getString(R.string.subscription,
          getApplicationContext().getString(R.string.subscription_remove,
              chatSubscriptions.get(key)));
      valueToRemove.add(key);
      webSocketConnection.send(req);
    }

    for (String key : valueToRemove) {
      chatSubscriptions.remove(key);
    }
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
          unsubscribeRoomUpdate(intent.getStringExtra(ROOM_ID));
        } else if (type.equals(CHAT_SUBSCRIBE)) {
          String usersFromatedIds = intent.getStringExtra(CHAT_IDS);
          subscribeChat(usersFromatedIds);
        } else if (type.equals(CHAT_SUBSCRIBE_IMTYPING)) {
          String usersFromatedIds = intent.getStringExtra(CHAT_IDS);
          subscribeImTyping(usersFromatedIds);
        } else if (type.equals(CHAT_SUBSCRIBE_IMTALKING)) {
          String usersFromatedIds = intent.getStringExtra(CHAT_IDS);
          subscribeImTalking(usersFromatedIds);
        } else if (type.equals(CHAT_SUBSCRIBE_IMREADING)) {
          String usersFromatedIds = intent.getStringExtra(CHAT_IDS);
          subscribeImReading(usersFromatedIds);
        } else if (type.equals(CHAT_UNSUBSCRIBE)) {
          String usersFromatedIds = intent.getStringExtra(CHAT_IDS);
          unsubscribeChat(usersFromatedIds);
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

    userSubscribed.clear();
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
                    List<String> roomUserIds = newInvite.getRoom().getUserIds();
                    roomUserIds.remove(user.getId());
                    Shortcut shortcut = null;

                    ShortcutRealm shortcutRealm = userCache.shortcutForUserIdsNoObs(
                        roomUserIds.toArray(new String[roomUserIds.size()]));

                    if (shortcutRealm != null) {
                      shortcut =
                          userRealmDataMapper.getShortcutRealmDataMapper().transform(shortcutRealm);
                      newInvite.setShortcut(shortcut);
                      liveCache.putLive(shortcut.getId());
                    }

                    subscribeRoomUpdate(newInvite.getRoom().getId());

                    for (User user : newInvite.getRoom().getLiveUsers()) {
                      liveCache.putLive(user.getId());
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
          //if (invite.getRoom() != null) unsubscribeRoomUpdate(invite.getRoom().getId());
          liveCache.removeInvite(invite);
        }));

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
      if (!messagRealm.getAuthor().getId().equals(user.getId())) {
        chatCache.setOnMessageReceived(messages);
      }
      chatCache.putMessages(messages, messagRealm.getThreadId());
    }));

    persistentSubscriptions.add(jsonToModel.onTyping().subscribe(userID -> {
      chatCache.onTyping(userID);
    }));

    persistentSubscriptions.add(jsonToModel.onTalking().subscribe(userID -> {
      chatCache.onTalking(userID);
    }));

    persistentSubscriptions.add(jsonToModel.onReading().subscribe(userID -> {
      chatCache.onReading(userID);
    }));

    persistentSubscriptions.add(
        jsonToModel.onFbIdUpdated().subscribe(userUpdated -> liveCache.onFbIdUpdated(userUpdated)));

    persistentSubscriptions.add(
        jsonToModel.onRoomUpdated().observeOn(AndroidSchedulers.mainThread()).subscribe(room -> {
          Timber.e("PUT ROOM UPDATED ON CASH " + room.getId());
          liveCache.onRoomUpdated(room);
        }));

    persistentSubscriptions.add(jsonToModel.onUserListUpdated()
        .subscribe(userRealmList -> userCache.updateUserRealmList(userRealmList)));

    persistentSubscriptions.add(jsonToModel.onShortcutCreated().subscribe(shortcutRealm -> {
      for (UserRealm userRealm : shortcutRealm.getMembers()) {
        if (!userSubscribed.contains(userRealm.getId())) {
          sendSubscription(getApplicationContext().getString(R.string.subscription_userUpdated,
              generateHash() + USER_SUFFIX + userSubscribed.size(), user.getId()));
        }
      }

      userCache.addShortcut(shortcutRealm);
    }));

    persistentSubscriptions.add(jsonToModel.onShortcutUpdated().subscribe(shortcutRealm -> {
      if (!shortcutRealm.isOnline()) {
        liveCache.removeOnline(shortcutRealm.getId());
      } else if (shortcutRealm.isOnline()) liveCache.putOnline(shortcutRealm.getId());
      userCache.updateShortcut(shortcutRealm);
    }));

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
        for (UserRealm user : shortcutRealm.getMembers()) {
          if (!userSubscribed.contains(user.getId())) {
            sendSubscription(getApplicationContext().getString(R.string.subscription_userUpdated,
                hash + USER_SUFFIX + count, user.getId()));

            userSubscribed.add(user.getId());
            count++;
          }
        }
      }
    }).subscribe());

    persistentSubscriptions.add(liveCache.inviteMap().subscribe(stringInviteMap -> {
      for (Invite invite : stringInviteMap.values()) {
        subscribeRoomUpdate(invite.getRoom().getId());
      }
    }));
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