package com.tribe.app.data.cache;

import android.content.Context;

import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.domain.entity.User;

import java.util.List;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by tiago on 06/05/2016.
 */
public class TribeCacheImpl implements TribeCache {

    private Context context;
    private User currentUser;

    @Inject
    public TribeCacheImpl(Context context, User currentUser) {
        this.context = context;
        this.currentUser = currentUser;
    }

    public boolean isExpired() {
        return true;
    }

    public boolean isCached(int userId) {
        return false;
    }

    @Override
    public Observable<TribeRealm> put(TribeRealm tribeRealm) {
        return Observable.create(new Observable.OnSubscribe<TribeRealm>() {
            @Override
            public void call(final Subscriber<? super TribeRealm> subscriber) {
                Realm obsRealm = Realm.getDefaultInstance();
                obsRealm.beginTransaction();
                TribeRealm obj = obsRealm.copyToRealmOrUpdate(tribeRealm);
                obsRealm.commitTransaction();
                subscriber.onNext(obsRealm.copyFromRealm(obj));
                obsRealm.close();
            }
        });
    }

    @Override
    public void put(List<TribeRealm> tribeRealmList) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(tribeRealmList);
        realm.commitTransaction();
        realm.close();
    }

    @Override
    public Observable<Void> delete(TribeRealm tribeRealm) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(final Subscriber<? super Void> subscriber) {
                Realm obsRealm = Realm.getDefaultInstance();
                obsRealm.beginTransaction();
                final TribeRealm result = obsRealm.where(TribeRealm.class).equalTo("localId", tribeRealm.getLocalId()).findFirst();
                result.deleteFromRealm();
                obsRealm.commitTransaction();
                subscriber.onNext(null);
                obsRealm.close();
            }
        });
    }

    @Override
    public Observable<List<TribeRealm>> tribes() {
        return Observable.create(new Observable.OnSubscribe<List<TribeRealm>>() {
            @Override
            public void call(final Subscriber<? super List<TribeRealm>> subscriber) {
                Realm obsRealm = Realm.getDefaultInstance();
                final RealmResults<TribeRealm> results = obsRealm.where(TribeRealm.class).notEqualTo("from.id", currentUser.getId()).findAll();
                subscriber.onNext(obsRealm.copyFromRealm(results));
                obsRealm.close();
            }
        });
    }

    @Override
    public TribeRealm updateLocalWithServerRealm(TribeRealm local, TribeRealm server) {
        Realm obsRealm = Realm.getDefaultInstance();
        TribeRealm resultTribe;
        obsRealm.beginTransaction();
        final TribeRealm result = obsRealm.where(TribeRealm.class).equalTo("localId", local.getLocalId()).findFirst();
        result.setId(server.getId());
        resultTribe = obsRealm.copyFromRealm(result);
        obsRealm.commitTransaction();
        obsRealm.close();
        return resultTribe;
    }
}
