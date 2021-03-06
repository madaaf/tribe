package com.tribe.app.data.repository.chat;

import android.net.Uri;
import com.tribe.app.data.realm.mapper.MessageRealmDataMapper;
import com.tribe.app.data.realm.mapper.UserRealmDataMapper;
import com.tribe.app.data.repository.chat.datasource.ChatDataStore;
import com.tribe.app.data.repository.chat.datasource.ChatDataStoreFactory;
import com.tribe.app.domain.interactor.chat.ChatRepository;
import com.tribe.app.presentation.utils.DateUtils;
import com.tribe.app.presentation.view.widget.chat.model.Conversation;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import rx.Observable;

/**
 * Created by madaaflak on 12/09/2017.
 */

@Singleton public class CloudChatDataRepository implements ChatRepository {

  private final DateUtils dateUtils;
  private final MessageRealmDataMapper messageRealmDataMapper;
  private final UserRealmDataMapper userRealmDataMapper;
  private final ChatDataStoreFactory chatDataStoreFactory;

  @Inject
  public CloudChatDataRepository(DateUtils dateUtils, MessageRealmDataMapper messageRealmDataMapper,
      UserRealmDataMapper userRealmDataMapper, ChatDataStoreFactory chatDataStoreFactory) {
    this.dateUtils = dateUtils;
    this.messageRealmDataMapper = messageRealmDataMapper;
    this.userRealmDataMapper = userRealmDataMapper;
    this.chatDataStoreFactory = chatDataStoreFactory;
  }

  @Override public Observable<Message> createMessage(String[] userIds, String type, String data, String gameId, String intent) {
    String date = dateUtils.getUTCDateAsString();
    final ChatDataStore userDataStore = this.chatDataStoreFactory.createCloudDataStore();
    return userDataStore.createMessage(userIds, type, data, date, gameId, intent)
        .doOnError(Throwable::printStackTrace)
        .map(this.messageRealmDataMapper::transform);
  }

  @Override
  public Observable<List<Conversation>> getMessageSupport(String lang, String typeSupport) {
    final ChatDataStore userDataStore = this.chatDataStoreFactory.createCloudDataStore();
    return userDataStore.getMessageSupport(typeSupport);
  }

  @Override public Observable<List<Message>> loadMessages(String[] userIds, String dateBefore,
      String dateAfter) {
    final ChatDataStore userDataStore = this.chatDataStoreFactory.createCloudDataStore();
    return userDataStore.loadMessages(userIds, dateBefore, dateAfter)
        .doOnError(Throwable::printStackTrace)
        .map(userRealm -> this.userRealmDataMapper.transform(userRealm, true).getMessages());
  }

  @Override public Observable<List<Message>> getMessageZendesk() {
    final ChatDataStore userDataStore = this.chatDataStoreFactory.createCloudDataStore();
    return userDataStore.getMessageZendesk()
        .doOnError(Throwable::printStackTrace)
        .map(zendeskMessages -> zendeskMessages);
  }

  @Override public Observable addMessageZendesk(String typeMedia, String data, Uri uri) {
    final ChatDataStore userDataStore = this.chatDataStoreFactory.createCloudDataStore();
    return userDataStore.addMessageZendesk(typeMedia, data, uri)
        .doOnError(Throwable::printStackTrace)
        .map(zendeskMessages -> zendeskMessages);
  }

  @Override public Observable createRequestZendesk(String data) {
    final ChatDataStore userDataStore = this.chatDataStoreFactory.createCloudDataStore();
    return userDataStore.createRequestZendesk(data);
  }

  @Override public Observable<List<Message>> getMessagesImage(String[] userIds) {
    return null;
  }

  @Override public Observable<List<Message>> onMessageReceived() {
    return null;
  }

  @Override public Observable<Message> onMessageRemoved() {
    return null;
  }

  @Override public Observable<String> isTyping() {
    return null;
  }

  @Override public Observable<String> isTalking() {
    return null;
  }

  @Override public Observable<String> isReading() {
    return null;
  }

  @Override public Observable<Boolean> imTyping(String[] userIds) {
    final ChatDataStore userDataStore = this.chatDataStoreFactory.createCloudDataStore();
    return userDataStore.imTyping(userIds);
  }
}
