package com.tribe.app.data.network.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.tribe.app.data.network.entity.LookupObject;
import java.lang.reflect.Type;
import java.util.List;
import timber.log.Timber;

public class LookupObjectDeserializer implements JsonDeserializer<List<LookupObject>> {

  @Override public List<LookupObject> deserialize(JsonElement je, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {

    Timber.e("SOEF " + je);
    return null;
  }
}

