package com.tribe.app.data.network.deserializer;

import android.util.Pair;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.tribe.app.data.network.WSService;
import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.data.realm.UserPlayingRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.data.realm.mapper.MessageRealmDataMapper;
import com.tribe.app.data.realm.mapper.UserRealmDataMapper;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.utils.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import rx.Observable;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Created by tiago on 27/01/2017.
 */

@Singleton public class JsonToModel {

  // VARIABLES
  private Gson gson;

  // OBSERABLES
  private PublishSubject<List<UserRealm>> onUserListUpdated = PublishSubject.create();
  private PublishSubject<String> onAddedOnline = PublishSubject.create();
  private PublishSubject<User> onFbIdUpdated = PublishSubject.create();
  private PublishSubject<String> onRemovedOnline = PublishSubject.create();
  private PublishSubject<String> onAddedLive = PublishSubject.create();
  private PublishSubject<String> onRemovedLive = PublishSubject.create();
  private PublishSubject<Invite> onInviteCreated = PublishSubject.create();
  private PublishSubject<Invite> onInviteRemoved = PublishSubject.create();
  private PublishSubject<String> onRandomRoomAssigned = PublishSubject.create();
  private PublishSubject<MessageRealm> onMessageCreated = PublishSubject.create();
  private PublishSubject<MessageRealm> onMessageRemoved = PublishSubject.create();
  private PublishSubject<Room> onRoomUpdated = PublishSubject.create();
  private PublishSubject<ShortcutRealm> onShortcutCreated = PublishSubject.create();
  private PublishSubject<ShortcutRealm> onShortcutUpdated = PublishSubject.create();
  private PublishSubject<String> onShortcutRemoved = PublishSubject.create();
  private PublishSubject<String> onTyping = PublishSubject.create();
  private PublishSubject<String> onTalking = PublishSubject.create();
  private PublishSubject<String> onReading = PublishSubject.create();
  private PublishSubject<String> onRandomBannedUntil = PublishSubject.create();
  private PublishSubject<Pair<String, UserPlayingRealm>> onAddedPlaying = PublishSubject.create();
  private PublishSubject<String> onRemovedPlaying = PublishSubject.create();

  @Inject MessageRealmDataMapper messageRealmDataMapper;

  @Inject UserRealmDataMapper userRealmDataMapper;

  @Inject User user;

  @Inject public JsonToModel(@Named("simpleGson") Gson gson) {
    this.gson = gson;
  }

