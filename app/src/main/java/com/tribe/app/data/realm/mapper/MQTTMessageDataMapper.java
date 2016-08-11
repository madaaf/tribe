package com.tribe.app.data.realm.mapper;

import com.google.gson.Gson;
import com.tribe.app.data.realm.ChatRealm;
import com.tribe.app.data.rxmqtt.impl.RxMqttMessage;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by tiago on 06/05/2016.
 */
@Singleton
public class MQTTMessageDataMapper {

    private final Gson gson;

    @Inject
    public MQTTMessageDataMapper(Gson gson) {
        this.gson = gson;
    }

    /**
     * Transform a {@link RxMqttMessage} into an {@link List< ChatRealm >}.
     *
     * @param rxMqttMessage Object to be transformed.
     * @return {@link List< ChatRealm >} if valid {@link ChatRealm} otherwise null.
     */
    public List<ChatRealm> transform(RxMqttMessage rxMqttMessage) {
        List<ChatRealm> messageListRealm = new ArrayList<>();
        ChatRealm message = null;

        if (rxMqttMessage != null) {
            message = gson.fromJson(rxMqttMessage.getMessage(), ChatRealm.class);
            messageListRealm.add(message);
        }

        return messageListRealm;
    }
}
