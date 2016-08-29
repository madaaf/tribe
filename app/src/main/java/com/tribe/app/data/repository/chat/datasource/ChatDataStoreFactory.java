package com.tribe.app.data.repository.chat.datasource;

import android.content.Context;

import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.mapper.MQTTMessageDataMapper;
import com.tribe.app.data.rxmqtt.constants.Constants;
import com.tribe.app.data.rxmqtt.exceptions.RxMqttException;
import com.tribe.app.data.rxmqtt.impl.RxMqttAsyncClient;
import com.tribe.app.data.rxmqtt.interfaces.IRxMqttClient;

import java.text.SimpleDateFormat;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Factory that creates different implementations of {@link ChatDataStoreFactory}.
 */
@Singleton
public class ChatDataStoreFactory {

    private final Context context;
    private final ChatCache chatCache;
    private final UserCache userCache;
    private final TribeApi tribeApi;
    private final AccessToken accessToken;
    private final SimpleDateFormat simpleDateFormat;
    private IRxMqttClient client = null;
    private final MQTTMessageDataMapper mqttMessageDataMapper;

    @Inject
    public ChatDataStoreFactory(Context context, ChatCache chatCache, UserCache userCache, TribeApi tribeApi,
                                AccessToken accessToken, @Named("utcSimpleDate") SimpleDateFormat simpleDateFormat,
                                MQTTMessageDataMapper mqttMessageDataMapper) {
        if (context == null || chatCache == null) {
            throw new IllegalArgumentException("Constructor parameters cannot be null!");
        }
        this.context = context.getApplicationContext();
        this.chatCache = chatCache;
        this.userCache = userCache;
        this.tribeApi = tribeApi;
        this.accessToken = accessToken;
        this.simpleDateFormat = simpleDateFormat;
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

    public ChatDataStore createCloudChatStore() {
        return new CloudChatDataStore(this.chatCache, this.userCache, this.tribeApi, this.accessToken, this.context, this.simpleDateFormat);
    }
}