  public void convertToSubscriptionResponse(String json) {
    if (!StringUtils.isEmpty(json) && json.contains("data")) {
      JsonElement jsonElement = gson.fromJson(json, JsonElement.class);
      JsonObject jsonObject = jsonElement.getAsJsonObject();
      JsonObject results = jsonObject.getAsJsonObject("data");

      List<UserRealm> updatedUserList = new ArrayList<>();

      boolean shouldBulkLiveStatus = results.entrySet().size() > 5;

      for (Map.Entry<String, JsonElement> entry : results.entrySet()) {
        if (!entry.getValue().isJsonNull()) {
          String payload = entry.getValue().toString();
          JsonParser jsonParser = new JsonParser();
          JsonElement element = jsonParser.parse(payload);

          if (element != null && element.isJsonObject()) {
            JsonObject jo = element.getAsJsonObject();

            if (entry.getKey().contains(WSService.USER_SUFFIX)) {
              boolean shouldUpdateOnlineStatus = false, shouldUpdatePlayingStatus = false;

              UserRealm userRealm = gson.fromJson(entry.getValue().toString(), UserRealm.class);
              userRealm.setJsonPayloadUpdate(jo);

              if (jo.has("random_banned_until") && !jo.get("random_banned_until").isJsonNull()) {
                String date = jo.get("random_banned_until").getAsString();
                onRandomBannedUntil.onNext(date);
              }

              if (jo.has("is_online")) shouldUpdateOnlineStatus = true;
              if (jo.has("is_playing")) shouldUpdatePlayingStatus = true;
              if (jo.has("fbid") && !jo.get("fbid").isJsonNull()) {
                String fbId = jo.get("fbid").getAsString();
                User user = new User(jo.get("id").getAsString());
                user.setFbid(fbId);
                onFbIdUpdated.onNext(user);
              }

              if (shouldUpdateOnlineStatus) {
                if (userRealm.isOnline()) {
                  onAddedOnline.onNext(userRealm.getId());
                } else {
                  onRemovedOnline.onNext(userRealm.getId());
                }
              }

              if (shouldUpdatePlayingStatus) {
                if (userRealm.isPlaying() != null && !StringUtils.isEmpty(
                    userRealm.isPlaying().getGame_id())) {
                  onAddedPlaying.onNext(Pair.create(userRealm.getId(),
                      gson.fromJson(jo.get("is_playing"), UserPlayingRealm.class)));
                } else {
                  onRemovedPlaying.onNext(userRealm.getId());
                }
              }

              updatedUserList.add(userRealm);
            } else if (entry.getKey().contains(WSService.INVITE_CREATED_SUFFIX)) {
              Timber.d("Invite created : " + entry.getValue().toString());
              Invite invite = gson.fromJson(entry.getValue().toString(), Invite.class);
              onInviteCreated.onNext(invite);
            } else if (entry.getKey().contains(WSService.INVITE_REMOVED_SUFFIX)) {
              Timber.d("Invite removed : " + entry.getValue().toString());
              Invite invite = gson.fromJson(entry.getValue().toString(), Invite.class);
              onInviteRemoved.onNext(invite);
            } else if (entry.getKey().contains(WSService.RANDOM_ROOM_ASSIGNED_SUFFIX)) {
              Timber.d("onRandomRoomAssigned : " + entry.getValue().toString());
              onRandomRoomAssigned.onNext(
                  entry.getValue().getAsJsonObject().get("assignedRoomId").getAsString());
            } else if (entry.getKey().contains(WSService.MESSAGE_IS_TYPING_SUFFIX)) {
              String user_id = jo.get("user_id").getAsString();
              onTyping.onNext(user_id);
            } else if (entry.getKey().contains(WSService.MESSAGE_IS_TALKING_SUFFIX)) {
              String user_id = jo.get("user_id").getAsString();
              if (!user_id.equals(user.getId())) onTalking.onNext(user_id);
            } else if (entry.getKey().contains(WSService.MESSAGE_IS_READING_SUFFIX)) {
              String user_id = jo.get("user_id").getAsString();
              if (!user_id.equals(user.getId())) onReading.onNext(user_id);
            } else if (entry.getKey().contains(WSService.MESSAGE_IS_REMOVED_SUFFIX)) {
              Timber.d("setOnMessageRemoved: " + entry.getValue().toString());
              onMessageRemoved.onNext(getMessage(entry, jo));
            } else if (entry.getKey().contains(WSService.MESSAGE_CREATED_SUFFIX)) {
              Timber.d("onMessageReceived : " + entry.getValue().toString());
              onMessageCreated.onNext(getMessage(entry, jo));
            } else if (entry.getKey().contains(WSService.ROOM_UDPATED_SUFFIX)) {
              Timber.d("onRoomUpdate : " + entry.getValue().toString());
              JsonObject roomJson = entry.getValue().getAsJsonObject();
              Room room = new Room(roomJson.get("id").getAsString());

              String roomName =
                  (!roomJson.get("name").isJsonNull()) ? roomJson.get("name").getAsString() : null;
              room.setName(roomName);

              Boolean accept_random =
                  !roomJson.get("accept_random").isJsonNull() && roomJson.get("accept_random")
                      .getAsBoolean();
              room.setAcceptRandom(accept_random);

              JsonArray live_users_json =
                  (roomJson.get("live_users").isJsonNull()) ? new JsonArray()
                      : roomJson.get("live_users").getAsJsonArray();

              JsonArray invited_users_json =
                  (roomJson.get("invited_users").isJsonNull()) ? new JsonArray()
                      : roomJson.get("invited_users").getAsJsonArray();

              if (roomJson.has("initiator")) {
                try {
                  UserRealm initiator = gson.fromJson(roomJson.get("initiator"), UserRealm.class);
                  if (initiator != null) {
                    room.setInitiator(userRealmDataMapper.transform(initiator, true));
                  }
                } catch (Exception ex) {
                  ex.printStackTrace();
                }
              }

              if (live_users_json.size() > 0) {
                List<User> live_users;
                try {
                  live_users =
                      gson.fromJson(live_users_json.toString(), new TypeToken<List<User>>() {
                      }.getType());
                } catch (Exception ex) {
                  ex.printStackTrace();

                  List<String> live_users_ids =
                      gson.fromJson(live_users_json.toString(), new TypeToken<List<String>>() {
                      }.getType());

                  live_users = new ArrayList<>();

                  for (String id : live_users_ids) {
                    live_users.add(new User(id));
                  }
                }

                Timber.d("Live_users : " + live_users);
                room.setLiveUsers(live_users);
              }

              if (invited_users_json.size() > 0) {
                List<User> invited_users;
                try {
                  invited_users =
                      gson.fromJson(invited_users_json.toString(), new TypeToken<List<User>>() {
                      }.getType());
                } catch (Exception ex) {
                  ex.printStackTrace();

                  List<String> invited_users_ids =
                      gson.fromJson(live_users_json.toString(), new TypeToken<List<String>>() {
                      }.getType());

                  invited_users = new ArrayList<>();

                  for (String id : invited_users_ids) {
                    invited_users.add(new User(id));
                  }
                }

                Timber.d("invited_users : " + invited_users);
                room.setInvitedUsers(invited_users);
              }

              onRoomUpdated.onNext(room);
            } else if (entry.getKey().contains(WSService.SHORTCUT_CREATED_SUFFIX)) {
              Timber.d("Shortcut created : " + entry.getValue().toString());
              ShortcutRealm shortcutRealm =
                  gson.fromJson(entry.getValue().toString(), ShortcutRealm.class);
              onShortcutCreated.onNext(shortcutRealm);
            } else if (entry.getKey().contains(WSService.SHORTCUT_UPDATED_SUFFIX)) {
              Timber.d("Shortcut updated : " + entry.getValue().toString());
              ShortcutRealm shortcutRealm =
                  gson.fromJson(entry.getValue().toString(), ShortcutRealm.class);
              onShortcutUpdated.onNext(shortcutRealm);
            } else if (entry.getKey().contains(WSService.SHORTCUT_REMOVED_SUFFIX)) {
              Timber.d("Shortcut removed : " + entry.getValue().toString());
              if (!entry.getValue().getAsJsonObject().get("shortcut_id").isJsonNull()) {
                String shortcutId =
                    entry.getValue().getAsJsonObject().get("shortcut_id").getAsString();
                onShortcutRemoved.onNext(shortcutId);
              }
            }
          }
        }
      }

      if (updatedUserList.size() > 0) onUserListUpdated.onNext(updatedUserList);
    }
  }

