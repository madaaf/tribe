package com.tribe.app.data.network.deserializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.tribe.app.data.realm.UserRealm;

import java.lang.reflect.Type;

import io.realm.RealmList;

public class UserRealmListDeserializer implements JsonDeserializer<RealmList<UserRealm>> {

    @Override
    public RealmList<UserRealm> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        RealmList<UserRealm> realmUsers = new RealmList<>();
        JsonArray ja = json.getAsJsonArray();
        for (JsonElement je : ja) {
            if (!(je instanceof JsonNull) && je != null) realmUsers.add(context.deserialize(je, UserRealm.class));
        }

        return realmUsers;
    }
}