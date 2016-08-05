package com.tribe.app.data.network.deserializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tribe.app.data.realm.GroupRealm;
import com.tribe.app.data.realm.LocationRealm;
import com.tribe.app.data.realm.RecipientRealm;
import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.data.realm.UserTribeRealm;
import com.tribe.app.data.realm.WeatherRealm;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.realm.RealmList;

public class UserTribeListDeserializer<T> implements JsonDeserializer<T> {

    private SimpleDateFormat utcSimpleDate;

    @Inject
    public UserTribeListDeserializer(SimpleDateFormat utcSimpleDate) {
        this.utcSimpleDate = utcSimpleDate;
    }

    @Override
    public T deserialize(JsonElement je, Type typeOfT,
                               JsonDeserializationContext context) throws JsonParseException {

        JsonArray results = je.getAsJsonObject().getAsJsonObject("data").getAsJsonObject("user").getAsJsonArray("tribes");
        JsonArray resultsTribesSent = je.getAsJsonObject().getAsJsonObject("data").getAsJsonArray("tribes");

        List<TribeRealm> tribes = new ArrayList<>();

        for (JsonElement obj : results) {
            TribeRealm tribeRealm = parseTribe(obj);
            tribes.add(tribeRealm);
        }

        for (JsonElement obj : resultsTribesSent) {
            TribeRealm tribeRealm = new TribeRealm();
            JsonObject json = obj.getAsJsonObject();
            tribeRealm.setId(json.get("id").getAsString());

            RealmList<RecipientRealm> recipientRealmList = new RealmList<>();

            for (JsonElement recipient : json.getAsJsonArray("recipients")) {
                JsonObject jsonRecipient = recipient.getAsJsonObject();
                RecipientRealm recipientRealm = new RecipientRealm();
                UserTribeRealm userTribeRealm = new UserTribeRealm();
                userTribeRealm.setId(jsonRecipient.get("to").getAsString());
                recipientRealm.setTo(userTribeRealm);
                recipientRealm.setIsSeen(jsonRecipient.get("is_seen").getAsBoolean());
                recipientRealmList.add(recipientRealm);
            }

            tribeRealm.setRecipientList(recipientRealmList);
            tribes.add(tribeRealm);
        }

        return (T) tribes;
    }

    private TribeRealm parseTribe(JsonElement obj) {
        TribeRealm tribeRealm = new TribeRealm();
        UserTribeRealm userTribeRealm = null;
        GroupRealm groupRealm = null;
        JsonObject json = obj.getAsJsonObject();
        tribeRealm.setId(json.get("id").getAsString());
        tribeRealm.setLocalId(json.get("id").getAsString());

        boolean toGroup = json.get("to_group").getAsBoolean();

        if (toGroup) {
            groupRealm = new GroupRealm();
            groupRealm.setId(json.get("to").getAsString());
            tribeRealm.setGroup(groupRealm);
        } else {
            userTribeRealm = new UserTribeRealm();
            userTribeRealm.setId(json.get("to").getAsString());
            tribeRealm.setUser(userTribeRealm);
        }

        tribeRealm.setToGroup(toGroup);

        UserTribeRealm from = new UserTribeRealm();
        from.setId(json.get("from").getAsString());
        tribeRealm.setFrom(from);

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
}