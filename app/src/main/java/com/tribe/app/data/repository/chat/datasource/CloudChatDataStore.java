package com.tribe.app.data.repository.chat.datasource;

import android.content.Context;
import com.tribe.app.R;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.presentation.view.widget.chat.Message;
import rx.Observable;
import timber.log.Timber;

/**
 * Created by madaaflak on 12/09/2017.
 */

public class CloudChatDataStore implements ChatDataStore {

  private final Context context;
  private final TribeApi tribeApi;

  public CloudChatDataStore(Context context, TribeApi tribeApi) {
    this.context = context;
    this.tribeApi = tribeApi;
  }

  @Override
  public Observable<MessageRealm> createMessage(String[] userIds, String type, String data,
      String date) {
    String request =
        context.getString(R.string.messages_create, arrayToJson(userIds), type, date, data,
            context.getString(R.string.messagefragment_info));
    Timber.i("SOEF CREATE MESSAGE REQUEST " + request);
    return this.tribeApi.createMessage(
        context.getString(R.string.messages_create, arrayToJson(userIds), type, date, data,
            context.getString(R.string.messagefragment_info)));
  }

  @Override public Observable<UserRealm> userMessage(String[] userIds) {
    return this.tribeApi.getUserMessage(
        context.getString(R.string.messages_details, arrayToJson(userIds),
            context.getString(R.string.messagefragment_info)));
  }

  @Override public Observable<Message> createdMessages() {
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
