package com.tribe.app.data.network.deserializer;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.tribe.app.data.realm.MembershipRealm;

import java.lang.reflect.Type;

public class NewMembershipDeserializer implements JsonDeserializer<MembershipRealm> {

  @Override public MembershipRealm deserialize(JsonElement je, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {
    JsonObject results = je.getAsJsonObject().getAsJsonObject("data");
    JsonElement toParse = null;

    if (results.getAsJsonObject("createMembership") != null) {
      toParse = results.getAsJsonObject("createMembership");
    }

    if (results.getAsJsonObject("generatePrivateLink") != null) {
      toParse = results.getAsJsonObject("generatePrivateLink");
    }

    if (results.getAsJsonObject("removePrivateLink") != null) {
      toParse = results.getAsJsonObject("removePrivateLink");
    }

    return new Gson().fromJson(toParse, typeOfT);
  }
}