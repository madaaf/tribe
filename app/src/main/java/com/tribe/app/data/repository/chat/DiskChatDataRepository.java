package com.tribe.app.data.repository.chat;

import com.tribe.app.data.realm.mapper.MessageRealmDataMapper;
import com.tribe.app.data.realm.mapper.UserRealmDataMapper;
import com.tribe.app.data.repository.chat.datasource.ChatDataStoreFactory;
import com.tribe.app.data.repository.chat.datasource.DiskChatDataStore;
import com.tribe.app.domain.interactor.chat.ChatRepository;
import com.tribe.app.presentation.view.widget.chat.Message;
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

  @Override public Observable<Message> createMessage(String[] userIds, String type, String data) {
    return null;
  }
  /*    return Observable.combineLatest(chatDataStore.getMessages(userIds),
          chatDataStore.createdMessages(), (loadedMessages, newMessage) -> {
            List<Message> list = messageRealmDataMapper.transform(loadedMessages);
            Message newMess = messageRealmDataMapper.transform(newMessage);
            list.add(newMess);
            return list;
          }).doOnError(throwable -> Timber.e("SOEF TRHOWABLE ERROR " + throwable.toString()));*/

  @Override public Observable<List<Message>> loadMessages(String[] userIds) {
    final DiskChatDataStore chatDataStore =
        (DiskChatDataStore) this.chatDataStoreFactory.createDiskDataStore();

    return chatDataStore.getMessages(userIds)
        .doOnError(Throwable::printStackTrace)
        .map(messageRealmDataMapper::transform);
   /* return chatDataStore.loadMessages(userIds)
        .doOnError(Throwable::printStackTrace)
        .map(userRealm -> this.userRealmDataMapper.transform(userRealm).getMessages());*/
  }

  @Override public Observable<Message> createdMessages() {
    final DiskChatDataStore chatDataStore =
        (DiskChatDataStore) this.chatDataStoreFactory.createDiskDataStore();
    return chatDataStore.createdMessages()
        .doOnError(Throwable::printStackTrace)
        .map(messageRealmDataMapper::transform);
  }
}
