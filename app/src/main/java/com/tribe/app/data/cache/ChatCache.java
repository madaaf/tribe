package com.tribe.app.data.cache;

import com.tribe.app.data.realm.MessageRealm;
import io.realm.RealmList;
import java.util.List;
import javax.inject.Singleton;
import rx.Observable;

/**
 * Created by madaaflak on 12/09/2017.
 */

@Singleton public interface ChatCache {

  void putMessages(RealmList<MessageRealm> messages, String userIds);

  Observable<List<MessageRealm>> onMessageReceived();

  void onTyping(String userIds);

  Observable<String> isTyping();

  Observable<List<MessageRealm>> getMessages(String[] userIds);

  void setOnMessageReceived(RealmList<MessageRealm> messages);
}

