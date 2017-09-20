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
import rx.functions.Action1;
import timber.log.Timber;

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

  @Override public Observable<List<Message>> loadMessages(String[] userIds) {
    final DiskChatDataStore chatDataStore =
        (DiskChatDataStore) this.chatDataStoreFactory.createDiskDataStore();
    Timber.e("SOEF LOAD MESSAGE");
/*    return chatDataStore.loadMessages(null).doOnError(Throwable::printStackTrace).map(userRealm -> {
      return userRealmDataMapper.transform(userRealm).getMessages();
    });*/

    return chatDataStore.getMessages(userIds).doOnError(new Action1<Throwable>() {
      @Override public void call(Throwable throwable) {
        Timber.e("SOEF DISK CHAT ERROR getMessages " + chatDataStore);
      }
    }).map(list -> {
      List<Message> ok = messageRealmDataMapper.transform(list);
      Timber.e("SOEF DISK CHAT DATA STORE " + ok.size());
      return ok;
    });
  }

  @Override public Observable<Message> createdMessages() {
    final DiskChatDataStore chatDataStore =
        (DiskChatDataStore) this.chatDataStoreFactory.createDiskDataStore();
    return chatDataStore.createdMessages()
        .doOnError(Throwable::printStackTrace)
        .map(messageRealmDataMapper::transform);
  }
}
