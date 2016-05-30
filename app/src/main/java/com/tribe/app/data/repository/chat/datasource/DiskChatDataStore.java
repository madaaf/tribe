package com.tribe.app.data.repository.chat.datasource;

import com.fernandocejas.frodo.annotation.RxLogObservable;
import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.data.realm.mapper.MQTTMessageDataMapper;
import com.tribe.app.data.repository.friendship.datasource.FriendshipDataStore;
import com.tribe.app.data.rxmqtt.interfaces.IRxMqttClient;

import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.util.List;

import rx.Observable;
import rx.functions.Action1;

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

    @RxLogObservable
    @Override
    public Observable<List<MessageRealm>> messages(String topic) {
        return chatCache.messages();
    }
}
