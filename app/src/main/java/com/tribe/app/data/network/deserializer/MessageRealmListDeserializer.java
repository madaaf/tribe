package com.tribe.app.data.network.deserializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.data.cache.TribeCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.ChatRealm;
import com.tribe.app.data.realm.LocationRealm;
import com.tribe.app.data.realm.MessageRecipientRealm;
import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.data.realm.WeatherRealm;
import com.tribe.app.presentation.view.utils.MessageDownloadingStatus;
import com.tribe.app.presentation.view.utils.MessageReceivingStatus;
import com.tribe.app.presentation.view.utils.MessageSendingStatus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tiago on 31/08/2016.
 */
public class MessageRealmListDeserializer {

    protected SimpleDateFormat utcSimpleDate;
    protected AccessToken accessToken;
    protected UserCache userCache;
    protected TribeCache tribeCache;
    protected ChatCache chatCache;

    public MessageRealmListDeserializer(SimpleDateFormat utcSimpleDate, UserCache userCache,
                                        TribeCache tribeCache, ChatCache chatCache, AccessToken accessToken) {
        this.utcSimpleDate = utcSimpleDate;
        this.userCache = userCache;
        this.tribeCache = tribeCache;
        this.chatCache = chatCache;
        this.accessToken = accessToken;
    }

    protected List<ChatRealm> deserializeChatRealmArray(JsonArray array) {
        List<ChatRealm> chatRealmList = new ArrayList<>();

        for (JsonElement obj : array) {
            ChatRealm chatRealm = parseChat(obj);

            if (((chatRealm.isToGroup() && chatRealm.getMembershipRealm() != null) || !chatRealm.isToGroup()))
                chatRealmList.add(chatRealm);
        }

        for (ChatRealm chatRealm : chatRealmList) {
            if (chatRealm.getRecipientList() != null) {
                int countSeen = 0;

                for (MessageRecipientRealm recipient : chatRealm.getRecipientList()) {
                    if (recipient.getTo().equals(accessToken.getUserId())) recipient.setIsSeen(true);

                    if (recipient.isSeen()) countSeen++;
                }

                if (countSeen == chatRealm.getRecipientList().size()) chatRealm.setMessageSendingStatus(MessageSendingStatus.STATUS_OPENED);
                else chatRealm.setMessageSendingStatus(MessageSendingStatus.STATUS_OPENED_PARTLY);
            }
        }

        return chatRealmList;
    }

