package com.tribe.app.data.repository.chat.datasource;

import android.net.Uri;
import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.data.realm.mapper.MessageRealmDataMapper;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.presentation.view.widget.chat.model.Conversation;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import io.realm.RealmList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rx.Observable;

/**
 * Created by madaaflak on 12/09/2017.
 */

public class DiskChatDataStore implements ChatDataStore {

  private final ChatCache chatCache;
  private final MessageRealmDataMapper messageRealmDataMapper;

  public DiskChatDataStore(ChatCache chatCache, MessageRealmDataMapper messageRealmDataMapper) {
    this.chatCache = chatCache;
    this.messageRealmDataMapper = messageRealmDataMapper;
  }

  @Override
  public Observable<MessageRealm> createMessage(String[] userIds, String type, String data,
      String date) {
    return null;
  }

  @Override
  public Observable<UserRealm> loadMessages(String[] userIds, String dateBefore, String dateAfter) {
    return null;
  }

  @Override public Observable<List<Message>> getMessageZendesk(String supportId) {
    return null;
  }

  @Override public Observable<Boolean> addMessageZendesk(String supportId, String data, Uri uri) {
    return null;
  }

  @Override public Observable<List<MessageRealm>> getMessages(String[] userIds) {
    return chatCache.getMessages(userIds).debounce(600, TimeUnit.MILLISECONDS);
  }

  @Override public Observable<List<MessageRealm>> getMessagesImage(String[] userIds) {
    return chatCache.getMessagesImage(userIds).debounce(600, TimeUnit.MILLISECONDS);
  }

  @Override public Observable<String> isTyping() {
    return chatCache.isTyping();
  }

  @Override public Observable<String> isTalking() {
    return chatCache.isTalking();
  }

  @Override public Observable<String> isReading() {
    return chatCache.isReading();
  }

  @Override public Observable<List<MessageRealm>> onMessageReceived() {
    return chatCache.onMessageReceived();
  }

  @Override public Observable<MessageRealm> onMessageRemoved() {
    return chatCache.onMessageRemoved();
  }

  @Override

  public Observable<Boolean> imTyping(String[] userIds) {
    return null;
  }

  @Override public Observable<List<Conversation>> getMessageSupport(int typeSupport) {
    return null;
  }


}
