package com.tribe.app.data.cache;

import android.content.Context;
import android.support.v4.util.Pair;

import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.MessageRecipientRealm;
import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.data.realm.helpers.ChangeHelper;
import com.tribe.app.presentation.view.utils.MessageDownloadingStatus;
import com.tribe.app.presentation.view.utils.MessageReceivingStatus;
import com.tribe.app.presentation.view.utils.MessageSendingStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by tiago on 06/05/2016.
 */
public class TribeCacheImpl implements TribeCache {

    private Context context;
    private AccessToken accessToken;
    private Realm realm;
    private RealmResults<TribeRealm> messageNotSeen;

    @Inject
    public TribeCacheImpl(Context context, Realm realm, AccessToken accessToken) {
        this.context = context;
        this.accessToken = accessToken;
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
        Set<String> setProcessedRecipient = new HashSet<>();

        try {
            realm.executeTransaction(realm1 -> {
                for (TribeRealm tribeRealm : tribeRealmList) {
                    TribeRealm toEdit = realm1.where(TribeRealm.class)
                            .equalTo("id", tribeRealm.getId()).findFirst();
                    if (toEdit != null) {
                        toEdit = realm1.copyFromRealm(toEdit);

                        boolean shouldUpdate = false;

                        if (tribeRealm.getMessageSendingStatus() != null && tribeRealm.getMessageSendingStatus().equals(MessageSendingStatus.STATUS_OPENED))
                            shouldUpdate = true;

                        if (shouldUpdate) {
                            toEdit.setMessageSendingStatus(tribeRealm.getMessageSendingStatus());
                            realm1.insertOrUpdate(toEdit);
                        }
                    } else if (toEdit == null) {
                        try {
                            realm1.insertOrUpdate(tribeRealm);
                            if (tribeRealm.getFrom() != null && tribeRealm.getMembershipRealm() == null) {
                                if (!setProcessedRecipient.contains(tribeRealm.getFrom().getId())) {
                                    deletePreviousSentTribeForFriendship(realm1, tribeRealm.getFrom());
                                    setProcessedRecipient.add(tribeRealm.getFrom().getId());
                                }
                            } else if (tribeRealm.getMembershipRealm() != null) {
                                if (!setProcessedRecipient.contains(tribeRealm.getMembershipRealm().getId())) {
                                    deletePreviousSentTribe(realm1, tribeRealm);
                                    setProcessedRecipient.add(tribeRealm.getMembershipRealm().getId());
                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });
        } finally {
            realm.close();
        }
    }

    @Override
    public TribeRealm insert(TribeRealm tribeRealm) {
        tribeRealm.setUpdatedAt(new Date());

        realm.executeTransactionAsync(realm1 -> {
            TribeRealm obj = realm1.where(TribeRealm.class).equalTo("localId", tribeRealm.getLocalId()).findFirst();
            if (obj == null) {
                realm1.insertOrUpdate(tribeRealm);
            }

            deletePreviousSentTribe(realm1, tribeRealm);
        });

        return tribeRealm;
    }

    private void deletePreviousSentTribeForFriendship(Realm realm1, UserRealm userRealm) {
        RealmResults<TribeRealm> tribesSentToRecipient = realm1.where(TribeRealm.class)
                .equalTo("friendshipRealm.friend.id", userRealm.getId())
                .equalTo("from.id", accessToken.getUserId())
                .beginGroup()
                .equalTo("messageSendingStatus", MessageSendingStatus.STATUS_SENT)
                .or()
                .equalTo("messageSendingStatus", MessageSendingStatus.STATUS_OPENED)
                .or()
                .equalTo("messageSendingStatus", MessageSendingStatus.STATUS_OPENED_PARTLY)
                .endGroup()
                .findAllSorted("recorded_at", Sort.ASCENDING);

        if (tribesSentToRecipient != null) tribesSentToRecipient.deleteAllFromRealm();
    }

    private void deletePreviousSentTribe(Realm realm1, TribeRealm tribeRealm) {
        if (!tribeRealm.isToGroup()) {
            RealmResults<TribeRealm> tribesSentToRecipient = realm1.where(TribeRealm.class)
                    .equalTo("friendshipRealm.id", tribeRealm.getFriendshipRealm().getId())
                    .equalTo("from.id", accessToken.getUserId())
                    .notEqualTo("id", tribeRealm.getLocalId())
                    .equalTo("messageSendingStatus", MessageSendingStatus.STATUS_SENT)
                    .findAllSorted("recorded_at", Sort.ASCENDING);
            if (tribesSentToRecipient != null) tribesSentToRecipient.deleteAllFromRealm();
        } else {
            RealmResults<TribeRealm> tribesSentToRecipient = realm1.where(TribeRealm.class)
                    .equalTo("membershipRealm.id", tribeRealm.getMembershipRealm().getId())
                    .equalTo("from.id", accessToken.getUserId())
                    .notEqualTo("id", tribeRealm.getLocalId())
                    .equalTo("messageSendingStatus", MessageSendingStatus.STATUS_SENT)
                    .findAllSorted("recorded_at", Sort.ASCENDING);
            if (tribesSentToRecipient != null) tribesSentToRecipient.deleteAllFromRealm();
        }
    }

    @Override
    public void update(TribeRealm tribeRealm) {
        tribeRealm.setUpdatedAt(new Date());
        Realm obsRealm = Realm.getDefaultInstance();
        try {
            obsRealm.beginTransaction();

            TribeRealm obj = obsRealm.where(TribeRealm.class).equalTo("localId", tribeRealm.getLocalId()).findFirst();
            if (obj != null) {
                obj.setMessageSendingStatus(tribeRealm.getMessageSendingStatus());
                obj.setMessageDownloadingStatus(tribeRealm.getMessageDownloadingStatus());
                obj.setMessageReceivingStatus(tribeRealm.getMessageReceivingStatus());
            } else {
                obsRealm.insertOrUpdate(tribeRealm);
            }

            obsRealm.commitTransaction();
        } catch(IllegalStateException ex) {
            ex.printStackTrace();
            if (obsRealm.isInTransaction()) obsRealm.cancelTransaction();
        } finally {
            obsRealm.close();
        }
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
            obsRealm.executeTransaction(realm -> {
                for (String key : valuesToUpdate.keySet()) {
                    TribeRealm obj = realm.where(TribeRealm.class).equalTo("localId", key).findFirst();
                    if (obj != null) {
                        List<Pair<String, Object>> values = valuesToUpdate.get(key);
                        updateSingleObject(obj, values.toArray(new Pair[values.size()]));
                    }
                }
            });
        } finally {
            if (obsRealm != null) obsRealm.close();
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
            } else if (pair.first.equals(TribeRealm.FRIEND_TO_ID_UPDATED_AT)) { // IN THE CASE WHERE I'M THE ONE SENDING THE TRIBE
                obj.getFriendshipRealm().getFriend().setUpdatedAt((Date) pair.second);
            } else if (pair.first.equals(TribeRealm.GROUP_ID_UPDATED_AT)) {
                obj.getMembershipRealm().setUpdatedAt((Date) pair.second);
            } else if (pair.first.equals(TribeRealm.PROGRESS)) {
                obj.setProgress((Long) pair.second);
            } else if (pair.first.equals(TribeRealm.TOTAL_SIZE)) {
                obj.setTotalSize((Long) pair.second);
            }
        }

        obj.setUpdatedAt(new Date());
    }

    @Override
    public RealmList<MessageRecipientRealm> createTribeRecipientRealm(List<MessageRecipientRealm> tribeRecipientRealmList) {
        Realm realmObs = Realm.getDefaultInstance();
        RealmList<MessageRecipientRealm> results = new RealmList<>();
        try {
            Realm.Transaction transaction = realm1 -> {
                for (MessageRecipientRealm tribeRecipientRealm : tribeRecipientRealmList) {
                    MessageRecipientRealm realmRecipient = realm1.where(MessageRecipientRealm.class).equalTo("id", tribeRecipientRealm.getId()).findFirst();
                    if (realmRecipient != null)
                        realm1.where(MessageRecipientRealm.class).equalTo("id", tribeRecipientRealm.getId()).findFirst().deleteFromRealm();
                }

                List<MessageRecipientRealm> tribeRecipientRealmManaged = realm1.copyFromRealm(realm1.copyToRealmOrUpdate(tribeRecipientRealmList));
                results.addAll(tribeRecipientRealmManaged);
            };

            realmObs.executeTransaction(transaction);
        } catch (IllegalStateException ex) {
            if (realmObs.isInTransaction()) realmObs.cancelTransaction();
            ex.printStackTrace();
        } finally {
            realmObs.close();
        }

        return results;
    }

    @Override
    public void delete(TribeRealm tribeRealm) {
        Realm otherRealm = Realm.getDefaultInstance();

        try {
            otherRealm.executeTransactionAsync(realm1 -> {
                final TribeRealm result = realm1.where(TribeRealm.class).equalTo("localId", tribeRealm.getLocalId()).findFirst();
                if (result != null) result.deleteFromRealm();
            });
        } finally {
            otherRealm.close();
        }
    }

    private ChangeHelper<RealmResults<TribeRealm>> changeSetNotSeen = new ChangeHelper<>();

    @Override
    public Observable<List<TribeRealm>> tribesNotSeen(String friendshipId) {
        changeSetNotSeen.clear();

        if (friendshipId == null) {
            messageNotSeen = realm.where(TribeRealm.class)
                    .beginGroup()
                    .equalTo("messageReceivingStatus", MessageReceivingStatus.STATUS_NOT_SEEN)
                    .notEqualTo("from.id", accessToken.getUserId())
                    .endGroup()
                    .or()
                    .equalTo("from.id", accessToken.getUserId())
                    .findAllSorted("recorded_at", Sort.ASCENDING);
        } else {
            messageNotSeen = realm.where(TribeRealm.class)
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

        return messageNotSeen
                .asObservable()
                .filter(tribeRealms -> tribeRealms.isLoaded())
                .filter(tribeRealms -> changeSetNotSeen.filter(tribeRealms))
//                .flatMap(new Func1<List<TribeRealm>, Observable<DiffUtil.DiffResult>>() {
//                    @Override
//                    public Observable<DiffUtil.DiffResult> call(List<TribeRealm> products) {
//                        return Observable.just(DiffUtil.calculateDiff(new ProductListDiffCallback(oldData, products)));
//                    }
//                })
                .map(tribeRealms -> realm.copyFromRealm(tribeRealms))
                .unsubscribeOn(AndroidSchedulers.mainThread());
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
                    .notEqualTo("from.id", accessToken.getUserId())
                    .endGroup()
                    .or()
                    .equalTo("from.id", accessToken.getUserId())
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

        obsRealm.close();

        return tribeRealmList;
    }

    private ChangeHelper<RealmResults<TribeRealm>> changeSetReceived = new ChangeHelper<>();

    @Override
    public Observable<List<TribeRealm>> tribesReceived(String recipientId) {
        changeSetReceived.clear();

        return realm.where(TribeRealm.class)
                .equalTo("messageReceivingStatus", MessageReceivingStatus.STATUS_RECEIVED)
                .notEqualTo("from.id", accessToken.getUserId())
                .findAllSorted("recorded_at", Sort.DESCENDING)
                .asObservable()
                .filter(tribeRealms -> tribeRealms.isLoaded())
                .filter(tribeRealms -> changeSetReceived.filter(tribeRealms))
                .map(tribeRealms -> realm.copyFromRealm(tribeRealms))
                .unsubscribeOn(AndroidSchedulers.mainThread());
    }

    private ChangeHelper<RealmResults<TribeRealm>> changeSetForRecipient = new ChangeHelper<>();

    @Override
    public Observable<List<TribeRealm>> tribesForARecipient(String recipientId) {
        changeSetForRecipient.clear();

        return realm.where(TribeRealm.class)
                .beginGroup()
                .beginGroup()
                .equalTo("from.id", recipientId)
                .isNull("friendshipRealm")
                .isNull("membershipRealm")
                .endGroup()
                .or()
                .equalTo("membershipRealm.group.id", recipientId)
                .endGroup()
                .notEqualTo("from.id", accessToken.getUserId())
                .beginGroup()
                .equalTo("messageReceivingStatus", MessageReceivingStatus.STATUS_RECEIVED)
                .or()
                .equalTo("messageReceivingStatus", MessageReceivingStatus.STATUS_NOT_SEEN)
                .endGroup()
                .findAllSorted("recorded_at", Sort.ASCENDING)
                .asObservable()
                .filter(tribeRealms -> tribeRealms.isLoaded())
                .filter(tribeRealms -> changeSetForRecipient.filter(tribeRealms))
                .map(tribeRealms -> realm.copyFromRealm(tribeRealms))
                .unsubscribeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public List<TribeRealm> tribesReceivedNoObs(String recipientId) {
        List<TribeRealm> results = new ArrayList<>();
        Realm tmpRealm = Realm.getDefaultInstance();

        if (recipientId == null) {
            RealmResults realmResults = tmpRealm.where(TribeRealm.class)
                    .equalTo("messageReceivingStatus", MessageReceivingStatus.STATUS_RECEIVED)
                    .notEqualTo("from.id", accessToken.getUserId())
                    .findAllSorted("recorded_at", Sort.DESCENDING);

            if (realmResults != null && realmResults.size() > 0)
                results = tmpRealm.copyFromRealm(realmResults);
        }

        tmpRealm.close();

        return results;
    }

    private ChangeHelper<RealmResults<TribeRealm>> changeSetPending = new ChangeHelper<>();

    @Override
    public Observable<List<TribeRealm>> tribesPending() {
        changeSetPending.clear();

        return realm.where(TribeRealm.class)
                .equalTo("from.id", accessToken.getUserId())
                .equalTo("messageSendingStatus", MessageSendingStatus.STATUS_ERROR)
                .findAllSorted("recorded_at", Sort.ASCENDING)
                .asObservable()
                .filter(tribeRealms -> tribeRealms.isLoaded())
                .filter(tribeRealms -> changeSetPending.filter(tribeRealms))
                .map(tribeRealms -> realm.copyFromRealm(tribeRealms))
                .unsubscribeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public List<TribeRealm> tribesNotSent() {
        Realm otherRealm = Realm.getDefaultInstance();
        List<TribeRealm> tribeRealmList;
        RealmResults<TribeRealm> sentTribes = otherRealm.where(TribeRealm.class).equalTo("from.id", accessToken.getUserId())
                .equalTo("messageSendingStatus", MessageSendingStatus.STATUS_PENDING).findAllSorted("recorded_at", Sort.ASCENDING);
        tribeRealmList = otherRealm.copyFromRealm(sentTribes);
        otherRealm.close();
        return tribeRealmList;
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
        Realm obsRealm = null;
        TribeRealm resultTribe = null;

        try {
            obsRealm = Realm.getDefaultInstance();
            local.setId(server.getId());

            obsRealm.executeTransaction(realm1 -> {
                final TribeRealm result = realm1.where(TribeRealm.class).equalTo("localId", local.getLocalId()).findFirst();
                result.setId(server.getId());

                // WE GET THE OLDER SENT CHAT MESSAGES TO REMOVE THEIR STATUS
                RealmResults<TribeRealm> sentTribes = realm1.where(TribeRealm.class)
                        .beginGroup()
                        .equalTo("from.id", accessToken.getUserId())
                        .notEqualTo("localId", result.getLocalId())
                        .endGroup()
                        .findAllSorted("created_at", Sort.ASCENDING);

                if (!result.isToGroup()) {
                    sentTribes = sentTribes.where()
                            .beginGroup()
                            .equalTo("friendshipRealm.friend.id", result.getFriendshipRealm().getFriend().getId())
                            .endGroup()
                            .findAllSorted("created_at", Sort.ASCENDING);
                } else {
                    sentTribes = sentTribes.where()
                            .beginGroup()
                            .equalTo("membershipRealm.group.id", result.getMembershipRealm().getGroup().getId())
                            .endGroup()
                            .findAllSorted("created_at", Sort.ASCENDING);
                }

                sentTribes.deleteAllFromRealm();
            });
        } finally {
            obsRealm.close();
        }

        return resultTribe;
    }

    private ChangeHelper<RealmResults<TribeRealm>> changeSetToDownload = new ChangeHelper<>();

    @Override
    public Observable<List<TribeRealm>> tribesToDownload(String recipientId) {
        changeSetToDownload.clear();

        if (recipientId == null) {
            return realm.where(TribeRealm.class)
                    .equalTo("messageDownloadingStatus", MessageDownloadingStatus.STATUS_TO_DOWNLOAD)
                    .notEqualTo("from.id", accessToken.getUserId())
                    .findAllSorted("recorded_at", Sort.DESCENDING)
                    .asObservable()
                    .filter(tribeRealms -> tribeRealms.isLoaded())
                    .filter(tribeRealms -> changeSetToDownload.filter(tribeRealms))
                    .map(tribeRealms -> realm.copyFromRealm(tribeRealms))
                    .unsubscribeOn(AndroidSchedulers.mainThread());
        } else {
            return realm.where(TribeRealm.class)
                    .equalTo("messageDownloadingStatus", MessageDownloadingStatus.STATUS_TO_DOWNLOAD)
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
                    .findAllSorted("recorded_at", Sort.ASCENDING)
                    .asObservable()
                    .filter(tribeRealms -> tribeRealms.isLoaded())
                    .filter(tribeRealms -> changeSetToDownload.filter(tribeRealms))
                    .map(tribeRealms -> realm.copyFromRealm(tribeRealms))
                    .unsubscribeOn(AndroidSchedulers.mainThread());
        }
    }

    @Override
    public void refactorTribeError(String tribeId) {
        Realm otherRealm = Realm.getDefaultInstance();

        try {
            otherRealm.executeTransaction(realm1 -> {
                TribeRealm result = realm1.where(TribeRealm.class)
                        .equalTo("id", tribeId)
                        .findFirst();

                if (result != null) {
                    result.setMessageDownloadingStatus(MessageDownloadingStatus.STATUS_TO_DOWNLOAD);
                }
            });
        } finally {
            otherRealm.close();
        }
    }


    @Override
    public List<TribeRealm> tribesDownloading() {
        Realm newRealm = Realm.getDefaultInstance();
        List<TribeRealm> result = new ArrayList<>();

        RealmResults<TribeRealm> realmResults = newRealm.where(TribeRealm.class)
                .equalTo("messageDownloadingStatus", MessageDownloadingStatus.STATUS_DOWNLOADING)
                .notEqualTo("from.id", accessToken.getUserId())
                .findAllSorted("recorded_at", Sort.DESCENDING);

        if (realmResults != null && realmResults.size() > 0)
            result.addAll(newRealm.copyFromRealm(realmResults));

        newRealm.close();
        return result;
    }

    @Override
    public void updateTribesReceivedToNotSeen() {
        Realm otherRealm = Realm.getDefaultInstance();

        try {
            otherRealm.executeTransaction(realm1 -> {
                final RealmResults<TribeRealm> result = realm1.where(TribeRealm.class)
                        .equalTo("messageReceivingStatus", MessageReceivingStatus.STATUS_RECEIVED)
                        .notEqualTo("from.id", accessToken.getUserId())
                        .findAllSorted("recorded_at", Sort.DESCENDING);

                if (result != null && result.size() > 0) {
                    for (TribeRealm tribeRealm : result) {
                        tribeRealm.setMessageReceivingStatus(MessageReceivingStatus.STATUS_NOT_SEEN);
                    }
                }
            });
        } finally {
            otherRealm.close();
        }
    }

    @Override
    public TribeRealm get(String tribeId) {
        TribeRealm tribeRealm = realm.where(TribeRealm.class).equalTo("localId", tribeId).findFirst();
        return realm.copyFromRealm(tribeRealm);
    }
}
