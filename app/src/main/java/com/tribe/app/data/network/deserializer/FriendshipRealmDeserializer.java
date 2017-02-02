package com.tribe.app.data.network.deserializer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.SearchResultRealm;
import java.lang.reflect.Type;
import org.json.JSONObject;

public class FriendshipRealmDeserializer implements JsonDeserializer<FriendshipRealm> {

  @Override public FriendshipRealm deserialize(JsonElement je, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {
    JsonObject results = je.getAsJsonObject().getAsJsonObject("data");

    Gson gson = new Gson();
    JsonObject friendshipJson = results.getAsJsonObject("updateFriendship");
    return gson.fromJson(friendshipJson, FriendshipRealm.class);
  }
}