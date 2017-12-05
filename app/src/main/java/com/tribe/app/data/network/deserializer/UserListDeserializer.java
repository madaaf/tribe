package com.tribe.app.data.network.deserializer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tribe.app.data.realm.UserRealm;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class UserListDeserializer implements JsonDeserializer<List<UserRealm>> {

  @Override public List<UserRealm> deserialize(JsonElement je, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {

    JsonArray results = je.getAsJsonObject().getAsJsonObject("data").getAsJsonArray("users");
    if (results == null) {
      JsonElement element = je.getAsJsonObject().getAsJsonObject("data");
      JsonObject propertyToBeCopied = (JsonObject) element;
      if (!(propertyToBeCopied.get("lookupByUserId") instanceof JsonNull)) {
        results = propertyToBeCopied.getAsJsonArray("lookupByUserId");
      }
    }

    if (results == null || results.size() == 0) {
      return new ArrayList<>();
    } else {
      return new Gson().fromJson(results, typeOfT);
    }
  }
}