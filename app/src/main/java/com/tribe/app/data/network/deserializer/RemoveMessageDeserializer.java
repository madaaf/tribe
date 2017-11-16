package com.tribe.app.data.network.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tribe.app.data.network.entity.RemoveMessageEntity;
import java.lang.reflect.Type;

public class RemoveMessageDeserializer implements JsonDeserializer<RemoveMessageEntity> {
  private final static String removeMessageLabel = "removeMessage";

  @Override public RemoveMessageEntity deserialize(JsonElement json, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {

    JsonObject results = json.getAsJsonObject().getAsJsonObject("data");
    RemoveMessageEntity removeMessageEntity = new RemoveMessageEntity();

    if (results != null && results.has(removeMessageLabel) && !results.get(removeMessageLabel)
        .isJsonNull()) {
      removeMessageEntity.setIsRemoved(results.get(removeMessageLabel).getAsBoolean());
      return removeMessageEntity;
    }
    removeMessageEntity.setIsRemoved(false);
    return removeMessageEntity;
  }
}