package com.tribe.app.data.network.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tribe.app.data.network.entity.BookRoomLinkEntity;
import java.lang.reflect.Type;

public class BookRoomLinkDeserializer implements JsonDeserializer<BookRoomLinkEntity> {

  @Override public BookRoomLinkEntity deserialize(JsonElement json, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {

    JsonObject results = json.getAsJsonObject().getAsJsonObject("data");
    BookRoomLinkEntity bookRoomLinkEntity = new BookRoomLinkEntity();

    if (results != null && results.has("bookRoomLink") && !results.get("bookRoomLink")
        .isJsonNull()) {
      bookRoomLinkEntity.setRoomBooked(results.get("bookRoomLink").getAsBoolean());
      return bookRoomLinkEntity;
    }
    bookRoomLinkEntity.setRoomBooked(false);
    return bookRoomLinkEntity;
  }
}