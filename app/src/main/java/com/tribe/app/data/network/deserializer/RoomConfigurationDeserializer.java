package com.tribe.app.data.network.deserializer;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tribe.app.domain.entity.RoomConfiguration;
import java.lang.reflect.Type;

public class RoomConfigurationDeserializer implements JsonDeserializer<RoomConfiguration> {

  @Override public RoomConfiguration deserialize(JsonElement je, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {
    JsonObject results = je.getAsJsonObject().getAsJsonObject("data");
    JsonElement toParse = results.getAsJsonObject("getRoomParameters");

    return new Gson().fromJson(toParse, typeOfT);
  }
}