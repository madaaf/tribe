package com.tribe.app.data.repository.chat;

import android.net.Uri;
import com.tribe.app.data.realm.mapper.MessageRealmDataMapper;
import com.tribe.app.data.realm.mapper.UserRealmDataMapper;
import com.tribe.app.data.repository.chat.datasource.ChatDataStoreFactory;
import com.tribe.app.data.repository.chat.datasource.DiskChatDataStore;
import com.tribe.app.domain.interactor.chat.ChatRepository;
import com.tribe.app.presentation.view.widget.chat.model.Conversation;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import rx.Observable;

/**
 * Created by madaaflak on 12/09/2017.
 */

@Singleton public class DiskChatDataRepository implements ChatRepository {

  private final ChatDataStoreFactory chatDataStoreFactory;
  private final MessageRealmDataMapper messageRealmDataMapper;
  private final UserRealmDataMapper userRealmDataMapper;

  @Inject public DiskChatDataRepository(ChatDataStoreFactory chatDataStoreFactory,
      MessageRealmDataMapper messageRealmDataMapper, UserRealmDataMapper userRealmDataMapper) {
    this.chatDataStoreFactory = chatDataStoreFactory;
    this.messageRealmDataMapper = messageRealmDataMapper;
    this.userRealmDataMapper = userRealmDataMapper;
  }

  @Override public Observable<Message> createMessage(String[] userIds, String type, String data,  String gameId, String intent) {
    return null;
  }

  @Override public Observable<List<Message>> loadMessages(String[] userIds, String o1, String o2) {
    final DiskChatDataStore chatDataStore =
        (DiskChatDataStore) this.chatDataStoreFactory.createDiskDataStore();
    return chatDataStore.getMessages(userIds).doOnError(Throwable::printStackTrace).map(list -> {
      return messageRealmDataMapper.transform(list);
    });
  }

  @Override public Observable<List<Message>> getMessageZendesk() {
    return null;
  }

  @Override public Observable<List<Message>> getMessagesImage(String[] userIds) {
    final DiskChatDataStore chatDataStore =
        (DiskChatDataStore) this.chatDataStoreFactory.createDiskDataStore();
    return chatDataStore.getMessagesImage(userIds)
        .doOnError(Throwable::printStackTrace)
        .map(messageRealmDataMapper::transform);
  }

  @Override public Observable<String> isTyping() {
    final DiskChatDataStore chatDataStore =
        (DiskChatDataStore) this.chatDataStoreFactory.createDiskDataStore();
    return chatDataStore.isTyping();
  }

  @Override public Observable<String> isTalking() {
    final DiskChatDataStore chatDataStore =
        (DiskChatDataStore) this.chatDataStoreFactory.createDiskDataStore();
    return chatDataStore.isTalking();
  }

  @Override public Observable<String> isReading() {
    final DiskChatDataStore chatDataStore =
        (DiskChatDataStore) this.chatDataStoreFactory.createDiskDataStore();
    return chatDataStore.isReading();
  }

  @Override public Observable<List<Message>> onMessageReceived() {
    final DiskChatDataStore chatDataStore =
        (DiskChatDataStore) this.chatDataStoreFactory.createDiskDataStore();
    return chatDataStore.onMessageReceived()
        .doOnError(Throwable::printStackTrace)
        .map(messageRealmDataMapper::transform);
  }

  @Override public Observable<Message> onMessageRemoved() {
    final DiskChatDataStore chatDataStore =
        (DiskChatDataStore) this.chatDataStoreFactory.createDiskDataStore();
    return chatDataStore.onMessageRemoved()
        .doOnError(Throwable::printStackTrace)
        .map(messageRealmDataMapper::transform);
  }

  public Observable<Boolean> imTyping(String[] userIds) {
    return null;
  }

  @Override
  public Observable<List<Conversation>> getMessageSupport(String lang, String typeSupport) {
    return null;
  }

  @Override public Observable addMessageZendesk(String typeMedia, String data, Uri uri) {
    return null;
  }

  @Override public Observable createRequestZendesk(String data) {
    return null;
  }
}
