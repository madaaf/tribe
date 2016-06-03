package com.tribe.app.domain.interactor.text;

/**
 * Created by tiago on 04/05/2016.
 */

import com.tribe.app.domain.entity.Message;

import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.util.List;

import rx.Observable;

/**
 * Interface that represents a Repository for observing text messages MQTT.
 */
public interface ChatRepository {

    /**
     * Get an {@link Observable} which will emit a {@link org.eclipse.paho.client.mqttv3.IMqttToken}.
     *
     * @param topic The topic to subscribe to after connecting.
     */
    Observable<IMqttToken> connectAndSubscribe(final String topic);

    /**
     * Get an {@link Observable} which will emit a {@link com.tribe.app.data.rxmqtt.impl.RxMqttMessage}.
     *
     * @param topic observing the incoming messages.
     */
    Observable<List<Message>> subscribing(final String topic);

    /**
     * Get an {@link Observable} which will emit a {@link IMqttToken}.
     */
    Observable<IMqttToken> disconnect();

    /**
     * Get an {@link Observable} which will emit a {@link com.tribe.app.data.rxmqtt.impl.RxMqttMessage}.
     *
     * @param topic observing the incoming messages.
     */
    Observable<IMqttToken> unsubscribe(final String topic);
}
