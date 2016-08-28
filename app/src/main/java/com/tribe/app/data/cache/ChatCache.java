package com.tribe.app.data.cache;

import com.tribe.app.data.realm.ChatRealm;
import com.tribe.app.data.realm.MessageRecipientRealm;

import java.util.List;
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
    public boolean isCached(int messageId);
    public Observable<ChatRealm> put(ChatRealm chatRealm);
    public void update(ChatRealm chatRealm);
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
    public void updateToError(List<ChatRealm> chatRealmList);
    public void updateMessageStatus(String recipientId);
}
