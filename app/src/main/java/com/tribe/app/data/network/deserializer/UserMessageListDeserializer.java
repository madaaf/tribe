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
import com.tribe.app.data.realm.LocationRealm;
import com.tribe.app.data.realm.MessageRealmInterface;
import com.tribe.app.data.realm.MessageRecipientRealm;
import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.data.realm.WeatherRealm;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.view.utils.MessageDownloadingStatus;
import com.tribe.app.presentation.view.utils.MessageReceivingStatus;

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
    private ChatCache chatCache;

    public UserMessageListDeserializer(SimpleDateFormat utcSimpleDate, UserCache userCache,
                                       TribeCache tribeCache, ChatCache chatCache, User currentUser) {
        this.utcSimpleDate = utcSimpleDate;
        this.userCache = userCache;
        this.tribeCache = tribeCache;
        this.chatCache = chatCache;
        this.currentUser = currentUser;
    }

    @Override
    public T deserialize(JsonElement je, Type typeOfT,
                               JsonDeserializationContext context) throws JsonParseException {

        JsonObject user = je.getAsJsonObject().getAsJsonObject("data").getAsJsonObject("user");
        JsonArray resultsTribes = user.getAsJsonArray("tribes");
        JsonArray resultsChatMessages = user.getAsJsonArray("unSeenMessages");
        JsonArray resultsTribesSent = je.getAsJsonObject().getAsJsonObject("data").getAsJsonArray("tribes");
        JsonArray resultsMessagesSent = je.getAsJsonObject().getAsJsonObject("data").getAsJsonArray("messages");

        List<MessageRealmInterface> messages = new ArrayList<>();

        List<TribeRealm> tribeRealmList = new ArrayList<>();
        for (JsonElement obj : resultsTribes) {
            TribeRealm tribeRealm = parseTribe(obj);
            if (((tribeRealm.isToGroup() && tribeRealm.getGroup() != null) || !tribeRealm.isToGroup()))
                tribeRealmList.add(tribeRealm);
        }

        for (TribeRealm tribeRealm : tribeRealmList) {
            if (tribeRealm.getRecipientList() != null) {
                int countSeen = 0;

                for (MessageRecipientRealm recipient : tribeRealm.getRecipientList()) {
                    if (recipient.isSeen()) countSeen++;
                }

                //if (countSeen == tribeRealm.getRecipientList().size()) tribeRealm.setMessageSendingStatus(MessageSendingStatus.STATUS_OPENED);
                //else tribeRealm.setMessageSendingStatus(MessageSendingStatus.STATUS_OPENED_PARTLY);
            }
        }

        messages.addAll(tribeRealmList);

        List<ChatRealm> chatRealmList = new ArrayList<>();
        for (JsonElement obj : resultsChatMessages) {
            ChatRealm chatRealm = parseChat(obj);

            if (((chatRealm.isToGroup() && chatRealm.getGroup() != null) || !chatRealm.isToGroup()))
                chatRealmList.add(chatRealm);
        }

//        for (ChatRealm chatRealm : chatRealmList) {
//            if (chatRealm.getRecipientList() != null) {
//                int countSeen = 0;
//
//                for (MessageRecipientRealm recipient : chatRealm.getRecipientList()) {
//                    if (recipient.isSeen()) countSeen++;
//                }
//
//                if (countSeen == chatRealm.getRecipientList().size()) chatRealm.setMessageSendingStatus(MessageSendingStatus.STATUS_OPENED);
//                else chatRealm.setMessageSendingStatus(MessageSendingStatus.STATUS_OPENED_PARTLY);
//            }
//        }

        messages.addAll(chatRealmList);

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

//        if (resultsMessagesSent != null) {
//            for (JsonElement obj : resultsMessagesSent) {
//                if (!(obj instanceof JsonNull)) {
//                    ChatRealm chatRealm = new ChatRealm();
//                    JsonObject json = obj.getAsJsonObject();
//                    chatRealm.setId(json.get("id").getAsString());
//                    chatRealm.setRecipientList(chatCache.createMessageRecipientRealm(parseRecipients(chatRealm.getId(), json.getAsJsonArray("recipients"))));
//                    messages.add(chatRealm);
//                }
//            }
//        }

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

        UserRealm from = new UserRealm();
        from.setId(json.get("from").getAsString());
        tribeRealm.setFrom(from);

        LocationRealm locationRealm = new LocationRealm();
        locationRealm.setLatitude(json.get("lat") instanceof JsonNull ? 0.0D : json.get("lat").getAsDouble());
        locationRealm.setLongitude(json.get("lng") instanceof JsonNull ? 0.0D : json.get("lng").getAsDouble());
        locationRealm.setCity(!(json.get("location") instanceof JsonNull) ? json.getAsJsonObject("location").get("city").getAsString() : null);
        locationRealm.setHasLocation(!(json.get("lat") instanceof JsonNull));
        tribeRealm.setLocationRealm(locationRealm);

        tribeRealm.setMessageReceivingStatus(MessageReceivingStatus.STATUS_RECEIVED);
        tribeRealm.setMessageDownloadingStatus(MessageDownloadingStatus.STATUS_TO_DOWNLOAD);

        tribeRealm.setType(json.get("type").getAsString());
        tribeRealm.setUrl(json.get("url").getAsString());

        tribeRealm.setRecipientList(tribeCache.createTribeRecipientRealm(parseRecipients(tribeRealm.getId(), json.getAsJsonArray("recipients"))));

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

        UserRealm from = new UserRealm();
        from.setId(json.get("from").getAsString());
        chatRealm.setFrom(from);

        chatRealm.setType(json.get("type").getAsString());
        chatRealm.setContent(json.get("content").getAsString());

        chatRealm.setMessageReceivingStatus(MessageReceivingStatus.STATUS_RECEIVED);
        chatRealm.setMessageDownloadingStatus(MessageDownloadingStatus.STATUS_TO_DOWNLOAD);

//        chatRealm.setRecipientList(chatCache.createMessageRecipientRealm(parseRecipients(chatRealm.getId(), json.getAsJsonArray("recipients"))));

        try {
            chatRealm.setRecordedAt(utcSimpleDate.parse(json.get("recorded_at").getAsString()));
            chatRealm.setCreatedAt(utcSimpleDate.parse(json.get("created_at").getAsString()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return chatRealm;
    }

    /***
     *
     * @param id the id of the tribe / message
     * @param recipients the json holding the recipients data
     */
    private List<MessageRecipientRealm> parseRecipients(String id, JsonArray recipients) {
        List<MessageRecipientRealm> messageRecipientRealmList = new ArrayList<>();

        for (JsonElement recipient : recipients) {
            JsonObject jsonRecipient = recipient.getAsJsonObject();
            MessageRecipientRealm chatRecipientRealm = new MessageRecipientRealm();
            chatRecipientRealm.setId(id + jsonRecipient.get("to").getAsString());
            chatRecipientRealm.setTo(jsonRecipient.get("to").getAsString());
            chatRecipientRealm.setIsSeen(currentUser.getId().equals(chatRecipientRealm.getTo()) ? true : jsonRecipient.get("is_seen").getAsBoolean());
            messageRecipientRealmList.add(chatRecipientRealm);
        }

        return messageRecipientRealmList;
    }
}