package com.tribe.app.data.cache;

import android.content.Context;
import android.support.v4.util.Pair;

import com.tribe.app.data.realm.ChatRealm;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.MembershipRealm;
import com.tribe.app.data.realm.MessageRecipientRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.domain.entity.User;
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
import rx.Subscriber;

/**
 * Created by tiago on 06/05/2016.
 */
public class ChatCacheImpl implements ChatCache {

    private Context context;
    private Realm realm;
    private RealmResults<ChatRealm> messagesNotSeen;
    private RealmResults<ChatRealm> messages;
    private RealmResults<ChatRealm> messagesError;
    private RealmResults<ChatRealm> messagesReceived;
    private User currentUser;

    @Inject
    public ChatCacheImpl(Context context, Realm realm, User currentUser) {
        this.context = context;
        this.realm = realm;
        this.currentUser = currentUser;
    }

    public boolean isExpired() {
        return true;
    }

    public boolean isCached(String messageId) {
        Realm obsRealm = Realm.getDefaultInstance();
        ChatRealm obj = obsRealm.where(ChatRealm.class).equalTo("id", messageId).findFirst();

        if (obj != null) {
            return true;
        }

        return false;
    }

    @Override
    public Observable<List<ChatRealm>> messages() {
        return Observable.create((Observable.OnSubscribe<List<ChatRealm>>) subscriber -> {

        });
    }

