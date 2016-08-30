package com.tribe.app.data.cache;

import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.data.realm.MessageRecipientRealm;

import java.util.List;
import java.util.Set;

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
    public RealmList<MessageRecipientRealm> createTribeRecipientRealm(List<MessageRecipientRealm> tribeRecipientRealm);
    public Observable<Void> delete(TribeRealm tribeRealm);
    public Observable<List<TribeRealm>> tribesNotSeen(String friendshipId);
    public List<TribeRealm> tribesNotSeenNoObs(String friendshipId);
    public Observable<List<TribeRealm>> tribesReceived(String friendshipId);
    public List<TribeRealm> tribesReceivedNoObs(String friendshipId);
    public Observable<List<TribeRealm>> tribesPending();
    public List<TribeRealm> tribesNotSent();
    public List<TribeRealm> tribesSent(Set<String> toIds);
    public TribeRealm updateLocalWithServerRealm(TribeRealm local, TribeRealm server);
    public void updateToError(List<TribeRealm> tribeRealmList);
}
