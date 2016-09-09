package com.tribe.app.data.network.deserializer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tribe.app.data.network.entity.LookupEntity;
import com.tribe.app.data.realm.UserRealm;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LookupDeserializer implements JsonDeserializer<LookupEntity> {

    @Override
    public LookupEntity deserialize(JsonElement je, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject results = je.getAsJsonObject().getAsJsonObject("data");

        Gson gson = new Gson();
        int count = 0;
        LookupEntity lookupEntity = new LookupEntity();
        List<UserRealm> lookup = new ArrayList<>();
        boolean hasResult = true;

        while (hasResult) {
            JsonArray array = results.getAsJsonArray("lookupPhone" + count);
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

        hasResult = true;
        count = 0;

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

        lookupEntity.setLookup(lookup);

        return lookupEntity;
    }
}