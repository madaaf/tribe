package com.tribe.app.data.repository.chat.datasource;

import android.content.Context;
import com.tribe.app.R;
import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.data.realm.UserRealm;
import java.util.List;
import rx.Observable;
import rx.functions.Action1;

/**
 * Created by madaaflak on 12/09/2017.
 */

public class CloudChatDataStore implements ChatDataStore {

  private final Context context;
  private final TribeApi tribeApi;
  private ChatCache chatCache;

  public CloudChatDataStore(Context context, TribeApi tribeApi, ChatCache chatCache) {
    this.context = context;
    this.tribeApi = tribeApi;
    this.chatCache = chatCache;
  }

  @Override
  public Observable<MessageRealm> createMessage(String[] userIds, String type, String data,
      String date) {
    return this.tribeApi.createMessage(
        context.getString(R.string.messages_create, arrayToJson(userIds), type, data,
            context.getString(R.string.messagefragment_info)));
  }

  @Override public Observable<UserRealm> loadMessages(String[] userIds) {
    return this.tribeApi.getUserMessage(
        context.getString(R.string.messages_details, arrayToJson(userIds),
            context.getString(R.string.messagefragment_info))).doOnNext(saveToCacheUser);
  }

  private final Action1<UserRealm> saveToCacheUser = userRealm -> {
    //this.userCache.put(userRealm);
    chatCache.putMessages(userRealm.getMessages());
  };

  @Override public Observable<MessageRealm> createdMessages() {
    return null;
  }

  @Override public Observable<List<MessageRealm>> getMessages() {
    return null;
  }

  public String arrayToJson(String[] array) {
    String json = "\"";
    for (int i = 0; i < array.length; i++) {
      if (i == array.length - 1) {
        json += array[i] + "\"";
      } else {
        json += array[i] + "\", \"";
      }
    }
    if (array.length == 0) json += "\"";
    return json;
  }
}
