package com.tribe.app.data.repository.chat.datasource;

import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.data.realm.ChatRealm;
import com.tribe.app.presentation.view.utils.MessageReceivingStatus;

import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;

/**
 * {@link ChatDataStore} implementation based on the local database.
 */
public class DiskChatDataStore implements ChatDataStore {

    private final ChatCache chatCache;

    /**
     * Construct a {@link ChatDataStore} based on connections to the database cache.
     * @param chatCache A {@link ChatCache} to retrieve the data.
     */
    public DiskChatDataStore(ChatCache chatCache) {
        this.chatCache = chatCache;
    }

    @Override
    public Observable<IMqttToken> connectAndSubscribe(String topic) {
        return null;
    }

    @Override
    public Observable<IMqttToken> disconnect() {
        return null;
    }

    @Override
    public Observable<IMqttToken> unsubscribe(String topic) {
        return null;
    }

    @Override
    public Observable<List<ChatRealm>> messages(String recipientId) {
        return chatCache.messages(recipientId);
    }

    @Override
    public Observable<ChatRealm> sendMessage(ChatRealm chatRealm) {
        return chatCache.put(chatRealm);
    }

    @Override
    public Observable<Void> deleteMessage(ChatRealm chatRealm) {
        return chatCache.delete(chatRealm);
    }

    @Override
    public Observable<Void> deleteConversation(String friendshipId) {
        return chatCache.deleteConversation(friendshipId);
    }

    @Override
    public Observable<List<ChatRealm>> markMessageListAsRead(List<ChatRealm> messageRealmList) {
        for (ChatRealm chatRealm : messageRealmList) {
            chatRealm.setMessageReceivingStatus(MessageReceivingStatus.STATUS_SEEN);
        }

        chatCache.put(messageRealmList);

        return Observable.just(messageRealmList);
    }

    @Override
    public Observable<List<ChatRealm>> messagesError(String recipientId) {
        return chatCache.messagesError(recipientId);
    }

    @Override
    public Observable<List<ChatRealm>> messagesReceived(String friendshipId) {
        return chatCache.messagesReceived(friendshipId).debounce(500, TimeUnit.MILLISECONDS);
    }

    @Override
    public Observable<Void> updateStatuses(String friendshipId) {
        return null;
    }

    @Override
    public Observable<List<ChatRealm>> manageChatHistory(String recipientId) {
        return null;
    }
}
