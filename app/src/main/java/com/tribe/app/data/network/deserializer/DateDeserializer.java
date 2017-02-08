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
  private SimpleDateFormat tempSimpleDate;

  @Inject public DateDeserializer(SimpleDateFormat utcSimpleDate, SimpleDateFormat tempSimpleDate) {
    this.utcSimpleDate = utcSimpleDate;
    this.tempSimpleDate = tempSimpleDate;
  }

  @Override public Date deserialize(JsonElement element, Type arg1, JsonDeserializationContext arg2)
      throws JsonParseException {
    String date = element.getAsString();

    try {
      return utcSimpleDate.parse(date);
    } catch (ParseException e) {
      try {
        return tempSimpleDate.parse(date);
      } catch (ParseException e1) {
        e1.printStackTrace();
        return null;
      }
    }
  }
}