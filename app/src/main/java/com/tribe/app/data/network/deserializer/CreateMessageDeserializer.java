package com.tribe.app.data.network.deserializer;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tribe.app.data.realm.MessageRealm;
import java.lang.reflect.Type;

public class CreateMessageDeserializer implements JsonDeserializer<MessageRealm> {

  @Override
  public MessageRealm deserialize(JsonElement je, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    JsonObject results = je.getAsJsonObject().getAsJsonObject("data");
    if (results == null || results.get("createMessage") instanceof JsonNull) {
      MessageRealm messageRealm = new MessageRealm();
      return messageRealm;
    } else if (results != null && results.has("createMessage")) {
      JsonElement toParse = results.getAsJsonObject("createMessage");
      return new Gson().fromJson(toParse, typeOfT);
    }
    return null;
  }
}
