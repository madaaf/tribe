package com.tribe.app.data.network.deserializer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class HowManyFriendsDeserializer implements JsonDeserializer<List<Integer>> {

  @Override
  public List<Integer> deserialize(JsonElement je, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    JsonObject results = je.getAsJsonObject().getAsJsonObject("data");

    Gson gson = new Gson();
    int count = 0;
    List<Integer> howManyFriends = new ArrayList<>();
    boolean hasResult = true;

    while (hasResult) {
      JsonArray array = results.getAsJsonArray("howManyFriends" + count);
      if (array != null) {
        for (final JsonElement jsonElement : array) {
          if (!jsonElement.isJsonNull()) {
            howManyFriends.add(jsonElement.getAsInt());
          } else {
            howManyFriends.add(0);
          }
        }
      } else {
        hasResult = false;
      }

      count++;
    }

    return howManyFriends;
  }
}