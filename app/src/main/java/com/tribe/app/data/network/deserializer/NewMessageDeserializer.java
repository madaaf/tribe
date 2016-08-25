package com.tribe.app.data.network.deserializer;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class NewMessageDeserializer<T> implements JsonDeserializer<T> {

    @Override
    public T deserialize(JsonElement je, Type typeOfT,
                               JsonDeserializationContext context) throws JsonParseException {

            JsonElement results = je.getAsJsonObject().getAsJsonObject("data").getAsJsonObject("newMessage");

            return new Gson().fromJson(results, typeOfT);
        }
    }