    private ChatRealm parseChat(JsonElement obj) {
        ChatRealm chatRealm = new ChatRealm();
        JsonObject json = obj.getAsJsonObject();
        chatRealm.setId(json.get("id").getAsString());
        chatRealm.setLocalId(json.get("id").getAsString());

        boolean toGroup = json.get("to_group").getAsBoolean();

        if (toGroup) {
            chatRealm.setMembershipRealm(userCache.membershipForGroupId(json.get("to").getAsString()));
        } else {
            if (!accessToken.getUserId().equals(json.get("to").getAsString())) {
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

        chatRealm.setRecipientList(chatCache.createMessageRecipientRealm(parseRecipients(chatRealm.getId(), json.getAsJsonArray("recipients"))));

        try {
            chatRealm.setRecordedAt(utcSimpleDate.parse(json.get("recorded_at").getAsString()));
            chatRealm.setCreatedAt(utcSimpleDate.parse(json.get("created_at").getAsString()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return chatRealm;
    }

    protected List<TribeRealm> deserializeTribeRealmArray(JsonArray array) {
        List<TribeRealm> tribeRealmList = new ArrayList<>();
        for (JsonElement obj : array) {
            if (obj != null && !(obj instanceof JsonNull)) {
                TribeRealm tribeRealm = parseTribe(obj);
                if (((tribeRealm.isToGroup() && tribeRealm.getMembershipRealm() != null) || !tribeRealm.isToGroup()))
                    tribeRealmList.add(tribeRealm);
            }
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

        return tribeRealmList;
    }

    private TribeRealm parseTribe(JsonElement obj) {
        TribeRealm tribeRealm = new TribeRealm();
        JsonObject json = obj.getAsJsonObject();
        tribeRealm.setId(json.get("id").getAsString());
        tribeRealm.setLocalId(json.get("id").getAsString());

        boolean toGroup = json.get("to_group").getAsBoolean();

        if (toGroup) {
            tribeRealm.setMembershipRealm(userCache.membershipForGroupId(json.get("to").getAsString()));
        } else {
            if (!accessToken.getUserId().equals(json.get("to").getAsString())) {
                tribeRealm.setFriendshipRealm(userCache.friendshipForUserId(json.get("to").getAsString()));
            }
        }

        tribeRealm.setToGroup(toGroup);

        UserRealm from = new UserRealm();
        from.setId(json.get("from").getAsString());
        tribeRealm.setFrom(from);

        LocationRealm locationRealm = new LocationRealm();
        locationRealm.setId(from.getId());

        if (!(json.get("coord") instanceof JsonNull)) {
            JsonObject coord = json.get("coord").getAsJsonObject();
            locationRealm.setLatitude(coord.get("latitude").getAsDouble());
            locationRealm.setLongitude(coord.get("longitude").getAsDouble());

            if (json.get("location") != null && !json.get("location").isJsonNull()) {
                JsonObject location = json.get("location").getAsJsonObject();
                locationRealm.setCity((location.get("city") != null && !location.get("city").isJsonNull()) ? location.get("city").getAsString() : "");
                locationRealm.setCountryCode((location.get("country") != null && !location.get("country").isJsonNull()) ? location.get("country").getAsString() : "");
            }

            tribeRealm.setLocationRealm(locationRealm);
            locationRealm.setHasLocation(true);
        }

        tribeRealm.setMessageReceivingStatus(MessageReceivingStatus.STATUS_RECEIVED);
        tribeRealm.setMessageDownloadingStatus(MessageDownloadingStatus.STATUS_TO_DOWNLOAD);

        tribeRealm.setType(json.get("type").getAsString());
        tribeRealm.setUrl(json.get("url").getAsString());

        //tribeRealm.setRecipientList(tribeCache.createTribeRecipientRealm(parseRecipients(tribeRealm.getId(), json.getAsJsonArray("recipients"))));

        if (!(json.get("weather") instanceof JsonNull)) {
            JsonObject weather = json.get("weather").getAsJsonObject();
            WeatherRealm weatherRealm = new WeatherRealm();
            JsonElement icon = weather.get("icon");
            weatherRealm.setIcon((icon != null && !(icon instanceof JsonNull)) ? icon.getAsString() : "");
            weatherRealm.setTempC(weather.get("temp_c").getAsInt());
            weatherRealm.setTempF(weather.get("temp_f").getAsInt());
            tribeRealm.setWeatherRealm(weatherRealm);
        }

        if (json.has("transcript") && !(json.get("transcript") instanceof JsonNull)) {
            tribeRealm.setTranscript(json.get("transcript").getAsString());
        }

        tribeRealm.setCanSave(json.get("can_save").getAsBoolean());

        try {
            tribeRealm.setRecordedAt(utcSimpleDate.parse(json.get("recorded_at").getAsString()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return tribeRealm;
    }

    /***
     *
     * @param id the id of the tribe / message
     * @param recipients the json holding the recipients data
     */
    protected List<MessageRecipientRealm> parseRecipients(String id, JsonArray recipients) {
        List<MessageRecipientRealm> messageRecipientRealmList = new ArrayList<>();

        for (JsonElement recipient : recipients) {
            JsonObject jsonRecipient = recipient.getAsJsonObject();
            MessageRecipientRealm chatRecipientRealm = new MessageRecipientRealm();
            chatRecipientRealm.setId(id + jsonRecipient.get("to").getAsString());
            chatRecipientRealm.setTo(jsonRecipient.get("to").getAsString());
            chatRecipientRealm.setIsSeen(accessToken.getUserId().equals(chatRecipientRealm.getTo()) ? true : jsonRecipient.get("is_seen").getAsBoolean());
            messageRecipientRealmList.add(chatRecipientRealm);
        }

        return messageRecipientRealmList;
    }
}
