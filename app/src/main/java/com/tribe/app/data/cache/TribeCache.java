package com.tribe.app.data.cache;

import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.data.realm.TribeRecipientRealm;

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
    public void update(TribeRealm tribeRealm);
    public void put(List<TribeRealm> tribeRealmList);
    public RealmList<TribeRecipientRealm> createTribeRecipientRealm(List<TribeRecipientRealm> tribeRecipientRealm);
    public Observable<Void> delete(TribeRealm tribeRealm);
    public Observable<List<TribeRealm>> tribes();
    public Observable<List<TribeRealm>> tribesPending();
    public List<TribeRealm> tribesNotSent();
    public List<TribeRealm> tribesSent();
    public TribeRealm updateLocalWithServerRealm(TribeRealm local, TribeRealm server);
    public void updateToError(List<TribeRealm> tribeRealmList);
}
