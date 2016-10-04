package com.tribe.app.data.network.deserializer;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
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

        GroupRealm groupRealm = new GroupRealm();


        JsonObject data = json.getAsJsonObject().getAsJsonObject("data");
        JsonObject group;
        try {
            group = data.getAsJsonArray("groups").get(0).getAsJsonObject();
            JsonArray members = group.getAsJsonArray("members");
            for (int i = 0; i < members.size(); i++) {
                if (members.get(i).isJsonNull()) {
                    members.remove(i);
                }
            }
            JsonArray admins = group.getAsJsonArray("admins");
            RealmList<UserRealm> users = new GsonBuilder().create().fromJson(members, new TypeToken<RealmList<UserRealm>>(){}.getType());
            RealmList<UserRealm> adminsList = new GsonBuilder().create().fromJson(admins, new TypeToken<RealmList<UserRealm>>(){}.getType());
            groupRealm.setMembers(users);
            groupRealm.setAdmins(adminsList);
        } catch (NullPointerException e) {
            group = data.getAsJsonObject("updateGroup");
            if (group == null) group = data.getAsJsonObject("createGroup");
        }
        JsonElement groupLink = group.get("link");
        if (groupLink != null) groupRealm.setGroupLink(groupLink.toString());
        groupRealm.setId(group.get("id").getAsString());
        groupRealm.setName(group.get("name").getAsString());
        groupRealm.setPicture(group.get("picture") != null && !group.get("picture").isJsonNull() ? group.get("picture").getAsString() : null);
        groupRealm.setPrivateGroup(group.get("type").getAsString().equals("PRIVATE"));

        return groupRealm;
    }
}
