package com.tribe.app.data.network.deserializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tribe.app.data.realm.AnimationIconRealm;
import com.tribe.app.data.realm.GameRealm;
import com.tribe.app.data.realm.ScoreRealm;
import io.realm.RealmList;
import java.lang.reflect.Type;

public class GameRealmDeserializer implements JsonDeserializer<GameRealm> {

  private Gson gson;

  public GameRealmDeserializer() {
    this.gson =
        new GsonBuilder().registerTypeAdapter(ScoreRealm.class, new ScoreRealmDeserializer())
            .excludeFieldsWithoutExposeAnnotation()
            .create();
  }

  @Override
  public GameRealm deserialize(JsonElement je, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    GameRealm gameRealm = gson.fromJson(je, GameRealm.class);
    RealmList<AnimationIconRealm> icons = new RealmList<>();
    JsonObject obj = je.getAsJsonObject();
    JsonArray array = obj.getAsJsonArray("animation_icons");
    for (JsonElement str : array) {
      icons.add(new AnimationIconRealm(str.getAsString()));
    }
    gameRealm.setAnimation_icons(icons);
    return gameRealm;
  }
}