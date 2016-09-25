package com.tribe.app.data.cache;

import android.support.v4.util.Pair;

import com.tribe.app.data.realm.MessageRecipientRealm;
import com.tribe.app.data.realm.TribeRealm;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;

import io.realm.RealmList;
import rx.Observable;

/**
 * Created by tiago on 28/06/2016.
 */
@Singleton
public interface TribeCache {

    boolean isExpired();
    boolean isCached(int messageId);
    void insert(List<TribeRealm> tribeRealmList);
    Observable<TribeRealm> insert(TribeRealm tribeRealm);
    void update(TribeRealm tribeRealm);
    /**
     *
     * @param id
     * @param valuesToUpdate keys have to be of {@link com.tribe.app.data.realm.TribeRealm.TribeRealmAttributes}
     */
    void update(String id, Pair<String, Object>... valuesToUpdate);
    /**
     *
     * @param valuesToUpdate Map of the id of the tribe + the values paired to update
     *  keys of pair have to be of {@link com.tribe.app.data.realm.TribeRealm.TribeRealmAttributes}
     */
    void update(Map<String, List<Pair<String, Object>>> valuesToUpdate);

    RealmList<MessageRecipientRealm> createTribeRecipientRealm(List<MessageRecipientRealm> tribeRecipientRealm);
    Observable<Void> delete(TribeRealm tribeRealm);
    Observable<List<TribeRealm>> tribesNotSeen(String recipientId);
    List<TribeRealm> tribesNotSeenNoObs(String recipientId);
    Observable<List<TribeRealm>> tribesReceived(String recipientId);
    Observable<List<TribeRealm>> tribesForARecipient(String recipientId);
    List<TribeRealm> tribesReceivedNoObs(String recipientId);
    Observable<List<TribeRealm>> tribesPending();
    List<TribeRealm> tribesNotSent();
    List<TribeRealm> tribesSent(Set<String> toIds);
    TribeRealm updateLocalWithServerRealm(TribeRealm local, TribeRealm server);
}
