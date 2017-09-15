package com.tribe.app.data.cache;

import android.content.Context;
import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.tribelivesdk.util.JsonUtils;
import io.realm.Realm;
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
    return realm.where(UserRealm.class)
        .equalTo("id", JsonUtils.arrayToJson(userIds))
        .findAll()
        .asObservable()
        .filter(userRealmList -> userRealmList.isLoaded() && userRealmList.size() > 0)
        .map(userRealmList -> userRealmList.get(0))
        .map(user -> realm.copyFromRealm(user))
        .unsubscribeOn(AndroidSchedulers.mainThread());
  }
}
