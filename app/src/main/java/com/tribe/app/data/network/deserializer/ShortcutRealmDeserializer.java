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

    Gson gson = new Gson();
    
    JsonObject shortcutJson = results.getAsJsonObject("updateShortcut");
    return gson.fromJson(shortcutJson, ShortcutRealm.class);
  }
}