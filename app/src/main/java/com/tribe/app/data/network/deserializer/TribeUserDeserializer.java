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
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.GroupRealm;
import com.tribe.app.data.realm.UserRealm;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import io.realm.RealmList;

public class TribeUserDeserializer implements JsonDeserializer<UserRealm> {

    private SimpleDateFormat simpleDateFormat;

    public TribeUserDeserializer(SimpleDateFormat simpleDateFormat) {
        this.simpleDateFormat = simpleDateFormat;
    }

    @Override
    public UserRealm deserialize(JsonElement je, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Gson gson = new GsonBuilder().create();
        UserRealm userRealm = new UserRealm();

        JsonObject result = je.getAsJsonObject().getAsJsonObject("data").getAsJsonObject("user");
        if (result == null) result =  je.getAsJsonObject().getAsJsonObject("data").getAsJsonObject("updateUser");

        userRealm.setId(result.get("id").getAsString());
        userRealm.setPhone(result.get("phone").getAsString());
        userRealm.setFbid(result.get("fbid").getAsString());
        userRealm.setInvisibleMode(result.get("invisible_mode").getAsBoolean());
        userRealm.setScore(result.get("score").getAsInt());
        userRealm.setUsername(result.get("username").getAsString());
        userRealm.setDisplayName(result.get("display_name").getAsString());
        userRealm.setProfilePicture(result.get("picture") != null ? result.get("picture").getAsString() : null);
        try {
            userRealm.setCreatedAt(simpleDateFormat.parse(result.get("created_at").getAsString()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        JsonArray resultsFriendships = result.getAsJsonArray("friendships");
        JsonArray resultsGroups = result.getAsJsonArray("groups");
        RealmList<FriendshipRealm> realmListFriendships = new RealmList();
        RealmList<GroupRealm> realmListGroups = new RealmList();

        if (resultsFriendships != null) {
            for (JsonElement obj : resultsFriendships) {
                if (!(obj instanceof JsonNull)) {
                    FriendshipRealm friendshipRealm = gson.fromJson(obj, FriendshipRealm.class);
                    realmListFriendships.add(friendshipRealm);
                }
            }
        }

        if (resultsGroups != null) {
            for (JsonElement obj : resultsGroups) {
                if (!(obj instanceof JsonNull)) {
                    GroupRealm groupRealm = gson.fromJson(obj, GroupRealm.class);
                    realmListGroups.add(groupRealm);
                }
            }
        }

        userRealm.setFriendships(realmListFriendships);
        userRealm.setGroups(realmListGroups);

        return userRealm;
    }
}