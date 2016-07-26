package com.tribe.app.data.cache;

import android.content.Context;

import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.view.utils.MessageStatus;

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
public class TribeCacheImpl implements TribeCache {

    private Context context;
    private User currentUser;
    private Realm realm;
    private RealmResults<TribeRealm> pendingTribes;
    private RealmResults<TribeRealm> tribes;

    @Inject
    public TribeCacheImpl(Context context, Realm realm, User currentUser) {
        this.context = context;
        this.currentUser = currentUser;
        this.realm = realm;
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
                tribeRealm.setUpdatedAt(new Date());
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
    public void update(TribeRealm tribeRealm) {
        tribeRealm.setUpdatedAt(new Date());
        Realm obsRealm = Realm.getDefaultInstance();
        obsRealm.beginTransaction();
        obsRealm.copyToRealmOrUpdate(tribeRealm);
        obsRealm.commitTransaction();
        obsRealm.close();
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
                tribes = realm.where(TribeRealm.class)
                        .beginGroup()
                        .notEqualTo("messageStatus", MessageStatus.STATUS_OPENED)
                        .notEqualTo("from.id", currentUser.getId())
                        .endGroup()
                        .or()
                        .beginGroup()
                        .equalTo("from.id", currentUser.getId())
                        .endGroup()
                        .findAllSorted("recorded_at", Sort.ASCENDING);
                tribes.removeChangeListeners();
                tribes.addChangeListener(tribesUpdated -> subscriber.onNext(realm.copyFromRealm(tribesUpdated)));
                subscriber.onNext(realm.copyFromRealm(tribes));
            }
        });
    }

    @Override
    public Observable<List<TribeRealm>> tribesPending() {
        return Observable.create(new Observable.OnSubscribe<List<TribeRealm>>() {
            @Override
            public void call(final Subscriber<? super List<TribeRealm>> subscriber) {
                pendingTribes = realm.where(TribeRealm.class).equalTo("from.id", currentUser.getId())
                        .equalTo("messageStatus", MessageStatus.STATUS_ERROR).findAllSorted("recorded_at", Sort.ASCENDING);
                pendingTribes.removeChangeListeners();
                pendingTribes.addChangeListener(tribesPending -> subscriber.onNext(realm.copyFromRealm(tribesPending)));
                subscriber.onNext(realm.copyFromRealm(pendingTribes));
            }
        });
    }

    @Override
    public List<TribeRealm> tribesNotSent() {
        Realm otherRealm = Realm.getDefaultInstance();
        RealmResults<TribeRealm> sentTribes = otherRealm.where(TribeRealm.class).equalTo("from.id", currentUser.getId())
                .equalTo("messageStatus", MessageStatus.STATUS_PENDING).findAllSorted("recorded_at", Sort.ASCENDING);
        return otherRealm.copyFromRealm(sentTribes);
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

    @Override
    public void updateToError(List<TribeRealm> tribeRealmList) {
        for (TribeRealm tribeRealm : tribeRealmList) {
            tribeRealm.setMessageStatus(MessageStatus.STATUS_ERROR);
            tribeRealm.setUpdatedAt(new Date());
        }

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(tribeRealmList);
        realm.commitTransaction();
        realm.close();
    }
}
