package com.tribe.app.data.network.deserializer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tribe.app.data.network.entity.CreateFriendshipEntity;
import com.tribe.app.data.realm.FriendshipRealm;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CreateFriendshipDeserializer implements JsonDeserializer<CreateFriendshipEntity> {

    @Override
    public CreateFriendshipEntity deserialize(JsonElement je, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject results = je.getAsJsonObject().getAsJsonObject("data");

        Gson gson = new Gson();
        int count = 0;
        CreateFriendshipEntity createFriendshipEntity = new CreateFriendshipEntity();
        List<FriendshipRealm> friendshipRealmList = new ArrayList<>();
        boolean hasResult = true;

        while (hasResult) {
            if (results.isJsonArray()) {
                JsonArray array = results.getAsJsonArray("createFriendship" + count);
                if (array != null) {
                    for (final JsonElement jsonElement : array) {
                        if (!jsonElement.isJsonNull()) {
                            friendshipRealmList.add(gson.fromJson(jsonElement, FriendshipRealm.class));
                        }
                    }
                } else {
                    hasResult = false;
                }
            } else {
                JsonElement element = results.getAsJsonObject("createFriendship0");
                friendshipRealmList.add(gson.fromJson(element, FriendshipRealm.class));
                hasResult = false;
            }

            count++;
        }

        createFriendshipEntity.setNewFriendshipList(friendshipRealmList);

        return createFriendshipEntity;
    }
}