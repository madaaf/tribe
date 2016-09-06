package com.tribe.app.data.network.deserializer;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class NewTribeDeserializer<T> implements JsonDeserializer<T> {

    @Override
    public T deserialize(JsonElement je, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject results = je.getAsJsonObject().getAsJsonObject("data");
        JsonElement toParse = null;

        if (results.getAsJsonObject("sendTribeToGroup") != null) {
            toParse = results.getAsJsonObject("sendTribeToGroup");
        } else {
            toParse = results.getAsJsonObject("sendTribeToUser");
        }

        return new Gson().fromJson(toParse, typeOfT);
    }
}