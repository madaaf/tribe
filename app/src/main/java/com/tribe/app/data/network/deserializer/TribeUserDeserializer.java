package com.tribe.app.data.network.deserializer;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class TribeUserDeserializer<T> implements JsonDeserializer<T> {

    @Override
    public T deserialize(JsonElement je, Type typeOfT,
                               JsonDeserializationContext context) throws JsonParseException {

            JsonElement results = je.getAsJsonObject().getAsJsonObject("data").getAsJsonObject("user");

            return new GsonBuilder().setDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT'Z '(UTC)'")
                    .create()
                    .fromJson(results, typeOfT);


        }
    }