package com.tribe.app.data.repository.chat.datasource;

import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.presentation.view.widget.chat.model.Conversation;
import java.util.List;
import rx.Observable;

/**
 * Created by madaaflak on 12/09/2017.
 */

public interface ChatDataStore {

  Observable<MessageRealm> createMessage(final String[] userIds, String type, String data,
      String date);

  Observable<UserRealm> loadMessages(final String[] userIds, String dateBefore, String dateAfter);

  Observable<List<MessageRealm>> getMessages(String[] userIds);

  Observable<List<MessageRealm>> getMessagesImage(String[] userIds);

  Observable<String> isTyping();

  Observable<String> isTalking();

  Observable<String> isReading();

  Observable<List<MessageRealm>> onMessageReceived();

  Observable<MessageRealm> onMessageRemoved();

  Observable<Boolean> imTyping(String[] userIds);


  Observable<List<Conversation>> getMessageSupport();
}
