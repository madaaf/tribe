package com.tribe.app.data.realm.mapper;

import com.google.gson.Gson;
import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.data.rxmqtt.impl.RxMqttMessage;
import com.tribe.app.domain.entity.Message;

import java.util.ArrayList;
import java.util.Collection;
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
     * Transform a {@link RxMqttMessage} into an {@link List<MessageRealm>}.
     *
     * @param rxMqttMessage Object to be transformed.
     * @return {@link List<MessageRealm>} if valid {@link MessageRealm} otherwise null.
     */
    public List<MessageRealm> transform(RxMqttMessage rxMqttMessage) {
        List<MessageRealm> messageListRealm = new ArrayList<>();
        MessageRealm message = null;

        if (rxMqttMessage != null) {
            message = gson.fromJson(rxMqttMessage.getMessage(), MessageRealm.class);
            messageListRealm.add(message);
        }

        return messageListRealm;
    }
}