    @Override
    public Observable<List<ChatRealm>> messages(String recipientId) {
        return Observable.create(new Observable.OnSubscribe<List<ChatRealm>>() {
            @Override
            public void call(final Subscriber<? super List<ChatRealm>> subscriber) {
                if (recipientId == null) {
                    Realm obsRealm = Realm.getDefaultInstance();
                    messagesNotSeen = realm.where(ChatRealm.class)
                            .equalTo("messageReceivingStatus", MessageReceivingStatus.STATUS_NOT_SEEN)
                            .notEqualTo("from.id", currentUser.getId())
                            .findAllSorted("created_at", Sort.DESCENDING);
                    subscriber.onNext(obsRealm.copyFromRealm(messagesNotSeen));
                    messagesNotSeen.removeChangeListeners();
                    messagesNotSeen.addChangeListener(messagesUpdated -> subscriber.onNext(realm.copyFromRealm(messagesUpdated)));
                    subscriber.onNext(realm.copyFromRealm(messagesNotSeen));
                } else {
                    try {
                        messages =
                                realm.where(ChatRealm.class)
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
                                        .endGroup()
                                        .endGroup()
                                        .or()
                                        .beginGroup()
                                        .equalTo("membershipRealm.group.id", recipientId)
                                        .endGroup()
                                        .findAllSorted("created_at", Sort.ASCENDING);
                        messages.removeChangeListeners();
                        messages.addChangeListener(messagesUpdated -> subscriber.onNext(realm.copyFromRealm(messagesUpdated)));
                        subscriber.onNext(realm.copyFromRealm(messages));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void put(List<ChatRealm> messageListRealm) {
        Realm obsRealm = Realm.getDefaultInstance();
        try {
            obsRealm.beginTransaction();

            Set<String> membershipSet = new HashSet<>();
            Set<String> friendshipSet = new HashSet<>();

            for (ChatRealm chatRealm : messageListRealm) {
                if ((chatRealm.isToGroup() && chatRealm.getMembershipRealm() != null))
                    membershipSet.add(chatRealm.getMembershipRealm().getGroup().getId());
                else if (!chatRealm.isToGroup()) {
                    if (chatRealm.getFrom() != null && !chatRealm.getFrom().getId().equals(currentUser.getId()))
                        friendshipSet.add(chatRealm.getFrom().getId());
                    else if (chatRealm.getFriendshipRealm() != null) {
                        friendshipSet.add(chatRealm.getFriendshipRealm().getFriend().getId());
                    }
                }

                ChatRealm toEdit = obsRealm.where(ChatRealm.class)
                        .equalTo("id", chatRealm.getId()).findFirst();

                if (toEdit != null) {
                    toEdit = obsRealm.copyFromRealm(toEdit);

                    boolean shouldUpdate = false;

                    if (chatRealm.getMessageSendingStatus() != null) {
                        toEdit.setMessageSendingStatus(chatRealm.getMessageSendingStatus());
                        shouldUpdate = true;
                    }

                    if (chatRealm.getMessageReceivingStatus() != null && !chatRealm.getMessageReceivingStatus().equals(MessageReceivingStatus.STATUS_RECEIVED)) {
                        toEdit.setMessageReceivingStatus(chatRealm.getMessageReceivingStatus());
                        shouldUpdate = true;
                    }

                    if (chatRealm.getRecipientList() != null && chatRealm.getRecipientList().size() > 0) {
                        toEdit.setRecipientList(chatRealm.getRecipientList());
                        shouldUpdate = true;
                    }

                    if (chatRealm.getFrom() != null && toEdit.getFrom() == null) {
                        toEdit.setFrom(chatRealm.getFrom());
                        shouldUpdate = true;
                    } else if (chatRealm.getFrom() != null && toEdit.getFrom() != null
                            && chatRealm.getFrom().getUpdatedAt() != null
                            && (toEdit.getFrom().getUpdatedAt() == null || toEdit.getFrom().getUpdatedAt().before(chatRealm.getFrom().getUpdatedAt()))) {
                        toEdit.getFrom().setUpdatedAt(chatRealm.getFrom().getUpdatedAt());
                        shouldUpdate = true;
                    }

                    if (chatRealm.getFriendshipRealm() != null && toEdit.getFriendshipRealm() == null) {
                        toEdit.setFriendshipRealm(chatRealm.getFriendshipRealm());
                        shouldUpdate = true;
                    }

                    if (chatRealm.getMembershipRealm() != null && toEdit.getMembershipRealm() == null) {
                        toEdit.setMembershipRealm(chatRealm.getMembershipRealm());
                        shouldUpdate = true;
                    } else if (chatRealm.getMembershipRealm() != null && toEdit.getMembershipRealm() != null
                            && toEdit.getMembershipRealm().getUpdatedAt() != null
                            && (toEdit.getMembershipRealm().getUpdatedAt() == null || toEdit.getMembershipRealm().getUpdatedAt().before(chatRealm.getMembershipRealm().getUpdatedAt()))) {
                        toEdit.getMembershipRealm().setUpdatedAt(chatRealm.getMembershipRealm().getUpdatedAt());
                        shouldUpdate = true;
                    }

                    if (shouldUpdate) {
                        toEdit.setUpdatedAt(new Date());
                        obsRealm.copyToRealmOrUpdate(toEdit);
                    }
                } else if (toEdit == null) {
                    obsRealm.copyToRealmOrUpdate(chatRealm);
                }
            }

            for (String idTo : membershipSet) {
                RealmResults<ChatRealm> latest = obsRealm.where(ChatRealm.class)
                        .equalTo("membership.group.id", idTo)
                        .beginGroup()
                        .equalTo("messageSendingStatus", MessageSendingStatus.STATUS_OPENED)
                        .or()
                        .equalTo("messageSendingStatus", MessageSendingStatus.STATUS_OPENED_PARTLY)
                        .endGroup()
                        .findAllSorted("created_at", Sort.DESCENDING);

                if (latest != null && latest.size() > 0) {
                    RealmResults<ChatRealm> toRemoveStatus = obsRealm.where(ChatRealm.class)
                            .lessThan("created_at", latest.get(0).getCreatedAt())
                            .equalTo("membership.group.id", idTo)
                            .beginGroup()
                            .equalTo("messageSendingStatus", MessageSendingStatus.STATUS_SENT)
                            .or()
                            .equalTo("messageSendingStatus", MessageSendingStatus.STATUS_OPENED)
                            .or()
                            .equalTo("messageSendingStatus", MessageSendingStatus.STATUS_OPENED_PARTLY)
                            .endGroup()
                            .findAllSorted("created_at", Sort.DESCENDING);

                    for (ChatRealm chatToRemoveStatus : toRemoveStatus) {
                        chatToRemoveStatus.setMessageSendingStatus(null);
                    }
                }
            }

            for (String idTo : friendshipSet) {
                RealmResults<ChatRealm> latest = obsRealm.where(ChatRealm.class)
                        .beginGroup()
                        .equalTo("messageSendingStatus", MessageSendingStatus.STATUS_OPENED)
                        .or()
                        .equalTo("messageSendingStatus", MessageSendingStatus.STATUS_OPENED_PARTLY)
                        .endGroup()
                        .beginGroup()
                        .beginGroup()
                        .beginGroup()
                        .equalTo("from.id", idTo)
                        .isNull("friendshipRealm")
                        .isNull("membershipRealm")
                        .endGroup()
                        .or()
                        .beginGroup()
                        .equalTo("friendshipRealm.friend.id", idTo)
                        .isNull("membershipRealm")
                        .endGroup()
                        .endGroup()
                        .endGroup()
                        .findAllSorted("created_at", Sort.DESCENDING);

                if (latest != null && latest.size() > 0) {
                    RealmResults<ChatRealm> toRemoveStatus = obsRealm.where(ChatRealm.class)
                            .lessThan("created_at", latest.get(0).getCreatedAt())
                            .beginGroup()
                            .equalTo("messageSendingStatus", MessageSendingStatus.STATUS_SENT)
                            .or()
                            .equalTo("messageSendingStatus", MessageSendingStatus.STATUS_OPENED)
                            .or()
                            .equalTo("messageSendingStatus", MessageSendingStatus.STATUS_OPENED_PARTLY)
                            .endGroup()
                            .beginGroup()
                            .beginGroup()
                            .beginGroup()
                            .beginGroup()
                            .equalTo("from.id", idTo)
                            .isNull("friendshipRealm")
                            .isNull("membershipRealm")
                            .endGroup()
                            .or()
                            .beginGroup()
                            .equalTo("friendshipRealm.friend.id", idTo)
                            .isNull("membershipRealm")
                            .endGroup()
                            .endGroup()
                            .endGroup()
                            .or()
                            .equalTo("membershipRealm.group.id", idTo)
                            .endGroup()
                            .findAllSorted("created_at", Sort.DESCENDING);

                    for (ChatRealm chatToRemoveStatus : toRemoveStatus) {
                        chatToRemoveStatus.setMessageSendingStatus(null);
                    }
                }
            }

            obsRealm.commitTransaction();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            obsRealm.close();
        }
    }

    @Override
    public Observable<ChatRealm> put(ChatRealm chatRealm) {
        return Observable.create(new Observable.OnSubscribe<ChatRealm>() {
            @Override
            public void call(final Subscriber<? super ChatRealm> subscriber) {
                chatRealm.setUpdatedAt(new Date());
                Realm obsRealm = Realm.getDefaultInstance();

                try {
                    obsRealm.beginTransaction();

                    ChatRealm obj = obsRealm.where(ChatRealm.class).equalTo("localId", chatRealm.getLocalId()).findFirst();
                    if (obj != null) {
                        obj.setMessageSendingStatus(chatRealm.getMessageSendingStatus());
                        obj.setMessageDownloadingStatus(chatRealm.getMessageDownloadingStatus());
                        obj.setMessageReceivingStatus(chatRealm.getMessageReceivingStatus());
                    } else {
                        obj = obsRealm.copyToRealmOrUpdate(chatRealm);
                    }

                    obsRealm.commitTransaction();
                    subscriber.onNext(obsRealm.copyFromRealm(obj));
                    subscriber.onCompleted();
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
    public void insert(ChatRealm chatRealm) {
        chatRealm.setUpdatedAt(new Date());
        Realm obsRealm = Realm.getDefaultInstance();

        try {
            obsRealm.beginTransaction();

            ChatRealm obj = obsRealm.where(ChatRealm.class).equalTo("localId", chatRealm.getLocalId()).findFirst();
            if (obj == null) {
                if (chatRealm.isToGroup()) {
                    chatRealm.setMembershipRealm(obsRealm.where(MembershipRealm.class).equalTo("id", chatRealm.getMembershipRealm().getId()).findFirst());
                } else {
                    chatRealm.setFriendshipRealm(obsRealm.where(FriendshipRealm.class).equalTo("id", chatRealm.getFriendshipRealm().getId()).findFirst());
                }

                chatRealm.setFrom(obsRealm.where(UserRealm.class).equalTo("id", chatRealm.getFrom().getId()).findFirst());

                obsRealm.copyToRealmOrUpdate(chatRealm);
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
    public void update(String id, Pair<String, Object>... valuesToUpdate) {
        Realm obsRealm = Realm.getDefaultInstance();
        try {
            obsRealm.beginTransaction();

            ChatRealm obj = obsRealm.where(ChatRealm.class).equalTo("localId", id).findFirst();
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
                ChatRealm obj = obsRealm.where(ChatRealm.class).equalTo("localId", key).findFirst();
                if (obj != null) {
                    List<Pair<String, Object>> values = valuesToUpdate.get(key);
                    updateSingleObject(obj, values.toArray(new Pair[values.size()]));
                }
            }

            obsRealm.commitTransaction();
        } catch (IllegalStateException ex) {
            if (obsRealm.isInTransaction()) obsRealm.cancelTransaction();
            ex.printStackTrace();
        } finally {
            obsRealm.close();
        }
    }

    private void updateSingleObject(ChatRealm obj, Pair<String, Object>... valuesToUpdate) {
        for (Pair<String, Object> pair : valuesToUpdate) {
            if (pair.first.equals(ChatRealm.MESSAGE_DOWNLOADING_STATUS)) {
                obj.setMessageDownloadingStatus((String) pair.second);
            } else if (pair.first.equals(ChatRealm.MESSAGE_SENDING_STATUS)) {
                obj.setMessageSendingStatus((String) pair.second);
            } else if (pair.first.equals(ChatRealm.MESSAGE_RECEIVING_STATUS)) {
                obj.setMessageReceivingStatus((String) pair.second);
            } else if (pair.first.equals(ChatRealm.PROGRESS)) {
                obj.setProgress((Integer) pair.second);
            } else if (pair.first.equals(ChatRealm.TOTAL_SIZE)) {
                obj.setTotalSize((Long) pair.second);
            } else if (pair.first.equals(ChatRealm.FRIEND_ID_UPDATED_AT)) {
                obj.getFrom().setUpdatedAt((Date) pair.second);
            } else if (pair.first.equals(ChatRealm.GROUP_ID_UPDATED_AT)) {
                obj.getMembershipRealm().setUpdatedAt((Date) pair.second);
            }
        }

        obj.setUpdatedAt(new Date());
    }

    @Override
    public Observable<Void> delete(ChatRealm chatRealm) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(final Subscriber<? super Void> subscriber) {
                Realm obsRealm = Realm.getDefaultInstance();
                try {
                    obsRealm.beginTransaction();
                    final ChatRealm result = obsRealm.where(ChatRealm.class).equalTo("localId", chatRealm.getLocalId()).findFirst();
                    result.deleteFromRealm();
                    obsRealm.commitTransaction();
                    subscriber.onNext(null);
                    subscriber.onCompleted();
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
    public ChatRealm updateLocalWithServerRealm(ChatRealm local, ChatRealm server) {
        Realm obsRealm = Realm.getDefaultInstance();
        ChatRealm resultChat = null;

        try {
            obsRealm.beginTransaction();
            final ChatRealm result = obsRealm.where(ChatRealm.class).equalTo("localId", local.getLocalId()).findFirst();
            result.setId(server.getId());
            result.setCreatedAt(server.getCreatedAt());

            // WE GET THE OLDER SENT CHAT MESSAGES TO REMOVE THEIR STATUS
            RealmResults<ChatRealm> sentMessages = obsRealm.where(ChatRealm.class)
                    .beginGroup()
                    .equalTo("from.id", currentUser.getId())
                    .notEqualTo("localId", result.getLocalId())
                    .endGroup()
                    .findAllSorted("created_at", Sort.ASCENDING);

            if (!result.isToGroup()) {
                sentMessages = sentMessages.where()
                        .beginGroup()
                        .equalTo("friendshipRealm.friend.id", result.getFriendshipRealm().getFriend().getId())
                        .endGroup()
                        .findAllSorted("created_at", Sort.ASCENDING);
            } else {
                sentMessages = sentMessages.where()
                        .beginGroup()
                        .equalTo("membershipRealm.group.id", result.getMembershipRealm().getGroup().getId())
                        .endGroup()
                        .findAllSorted("created_at", Sort.ASCENDING);
            }

            for (ChatRealm message : sentMessages) {
                if (message.getCreatedAt().before(result.getCreatedAt())) {
                    message.setMessageSendingStatus(null);
                }
            }

            resultChat = obsRealm.copyFromRealm(result);
            obsRealm.commitTransaction();
        } catch (IllegalStateException ex) {
            if (obsRealm.isInTransaction()) obsRealm.cancelTransaction();
            ex.printStackTrace();
        } finally {
            obsRealm.close();
        }

        return resultChat;
    }

    @Override
    public Observable<Void> deleteConversation(String recipientId) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(final Subscriber<? super Void> subscriber) {
                Realm obsRealm = Realm.getDefaultInstance();

                try {
                    obsRealm.beginTransaction();
                    RealmResults results = obsRealm.where(ChatRealm.class)
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
                            .endGroup()
                            .endGroup()
                            .or()
                            .equalTo("membershipRealm.group.id", recipientId)
                            .findAllSorted("created_at", Sort.ASCENDING);

                    if (results != null && results.size() > 0) results.deleteAllFromRealm();

                    obsRealm.commitTransaction();
                    subscriber.onCompleted();
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
    public List<ChatRealm> messagesSent(Set<String> idsTo) {
        Realm obsRealm = Realm.getDefaultInstance();
        List<ChatRealm> result = new ArrayList<>();

        for (String id : idsTo) {
            RealmResults<ChatRealm> sentMessages = obsRealm.where(ChatRealm.class)
                    .beginGroup()
                    .equalTo("messageSendingStatus", MessageSendingStatus.STATUS_SENT)
                    .or()
                    .equalTo("messageSendingStatus", MessageSendingStatus.STATUS_OPENED_PARTLY)
                    .endGroup()
                    .beginGroup()
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
                    .endGroup()
                    .or()
                    .equalTo("membershipRealm.group.id", id)
                    .endGroup()
                    .findAllSorted("created_at", Sort.ASCENDING);

            if (sentMessages != null && sentMessages.size() > 0) result.addAll(obsRealm.copyFromRealm(sentMessages.subList(0, 1)));
        }

        obsRealm.close();
        return result;
    }

    @Override
    public List<ChatRealm> messagesPending(String recipientId) {
        Realm obsRealm = Realm.getDefaultInstance();
        List<ChatRealm> result = new ArrayList<>();

        RealmResults<ChatRealm> sentMessages =
                obsRealm.where(ChatRealm.class)
                        .equalTo("messageSendingStatus", MessageSendingStatus.STATUS_PENDING)
                        .beginGroup()
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
                        .endGroup()
                        .endGroup()
                        .or()
                        .equalTo("membershipRealm.group.id", recipientId)
                        .endGroup()
                        .findAllSorted("created_at", Sort.ASCENDING);

        if (sentMessages != null && sentMessages.size() > 0)
            result.addAll(obsRealm.copyFromRealm(sentMessages.subList(0, 1)));

        obsRealm.close();
        return result;
    }

    @Override
    public Observable<List<ChatRealm>> messagesError(String recipientId) {
        return Observable.create((Observable.OnSubscribe<List<ChatRealm>>) subscriber -> {
            messagesError = realm.where(ChatRealm.class)
                    .equalTo("messageSendingStatus", MessageSendingStatus.STATUS_ERROR)
                    .beginGroup()
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
                    .endGroup()
                    .endGroup()
                    .or()
                    .equalTo("membershipRealm.group.id", recipientId)
                    .endGroup()
                    .findAllSorted("created_at", Sort.ASCENDING);

            subscriber.onNext(realm.copyFromRealm(messagesError));
        });
    }

    @Override
    public RealmList<MessageRecipientRealm> createMessageRecipientRealm(List<MessageRecipientRealm> messageRecipientRealmList) {
        Realm realmObs = Realm.getDefaultInstance();
        realmObs.beginTransaction();

        for (MessageRecipientRealm messageRecipientRealm : messageRecipientRealmList) {
            MessageRecipientRealm realmRecipient = realmObs.where(MessageRecipientRealm.class).equalTo("id", messageRecipientRealm.getId()).findFirst();

            if (realmRecipient != null)
                realmObs.where(MessageRecipientRealm.class).equalTo("id", messageRecipientRealm.getId()).findFirst().deleteFromRealm();
        }

        List<MessageRecipientRealm> messageRecipientRealmManaged = realmObs.copyFromRealm(realmObs.copyToRealmOrUpdate(messageRecipientRealmList));
        realmObs.commitTransaction();
        realmObs.close();

        RealmList<MessageRecipientRealm> results = new RealmList<>();
        results.addAll(messageRecipientRealmManaged);

        return results;
    }

    @Override
    public List<ChatRealm> messagesToUpdateStatus(Set<String> idsRecipient) {
        Realm obsRealm = Realm.getDefaultInstance();
        List<ChatRealm> result = new ArrayList<>();

        for (String id : idsRecipient) {
            RealmResults<ChatRealm> messages = obsRealm.where(ChatRealm.class)
                    .beginGroup()
                    .equalTo("messageSendingStatus", MessageSendingStatus.STATUS_SENT)
                    .or()
                    .equalTo("messageSendingStatus", MessageSendingStatus.STATUS_OPENED_PARTLY)
                    .endGroup()
                    .beginGroup()
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
                    .endGroup()
                    .or()
                    .equalTo("membershipRealm.group.id", id)
                    .endGroup()
                    .findAllSorted("created_at", Sort.DESCENDING);

            if (messages != null && messages.size() > 0)
                result.addAll(obsRealm.copyFromRealm(messages));
        }

        obsRealm.close();
        return result;
    }

    @Override
    public Observable<List<ChatRealm>> messagesReceived(String friendshipId) {
        return Observable.create(new Observable.OnSubscribe<List<ChatRealm>>() {
            @Override
            public void call(final Subscriber<? super List<ChatRealm>> subscriber) {
                if (friendshipId == null) {
                    messagesReceived = realm.where(ChatRealm.class)
                            .equalTo("messageReceivingStatus", MessageReceivingStatus.STATUS_RECEIVED)
                            .notEqualTo("from.id", currentUser.getId())
                            .findAllSorted("recorded_at", Sort.DESCENDING);
                }

                messagesReceived.removeChangeListeners();
                messagesReceived.addChangeListener(messagesUpdated -> {
                    subscriber.onNext(realm.copyFromRealm(messagesUpdated));
                });
                subscriber.onNext(realm.copyFromRealm(messagesReceived));
            }
        });
    }
}
