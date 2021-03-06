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
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.utils.DateUtils;
import com.tribe.app.presentation.view.ShortcutUtil;
import com.tribe.app.presentation.view.utils.DeviceUtils;
import com.tribe.app.presentation.view.widget.chat.model.Conversation;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import com.tribe.app.presentation.view.widget.chat.model.MessageText;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DataSupportMessageDeserializer implements JsonDeserializer<List<Conversation>> {

  private static String CONVERSATONS = "conversations";
  private static String DEFAULT = "default";

  private String lang;
  private DateUtils dateUtils;

  public DataSupportMessageDeserializer(Context context, DateUtils dateUtils) {
    this.lang = DeviceUtils.getLanguage(context);
    this.dateUtils = dateUtils;
  }

  @Override public List<Conversation> deserialize(JsonElement json, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {
    List<Conversation> nameList = new ArrayList<>();
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

    int i = 0;
    for (Conversation conversation : nameList) {
      List<Message> list = new ArrayList<>();
      User u = ShortcutUtil.createUserSupport();

      for (Message message : conversation.getMessages()) {
        i++;
        MessageText m = new MessageText(Shortcut.SUPPORT + "_" + conversation.getId() + "_" + i);
        m.setAuthor(u);
        m.setCreationDate(dateUtils.getUTCDateForMessage());
        m.setMessage(message.getContent());
        m.setType(Message.MESSAGE_TEXT);
        list.add(m);
      }
      conversation.setMessages(list);
    }

    return nameList;
  }
}