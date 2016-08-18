package com.tribe.app.data.cache;

import android.content.Context;

import com.tribe.app.data.realm.ChatRealm;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by tiago on 06/05/2016.
 */
public class ChatCacheImpl implements ChatCache {

    private Context context;
    private Realm realm;
    private RealmResults<ChatRealm> messages;

    @Inject
    public ChatCacheImpl(Context context, Realm realm) {
        this.context = context;
        this.realm = realm;
    }

    public boolean isExpired() {
        return true;
    }

    public boolean isCached(int userId) {
        return false;
    }

    @Override
    public Observable<List<ChatRealm>> messages() {
        return Observable.create(new Observable.OnSubscribe<List<ChatRealm>>() {
            @Override
            public void call(final Subscriber<? super List<ChatRealm>> subscriber) {
                Realm obsRealm = Realm.getDefaultInstance();
                final RealmResults<ChatRealm> results = obsRealm.where(ChatRealm.class).findAll();
                subscriber.onNext(obsRealm.copyFromRealm(results));
                obsRealm.close();
            }
        });
    }

    @Override
    public Observable<List<ChatRealm>> messages(String friendshipId) {
        return Observable.create(new Observable.OnSubscribe<List<ChatRealm>>() {
            @Override
            public void call(final Subscriber<? super List<ChatRealm>> subscriber) {
                messages = realm.where(ChatRealm.class)
                        .beginGroup()
                        .equalTo("from.id", friendshipId)
                        .endGroup()
                        .or()
                        .beginGroup()
                        .equalTo("friendshipRealm.friend.id", friendshipId)
                        .endGroup()
                        .or()
                        .beginGroup()
                        .equalTo("group.id", friendshipId)
                        .endGroup()
                        .findAllSorted("created_at", Sort.ASCENDING);
                messages.removeChangeListeners();
                messages.addChangeListener(messagesUpdated -> subscriber.onNext(realm.copyFromRealm(messagesUpdated)));
                subscriber.onNext(realm.copyFromRealm(messages));
            }
        });
    }

    @Override
    public void put(List<ChatRealm> messageListRealm) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        for (ChatRealm chatRealm : messageListRealm) {
            ChatRealm toEdit = realm.where(ChatRealm.class)
                    .equalTo("id", chatRealm.getId()).findFirst();

            if (toEdit != null) {
                toEdit = realm.copyFromRealm(toEdit);

                boolean shouldUpdate = false;

                if (chatRealm.getMessageStatus() != null) {
                    toEdit.setMessageStatus(chatRealm.getMessageStatus());
                    shouldUpdate = true;
                }

                if (chatRealm.getRecipientList() != null && chatRealm.getRecipientList().size() > 0) {
                    toEdit.setRecipientList(chatRealm.getRecipientList());
                    shouldUpdate = true;
                }

                if (chatRealm.getFrom() != null && toEdit.getFrom() == null) {
                    toEdit.setFrom(chatRealm.getFrom());
                    shouldUpdate = true;
                }

                if (chatRealm.getFriendshipRealm() != null && toEdit.getFriendshipRealm() == null) {
                    toEdit.setFriendshipRealm(chatRealm.getFriendshipRealm());
                    shouldUpdate = true;
                }

                if (chatRealm.getGroup() != null && toEdit.getGroup() == null) {
                    toEdit.setGroup(chatRealm.getGroup());
                    shouldUpdate = true;
                }

                if (shouldUpdate) {
                    toEdit.setUpdatedAt(new Date());
                    realm.copyToRealmOrUpdate(toEdit);
                }
            } else if (toEdit == null) {
                realm.copyToRealmOrUpdate(chatRealm);
            }
        }

        realm.commitTransaction();
        realm.close();
    }

    @Override
    public Observable<ChatRealm> put(ChatRealm chatRealm) {
        return Observable.create(new Observable.OnSubscribe<ChatRealm>() {
            @Override
            public void call(final Subscriber<? super ChatRealm> subscriber) {
                chatRealm.setUpdatedAt(new Date());
                Realm obsRealm = Realm.getDefaultInstance();
                obsRealm.beginTransaction();

                ChatRealm obj = obsRealm.where(ChatRealm.class).equalTo("localId", chatRealm.getLocalId()).findFirst();
                if (obj != null)
                    obj.setMessageStatus(chatRealm.getMessageStatus());
                else
                    obj = obsRealm.copyToRealmOrUpdate(chatRealm);

                obsRealm.commitTransaction();
                subscriber.onNext(obsRealm.copyFromRealm(obj));
                subscriber.onCompleted();
                obsRealm.close();
            }
        });
    }

    @Override
    public void update(ChatRealm chatRealm) {
        chatRealm.setUpdatedAt(new Date());
        Realm obsRealm = Realm.getDefaultInstance();
        obsRealm.beginTransaction();

        ChatRealm obj = obsRealm.where(ChatRealm.class).equalTo("localId", chatRealm.getId()).findFirst();
        if (obj != null)
            obj.setMessageStatus(chatRealm.getMessageStatus());
        else
            obsRealm.copyToRealmOrUpdate(chatRealm);

        obsRealm.commitTransaction();
        obsRealm.close();
    }

    @Override
    public Observable<Void> delete(ChatRealm chatRealm) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(final Subscriber<? super Void> subscriber) {
                Realm obsRealm = Realm.getDefaultInstance();
                obsRealm.beginTransaction();
                final ChatRealm result = obsRealm.where(ChatRealm.class).equalTo("localId", chatRealm.getLocalId()).findFirst();
                result.deleteFromRealm();
                obsRealm.commitTransaction();
                subscriber.onNext(null);
                subscriber.onCompleted();
                obsRealm.close();
            }
        });
    }

    @Override
    public ChatRealm updateLocalWithServerRealm(ChatRealm local, ChatRealm server) {
        Realm obsRealm = Realm.getDefaultInstance();
        ChatRealm resultChat;
        obsRealm.beginTransaction();
        final ChatRealm result = obsRealm.where(ChatRealm.class).equalTo("localId", local.getLocalId()).findFirst();
        result.setId(server.getId());
        result.setCreatedAt(server.getCreatedAt());
        resultChat = obsRealm.copyFromRealm(result);
        obsRealm.commitTransaction();
        obsRealm.close();
        return resultChat;
    }

    @Override
    public Observable<Void> deleteConversation(String friendshipId) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(final Subscriber<? super Void> subscriber) {
                Realm obsRealm = Realm.getDefaultInstance();
                obsRealm.beginTransaction();
                RealmResults results = obsRealm.where(ChatRealm.class)
                        .beginGroup()
                        .equalTo("from.id", friendshipId)
                        .endGroup()
                        .or()
                        .beginGroup()
                        .equalTo("friendshipRealm.friend.id", friendshipId)
                        .endGroup()
                        .or()
                        .beginGroup()
                        .equalTo("group.id", friendshipId)
                        .endGroup()
                        .findAllSorted("created_at", Sort.ASCENDING);

                if (results != null && results.size() > 0) results.deleteAllFromRealm();
                obsRealm.commitTransaction();
                subscriber.onCompleted();
                obsRealm.close();
            }
        });
    }
}
