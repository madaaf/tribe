package com.tribe.app.data.network.deserializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tribe.app.data.realm.Installation;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class InstallsDeserializer implements JsonDeserializer<List<Installation>> {

  @Override public List<Installation> deserialize(JsonElement je, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {
    Gson gson = new GsonBuilder().create();
    List<Installation> installations = new ArrayList<>();

    JsonObject result = je.getAsJsonObject().getAsJsonObject("data").getAsJsonObject("user");

    JsonArray resultsInstalls = result.getAsJsonArray("installs");

    if (resultsInstalls != null) {
      for (JsonElement obj : resultsInstalls) {
        if (!(obj instanceof JsonNull)) {
          Installation installation = gson.fromJson(obj, Installation.class);
          installations.add(installation);
        }
      }
    }

    return installations;
  }
}