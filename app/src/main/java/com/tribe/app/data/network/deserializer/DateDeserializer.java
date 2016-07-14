package com.tribe.app.data.network.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

public class DateDeserializer implements JsonDeserializer<Date> {

    private SimpleDateFormat utcSimpleDate;

    @Inject
    public DateDeserializer(SimpleDateFormat utcSimpleDate) {
        this.utcSimpleDate = utcSimpleDate;
    }

    @Override
    public Date deserialize(JsonElement element, Type arg1, JsonDeserializationContext arg2) throws JsonParseException {
        String date = element.getAsString();

        try {
            return utcSimpleDate.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}