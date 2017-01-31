package com.tribe.app.data.network.deserializer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tribe.app.data.realm.SearchResultRealm;

import java.lang.reflect.Type;

public class SearchResultDeserializer implements JsonDeserializer<SearchResultRealm> {

  @Override public SearchResultRealm deserialize(JsonElement je, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {
    JsonObject results = je.getAsJsonObject().getAsJsonObject("data");

    Gson gson = new Gson();
    JsonArray array = results.getAsJsonArray("lookupByUsername");
    if (array != null && !array.isJsonNull() && array.isJsonArray() && !array.get(0).isJsonNull()) {
      JsonElement jsonElement = array.get(0).getAsJsonObject();
      if (!(jsonElement instanceof JsonNull) && jsonElement != null) {
        return gson.fromJson(jsonElement, SearchResultRealm.class);
      }
    }

    return null;
  }
}