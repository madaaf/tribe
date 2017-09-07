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
import com.google.gson.reflect.TypeToken;
import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.data.realm.UserRealm;
import io.realm.RealmList;
import java.lang.reflect.Type;
import java.util.List;

public class MessageRealmDeserializer implements JsonDeserializer<List<MessageRealm>> {

  @Override public List<MessageRealm> deserialize(JsonElement je, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {

    RealmList<MessageRealm> realmListMessage = new RealmList();

    Gson gson = new GsonBuilder().registerTypeAdapter(new TypeToken<RealmList<UserRealm>>() {
    }.getType(), new UserRealmListDeserializer()).create();

    JsonObject result = null;

    if (je.getAsJsonObject().getAsJsonObject("data").has("user")) {
      if (!je.getAsJsonObject().getAsJsonObject("data").get("user").isJsonNull()) {
        result = je.getAsJsonObject().getAsJsonObject("data").getAsJsonObject("user");

        JsonArray resultsMessage = result.getAsJsonArray("messages");

        if (resultsMessage != null) {
          for (JsonElement obj : resultsMessage) {
            if (!(obj instanceof JsonNull)) {
              MessageRealm membershipRealm = gson.fromJson(obj, MessageRealm.class);
              if (membershipRealm != null) realmListMessage.add(membershipRealm);
            }
          }
        }
      }
    }
    return realmListMessage;
  }
}