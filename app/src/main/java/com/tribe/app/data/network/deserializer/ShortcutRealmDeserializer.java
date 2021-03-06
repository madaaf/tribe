package com.tribe.app.data.network.deserializer;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tribe.app.data.realm.ShortcutRealm;
import java.lang.reflect.Type;

public class ShortcutRealmDeserializer implements JsonDeserializer<ShortcutRealm> {

  @Override
  public ShortcutRealm deserialize(JsonElement je, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    JsonObject results = je.getAsJsonObject().getAsJsonObject("data");
    JsonObject result = null;

    Gson gson = new Gson();

    if (results.has("updateShortcut")) {
      result = results.getAsJsonObject("updateShortcut");
    } else if (results.has("createShortcut") && !results.get("createShortcut").isJsonNull()) {
      result = results.getAsJsonObject("createShortcut");
    }

    return gson.fromJson(result, ShortcutRealm.class);
  }
}