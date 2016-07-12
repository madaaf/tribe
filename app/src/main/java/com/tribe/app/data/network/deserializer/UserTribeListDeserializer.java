package com.tribe.app.data.network.deserializer;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tribe.app.data.realm.GroupRealm;
import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.data.realm.UserTribeRealm;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

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

        List<TribeRealm> tribes = new ArrayList<>();

        for (JsonElement obj : results) {
            TribeRealm tribeRealm = new TribeRealm();
            UserRealm userRealm = null;
            GroupRealm groupRealm = null;
            JsonObject json = obj.getAsJsonObject();
            tribeRealm.setId(json.get("id").getAsString());
            tribeRealm.setLocalId(json.get("id").getAsString());

            if (json.get("to_group").getAsBoolean()) {
                groupRealm = new GroupRealm();
                groupRealm.setId(json.get("id").getAsString());
                tribeRealm.setGroup(groupRealm);
            } else {
                userRealm = new UserRealm();
                userRealm.setId(json.get("id").getAsString());
                tribeRealm.setUser(userRealm);
            }

            UserTribeRealm from = new UserTribeRealm();
            from.setId(json.get("from").getAsString());
            tribeRealm.setFrom(from);
            tribeRealm.setLat(json.get("lat") instanceof JsonNull ? 0.0D : json.get("lat").getAsDouble());
            tribeRealm.setLng(json.get("lng") instanceof JsonNull ? 0.0D : json.get("lng").getAsDouble());
            tribeRealm.setType(json.get("type").getAsString());
            tribeRealm.setUrl(json.get("url").getAsString());
            try {
                tribeRealm.setRecordedAt(utcSimpleDate.parse(json.get("recorded_at").getAsString()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            tribes.add(tribeRealm);
        }

        return (T) tribes;
    }
}