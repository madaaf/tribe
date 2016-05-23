package com.tribe.app.data.rxmqtt.impl;

import com.tribe.app.data.rxmqtt.enums.RxMqttClientState;
import com.tribe.app.data.rxmqtt.enums.RxMqttExceptionType;
import com.tribe.app.data.rxmqtt.exceptions.RxMqttException;
import com.tribe.app.data.rxmqtt.interfaces.IRxMqttClient;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import rx.Observable;
import rx.subjects.PublishSubject;


public abstract class RxMqttClient implements IRxMqttClient {
    private MqttConnectOptions conOpt;
    private RxMqttClientStatus status;
    private PublishSubject<RxMqttClientStatus> statusSubject;

    protected RxMqttClient() {
        this.conOpt = new MqttConnectOptions();
        this.configOtherOptions(true, 60, 300);
        //init status
        status = new RxMqttClientStatus();
        status.setLogTime(new Timestamp(System.currentTimeMillis()));
        status.setState(RxMqttClientState.INIT);
        statusSubject = PublishSubject.create();
    }

    public MqttConnectOptions getConOpt() {
        return conOpt;
    }

    public void setConOpt(MqttConnectOptions conOpt) {
        this.conOpt = conOpt;
    }

    public void configOtherOptions(boolean cleanSession, int connectionTimeout,
                                   int keepAliveInterval) {
        conOpt.setCleanSession(cleanSession);
        conOpt.setConnectionTimeout(connectionTimeout);
        conOpt.setKeepAliveInterval(keepAliveInterval);
    }

    public void configUserPassword(String username, String password) {
        conOpt.setUserName(username);
        if (null != password)
            conOpt.setPassword(password.toCharArray());
    }

    public void updateState(int state) {
        if (this.status.getState() != state) {
            this.status.setState(state);
            this.status.setLogTime(new Timestamp(System.currentTimeMillis()));
            statusSubject.onNext((RxMqttClientStatus) status.clone());
        }
    }

    @Override
    public Observable<RxMqttClientStatus> statusReport() {
        return statusSubject;
    }
}
