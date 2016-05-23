package com.tribe.app.data.repository.text.datasource;

import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.data.rxmqtt.impl.RxMqttMessage;

import org.eclipse.paho.client.mqttv3.IMqttToken;

import rx.Observable;

/**
 * Interface that represents a data store from where data is retrieved.
 */
public interface TextDataStore {
    /**
     * Get an {@link Observable} which will emit a logged an mqtt observable.
     * @param topic The topic to subscribe to.
     */
    Observable<IMqttToken> connectAndSubscribe(String topic);

    /**
     * Get an {@link Observable} which will emit a {@link com.tribe.app.data.rxmqtt.impl.RxMqttMessage}.
     *
     * @param topic observing the incoming messages.
     */
    Observable<RxMqttMessage> subscribing(final String topic);

    /**
     * Get an {@link Observable} which will emit a {@link IMqttToken}.
     */
    Observable<IMqttToken> disconnect();

    /**
     * Get an {@link Observable} which will emit a {@link IMqttToken}.
     *
     * @param topic to unsubscribe from.
     */
    Observable<IMqttToken> unsubscribe(final String topic);
}
