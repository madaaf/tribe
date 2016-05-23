package com.tribe.app.data.repository.text.datasource;

import com.tribe.app.data.cache.TextCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.data.repository.friendship.datasource.FriendshipDataStore;
import com.tribe.app.data.repository.user.datasource.UserDataStore;
import com.tribe.app.data.rxmqtt.impl.RxMqttClient;
import com.tribe.app.data.rxmqtt.impl.RxMqttMessage;
import com.tribe.app.data.rxmqtt.interfaces.IRxMqttClient;

import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;

import rx.Observable;

/**
 * {@link FriendshipDataStore} implementation based on connections to the api (Cloud).
 */
public class MQTTTextDataStore implements TextDataStore {

    private final IRxMqttClient mqttClient;
    private final TextCache textCache;

    /**
     * Construct a {@link TextDataStore} based on connections to the mqtt server.
     * @param textCache A {@link TextCache} to cache data retrieved from the api.
     * @param mqttClient an implementation of the mqtt client
     */
    public MQTTTextDataStore(TextCache textCache, IRxMqttClient mqttClient) {
        this.textCache = textCache;
        this.mqttClient = mqttClient;
    }

    @Override
    public Observable<IMqttToken> connectAndSubscribe(String topic) {
        return Observable.concat(mqttClient.connect(), mqttClient.subscribeTopic(topic, 2));
    }

    @Override
    public Observable<RxMqttMessage> subscribing(String topic) {
        return mqttClient.subscribing(topic);
    }

    @Override
    public Observable<IMqttToken> disconnect() {
        return mqttClient.disconnect();
    }

    @Override
    public Observable<IMqttToken> unsubscribe(String topic) {
        return mqttClient.unsubscribeTopic(topic);
    }
}
