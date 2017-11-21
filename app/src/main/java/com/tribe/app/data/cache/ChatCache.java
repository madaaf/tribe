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

  void onTalking(String userIds);

  void onReading(String userIds);

  Observable<String> isTyping();

  Observable<String> isTalking();

  Observable<String> isReading();

  Observable<List<MessageRealm>> getMessages(String[] userIds);

  MessageRealm getLastTextMessage(String[] userIds);

  Observable<List<MessageRealm>> getMessagesImage(String[] userIds);

  void setOnMessageReceived(RealmList<MessageRealm> messages);

  void deleteInCacheRemovedMessage(RealmList<MessageRealm> messages, String s,  String dateBefore, String dateAfter);
}

