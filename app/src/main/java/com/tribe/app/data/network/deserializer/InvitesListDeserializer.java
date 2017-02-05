package com.tribe.app.data.network.deserializer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.tribe.app.domain.entity.Invite;
import java.lang.reflect.Type;

public class InvitesListDeserializer<Invite> implements JsonDeserializer<Invite> {

    @Override
    public Invite deserialize(JsonElement je, Type typeOfT,
                         JsonDeserializationContext context) throws JsonParseException {

        JsonArray results = je.getAsJsonObject().getAsJsonObject("data").getAsJsonObject("user").getAsJsonArray("invites");

        return new Gson().fromJson(results, typeOfT);
    }
}