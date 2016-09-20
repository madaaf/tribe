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

    public boolean isExpired();
    public boolean isCached(String messageId);
    public Observable<ChatRealm> put(ChatRealm chatRealm);

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

    public void put(List<ChatRealm> messageListRealm);
    public Observable<List<ChatRealm>> messages();
    public Observable<List<ChatRealm>> messages(String friendshipId);
    public Observable<Void> delete(ChatRealm chatRealm);
    public ChatRealm updateLocalWithServerRealm(ChatRealm local, ChatRealm server);
    public Observable<Void> deleteConversation(String friendshipId);
    public List<ChatRealm> messagesSent(Set<String> idsRecipient);
    public List<ChatRealm> messagesToUpdateStatus(Set<String> idsRecipient);
    public Observable<List<ChatRealm>> messagesError(String recipientId);
    public List<ChatRealm> messagesPending(String recipientId);
    public RealmList<MessageRecipientRealm> createMessageRecipientRealm(List<MessageRecipientRealm> messageRecipientRealmList);
    public Observable<List<ChatRealm>> messagesReceived(String friendshipId);
}
