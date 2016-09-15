package com.tribe.app.data.network.deserializer;

import com.google.gson.Gson;
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
import com.tribe.app.domain.entity.User;

import java.lang.reflect.Type;
import java.util.List;

import io.realm.RealmList;

/**
 * Created by horatiothomas on 9/15/16.
 */
public class GroupDeserializer implements JsonDeserializer<GroupRealm> {
    @Override
    public GroupRealm deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        GroupRealm groupRealm = new GroupRealm();


        JsonObject data = json.getAsJsonObject().getAsJsonObject("data");
        JsonArray groups = data.getAsJsonArray("groups");
        JsonArray members = groups.get(0).getAsJsonObject().getAsJsonArray("members");
        for (int i = 0; i < members.size(); i++) {
            if (members.get(i).isJsonNull()) {
                members.remove(i);
            }
        }
        RealmList<UserRealm> users = new GsonBuilder().create().fromJson(members, new TypeToken<RealmList<UserRealm>>(){}.getType());

        groupRealm.setMembers(users);

        return groupRealm;
    }
}