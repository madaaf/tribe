package com.tribe.app.data.repository.chat;

import com.tribe.app.data.realm.mapper.MessageRealmDataMapper;
import com.tribe.app.data.realm.mapper.UserRealmDataMapper;
import com.tribe.app.data.repository.chat.datasource.ChatDataStore;
import com.tribe.app.data.repository.chat.datasource.ChatDataStoreFactory;
import com.tribe.app.domain.interactor.chat.ChatRepository;
import com.tribe.app.presentation.utils.DateUtils;
import com.tribe.app.presentation.view.widget.chat.Message;
import java.util.ArrayList;
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

  @Override public Observable<Message> createMessage(String[] userIds, String type, String data) {
    String date = dateUtils.getUTCDateAsString();
    final ChatDataStore userDataStore = this.chatDataStoreFactory.createCloudDataStore();
    return userDataStore.createMessage(userIds, type, data, date).doOnError(throwable -> {
      throwable.printStackTrace();
    }).map(this.messageRealmDataMapper::transform);
  }

  @Override public Observable<List<Message>> userMessageInfo(String[] userIds) {
    final ChatDataStore userDataStore = this.chatDataStoreFactory.createCloudDataStore();
    return userDataStore.userMessage(userIds).doOnError(throwable -> {
      throwable.printStackTrace();
    }).map(userRealm -> {
      List<Message> messageList = new ArrayList<Message>();
      messageList = this.userRealmDataMapper.transform(userRealm, false).getMessages();
      return messageList;
    });
  }
}
