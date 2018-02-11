package com.tribe.app.data.network.deserializer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.tribe.app.data.realm.ScoreRealm;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ScoreRealmListDeserializer implements JsonDeserializer<List<ScoreRealm>> {

  private Gson gson;

  public ScoreRealmListDeserializer() {
    this.gson = new Gson();
  }

  @Override public List<ScoreRealm> deserialize(JsonElement je, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {
    List<ScoreRealm> scoreRealmList = new ArrayList<>();
    JsonObject data = je.getAsJsonObject().getAsJsonObject("data");
    JsonArray results = null;

    if (data.has("game")) {
      results = data.getAsJsonObject("game").getAsJsonArray("scores");
    } else if (data.has("scores")) {
      results = data.getAsJsonArray("scores");
    }

    if (results == null) return scoreRealmList;

    for (JsonElement obj : results) {
      if (!(obj instanceof JsonNull)) {
        if (obj instanceof JsonPrimitive) {
          ScoreRealm scoreRealm = new ScoreRealm();
          scoreRealm.setValue(obj.getAsInt());
          scoreRealmList.add(scoreRealm);
        } else {
          ScoreRealm scoreRealm = ScoreRealm.deserialize(obj.getAsJsonObject(), gson);
          if (scoreRealm != null) scoreRealmList.add(scoreRealm);
        }
      }
    }

    return scoreRealmList;
  }
}