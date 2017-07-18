package com.tribe.app.data.network.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tribe.app.data.realm.AccessToken;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Date;

public class TribeAccessTokenDeserializer implements JsonDeserializer<AccessToken> {

  @Override
  public AccessToken deserialize(JsonElement je, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {

    JsonObject results = je.getAsJsonObject();
    AccessToken accessToken = new AccessToken();
    accessToken.setAccessToken(results.get("access_token").getAsString());
    accessToken.setRefreshToken(results.get("refresh_token").getAsString());

    long expiresIn = results.get("access_expires_in").getAsLong();
    if (expiresIn > 0) {
      Date expiresAt = new Date();
      expiresAt.setTime(expiresAt.getTime() + expiresIn * 1000);
      accessToken.setAccessExpiresAt(expiresAt);
    }

    JsonElement element = results.get("user_id");

    if (!(element instanceof JsonNull) && element != null) accessToken.setUserId(element.getAsString());

    accessToken.setTokenType("Bearer");
    return accessToken;
  }
}