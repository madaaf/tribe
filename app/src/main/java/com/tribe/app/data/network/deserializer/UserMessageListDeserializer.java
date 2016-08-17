package com.tribe.app.data.network.deserializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tribe.app.data.cache.TribeCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.realm.ChatRealm;
import com.tribe.app.data.realm.LocationRealm;
import com.tribe.app.data.realm.MessageRealmInterface;
import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.data.realm.TribeRecipientRealm;
import com.tribe.app.data.realm.WeatherRealm;
import com.tribe.app.domain.entity.User;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class UserMessageListDeserializer<T> implements JsonDeserializer<T> {

    private SimpleDateFormat utcSimpleDate;
    private User currentUser;
    private UserCache userCache;
    private TribeCache tribeCache;

    public UserMessageListDeserializer(SimpleDateFormat utcSimpleDate, UserCache userCache, TribeCache tribeCache, User currentUser) {
        this.utcSimpleDate = utcSimpleDate;
        this.userCache = userCache;
        this.tribeCache = tribeCache;
        this.currentUser = currentUser;
    }

    @Override
    public T deserialize(JsonElement je, Type typeOfT,
                               JsonDeserializationContext context) throws JsonParseException {

        JsonObject user = je.getAsJsonObject().getAsJsonObject("data").getAsJsonObject("user");
        JsonArray resultsTribes = user.getAsJsonArray("tribes");
        JsonArray resultsChatMessages = user.getAsJsonArray("unSeenMessages");
        JsonArray resultsTribesSent = je.getAsJsonObject().getAsJsonObject("data").getAsJsonArray("tribes");

        List<MessageRealmInterface> messages = new ArrayList<>();

        for (JsonElement obj : resultsTribes) {
            TribeRealm tribeRealm = parseTribe(obj);
            if (tribeRealm.getFrom() != null && ((tribeRealm.isToGroup() && tribeRealm.getGroup() != null) || !tribeRealm.isToGroup()))
                messages.add(tribeRealm);
        }

        for (JsonElement obj : resultsChatMessages) {
            ChatRealm chatRealm = parseChat(obj);
            if (chatRealm.getFrom() != null && ((chatRealm.isToGroup() && chatRealm.getGroup() != null) || !chatRealm.isToGroup()))
                messages.add(chatRealm);
        }

        if (resultsTribesSent != null) {
            for (JsonElement obj : resultsTribesSent) {
                TribeRealm tribeRealm = new TribeRealm();
                JsonObject json = obj.getAsJsonObject();
                tribeRealm.setId(json.get("id").getAsString());

                List<TribeRecipientRealm> tribeRecipientRealmList = new ArrayList<>();

                for (JsonElement recipient : json.getAsJsonArray("recipients")) {
                    JsonObject jsonRecipient = recipient.getAsJsonObject();
                    TribeRecipientRealm tribeRecipientRealm = new TribeRecipientRealm();
                    tribeRecipientRealm.setId(tribeRealm.getId() + jsonRecipient.get("to").getAsString());
                    tribeRecipientRealm.setTo(jsonRecipient.get("to").getAsString());
                    tribeRecipientRealm.setIsSeen(jsonRecipient.get("is_seen").getAsBoolean());
                    tribeRecipientRealmList.add(tribeRecipientRealm);
                }

                tribeRealm.setRecipientList(tribeCache.createTribeRecipientRealm(tribeRecipientRealmList));
                messages.add(tribeRealm);
            }
        }

        return (T) messages;
    }

    private TribeRealm parseTribe(JsonElement obj) {
        TribeRealm tribeRealm = new TribeRealm();
        JsonObject json = obj.getAsJsonObject();
        tribeRealm.setId(json.get("id").getAsString());
        tribeRealm.setLocalId(json.get("id").getAsString());

        boolean toGroup = json.get("to_group").getAsBoolean();

        if (toGroup) {
            tribeRealm.setGroup(userCache.groupInfos(json.get("to").getAsString()));
        } else {
            if (!currentUser.getId().equals(json.get("to").getAsString())) {
                tribeRealm.setFriendshipRealm(userCache.friendshipForUserId(json.get("to").getAsString()));
            }
        }

        tribeRealm.setToGroup(toGroup);

        tribeRealm.setFrom(userCache.userInfosNoObs(json.get("from").getAsString()));

        LocationRealm locationRealm = new LocationRealm();
        locationRealm.setLatitude(json.get("lat") instanceof JsonNull ? 0.0D : json.get("lat").getAsDouble());
        locationRealm.setLongitude(json.get("lng") instanceof JsonNull ? 0.0D : json.get("lng").getAsDouble());
        locationRealm.setCity(!(json.get("location") instanceof JsonNull) ? json.getAsJsonObject("location").get("city").getAsString() : null);
        locationRealm.setHasLocation(!(json.get("lat") instanceof JsonNull));
        tribeRealm.setLocationRealm(locationRealm);

        tribeRealm.setType(json.get("type").getAsString());
        tribeRealm.setUrl(json.get("url").getAsString());

        if (!(json.get("weather") instanceof JsonNull)) {
            JsonObject weather = json.get("weather").getAsJsonObject();
            WeatherRealm weatherRealm = new WeatherRealm();
            weatherRealm.setIcon(weather.get("icon").getAsString());
            weatherRealm.setTempC(weather.get("temp_c").getAsInt());
            weatherRealm.setTempF(weather.get("temp_f").getAsInt());
            tribeRealm.setWeatherRealm(weatherRealm);
        }

        try {
            tribeRealm.setRecordedAt(utcSimpleDate.parse(json.get("recorded_at").getAsString()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return tribeRealm;
    }

    private ChatRealm parseChat(JsonElement obj) {
        ChatRealm chatRealm = new ChatRealm();
        JsonObject json = obj.getAsJsonObject();
        chatRealm.setId(json.get("id").getAsString());
        chatRealm.setLocalId(json.get("id").getAsString());

        boolean toGroup = json.get("to_group").getAsBoolean();

        if (toGroup) {
            chatRealm.setGroup(userCache.groupInfos(json.get("to").getAsString()));
        } else {
            if (!currentUser.getId().equals(json.get("to").getAsString())) {
                chatRealm.setFriendshipRealm(userCache.friendshipForUserId(json.get("to").getAsString()));
            }
        }

        chatRealm.setToGroup(toGroup);
        chatRealm.setFrom(userCache.userInfosNoObs(json.get("from").getAsString()));
        chatRealm.setType(json.get("type").getAsString());
        chatRealm.setContent(json.get("content").getAsString());

        try {
            chatRealm.setRecordedAt(utcSimpleDate.parse(json.get("recorded_at").getAsString()));
            chatRealm.setCreatedAt(utcSimpleDate.parse(json.get("created_at").getAsString()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return chatRealm;
    }
}