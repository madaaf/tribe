package com.tribe.app.data.cache;

import android.support.v4.util.Pair;

import com.tribe.app.data.realm.ChatRealm;
import com.tribe.app.data.realm.MessageRecipientRealm;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;

import io.realm.RealmList;
import rx.Observable;

/**
 * Created by tiago on 05/05/2016.
 */
@Singleton
public interface ChatCache {

    boolean isExpired();
    boolean isCached(String messageId);
    Observable<ChatRealm> put(ChatRealm chatRealm);

    void insert(ChatRealm chatRealm);

    /**
     *
     * @param id
     * @param valuesToUpdate keys have to be of {@link com.tribe.app.data.realm.ChatRealm.ChatRealmAttributes}
     */
    void update(String id, Pair<String, Object>... valuesToUpdate);
    /**
     *
     * @param valuesToUpdate Map of the id of the tribe + the values paired to update
     *  keys of pair have to be of {@link com.tribe.app.data.realm.ChatRealm.ChatRealmAttributes}
     */
    void update(Map<String, List<Pair<String, Object>>> valuesToUpdate);

    void put(List<ChatRealm> messageListRealm);
    Observable<List<ChatRealm>> messages();
    Observable<List<ChatRealm>> messages(String friendshipId);
    List<ChatRealm> messagesNoObs(String recipientId);
    Observable<Void> delete(ChatRealm chatRealm);
    ChatRealm updateLocalWithServerRealm(ChatRealm local, ChatRealm server);
    Observable<Void> deleteConversation(String friendshipId);
    List<ChatRealm> messagesSent(Set<String> idsRecipient);
    List<ChatRealm> messagesToUpdateStatus(Set<String> idsRecipient);
    Observable<List<ChatRealm>> messagesError(String recipientId);
    List<ChatRealm> messagesPending(String recipientId);
    RealmList<MessageRecipientRealm> createMessageRecipientRealm(List<MessageRecipientRealm> messageRecipientRealmList);
    Observable<List<ChatRealm>> messagesReceived(String friendshipId);
}
