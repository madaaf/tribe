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
package com.tribe.app.data.repository.user.datasource;

import android.content.Context;

import com.tribe.app.data.cache.FriendshipCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.repository.friendship.datasource.CloudFriendshipDataStore;
import com.tribe.app.data.repository.friendship.datasource.FriendshipDataStore;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory that creates different implementations of {@link UserDataStoreFactory}.
 */
@Singleton
public class UserDataStoreFactory {

    private final Context context;
    private final UserCache userCache;
    private final TribeApi tribeApi;

    @Inject
    public UserDataStoreFactory(Context context, UserCache userCache, TribeApi tribeApi) {
        if (context == null || userCache == null) {
            throw new IllegalArgumentException("Constructor parameters cannot be null!");
        }
        this.context = context.getApplicationContext();
        this.userCache = userCache;
        this.tribeApi = tribeApi;
    }

    /**
     * Create {@link UserDataStore}
     */
    public UserDataStore create() {
        return createCloudDataStore();
    }

    /**
     * Create {@link UserDataStore} to retrieve data from the Cloud.
     */
    public UserDataStore createCloudDataStore() {
        return new CloudUserDataStore(this.userCache, this.tribeApi, this.context);
    }
}
