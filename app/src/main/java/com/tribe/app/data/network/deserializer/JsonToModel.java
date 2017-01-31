package com.tribe.app.data.network.deserializer;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tribe.app.data.network.entity.SubscriptionResponse;
import com.tribe.app.data.realm.GroupRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.presentation.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Created by tiago on 27/01/2017.
 */

@Singleton public class JsonToModel {

  private Gson gson;

  @Inject public JsonToModel(@Named("simpleGson") Gson gson) {
    this.gson = gson;
  }

  public SubscriptionResponse convertToSubscriptionResponse(String json) {
    if (!StringUtils.isEmpty(json) && json.contains("data")) {
      JsonElement jsonElement = gson.fromJson(json, JsonElement.class);
      JsonObject jsonObject = jsonElement.getAsJsonObject();
      JsonObject results = jsonObject.getAsJsonObject("data");

      SubscriptionResponse subscriptionResponse = new SubscriptionResponse();
      List<UserRealm> updatedUserList = new ArrayList<>();
      List<GroupRealm> updatedGroupList = new ArrayList<>();
      Map<String, Boolean> onlineMap = new HashMap<>();
      Map<String, Boolean> liveMap = new HashMap<>();

      for (Map.Entry<String, JsonElement> entry : results.entrySet()) {
        if (!entry.getValue().isJsonNull()) {
          String payload = entry.getValue().toString();
          JsonParser jsonParser = new JsonParser();
          JsonObject jo = jsonParser.parse(payload).getAsJsonObject();

          if (entry.getKey().contains("___u")) {
            boolean shouldUpdateOnlineStatus = false;

            if (jo.has("is_online")) shouldUpdateOnlineStatus = true;

            UserRealm userRealm = gson.fromJson(entry.getValue().toString(), UserRealm.class);
            userRealm.setJsonPayloadUpdate(jo);

            if (shouldUpdateOnlineStatus) {
              onlineMap.put(userRealm.getId(), userRealm.isOnline());
              userRealm.setIsOnline(false);
            }

            updatedUserList.add(userRealm);
          } else if (entry.getKey().contains("___g")) {
            GroupRealm groupRealm = gson.fromJson(entry.getValue().toString(), GroupRealm.class);
            groupRealm.setJsonPayloadUpdate(jo);
            updatedGroupList.add(groupRealm);
          }
        }
      }

      subscriptionResponse.setUserUpdatedList(updatedUserList);
      subscriptionResponse.setGroupUpdatedList(updatedGroupList);
      subscriptionResponse.setOnlineMap(onlineMap);
      subscriptionResponse.setLiveMap(liveMap);

      return subscriptionResponse;
    }

    return null;
  }
}
