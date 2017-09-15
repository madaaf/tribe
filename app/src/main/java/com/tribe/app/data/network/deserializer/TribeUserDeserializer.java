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
import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.Invite;
import io.realm.RealmList;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class TribeUserDeserializer implements JsonDeserializer<UserRealm> {

  private SimpleDateFormat simpleDateFormat;

  public TribeUserDeserializer(SimpleDateFormat simpleDateFormat) {
    this.simpleDateFormat = simpleDateFormat;
  }

  @Override
  public UserRealm deserialize(JsonElement je, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    Gson gson = new GsonBuilder().registerTypeAdapter(new TypeToken<RealmList<UserRealm>>() {
    }.getType(), new UserRealmListDeserializer()).create();

    UserRealm userRealm = new UserRealm();
    JsonObject result = null;

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
    userRealm.setInvisibleMode(result.get("invisible_mode").getAsBoolean());
    userRealm.setUsername(
        result.get("username") != null && !result.get("username").isJsonNull() ? result.get(
            "username").getAsString() : null);
    userRealm.setDisplayName(result.get("display_name").getAsString());
    userRealm.setTimeInCall(result.get("time_in_call").getAsLong());
    userRealm.setProfilePicture(
        result.get("picture") != null && !result.get("picture").isJsonNull() ? result.get("picture")
            .getAsString() : null);
    userRealm.setPushNotif(result.get("push_notif").getAsBoolean());

    try {
      userRealm.setCreatedAt(simpleDateFormat.parse(result.get("created_at").getAsString()));
    } catch (ParseException e) {
      e.printStackTrace();
    }

    try {
      userRealm.setLastSeenAt(simpleDateFormat.parse(result.get("last_seen_at").getAsString()));
    } catch (ParseException e) {
      e.printStackTrace();
    }

    RealmList<ShortcutRealm> listShortcuts = new RealmList<>();

    if (result.has("shortcuts") && !(result.get("shortcuts") instanceof JsonNull)) {
      JsonObject resultsShortcuts = result.getAsJsonObject("shortcuts");
      if (resultsShortcuts != null) {
        manageShortcuts(gson, resultsShortcuts, listShortcuts, "recent");
        manageShortcuts(gson, resultsShortcuts, listShortcuts, "unread");
        manageShortcuts(gson, resultsShortcuts, listShortcuts, "online");
        manageShortcuts(gson, resultsShortcuts, listShortcuts, "blocked");
        manageShortcuts(gson, resultsShortcuts, listShortcuts, "hidden");
      }

      userRealm.setShortcuts(listShortcuts);
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

  private void manageShortcuts(Gson gson, JsonObject jsonObj,
      RealmList<ShortcutRealm> listShortcuts, String category) {
    if (jsonObj.has(category) && !(jsonObj.get(category) instanceof JsonNull)) {
      JsonArray shortcutsOnline = jsonObj.getAsJsonArray(category);
      for (JsonElement obj : shortcutsOnline) {
        if (!(obj instanceof JsonNull)) {
          ShortcutRealm shortcut = gson.fromJson(obj, ShortcutRealm.class);
          if (category.equals("online")) shortcut.setOnline(true);
          if (shortcut != null) listShortcuts.add(shortcut);
        }
      }
    }
  }
}