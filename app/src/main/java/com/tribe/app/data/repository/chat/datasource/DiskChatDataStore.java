package com.tribe.app.data.repository.chat.datasource;

import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.data.realm.ChatRealm;

import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.util.List;

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
}
