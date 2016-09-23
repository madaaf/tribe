package com.tribe.app.data.cache;

import android.content.Context;
import android.support.v4.util.Pair;

import com.tribe.app.data.realm.MessageRecipientRealm;
import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.view.utils.MessageReceivingStatus;
import com.tribe.app.presentation.view.utils.MessageSendingStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmList;
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
    private RealmResults<TribeRealm> tribesNotSeen;
    private RealmResults<TribeRealm> tribesReceived;

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
    public void insert(List<TribeRealm> tribeRealmList) {
        Realm realm = Realm.getDefaultInstance();

        try {
            realm.beginTransaction();

            for (TribeRealm tribeRealm : tribeRealmList) {
                TribeRealm toEdit = realm.where(TribeRealm.class)
                        .equalTo("id", tribeRealm.getId()).findFirst();

                if (toEdit != null) {
                    toEdit = realm.copyFromRealm(toEdit);

                    boolean shouldUpdate = false;

                    if (shouldUpdate) {
                        toEdit.setUpdatedAt(new Date());
                        realm.copyToRealmOrUpdate(toEdit);
                    }
                } else if (toEdit == null) {
                    realm.copyToRealmOrUpdate(tribeRealm);
                }
            }

            if (realm.isInTransaction()) {
                realm.commitTransaction();
            }
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
        } finally {
            if (realm.isInTransaction()) {
                realm.cancelTransaction();
            }
            realm.close();
        }
    }

    @Override
    public Observable<TribeRealm> insert(TribeRealm tribeRealm) {
        return Observable.create(new Observable.OnSubscribe<TribeRealm>() {
            @Override
            public void call(final Subscriber<? super TribeRealm> subscriber) {
                tribeRealm.setUpdatedAt(new Date());
                Realm obsRealm = Realm.getDefaultInstance();

                try {
                    obsRealm.beginTransaction();

                    TribeRealm obj = obsRealm.where(TribeRealm.class).equalTo("localId", tribeRealm.getLocalId()).findFirst();
                    if (obj == null) {
                        obj = obsRealm.copyToRealmOrUpdate(tribeRealm);
                    }

                    obsRealm.commitTransaction();
                    subscriber.onNext(obsRealm.copyFromRealm(obj));
                } catch (Exception ex) {
                    if (obsRealm.isInTransaction())
                        obsRealm.cancelTransaction();
                    ex.printStackTrace();
                } finally {
                    obsRealm.close();
                }
            }
        });
    }

    @Override
    public void update(TribeRealm tribeRealm) {
        tribeRealm.setUpdatedAt(new Date());
        Realm obsRealm = Realm.getDefaultInstance();
        obsRealm.beginTransaction();

        TribeRealm obj = obsRealm.where(TribeRealm.class).equalTo("localId", tribeRealm.getLocalId()).findFirst();
        if (obj != null) {
            obj.setMessageSendingStatus(tribeRealm.getMessageSendingStatus());
            obj.setMessageDownloadingStatus(tribeRealm.getMessageDownloadingStatus());
            obj.setMessageReceivingStatus(tribeRealm.getMessageReceivingStatus());
        } else {
            obsRealm.copyToRealmOrUpdate(tribeRealm);
        }

        obsRealm.commitTransaction();
        obsRealm.close();
    }

    @Override
    public void update(String id, Pair<String, Object>... valuesToUpdate) {
        Realm obsRealm = Realm.getDefaultInstance();
        try {
            obsRealm.beginTransaction();

            TribeRealm obj = obsRealm.where(TribeRealm.class).equalTo("localId", id).findFirst();
            if (obj != null) {
                updateSingleObject(obj, valuesToUpdate);
            }

            obsRealm.commitTransaction();
        } catch (IllegalStateException ex) {
            if (obsRealm.isInTransaction()) obsRealm.cancelTransaction();
            ex.printStackTrace();
        } finally {
            obsRealm.close();
        }
    }

    @Override
    public void update(Map<String, List<Pair<String, Object>>> valuesToUpdate) {
        Realm obsRealm = Realm.getDefaultInstance();
        try {
            obsRealm.beginTransaction();

            for (String key : valuesToUpdate.keySet()) {
                TribeRealm obj = obsRealm.where(TribeRealm.class).equalTo("localId", key).findFirst();
                if (obj != null) {
                    List<Pair<String, Object>> values = valuesToUpdate.get(key);
                    updateSingleObject(obj, values.toArray(new Pair[values.size()]));
                }
            }

            obsRealm.commitTransaction();
        } catch (Exception ex) {
            if (obsRealm.isInTransaction()) obsRealm.cancelTransaction();
            ex.printStackTrace();
        } finally {
            obsRealm.close();
        }
    }

    private void updateSingleObject(TribeRealm obj, Pair<String, Object>... valuesToUpdate) {
        for (Pair<String, Object> pair : valuesToUpdate) {
            if (pair.first.equals(TribeRealm.MESSAGE_DOWNLOADING_STATUS)) {
                obj.setMessageDownloadingStatus((String) pair.second);
            } else if (pair.first.equals(TribeRealm.MESSAGE_SENDING_STATUS)) {
                obj.setMessageSendingStatus((String) pair.second);
            } else if (pair.first.equals(TribeRealm.MESSAGE_RECEIVING_STATUS)) {
                obj.setMessageReceivingStatus((String) pair.second);
            } else if (pair.first.equals(TribeRealm.FRIEND_ID_UPDATED_AT)) {
                obj.getFrom().setUpdatedAt((Date) pair.second);
            } else if (pair.first.equals(TribeRealm.GROUP_ID_UPDATED_AT)) {
                obj.getMembershipRealm().setUpdatedAt((Date) pair.second);
            }
        }

        obj.setUpdatedAt(new Date());
    }

    @Override
    public RealmList<MessageRecipientRealm> createTribeRecipientRealm(List<MessageRecipientRealm> tribeRecipientRealmList) {
        Realm realmObs = Realm.getDefaultInstance();
        RealmList<MessageRecipientRealm> results = new RealmList<>();
        try {
            realmObs.beginTransaction();

            for (MessageRecipientRealm tribeRecipientRealm : tribeRecipientRealmList) {
                MessageRecipientRealm realmRecipient = realmObs.where(MessageRecipientRealm.class).equalTo("id", tribeRecipientRealm.getId()).findFirst();
                if (realmRecipient != null)
                    realmObs.where(MessageRecipientRealm.class).equalTo("id", tribeRecipientRealm.getId()).findFirst().deleteFromRealm();
            }

            List<MessageRecipientRealm> tribeRecipientRealmManaged = realmObs.copyFromRealm(realmObs.copyToRealmOrUpdate(tribeRecipientRealmList));
            results.addAll(tribeRecipientRealmManaged);

            realmObs.commitTransaction();
        } catch (IllegalStateException ex) {
            if (realmObs.isInTransaction()) realmObs.cancelTransaction();
            ex.printStackTrace();
        } finally {
            realmObs.close();
        }

        return results;
    }

    @Override
    public Observable<Void> delete(TribeRealm tribeRealm) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(final Subscriber<? super Void> subscriber) {
                Realm obsRealm = Realm.getDefaultInstance();
                try {
                    obsRealm.beginTransaction();
                    final TribeRealm result = obsRealm.where(TribeRealm.class).equalTo("localId", tribeRealm.getLocalId()).findFirst();
                    result.deleteFromRealm();
                    obsRealm.commitTransaction();
                    subscriber.onNext(null);
                } catch (IllegalStateException ex) {
                    if (obsRealm.isInTransaction()) obsRealm.cancelTransaction();
                    ex.printStackTrace();
                } finally {
                    obsRealm.close();
                }
            }
        });
    }

    @Override
    public Observable<List<TribeRealm>> tribesNotSeen(String friendshipId) {
        return Observable.create(new Observable.OnSubscribe<List<TribeRealm>>() {
            @Override
            public void call(final Subscriber<? super List<TribeRealm>> subscriber) {
                realm.setAutoRefresh(true);

                if (friendshipId == null) {
                    tribesNotSeen = realm.where(TribeRealm.class)
                            .beginGroup()
                            .equalTo("messageReceivingStatus", MessageReceivingStatus.STATUS_NOT_SEEN)
                            .notEqualTo("from.id", currentUser.getId())
                            .endGroup()
                            .or()
                            .equalTo("from.id", currentUser.getId())
                            .findAllSorted("recorded_at", Sort.ASCENDING);
                } else {
                    tribesNotSeen = realm.where(TribeRealm.class)
                            .equalTo("messageReceivingStatus", MessageReceivingStatus.STATUS_NOT_SEEN)
                            .beginGroup()
                            .beginGroup()
                            .beginGroup()
                            .equalTo("from.id", friendshipId)
                            .isNull("friendshipRealm")
                            .isNull("membershipRealm")
                            .endGroup()
                            .or()
                            .beginGroup()
                            .equalTo("friendshipRealm.friend.id", friendshipId)
                            .isNull("membershipRealm")
                            .endGroup()
                            .endGroup()
                            .or()
                            .equalTo("membershipRealm.group.id", friendshipId)
                            .endGroup()
                            .findAllSorted("recorded_at", Sort.ASCENDING);
                }

                tribesNotSeen.removeChangeListeners();
                tribesNotSeen.addChangeListener(tribesUpdated -> {
                    subscriber.onNext(realm.copyFromRealm(tribesUpdated));
                });
                subscriber.onNext(realm.copyFromRealm(tribesNotSeen));
            }
        });
    }

    @Override
    public List<TribeRealm> tribesNotSeenNoObs(String recipientId) {
        List<TribeRealm> tribeRealmList = new ArrayList<>();
        RealmResults<TribeRealm> realmResults;
        Realm obsRealm = Realm.getDefaultInstance();

        if (recipientId == null) {
            realmResults = obsRealm.where(TribeRealm.class)
                    .beginGroup()
                    .equalTo("messageReceivingStatus", MessageReceivingStatus.STATUS_NOT_SEEN)
                    .notEqualTo("from.id", currentUser.getId())
                    .endGroup()
                    .or()
                    .equalTo("from.id", currentUser.getId())
                    .findAllSorted("recorded_at", Sort.ASCENDING);
        } else {
            realmResults = obsRealm.where(TribeRealm.class)
                    .equalTo("messageSendingStatus", MessageReceivingStatus.STATUS_NOT_SEEN)
                    .beginGroup()
                    .beginGroup()
                    .beginGroup()
                    .equalTo("from.id", recipientId)
                    .isNull("friendshipRealm")
                    .isNull("membershipRealm")
                    .endGroup()
                    .or()
                    .beginGroup()
                    .equalTo("friendshipRealm.friend.id", recipientId)
                    .isNull("membershipRealm")
                    .endGroup()
                    .or()
                    .equalTo("membershipRealm.group.id", recipientId)
                    .endGroup()
                    .endGroup()
                    .findAllSorted("recorded_at", Sort.ASCENDING);
        }

        if (realmResults != null && realmResults.size() > 0) {
            tribeRealmList = obsRealm.copyFromRealm(realmResults);
        }

        return tribeRealmList;
    }

    @Override
    public Observable<List<TribeRealm>> tribesReceived(String friendshipId) {
        return Observable.create(new Observable.OnSubscribe<List<TribeRealm>>() {
            @Override
            public void call(final Subscriber<? super List<TribeRealm>> subscriber) {
                if (friendshipId == null) {
                    tribesReceived = realm.where(TribeRealm.class)
                            .equalTo("messageReceivingStatus", MessageReceivingStatus.STATUS_RECEIVED)
                            .notEqualTo("from.id", currentUser.getId())
                            .findAllSorted("recorded_at", Sort.DESCENDING);
                }

                tribesReceived.removeChangeListeners();
                tribesReceived.addChangeListener(tribesUpdated -> {
                    subscriber.onNext(realm.copyFromRealm(tribesUpdated));
                });
                subscriber.onNext(realm.copyFromRealm(tribesReceived));
            }
        });
    }

    @Override
    public List<TribeRealm> tribesReceivedNoObs(String friendshipId) {
        List<TribeRealm> results = new ArrayList<>();
        Realm tmpRealm = Realm.getDefaultInstance();

        if (friendshipId == null) {
            RealmResults realmResults = tmpRealm.where(TribeRealm.class)
                    .equalTo("messageReceivingStatus", MessageReceivingStatus.STATUS_RECEIVED)
                    .notEqualTo("from.id", currentUser.getId())
                    .findAllSorted("recorded_at", Sort.DESCENDING);

            if (realmResults != null && realmResults.size() > 0)
                results = tmpRealm.copyFromRealm(realmResults);
        }

        return results;
    }

    @Override
    public Observable<List<TribeRealm>> tribesPending() {
        return Observable.create(new Observable.OnSubscribe<List<TribeRealm>>() {
            @Override
            public void call(final Subscriber<? super List<TribeRealm>> subscriber) {
                pendingTribes = realm.where(TribeRealm.class)
                        .equalTo("from.id", currentUser.getId())
                        .equalTo("messageSendingStatus", MessageSendingStatus.STATUS_ERROR)
                        .findAllSorted("recorded_at", Sort.ASCENDING);
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
                .equalTo("messageSendingStatus", MessageSendingStatus.STATUS_PENDING).findAllSorted("recorded_at", Sort.ASCENDING);
        return otherRealm.copyFromRealm(sentTribes);
    }

    @Override
    public List<TribeRealm> tribesSent(Set<String> idsTo) {
        Realm obsRealm = Realm.getDefaultInstance();
        List<TribeRealm> result = new ArrayList<>();

        for (String id : idsTo) {
            RealmResults<TribeRealm> sentMessages = obsRealm.where(TribeRealm.class)
                    .equalTo("messageSendingStatus", MessageSendingStatus.STATUS_SENT)
                    .beginGroup()
                    .beginGroup()
                    .beginGroup()
                    .equalTo("from.id", id)
                    .isNull("friendshipRealm")
                    .isNull("membershipRealm")
                    .endGroup()
                    .or()
                    .beginGroup()
                    .equalTo("friendshipRealm.friend.id", id)
                    .isNull("membershipRealm")
                    .endGroup()
                    .endGroup()
                    .or()
                    .equalTo("membershipRealm.group.id", id)
                    .endGroup()
                    .findAllSorted("recorded_at", Sort.ASCENDING);

            if (sentMessages != null && sentMessages.size() > 0)
                result.addAll(obsRealm.copyFromRealm(sentMessages.subList(0, 1)));
        }

        obsRealm.close();
        return result;
    }

    @Override
    public TribeRealm updateLocalWithServerRealm(TribeRealm local, TribeRealm server) {
        Realm obsRealm = Realm.getDefaultInstance();
        TribeRealm resultTribe = null;

        try {
            obsRealm.beginTransaction();
            final TribeRealm result = obsRealm.where(TribeRealm.class).equalTo("localId", local.getLocalId()).findFirst();
            result.setId(server.getId());
            resultTribe = obsRealm.copyFromRealm(result);
            obsRealm.commitTransaction();
        } catch (IllegalStateException ex) {
            if (obsRealm.isInTransaction()) obsRealm.cancelTransaction();
            ex.printStackTrace();
        } finally {
            obsRealm.close();
        }

        return resultTribe;
    }
}
