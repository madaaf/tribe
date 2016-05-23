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
package com.tribe.app.data.repository.text.datasource;

import android.content.Context;

import com.tribe.app.data.cache.TextCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.repository.user.datasource.CloudUserDataStore;
import com.tribe.app.data.repository.user.datasource.UserDataStore;
import com.tribe.app.data.rxmqtt.constants.Constants;
import com.tribe.app.data.rxmqtt.exceptions.RxMqttException;
import com.tribe.app.data.rxmqtt.impl.RxMqttAsyncClient;
import com.tribe.app.data.rxmqtt.interfaces.IRxMqttClient;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory that creates different implementations of {@link TextDataStoreFactory}.
 */
@Singleton
public class TextDataStoreFactory {

    private final Context context;
    private final TextCache textCache;
    private IRxMqttClient client = null;

    @Inject
    public TextDataStoreFactory(Context context, TextCache textCache) {
        if (context == null || textCache == null) {
            throw new IllegalArgumentException("Constructor parameters cannot be null!");
        }
        this.context = context.getApplicationContext();
        this.textCache = textCache;
        try {
            this.client = new RxMqttAsyncClient(String.format("%s://%s:%d", Constants.TCP, "176.58.122.224", 1883), "tiago");
        } catch (RxMqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create {@link TextDataStore}
     */
    public TextDataStore create() {
        return createMQTTStore();
    }

    /**
     * Create {@link TextDataStore} to retrieve data from the Cloud.
     */
    public TextDataStore createMQTTStore() {
        return new MQTTTextDataStore(this.textCache, this.client);
    }
}
