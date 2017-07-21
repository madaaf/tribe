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
import com.tribe.app.domain.entity.Invite;
import io.realm.RealmList;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

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
    userRealm.setPhone(result.get("phone").getAsString());
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
      userRealm.setLastSeenAt(simpleDateFormat.parse(result.get("last_seen_at").getAsString()));
    } catch (ParseException e) {
      e.printStackTrace();
    }

    JsonArray resultsFriendships = result.getAsJsonArray("friendships");
    JsonArray resultsMemberships = result.getAsJsonArray("memberships");
    JsonArray resultsInvites = result.getAsJsonArray("invites");
    RealmList<FriendshipRealm> realmListFriendships = new RealmList();
    RealmList<MembershipRealm> realmListMemberships = new RealmList();
    List<Invite> listInvites = new ArrayList<>();

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
          if (membershipRealm != null) realmListMemberships.add(membershipRealm);
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
            if (groupRealm != null) realmListGroups.add(groupRealm);
          }
        }
      }

      userRealm.setGroups(realmListGroups);
    }

    if (resultsInvites != null) {
      for (JsonElement obj : resultsInvites) {
        if (!(obj instanceof JsonNull)) {
          Invite invite = gson.fromJson(obj, Invite.class);
          listInvites.add(invite);
        }
      }
    }

    userRealm.setInvites(listInvites);
    userRealm.setFriendships(realmListFriendships);
    userRealm.setMemberships(realmListMemberships);

    return userRealm;
  }
}