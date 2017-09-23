package com.tribe.app.data.cache;

import android.content.Context;
import com.tribe.app.data.realm.ImageRealm;
import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.tribelivesdk.util.JsonUtils;
import io.realm.Realm;
import io.realm.RealmList;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

/**
 * Created by madaaflak on 12/09/2017.
 */

public class ChatCacheImpl implements ChatCache {

  private Context context;
  private Realm realm;

  @Inject public ChatCacheImpl(Context context, Realm realm) {
    this.context = context;
    this.realm = realm;
  }

  @Override public void putMessages(RealmList<MessageRealm> messages, String userId) {
    Timber.e("SOEF PUT MESSAGE IN CACH " + userId);
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

          m.setLocalId(userId);
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

  @Override public Observable<List<MessageRealm>> getMessages(String[] userIds) {
    return realm.where(MessageRealm.class)
        .equalTo("localId", JsonUtils.arrayToJson(userIds))
        .findAll()
        .asObservable()
        .filter(singleShortcutList -> singleShortcutList.isLoaded())
        .map(singleShortcutList -> realm.copyFromRealm(singleShortcutList))
        .unsubscribeOn(AndroidSchedulers.mainThread());
  }
}
