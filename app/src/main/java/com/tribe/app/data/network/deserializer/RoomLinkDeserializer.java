package com.tribe.app.data.network.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tribe.app.data.network.entity.RoomLinkEntity;
import java.lang.reflect.Type;

public class RoomLinkDeserializer implements JsonDeserializer<RoomLinkEntity> {

  @Override public RoomLinkEntity deserialize(JsonElement json, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {

    JsonObject results = json.getAsJsonObject().getAsJsonObject("data");

    if (results != null && results.get("getRoomLink") != null) {
      String link = results.get("getRoomLink").getAsString();
      RoomLinkEntity roomLinkEntity = new RoomLinkEntity();
      roomLinkEntity.setLink(link);
      return roomLinkEntity;
    }

    return null;
  }
}