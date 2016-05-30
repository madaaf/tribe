package com.tribe.app.data.repository.chat.datasource;

import android.content.Context;

import com.google.gson.Gson;
import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.data.realm.mapper.MQTTMessageDataMapper;
import com.tribe.app.data.rxmqtt.constants.Constants;
import com.tribe.app.data.rxmqtt.exceptions.RxMqttException;
import com.tribe.app.data.rxmqtt.impl.RxMqttAsyncClient;
import com.tribe.app.data.rxmqtt.interfaces.IRxMqttClient;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory that creates different implementations of {@link ChatDataStoreFactory}.
 */
@Singleton
public class ChatDataStoreFactory {

    private final Context context;
    private final ChatCache chatCache;
    private IRxMqttClient client = null;
    private final MQTTMessageDataMapper mqttMessageDataMapper;

    @Inject
    public ChatDataStoreFactory(Context context, ChatCache chatCache, MQTTMessageDataMapper mqttMessageDataMapper) {
        if (context == null || chatCache == null) {
            throw new IllegalArgumentException("Constructor parameters cannot be null!");
        }
        this.context = context.getApplicationContext();
        this.chatCache = chatCache;
        this.mqttMessageDataMapper = mqttMessageDataMapper;
        try {
            this.client = new RxMqttAsyncClient(String.format("%s://%s:%d", Constants.TCP, "176.58.122.224", 1883), "tiago");
        } catch (RxMqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create {@link ChatDataStore}
     */
    public ChatDataStore create() {
        return createMQTTStore();
    }

    /**
     * Create {@link ChatDataStore} to retrieve data from the Cloud.
     */
    public ChatDataStore createMQTTStore() {
        return new MQTTChatDataStore(this.chatCache, this.client, this.mqttMessageDataMapper);
    }

    public ChatDataStore createDiskChatStore() {
        return new DiskChatDataStore(this.chatCache);
    }
}
