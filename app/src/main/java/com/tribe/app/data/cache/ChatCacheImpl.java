package com.tribe.app.data.cache;

import android.content.Context;
import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.data.realm.UserRealm;
import io.realm.Realm;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

/**
 * Created by madaaflak on 12/09/2017.
 */

public class ChatCacheImpl implements ChatCache {

  private Context context;
  private Realm realm;

  private PublishSubject<MessageRealm> onMessageCreated = PublishSubject.create();
  private PublishSubject<List<MessageRealm>> onLoadMessage = PublishSubject.create();

  @Inject public ChatCacheImpl(Context context, Realm realm) {
    this.context = context;
    this.realm = realm;
  }

  @Override public void messageCreated(MessageRealm message) {
    onMessageCreated.onNext(message);
  }

  @Override public Observable<MessageRealm> getMessageCreated() {
    return onMessageCreated;
  }

  @Override public Observable<UserRealm> loadMessage(String[] userIds) {
    return null;
  }

  @Override public void putMessages(List<MessageRealm> messages) {
    //onLoadMessage.onNext(messages);
    Realm realm = Realm.getDefaultInstance();
    try {
      realm.executeTransaction(realm1 -> {
        for (MessageRealm messageRealm : messages) {
          MessageRealm messageDB =
              realm1.where(MessageRealm.class).equalTo("id", messageRealm.getId()).findFirst();
          realm1.insertOrUpdate(messageDB);
        }
      });
    } finally {
      realm.close();
    }
  }

  @Override public Observable<List<MessageRealm>> getMessages() {
    return realm.where(MessageRealm.class).findAll().asObservable().map(singleShortcutList -> {
      List<MessageRealm> list = realm.copyFromRealm(singleShortcutList);
      return list;
    }).unsubscribeOn(AndroidSchedulers.mainThread());
  }
}
