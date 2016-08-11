package com.tribe.app.data.cache;

import android.content.Context;

import com.tribe.app.data.realm.ChatRealm;

import java.util.List;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by tiago on 06/05/2016.
 */
public class ChatCacheImpl implements ChatCache {

    private Context context;

    @Inject
    public ChatCacheImpl(Context context) {
        this.context = context;
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
    public void put(List<ChatRealm> messageListRealm) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(messageListRealm);
        realm.commitTransaction();
        realm.close();
    }

    @Override
    public void put(ChatRealm chatRealm) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(chatRealm);
        realm.commitTransaction();
        realm.close();
    }
}
