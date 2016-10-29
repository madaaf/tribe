package com.tribe.app.data.network.deserializer;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tribe.app.data.network.entity.ScoreEntity;
import com.tribe.app.data.realm.UserRealm;

import java.lang.reflect.Type;

public class ScoreEntityDeserializer implements JsonDeserializer<ScoreEntity> {

    @Override
    public ScoreEntity deserialize(JsonElement je, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject results = je.getAsJsonObject().getAsJsonObject("data");

        Gson gson = new Gson();
        int count = 0;
        ScoreEntity scoreEntity = new ScoreEntity();
        boolean hasResult = true;

        while (hasResult && results.get("updateScore" + count) != null && !results.get("updateScore" + count).isJsonNull()) {
            JsonElement element = results.getAsJsonObject("updateScore" + count);
            if (element != null && !element.isJsonNull()) {
                UserRealm userRealm = gson.fromJson(element, UserRealm.class);
                if (userRealm != null) {
                    scoreEntity.setScore(Math.max(scoreEntity.getScore(), userRealm.getScore()));
                }
            } else if (element == null) {
                hasResult = false;
            }

            count++;
        }

        return scoreEntity;
    }
}