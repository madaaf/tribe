package com.tribe.app.data.network.deserializer;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tribe.app.presentation.view.utils.DeviceUtils;
import com.tribe.app.presentation.view.widget.chat.model.Conversation;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DataSupportMessageDeserializer implements JsonDeserializer<List<Conversation>> {

  private static String CONVERSATONS = "conversations";
  private static String DEFAULT = "default";

  private String lang;

  public DataSupportMessageDeserializer(Context context) {
    this.lang = DeviceUtils.getLanguage(context);
  }

  @Override public List<Conversation> deserialize(JsonElement json, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {
    List<Conversation> nameList = new ArrayList<>();
    List<Message> messages = new ArrayList<>();
    Gson gson = new GsonBuilder().create();

    JsonObject results = null;
    if (((JsonObject) json).has(DEFAULT)) {
      results = json.getAsJsonObject().getAsJsonObject(DEFAULT);
    }

    if (results != null) {
      JsonArray convList = results.getAsJsonArray("conversations");

      if (convList != null) {
        for (JsonElement obj : convList) {
          if (!(obj instanceof JsonNull)) {
            Conversation c = gson.fromJson(obj, Conversation.class);
            nameList.add(c);
          }
        }
      }
    }

    return nameList;
  }
}