package com.tribe.app.data.network.deserializer;

import android.content.Context;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.utils.DeviceUtils;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class NameListPostItGameDeserializer implements JsonDeserializer<List<String>> {

  private static String GAME_POST_IT = "names";
  private static String GAME_CHALLENGES = "challenges";

  private String lang;

  public NameListPostItGameDeserializer(Context context) {
    this.lang = DeviceUtils.getLanguage(context);
  }

  @Override public List<String> deserialize(JsonElement json, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {
    List<String> nameList = new ArrayList<>();

    JsonObject results = null;
    if (((JsonObject) json).has(GAME_POST_IT)) {
      results = json.getAsJsonObject().getAsJsonObject(GAME_POST_IT);
    } else if (((JsonObject) json).has(GAME_CHALLENGES)) {
      results = json.getAsJsonObject().getAsJsonObject(GAME_CHALLENGES);
    }

    if (results != null) {
      for (JsonElement jsonElement : results.getAsJsonArray("default")) {
        nameList.add(jsonElement.getAsString());
      }

      if (!StringUtils.isEmpty(lang) && results.has(lang)) {
        for (JsonElement jsonElement : results.getAsJsonArray(lang)) {
          nameList.add(jsonElement.getAsString());
        }
      }
    }

    return nameList;
  }
}