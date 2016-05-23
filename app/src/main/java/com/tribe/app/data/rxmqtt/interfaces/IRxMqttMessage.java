package com.tribe.app.data.rxmqtt.interfaces;

public interface IRxMqttMessage {
    public String getTopic();
    public String getMessage();
    public int getQos();
}
