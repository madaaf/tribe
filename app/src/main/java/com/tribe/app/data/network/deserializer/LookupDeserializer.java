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
        LookupEntity lookup = new LookupEntity();
        List<UserRealm> lookupByPhone = new ArrayList<>();
        boolean hasResult = true;

        while (hasResult) {
            JsonArray array = results.getAsJsonArray("lookupPhone" + count);
            if (array != null) {
                for (final JsonElement jsonElement : array) {
                    if (!jsonElement.isJsonNull()) {
                        lookupByPhone.add(gson.fromJson(jsonElement, UserRealm.class));
                    }
                }
            } else {
                hasResult = false;
            }

            count++;
        }

        lookup.setLookup(lookupByPhone);

        return lookup;
    }
}