package com.tribe.app.data.network.deserializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.Invite;
import io.realm.RealmList;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TribeUserDeserializer implements JsonDeserializer<UserRealm> {

  private SimpleDateFormat simpleDateFormat;
  private Set<String> shortcutSet;

  public TribeUserDeserializer(SimpleDateFormat simpleDateFormat) {
    this.simpleDateFormat = simpleDateFormat;
    this.shortcutSet = new HashSet<>();
  }

  @Override
  public UserRealm deserialize(JsonElement je, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    Gson gson = new GsonBuilder().registerTypeAdapter(new TypeToken<RealmList<UserRealm>>() {
    }.getType(), new UserRealmListDeserializer()).create();

    UserRealm userRealm = new UserRealm();
    JsonObject result = null;

    shortcutSet.clear();

    if (je.getAsJsonObject().getAsJsonObject("data").has("user")) {
      if (!je.getAsJsonObject().getAsJsonObject("data").get("user").isJsonNull()) {
        result = je.getAsJsonObject().getAsJsonObject("data").getAsJsonObject("user");
      } else {
        return userRealm; // anonymous user
      }
    } else if (je.getAsJsonObject().getAsJsonObject("data").has("updateUser")) {
      result = je.getAsJsonObject().getAsJsonObject("data").getAsJsonObject("updateUser");
    }

    userRealm.setId(result.get("id").getAsString());
    userRealm.setPhone(
        result.get("phone") != null && !result.get("phone").isJsonNull() ? result.get("phone")
            .getAsString() : "");
    userRealm.setFbid(
        result.get("fbid") != null && !result.get("fbid").isJsonNull() ? result.get("fbid")
            .getAsString() : "");
    if (result.get("invisible_mode") != null) {
      userRealm.setInvisibleMode(result.get("invisible_mode").getAsBoolean());
    }
    userRealm.setUsername(
        result.get("username") != null && !result.get("username").isJsonNull() ? result.get(
            "username").getAsString() : null);
    if (result.get("display_name") != null) {
      userRealm.setDisplayName(result.get("display_name").getAsString());
    }
    if (result.get("time_in_call") != null) {
      userRealm.setTimeInCall(result.get("time_in_call").getAsLong());
    }
    userRealm.setProfilePicture(
        result.get("picture") != null && !result.get("picture").isJsonNull() ? result.get("picture")
            .getAsString() : null);
    if (result.get("push_notif") != null) {
      userRealm.setPushNotif(result.get("push_notif").getAsBoolean());
    }

    if (result.get("mute_online_notif") != null) {
      userRealm.setMute_online_notif(result.get("mute_online_notif").getAsBoolean());
    }

    try {
      if (result.get("created_at") != null) {
        userRealm.setCreatedAt(simpleDateFormat.parse(result.get("created_at").getAsString()));
      }
    } catch (ParseException e) {
      e.printStackTrace();
    }

    try {
      if (result.get("last_seen_at") != null) {
        userRealm.setLastSeenAt(simpleDateFormat.parse(result.get("last_seen_at").getAsString()));
        if (!result.get("random_banned_until").isJsonNull()) {
          userRealm.setRandom_banned_until(
              simpleDateFormat.parse(result.get("random_banned_until").getAsString()));
        }
        userRealm.setRandom_banned_permanently(
            result.get("random_banned_permanently").getAsBoolean());
      }
    } catch (ParseException e) {
      e.printStackTrace();
    }

    List<ShortcutRealm> listShortcuts = new ArrayList<>();

    if (result.has("shortcuts") && !(result.get("shortcuts") instanceof JsonNull)) {
      JsonObject resultsShortcuts = result.getAsJsonObject("shortcuts");
      if (resultsShortcuts != null) {
        manageShortcuts(gson, resultsShortcuts, listShortcuts, "unread");
        manageShortcuts(gson, resultsShortcuts, listShortcuts, "online");
        manageShortcuts(gson, resultsShortcuts, listShortcuts, "single");
        manageShortcuts(gson, resultsShortcuts, listShortcuts, "recent");
        manageShortcuts(gson, resultsShortcuts, listShortcuts, "blocked");
        manageShortcuts(gson, resultsShortcuts, listShortcuts, "hidden");
      }

      userRealm.setShortcuts(listShortcuts);
    }

    if (result.has("messages") && !(result.get("messages") instanceof JsonNull)) {
      JsonArray resultsMessages = result.getAsJsonArray("messages");
      RealmList<MessageRealm> realmListMessages = new RealmList();
      if (resultsMessages != null) {
        for (JsonElement obj : resultsMessages) {
          if (!(obj instanceof JsonNull)) {
            MessageRealm messageRealm = gson.fromJson(obj, MessageRealm.class);
            if (messageRealm != null) realmListMessages.add(messageRealm);
          }
        }

        userRealm.setMessages(realmListMessages);
      }
    }

    List<Invite> listInvites = new ArrayList<>();

    if (result.has("invites") && !(result.get("invites") instanceof JsonNull)) {
      JsonArray resultsInvites = result.getAsJsonArray("invites");
      if (resultsInvites != null) {
        for (JsonElement obj : resultsInvites) {
          if (!(obj instanceof JsonNull)) {
            Invite invite = gson.fromJson(obj, Invite.class);
            if (invite != null && invite.getRoom() != null) listInvites.add(invite);
          }
        }
      }

      userRealm.setInvites(listInvites);
    }

    return userRealm;
  }

  private void manageShortcuts(Gson gson, JsonObject jsonObj, List<ShortcutRealm> listShortcuts,
      String category) {
    if (jsonObj.has(category) && !(jsonObj.get(category) instanceof JsonNull)) {
      JsonArray shortcutsOnline = jsonObj.getAsJsonArray(category);
      for (JsonElement obj : shortcutsOnline) {
        if (!(obj instanceof JsonNull)) {
          ShortcutRealm shortcut = gson.fromJson(obj, ShortcutRealm.class);
          if (category.equals("online")) shortcut.setOnline(true);
          if (shortcut != null &&
              !shortcutSet.contains(shortcut.getId()) &&
              shortcut.getMembers() != null &&
              shortcut.getMembers().size() > 0) {
            listShortcuts.add(shortcut);
            shortcutSet.add(shortcut.getId());
          }
        }
      }
    }
  }
}