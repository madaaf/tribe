package com.tribe.app.data.network.deserializer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.tribe.app.data.realm.GameRealm;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GameListDeserializer implements JsonDeserializer<List<GameRealm>> {

  @Override public List<GameRealm> deserialize(JsonElement je, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {

    JsonArray results = je.getAsJsonObject().getAsJsonObject("data").getAsJsonArray("games");

    if (results == null || results.size() == 0) {
      return new ArrayList<>();
    } else {
      return new Gson().fromJson(results, typeOfT);
    }
  }
}