package com.tribe.app.data.rxmqtt.interfaces;

import com.tribe.app.data.rxmqtt.impl.RxMqttClientStatus;
import com.tribe.app.data.rxmqtt.impl.RxMqttMessage;

import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.util.regex.Pattern;

import rx.Observable;

public interface IRxMqttClient {

    public Observable<IMqttToken> connect();

    public Observable<IMqttToken> disconnect();

    public void disconnectForcibly();

    public Observable<IMqttToken> subscribeTopic(String topic, int qos);

    public Observable<IMqttToken> unsubscribeTopic(String topic);

    public Observable<RxMqttMessage> subscribing(String regularExpression);

    public Observable<RxMqttMessage> subscribing(Pattern pattern);

    public Observable<IMqttToken> publish(String topic, byte[] msg);

    public Observable<RxMqttClientStatus> statusReport();

    public Observable<IMqttToken> checkPing(Object userContext);
}
