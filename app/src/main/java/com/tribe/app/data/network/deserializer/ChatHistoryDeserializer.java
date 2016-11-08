package com.tribe.app.data.network.deserializer;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.data.cache.TribeCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.ChatRealm;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tiago on 31/08/2016.
 */
public class ChatHistoryDeserializer extends MessageRealmListDeserializer implements JsonDeserializer<List<ChatRealm>> {

    public ChatHistoryDeserializer(SimpleDateFormat utcSimpleDate, UserCache userCache,
                                       TribeCache tribeCache, ChatCache chatCache, AccessToken accessToken) {
        super(utcSimpleDate, userCache, tribeCache, chatCache, accessToken);
    }

    @Override
    public List<ChatRealm> deserialize(JsonElement je, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject data = je.getAsJsonObject().getAsJsonObject("data");
        JsonElement results = null;

        if (data != null) {
            if (data.getAsJsonArray("friendships") == null && data.getAsJsonArray("groups") == null) {
                results = data.getAsJsonArray("messages");
                return deserializeChatRealmArray(results.getAsJsonArray());
            } else if (data.getAsJsonArray("groups") != null) {
                JsonElement element = data.getAsJsonArray("groups").get(0);
                if (element != null && !(element instanceof JsonNull)) {
                    results =  element.getAsJsonObject().getAsJsonArray("messages");
                    return new Gson().fromJson(results, typeOfT);
                }
            } else if (data.getAsJsonArray("friendships") != null) {
                results = data.getAsJsonArray("friendships").get(0).getAsJsonObject().getAsJsonArray("messages");
                return new Gson().fromJson(results, typeOfT);
            }
        }

        return new ArrayList<>();
    }
}