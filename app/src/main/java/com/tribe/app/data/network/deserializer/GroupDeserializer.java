package com.tribe.app.data.network.deserializer;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.tribe.app.data.realm.GroupRealm;
import com.tribe.app.data.realm.UserRealm;

import java.lang.reflect.Type;

import io.realm.RealmList;

/**
 * Created by horatiothomas on 9/15/16.
 */
public class GroupDeserializer implements JsonDeserializer<GroupRealm> {
    @Override
    public GroupRealm deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject data = json.getAsJsonObject().getAsJsonObject("data");
        JsonObject group = null;
        JsonArray groupsArray = data.getAsJsonArray("groups");
        if (groupsArray != null && groupsArray.get(0) != null && !(groupsArray.get(0) instanceof JsonNull)) {
            group = data.getAsJsonArray("groups").get(0).getAsJsonObject();
        }

        if (group == null) group = data.getAsJsonObject("createGroup");
        if (group == null) group = data.getAsJsonObject("updateGroup");

        return parseGroup(group);
    }

    public GroupRealm parseGroup(JsonObject group) {
        GroupRealm groupRealm = new GroupRealm();

        JsonArray members = group.getAsJsonArray("members");
        if (members != null) {
            for (int i = 0; i < members.size(); i++) {
                if (members.get(i).isJsonNull()) {
                    members.remove(i);
                }
            }
        }

        JsonArray admins = group.getAsJsonArray("admins");

        if (admins != null && members != null && members.size() > 0) {
            RealmList<UserRealm> users = new GsonBuilder().create().fromJson(members, new TypeToken<RealmList<UserRealm>>() {}.getType());
            RealmList<UserRealm> adminsList = new GsonBuilder().create().fromJson(admins, new TypeToken<RealmList<UserRealm>>() {}.getType());
            groupRealm.setMembers(users);
            groupRealm.setAdmins(adminsList);
        }

        JsonElement groupLink = group.get("link");
        if (!groupLink.isJsonNull()) groupRealm.setLink(groupLink.toString());
        groupRealm.setId(group.get("id").getAsString());
        groupRealm.setName(group.get("name").getAsString());
        groupRealm.setPicture(group.get("picture") != null && !group.get("picture").isJsonNull() ? group.get("picture").getAsString() : null);
        groupRealm.setPrivateGroup(group.get("type").getAsString().equals("PRIVATE"));

        return groupRealm;
    }
}
