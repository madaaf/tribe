package com.tribe.app.data.cache;

import com.tribe.app.data.realm.TribeRealm;

import java.util.List;

import javax.inject.Singleton;

import io.realm.RealmList;
import rx.Observable;

/**
 * Created by tiago on 28/06/2016.
 */
@Singleton
public interface TribeCache {

    public boolean isExpired();
    public boolean isCached(int messageId);
    public Observable<TribeRealm> put(TribeRealm tribeRealm);
    public Observable<TribeRealm> put(RealmList<TribeRealm> tribeRealmList);
    public Observable<Void> delete(TribeRealm tribeRealm);
    public Observable<List<TribeRealm>> tribes();
    public TribeRealm updateLocalWithServerRealm(TribeRealm local, TribeRealm server);
}
