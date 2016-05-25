package com.tribe.app.data.cache;

import android.content.Context;

import com.fernandocejas.frodo.annotation.RxLogObservable;
import com.tribe.app.data.realm.MarvelCharacterRealm;
import com.tribe.app.data.realm.MessageRealm;

import java.util.ArrayList;
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

    @RxLogObservable
    @Override
    public Observable<List<MessageRealm>> messages() {
        return Observable.create(new Observable.OnSubscribe<List<MessageRealm>>() {
            @Override
            public void call(final Subscriber<? super List<MessageRealm>> subscriber) {
                Realm obsRealm = Realm.getDefaultInstance();
                final RealmResults<MessageRealm> results = obsRealm.where(MessageRealm.class).findAll();
                subscriber.onNext(obsRealm.copyFromRealm(results));
                obsRealm.close();
            }
        });
    }

    @Override
    public void put(List<MessageRealm> messageListRealm) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(messageListRealm);
        realm.commitTransaction();
        realm.close();
    }

    @Override
    public void put(MessageRealm messageRealm) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(messageRealm);
        realm.commitTransaction();
        realm.close();
    }
}
