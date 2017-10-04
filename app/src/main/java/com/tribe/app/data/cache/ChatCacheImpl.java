package com.tribe.app.data.cache;

import android.content.Context;
import com.tribe.app.data.realm.ImageRealm;
import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.tribelivesdk.util.JsonUtils;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
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
  private PublishSubject<String> onTyping = PublishSubject.create();
  private PublishSubject<List<MessageRealm>> onMessageReceived = PublishSubject.create();

  @Inject public ChatCacheImpl(Context context, Realm realm) {
    this.context = context;
    this.realm = realm;
  }

  @Override public Observable<List<MessageRealm>> onMessageReceived() {
    return onMessageReceived;
  }

  @Override public void setOnMessageReceived(RealmList<MessageRealm> messages) {
    onMessageReceived.onNext(messages);
  }

  @Override public void putMessages(RealmList<MessageRealm> messages, String userIds) {
    Timber.e("SOEF PUT MESSAGE IN CACH " + userIds);

    Realm obsRealm = Realm.getDefaultInstance();
    try {
      obsRealm.executeTransaction(realm1 -> {

        for (MessageRealm message : messages) {
          MessageRealm messageRealmDB =
              realm1.where(MessageRealm.class).equalTo("id", message.getId()).findFirst();

          MessageRealm m = null;
          if (messageRealmDB == null) {
            m = realm1.createObject(MessageRealm.class, message.getId());
          } else {
            m = messageRealmDB;
          }

          m.setLocalId(userIds);
          m.set__typename(message.get__typename());
          m.setData(message.getData());
          m.setAction(message.getAction());
          m.setCreated_at(message.getCreated_at());

          if (message.getOriginal() != null) {
            ImageRealm imageRealmDB = realm1.where(ImageRealm.class)
                .equalTo("url", message.getOriginal().getUrl())
                .findFirst();

            if (imageRealmDB == null) {
              imageRealmDB = realm1.createObject(ImageRealm.class, message.getOriginal().getUrl());
              imageRealmDB.setFilesize(message.getOriginal().getFilesize());
              imageRealmDB.setHeight(message.getOriginal().getHeight());
              imageRealmDB.setWidth(message.getOriginal().getWidth());
            }
            m.setOriginal(imageRealmDB);
          }

          if (message.getAlts() != null) {
            RealmList<ImageRealm> alts = new RealmList<>();

            for (ImageRealm imageRealm : message.getAlts()) {

              ImageRealm imageRealmDB =
                  realm1.where(ImageRealm.class).equalTo("url", imageRealm.getUrl()).findFirst();

              if (imageRealmDB == null) {
                imageRealmDB = realm1.createObject(ImageRealm.class, imageRealm.getUrl());
                imageRealmDB.setFilesize(imageRealm.getFilesize());
                imageRealmDB.setHeight(imageRealm.getHeight());
                imageRealmDB.setWidth(imageRealm.getWidth());
              }
              alts.add(imageRealmDB);
            }
            m.setAlts(alts);
          }

          if (message.getAuthor() != null && message.getAuthor().getId() != null) {
            UserRealm userRealmDB = realm1.where(UserRealm.class)
                .equalTo("id", message.getAuthor().getId())
                .findFirst();

            if (userRealmDB == null) {
              userRealmDB = realm1.createObject(UserRealm.class, message.getAuthor().getId());
              userRealmDB.setDisplayName(message.getAuthor().getDisplayName());
              userRealmDB.setProfilePicture(message.getAuthor().getProfilePicture());
            }

            m.setAuthor(userRealmDB);
          } else if (message.getUser() != null && message.getUser().getId() != null) {

            UserRealm userRealmDB =
                realm1.where(UserRealm.class).equalTo("id", message.getUser().getId()).findFirst();
            if (userRealmDB == null) {
              userRealmDB = realm1.createObject(UserRealm.class, message.getUser().getId());
              userRealmDB.setDisplayName(message.getUser().getDisplayName());
              userRealmDB.setProfilePicture(message.getUser().getProfilePicture());
            }
            m.setUser(userRealmDB);
          }
          realm1.insertOrUpdate(m);
        }
      });
    } catch (Exception ex) {
      Timber.e(ex.toString());
    } finally {
      obsRealm.close();
    }
  }

  public void delete(String userIds) {
    Realm obsRealm = Realm.getDefaultInstance();
    try {
      obsRealm.beginTransaction();

      RealmResults<MessageRealm> ok =
          obsRealm.where(MessageRealm.class).equalTo("localId", userIds).findAll();
      for (int i = 0; i < ok.size(); i++) {
        MessageRealm m = ok.get(i);
        m.deleteFromRealm();
      }

      obsRealm.commitTransaction();
    } catch (IllegalStateException ex) {
      if (obsRealm.isInTransaction()) obsRealm.cancelTransaction();
      ex.printStackTrace();
    } finally {
      obsRealm.close();
    }
  }

  @Override public void onTyping(String userId) {
    onTyping.onNext(userId);
  }

  @Override public Observable<String> isTyping() {
    return onTyping;
  }

  @Override public Observable<List<MessageRealm>> getMessages(String[] userIds) {
    RealmResults<MessageRealm> ok = realm.where(MessageRealm.class)
        .equalTo("localId", JsonUtils.arrayToJson(userIds))
        .findAll();

    Timber.e("GET MESSAGE " + ok.size());

    return ok.asObservable()
        .filter(RealmResults::isLoaded)
        .map(singleShortcutList -> realm.copyFromRealm(singleShortcutList))
        .unsubscribeOn(AndroidSchedulers.mainThread());
  }
}
