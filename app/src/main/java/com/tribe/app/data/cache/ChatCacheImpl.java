package com.tribe.app.data.cache;

import android.content.Context;
import com.tribe.app.data.realm.MediaRealm;
import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.presentation.utils.DateUtils;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import com.tribe.tribelivesdk.util.JsonUtils;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
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
  private DateUtils dateUtils;
  private PublishSubject<String> onTyping = PublishSubject.create();
  private PublishSubject<String> onTalking = PublishSubject.create();
  private PublishSubject<String> onReading = PublishSubject.create();
  private PublishSubject<List<MessageRealm>> onMessageReceived = PublishSubject.create();
  private PublishSubject<MessageRealm> onMessageRemoved = PublishSubject.create();

  @Inject public ChatCacheImpl(Context context, Realm realm, DateUtils dateUtils) {
    this.context = context;
    this.realm = realm;
    this.dateUtils = dateUtils;
  }

  @Override public Observable<List<MessageRealm>> onMessageReceived() {
    return onMessageReceived;
  }

  @Override public void setOnMessageReceived(RealmList<MessageRealm> messages) {
    onMessageReceived.onNext(messages);
  }

  @Override public void setOnMessageRemoved(MessageRealm messageRealm) {
    onMessageRemoved.onNext(messageRealm);
  }

  @Override public Observable<MessageRealm> onMessageRemoved() {
    return onMessageRemoved;
  }

  @Override
  public void deleteRemovedMessageFromCache(RealmList<MessageRealm> messages, String userIds,
      String dateBefore, String dateAfter) {
    Realm obsRealm = Realm.getDefaultInstance();
    try {
      obsRealm.beginTransaction();

      ArrayList<String> remoteMessageIds = new ArrayList<>();
      ArrayList<String> cacheRefactoredList = new ArrayList<>();

      for (MessageRealm message : messages) {
        remoteMessageIds.add(message.getId());
      }

      RealmResults<MessageRealm> cacheMessage =
          obsRealm.where(MessageRealm.class).equalTo("localId", userIds).findAll();

      for (MessageRealm message : cacheMessage) {
        String creationDate = message.getCreated_at();
        if (dateUtils.isBetween(creationDate, dateBefore, dateAfter)) {
          cacheRefactoredList.add(message.getId());
        }
      }

      for (String messageId : cacheRefactoredList) {
        if (!remoteMessageIds.contains(messageId)) {
          MessageRealm messageRealmDB =
              obsRealm.where(MessageRealm.class).equalTo("id", messageId).findFirst();
          if (messageRealmDB != null) {
            Timber.i("removed message from cache ");
            messageRealmDB.deleteFromRealm();
          }
        }
      }

      obsRealm.commitTransaction();
    } catch (IllegalStateException ex) {
      ex.printStackTrace();
    } finally {
      obsRealm.close();
    }
  }

  @Override public void putMessagesSupport(List<Message> messages) {

  }

  @Override public void putMessages(RealmList<MessageRealm> messages, String userIds) {
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
          m.setSupportAuthorId(message.getSupportAuthorId());
          m.setLocalId(userIds);
          m.set__typename(message.get__typename());
          m.setData(message.getData());
          m.setAction(message.getAction());
          m.setIntent(message.getIntent());
          m.setGame_id(message.getGame_id());
          m.setClient_message_id(message.getClient_message_id());
          m.setCreated_at(message.getCreated_at());

          if (message.getOriginal() != null) {
            MediaRealm mediaRealmDB = realm1.where(MediaRealm.class)
                .equalTo("url", message.getOriginal().getUrl())
                .findFirst();
            mediaRealmDB.setDuration(message.getOriginal().getDuration());

            if (mediaRealmDB == null) {
              mediaRealmDB = realm1.createObject(MediaRealm.class);
              mediaRealmDB.setUrl(message.getOriginal().getUrl());
              mediaRealmDB.setFilesize(message.getOriginal().getFilesize());
              mediaRealmDB.setHeight(message.getOriginal().getHeight());
              mediaRealmDB.setWidth(message.getOriginal().getWidth());
              mediaRealmDB.setDuration(message.getOriginal().getDuration());
            }
            m.setOriginal(mediaRealmDB);
          }

          if (message.getAlts() != null) {
            RealmList<MediaRealm> alts = new RealmList<>();

            for (MediaRealm mediaRealm : message.getAlts()) {

              MediaRealm mediaRealmDB =
                  realm1.where(MediaRealm.class).equalTo("url", mediaRealm.getUrl()).findFirst();

              if (mediaRealmDB == null) {
                mediaRealmDB = realm1.createObject(MediaRealm.class);
                mediaRealmDB.setUrl(mediaRealm.getUrl());
                mediaRealmDB.setFilesize(mediaRealm.getFilesize());
                mediaRealmDB.setHeight(mediaRealm.getHeight());
                mediaRealmDB.setWidth(mediaRealm.getWidth());
              }

              alts.add(mediaRealmDB);
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

  @Override public void onTalking(String userId) {
    onTalking.onNext(userId);
  }

  @Override public void onReading(String userId) {
    onReading.onNext(userId);
  }

  @Override public Observable<String> isTyping() {
    return onTyping;
  }

  @Override public Observable<String> isTalking() {
    return onTalking;
  }

  @Override public Observable<String> isReading() {
    return onReading;
  }

  @Override public Observable<List<MessageRealm>> getMessages(String[] userIds) {
    if (userIds == null || userIds.length == 0) return Observable.just(new ArrayList<>());

    String localIds = "";

    if (userIds[0].equals(Shortcut.SUPPORT)) {
      localIds = Shortcut.SUPPORT;
    } else {
      localIds = JsonUtils.arrayToJson(userIds);
    }

    RealmResults<MessageRealm> list =
        realm.where(MessageRealm.class).equalTo("localId", localIds).findAll();

    return list.asObservable()
        .filter(RealmResults::isLoaded)
        .map(singleShortcutList -> realm.copyFromRealm(singleShortcutList))
        .unsubscribeOn(AndroidSchedulers.mainThread());
  }

  @Override public MessageRealm getLastTextMessage(String[] userIds) {
    Realm obsRealm = Realm.getDefaultInstance();
    try {
      RealmResults<MessageRealm> messageRealmResults = obsRealm.where(MessageRealm.class)
          .equalTo("localId", JsonUtils.arrayToJson(userIds))
          .equalTo("__typename", Message.MESSAGE_TEXT)
          .findAllSorted("created_at", Sort.DESCENDING);

      if (messageRealmResults != null && messageRealmResults.size() > 0) {
        MessageRealm copy = obsRealm.copyFromRealm(messageRealmResults.first());
        obsRealm.close();
        return copy;
      }
    } catch (IllegalStateException ex) {
      if (!obsRealm.isClosed() && obsRealm.isInTransaction()) obsRealm.cancelTransaction();
      ex.printStackTrace();
    } finally {
      if (!obsRealm.isClosed()) obsRealm.close();
    }

    return null;
  }

  @Override public Observable<List<MessageRealm>> getMessagesImage(String[] userIds) {
    return realm.where(MessageRealm.class)
        .equalTo("localId", JsonUtils.arrayToJson(userIds))
        .equalTo("__typename", Message.MESSAGE_IMAGE)
        .findAll()
        .asObservable()
        .filter(RealmResults::isLoaded)
        .map(singleShortcutList -> realm.copyFromRealm(singleShortcutList))
        .unsubscribeOn(AndroidSchedulers.mainThread());
  }
}
