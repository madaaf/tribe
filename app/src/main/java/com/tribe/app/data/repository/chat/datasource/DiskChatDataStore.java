package com.tribe.app.data.repository.chat.datasource;

import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.data.realm.UserRealm;
import java.util.List;
import rx.Observable;

/**
 * Created by madaaflak on 12/09/2017.
 */

public class DiskChatDataStore implements ChatDataStore {

  private final ChatCache chatCache;

  public DiskChatDataStore(ChatCache chatCache) {
    this.chatCache = chatCache;
  }

  @Override
  public Observable<MessageRealm> createMessage(String[] userIds, String type, String data,
      String date) {
    return null;
  }

  @Override public Observable<UserRealm> loadMessages(String[] userIds) {
    return chatCache.loadMessage(userIds);
  }

  @Override public Observable<MessageRealm> createdMessages() {
    return chatCache.getMessageCreated();
  }

  @Override public Observable<List<MessageRealm>> getMessages() {
    return chatCache.getMessages();
  }
}
