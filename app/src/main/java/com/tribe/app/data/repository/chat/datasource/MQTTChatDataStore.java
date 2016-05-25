package com.tribe.app.data.repository.chat.datasource;

import com.google.gson.Gson;
import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.data.realm.MarvelCharacterRealm;
import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.data.realm.mapper.MQTTMessageDataMapper;
import com.tribe.app.data.repository.friendship.datasource.FriendshipDataStore;
import com.tribe.app.data.rxmqtt.impl.RxMqttMessage;
import com.tribe.app.data.rxmqtt.interfaces.IRxMqttClient;

import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.util.List;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * {@link FriendshipDataStore} implementation based on connections to the api (Cloud).
 */
public class MQTTChatDataStore implements ChatDataStore {

    private final IRxMqttClient mqttClient;
    private final ChatCache chatCache;
    private final MQTTMessageDataMapper mqttMessageDataMapper;

    private final Action1<List<MessageRealm>> saveToCacheAction = messageListRealm -> {
        if (messageListRealm != null) {
            MQTTChatDataStore.this.chatCache.put(messageListRealm);
        }
    };

    /**
     * Construct a {@link ChatDataStore} based on connections to the mqtt server.
     * @param chatCache A {@link ChatCache} to cache data retrieved from the api.
     * @param mqttClient an implementation of the mqtt client
     */
    public MQTTChatDataStore(ChatCache chatCache, IRxMqttClient mqttClient, MQTTMessageDataMapper mqttMessageDataMapper) {
        this.chatCache = chatCache;
        this.mqttClient = mqttClient;
        this.mqttMessageDataMapper = mqttMessageDataMapper;
    }

    @Override
    public Observable<IMqttToken> connectAndSubscribe(String topic) {
        return Observable.concat(mqttClient.connect(), mqttClient.subscribeTopic(topic, 2));
    }

    @Override
    public Observable<IMqttToken> disconnect() {
        return mqttClient.disconnect();
    }

    @Override
    public Observable<IMqttToken> unsubscribe(String topic) {
        return mqttClient.unsubscribeTopic(topic);
    }

    @Override
    public Observable<List<MessageRealm>> messages(String topic) {
        return mqttClient.subscribing(topic)
                .map(rxMqttMessage -> mqttMessageDataMapper.transform(rxMqttMessage))
                .doOnNext(saveToCacheAction);
    }
}
