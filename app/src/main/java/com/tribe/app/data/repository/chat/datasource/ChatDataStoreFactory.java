/**
 * Copyright (C) 2015 Fernando Cejas Open Source Project
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
