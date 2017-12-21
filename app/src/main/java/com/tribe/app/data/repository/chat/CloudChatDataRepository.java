package com.tribe.app.data.repository.chat;

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
import timber.log.Timber;

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

  @Override public Observable<Message> createMessage(String[] userIds, String type, String data) {
    String date = dateUtils.getUTCDateAsString();
    final ChatDataStore userDataStore = this.chatDataStoreFactory.createCloudDataStore();
    return userDataStore.createMessage(userIds, type, data, date)
        .doOnError(Throwable::printStackTrace)
        .map(this.messageRealmDataMapper::transform);
  }

  @Override public Observable<List<Conversation>> getMessageSupport(String lang, int typeSupport) {
    final ChatDataStore userDataStore = this.chatDataStoreFactory.createCloudDataStore();
    return userDataStore.getMessageSupport(typeSupport);
  }

  @Override public Observable addMessageSupportDisk(Message message) {
    return null;
  }

  @Override public Observable<List<Message>> loadMessages(String[] userIds, String dateBefore,
      String dateAfter) {
    final ChatDataStore userDataStore = this.chatDataStoreFactory.createCloudDataStore();
    return userDataStore.loadMessages(userIds, dateBefore, dateAfter)
        .doOnError(Throwable::printStackTrace)
        .map(userRealm -> this.userRealmDataMapper.transform(userRealm).getMessages());
  }

  @Override public Observable<List<Message>> getMessageZendesk(String lang, int typeSupport,
      String supportId) {
    final ChatDataStore userDataStore = this.chatDataStoreFactory.createCloudDataStore();
    return userDataStore.getMessageZendesk(supportId)
        .doOnError(Throwable::printStackTrace)
        .map(list -> {
          Timber.e("SOEF " + list);
          return list;
        });
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
