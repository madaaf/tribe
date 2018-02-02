package com.tribe.app.data.network.deserializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tribe.app.data.realm.AnimationIconRealm;
import com.tribe.app.data.realm.GameRealm;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.ScoreRealm;
import io.realm.RealmList;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GameListDeserializer implements JsonDeserializer<List<GameRealm>> {

  @Override public List<GameRealm> deserialize(JsonElement je, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {
    Gson gson = new GsonBuilder().registerTypeAdapter(ScoreRealm.class, new ScoreRealmDeserializer())
        .create();

    JsonArray results = je.getAsJsonObject().getAsJsonObject("data").getAsJsonArray("games");

    if (results == null || results.size() == 0) {
      return new ArrayList<>();
    } else {
      List<GameRealm> gameRealmList = new ArrayList<>();
      if (results != null) {
        for (JsonElement obj : results) {
          if (!(obj instanceof JsonNull)) {
            GameRealm gameRealm = gson.fromJson(obj, GameRealm.class);
            RealmList<AnimationIconRealm> icons = new RealmList<>();
            JsonArray array = obj.getAsJsonObject().getAsJsonArray("animation_icons");
            for (JsonElement str : array) {
              icons.add(new AnimationIconRealm(str.getAsString()));
            }
            gameRealm.setAnimation_icons(icons);
            gameRealmList.add(gameRealm);
          }
        }
      }

      return gameRealmList;
    }
  }
}