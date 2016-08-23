package com.tribe.app.data.network.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tribe.app.data.realm.AccessToken;

import java.lang.reflect.Type;

public class TribeAccessTokenDeserializer implements JsonDeserializer<AccessToken> {

    @Override
    public AccessToken deserialize(JsonElement je, Type typeOfT,
                               JsonDeserializationContext context) throws JsonParseException {

            JsonObject results = je.getAsJsonObject();
            AccessToken accessToken = new AccessToken();
            accessToken.setAccessToken(results.get("access_token").getAsString());
            accessToken.setUserId(results.get("user_id").getAsString());
            accessToken.setRefreshToken(results.get("refresh_token").getAsString());
            accessToken.setTokenType("Bearer");
            return accessToken;
        }
    }