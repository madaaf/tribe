package com.tribe.app.data.network.deserializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.data.cache.TribeCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.realm.ChatRealm;
import com.tribe.app.data.realm.MessageRealmInterface;
import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.domain.entity.User;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class UserMessageListDeserializer<T> extends MessageRealmListDeserializer implements JsonDeserializer<T> {

    public UserMessageListDeserializer(SimpleDateFormat utcSimpleDate, UserCache userCache,
                                     TribeCache tribeCache, ChatCache chatCache, User currentUser) {
        super(utcSimpleDate, userCache, tribeCache, chatCache, currentUser);
    }

    @Override
    public T deserialize(JsonElement je, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject user = je.getAsJsonObject().getAsJsonObject("data").getAsJsonObject("user");
        List<MessageRealmInterface> messages = new ArrayList<>();

        if (user != null) {
            JsonArray resultsTribes = user.getAsJsonArray("tribes");
            JsonArray resultsChatMessages = user.getAsJsonArray("unSeenMessages");
            messages.addAll(deserializeTribeRealmArray(resultsTribes));
            messages.addAll(deserializeChatRealmArray(resultsChatMessages));
        }

        JsonArray resultsTribesSent = je.getAsJsonObject().getAsJsonObject("data").getAsJsonArray("tribes");
        JsonArray resultsMessagesSent = je.getAsJsonObject().getAsJsonObject("data").getAsJsonArray("messages");

        if (resultsTribesSent != null) {
            for (JsonElement obj : resultsTribesSent) {
                if (!(obj instanceof JsonNull)) {
                    TribeRealm tribeRealm = new TribeRealm();
                    JsonObject json = obj.getAsJsonObject();
                    tribeRealm.setId(json.get("id").getAsString());
                    tribeRealm.setRecipientList(tribeCache.createTribeRecipientRealm(parseRecipients(tribeRealm.getId(), json.getAsJsonArray("recipients"))));
                    messages.add(tribeRealm);
                }
            }
        }

        if (resultsMessagesSent != null) {
            for (JsonElement obj : resultsMessagesSent) {
                if (!(obj instanceof JsonNull)) {
                    ChatRealm chatRealm = new ChatRealm();
                    JsonObject json = obj.getAsJsonObject();
                    chatRealm.setId(json.get("id").getAsString());
                    chatRealm.setRecipientList(chatCache.createMessageRecipientRealm(parseRecipients(chatRealm.getId(), json.getAsJsonArray("recipients"))));
                    messages.add(chatRealm);
                }
            }
        }

        return (T) messages;
    }
}