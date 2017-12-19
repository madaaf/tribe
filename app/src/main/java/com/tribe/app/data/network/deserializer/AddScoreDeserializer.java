package com.tribe.app.data.network.deserializer;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tribe.app.data.network.entity.AddScoreEntity;
import java.lang.reflect.Type;

public class AddScoreDeserializer implements JsonDeserializer<AddScoreEntity> {

  @Override public AddScoreEntity deserialize(JsonElement je, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {
    JsonObject results = je.getAsJsonObject().getAsJsonObject("data");

    if (results != null && results.has("addScore")) {
      JsonObject toParse = results.getAsJsonObject("addScore");
      return new Gson().fromJson(toParse, typeOfT);
    }

    return null;
  }
}