  private MessageRealm getMessage(Map.Entry<String, JsonElement> entry, JsonObject jo) {
    MessageRealm messageRealm = gson.fromJson(entry.getValue().toString(), MessageRealm.class);

    UserRealm userRealm =
        gson.fromJson(entry.getValue().getAsJsonObject().get("user"), UserRealm.class);
    if (userRealm != null) {
      messageRealm.setUser(userRealm);
    }
    JsonArray jsonElements = jo.get("thread_id").getAsJsonArray();

    ArrayList<String> list = new ArrayList<>();
    for (int i = 0; i < jsonElements.size(); i++) {
      String el = jsonElements.get(i).getAsString();
      if (!el.equals(user.getId())) list.add(el);
    }
    String formatedString = list.toString().replace("[", "\"").replace("]", "\"");
    messageRealm.setThreadId(formatedString);
    return messageRealm;
  }

  public Observable<List<UserRealm>> onUserListUpdated() {
    return onUserListUpdated;
  }

  public Observable<String> onAddedOnline() {
    return onAddedOnline;
  }

  public Observable<String> onRandomRoomAssigned() {
    return onRandomRoomAssigned;
  }

  public Observable<MessageRealm> onMessageRemoved() {
    return onMessageRemoved;
  }

  public Observable<MessageRealm> onMessageCreated() {
    return onMessageCreated;
  }

  public Observable<String> onRandomBannedUntil() {
    return onRandomBannedUntil;
  }

  public Observable<User> onFbIdUpdated() {
    return onFbIdUpdated;
  }

  public Observable<String> onRemovedOnline() {
    return onRemovedOnline;
  }

  public Observable<String> onAddedLive() {
    return onAddedLive;
  }

  public Observable<String> onRemovedLive() {
    return onRemovedLive;
  }

  public Observable<Invite> onInviteCreated() {
    return onInviteCreated;
  }

  public Observable<Invite> onInviteRemoved() {
    return onInviteRemoved;
  }

  public Observable<Room> onRoomUpdated() {
    return onRoomUpdated;
  }

  public Observable<ShortcutRealm> onShortcutCreated() {
    return onShortcutCreated;
  }

  public Observable<ShortcutRealm> onShortcutUpdated() {
    return onShortcutUpdated;
  }

  public Observable<String> onShortcutRemoved() {
    return onShortcutRemoved;
  }

  public Observable<String> onTyping() {
    return onTyping;
  }

  public Observable<String> onTalking() {
    return onTalking;
  }

  public Observable<String> onReading() {
    return onReading;
  }

  public Observable<String> onRemovedPlaying() {
    return onRemovedPlaying;
  }

  public Observable<Pair<String, UserPlayingRealm>> onAddedPlaying() {
    return onAddedPlaying;
  }
}
