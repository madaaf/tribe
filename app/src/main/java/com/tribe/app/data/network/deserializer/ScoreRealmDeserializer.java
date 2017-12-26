package com.tribe.app.data.network.deserializer;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tribe.app.data.realm.ScoreRealm;
import com.tribe.app.data.realm.ScoreUserRealm;
import java.lang.reflect.Type;

public class ScoreRealmDeserializer implements JsonDeserializer<ScoreRealm> {

  private Gson gson;

  public ScoreRealmDeserializer() {
    this.gson = new Gson();
  }

  @Override
  public ScoreRealm deserialize(JsonElement je, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    JsonObject jobject = je.getAsJsonObject();
    return ScoreRealm.deserialize(jobject, gson);
  }
}