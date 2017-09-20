package com.tribe.app.data.cache;

import android.content.Context;
import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.tribelivesdk.util.JsonUtils;
import io.realm.Realm;
import io.realm.RealmList;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import timber.log.Timber;

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

  @Override public void putMessages(RealmList<MessageRealm> messages, String userId) {
    Realm obsRealm = Realm.getDefaultInstance();

    try {
      obsRealm.executeTransaction(realm1 -> {
        realm1.delete(MessageRealm.class);

        for (MessageRealm message : messages) {
          MessageRealm m = realm1.createObject(MessageRealm.class, message.getId());

          m.setLocalId(userId);
          m.set__typename(message.get__typename());
          m.setOriginal(message.getOriginal());
          m.setAlts(message.getAlts());
          m.setAction(message.getAction());
          m.setCreated_at(message.getCreated_at());
          UserRealm userRealm;

          if (message.getAuthor() != null && message.getAuthor().getId() != null) {
            UserRealm userRealmDB = realm1.where(UserRealm.class)
                .equalTo("id", message.getAuthor().getId())
                .findFirst();

            if (userRealmDB == null) {
              userRealm = realm1.createObject(UserRealm.class, message.getAuthor().getId());
              userRealm.setDisplayName(message.getAuthor().getDisplayName());
              userRealm.setProfilePicture(message.getAuthor().getProfilePicture());
            } else {
              userRealm = userRealmDB;
            }

            m.setAuthor(userRealm);
          } else if (message.getUser() != null && message.getUser().getId() != null) {
            UserRealm userRealmDB =
                realm1.where(UserRealm.class).equalTo("id", message.getUser().getId()).findFirst();
            if (userRealmDB == null) {
              userRealm = realm1.createObject(UserRealm.class, message.getUser().getId());
              userRealm.setDisplayName(message.getUser().getDisplayName());
              userRealm.setProfilePicture(message.getUser().getProfilePicture());
            } else {
              userRealm = userRealmDB;
            }
            m.setUser(userRealm);
          }
          realm1.insertOrUpdate(m);
        }
      });
    } catch (Exception ex) {
      Timber.e("EXCEPTION SOEF " + ex.toString());
    } finally {
      obsRealm.close();
    }
  }

  @Override public Observable<List<MessageRealm>> getMessages(String[] userIds) {
    return realm.where(MessageRealm.class)
        .equalTo("localId", JsonUtils.arrayToJson(userIds))
        .findAll()
        .asObservable()
        .filter(singleShortcutList -> singleShortcutList.isLoaded())
        .map(singleShortcutList -> {

          List<MessageRealm> list = new ArrayList<>();
          list.addAll(singleShortcutList);
          Timber.e("SOEF singleShortcutList :"
              + realm.copyFromRealm(singleShortcutList).size()
              + " "
              + singleShortcutList.size());
          return list;
        })
        .unsubscribeOn(AndroidSchedulers.mainThread());
  }
}
