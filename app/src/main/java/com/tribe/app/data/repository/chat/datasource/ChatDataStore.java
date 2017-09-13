package com.tribe.app.data.repository.chat.datasource;

import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.presentation.view.widget.chat.Message;
import rx.Observable;

/**
 * Created by madaaflak on 12/09/2017.
 */

public interface ChatDataStore {

  Observable<MessageRealm> createMessage(final String[] userIds, String type, String data,
      String date);

  Observable<UserRealm> userMessage(final String[] userIds);

  Observable<Message> createdMessages();
}
