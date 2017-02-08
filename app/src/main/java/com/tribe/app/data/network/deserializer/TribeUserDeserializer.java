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
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.GroupRealm;
import com.tribe.app.data.realm.MembershipRealm;
import com.tribe.app.data.realm.UserRealm;
import io.realm.RealmList;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class TribeUserDeserializer implements JsonDeserializer<UserRealm> {

  private GroupDeserializer groupDeserializer;
  private SimpleDateFormat simpleDateFormat;

  public TribeUserDeserializer(GroupDeserializer groupDeserializer,
      SimpleDateFormat simpleDateFormat) {
    this.groupDeserializer = groupDeserializer;
    this.simpleDateFormat = simpleDateFormat;
  }

  @Override
  public UserRealm deserialize(JsonElement je, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    Gson gson = new GsonBuilder().registerTypeAdapter(new TypeToken<RealmList<UserRealm>>() {
    }.getType(), new UserRealmListDeserializer()).create();

    UserRealm userRealm = new UserRealm();

    JsonObject result = je.getAsJsonObject().getAsJsonObject("data").getAsJsonObject("user");
    if (result == null) {
      result = je.getAsJsonObject().getAsJsonObject("data").getAsJsonObject("updateUser");
    }

    userRealm.setId(result.get("id").getAsString());
    userRealm.setPhone(result.get("phone").getAsString());
    userRealm.setFbid(
        result.get("fbid") != null && !result.get("fbid").isJsonNull() ? result.get("fbid")
            .getAsString() : "");
    userRealm.setInvisibleMode(result.get("invisible_mode").getAsBoolean());
    userRealm.setUsername(
        result.get("username") != null && !result.get("username").isJsonNull() ? result.get(
            "username").getAsString() : null);
    userRealm.setDisplayName(result.get("display_name").getAsString());
    userRealm.setProfilePicture(
        result.get("picture") != null && !result.get("picture").isJsonNull() ? result.get("picture")
            .getAsString() : null);
    userRealm.setPushNotif(result.get("push_notif").getAsBoolean());

    try {
      userRealm.setCreatedAt(simpleDateFormat.parse(result.get("created_at").getAsString()));
      userRealm.setLastSeenAt(simpleDateFormat.parse(result.get("last_seen_at").getAsString()));
    } catch (ParseException e) {
      e.printStackTrace();
    }

    JsonArray resultsFriendships = result.getAsJsonArray("friendships");
    JsonArray resultsMemberships = result.getAsJsonArray("memberships");
    RealmList<FriendshipRealm> realmListFriendships = new RealmList();
    RealmList<MembershipRealm> realmListMemberships = new RealmList();

    if (resultsFriendships != null) {
      for (JsonElement obj : resultsFriendships) {
        if (!(obj instanceof JsonNull)) {
          FriendshipRealm friendshipRealm = gson.fromJson(obj, FriendshipRealm.class);
          realmListFriendships.add(friendshipRealm);
        }
      }
    }

    if (resultsMemberships != null) {
      for (JsonElement obj : resultsMemberships) {
        if (!(obj instanceof JsonNull)) {
          MembershipRealm membershipRealm = gson.fromJson(obj, MembershipRealm.class);
          realmListMemberships.add(membershipRealm);
        }
      }
    }

    if (result.get("groups") != null && !(result.get("groups") instanceof JsonNull)) {
      JsonArray resultsGroups = result.getAsJsonArray("groups");
      RealmList<GroupRealm> realmListGroups = new RealmList();

      if (resultsGroups != null) {
        for (JsonElement obj : resultsGroups) {
          if (!(obj instanceof JsonNull)) {
            GroupRealm groupRealm = groupDeserializer.parseGroup(obj.getAsJsonObject());
            realmListGroups.add(groupRealm);
          }
        }
      }

      userRealm.setGroups(realmListGroups);
    }

    userRealm.setFriendships(realmListFriendships);
    userRealm.setMemberships(realmListMemberships);

    return userRealm;
  }
}