package com.tribe.app.data.network.deserializer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tribe.app.domain.entity.Room;
import java.lang.reflect.Type;

public class RoomDeserializer implements JsonDeserializer<Room> {

  @Override
  public Room deserialize(JsonElement je, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    JsonObject results = je.getAsJsonObject().getAsJsonObject("data");

    if (results == null || results.get("getRoomParameters") instanceof JsonNull) {
      JsonArray errors = je.getAsJsonObject().getAsJsonArray("errors");
      JsonElement jsonElement = errors.get(0);
      String message = jsonElement.getAsJsonObject().get("message").getAsString();
      Room room = new Room();

      // TODO what about errors?
      //if (message.equals(Constants.USER_BLOCKED)) {
      //  roomConfiguration.setException(new BlockedException());
      //} else if (message.equals(Constants.ROOM_FULL)) {
      //  roomConfiguration.setException(new RoomFullException());
      //}

      return room;
    } else {
      JsonElement toParse = results.getAsJsonObject("getRoomParameters");
      return new Gson().fromJson(toParse, typeOfT);
    }
  }
}