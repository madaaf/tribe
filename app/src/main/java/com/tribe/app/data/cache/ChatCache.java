package com.tribe.app.data.cache;

import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.data.realm.UserRealm;
import java.util.List;
import javax.inject.Singleton;
import rx.Observable;

/**
 * Created by madaaflak on 12/09/2017.
 */

@Singleton public interface ChatCache {

  void messageCreated(MessageRealm message);

  Observable<MessageRealm> getMessageCreated();

  Observable<UserRealm> loadMessage(String[] userIds);

  void putMessages(List<MessageRealm> messages);

  Observable<List<MessageRealm>> getMessages();
}

