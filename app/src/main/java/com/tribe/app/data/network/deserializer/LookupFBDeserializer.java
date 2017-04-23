package com.tribe.app.data.network.deserializer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tribe.app.data.network.entity.LookupFBResult;
import com.tribe.app.data.realm.UserRealm;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LookupFBDeserializer implements JsonDeserializer<LookupFBResult> {

  @Override public LookupFBResult deserialize(JsonElement je, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {
    JsonObject results = je.getAsJsonObject().getAsJsonObject("data");

    Gson gson = new Gson();
    int count = 0;
    LookupFBResult lookupResult = new LookupFBResult();
    List<UserRealm> lookup = new ArrayList<>();
    boolean hasResult = true;

    while (hasResult) {
      JsonArray array = results.getAsJsonArray("lookupFB" + count);
      if (array != null) {
        for (final JsonElement jsonElement : array) {
          if (!jsonElement.isJsonNull()) {
            lookup.add(gson.fromJson(jsonElement, UserRealm.class));
          }
        }
      } else {
        hasResult = false;
      }

      count++;
    }

    lookupResult.setLookup(lookup);

    return lookupResult;
  }
}