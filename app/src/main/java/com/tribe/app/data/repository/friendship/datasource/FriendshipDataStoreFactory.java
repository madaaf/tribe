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
package com.tribe.app.data.repository.friendship.datasource;

import android.content.Context;

import com.tribe.app.data.cache.FriendshipCache;
import com.tribe.app.data.network.TribeApi;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory that creates different implementations of {@link FriendshipDataStore}.
 */
@Singleton
public class FriendshipDataStoreFactory {

    private final Context context;
    private final FriendshipCache friendshipCache;
    private final TribeApi tribeApi;

    @Inject
    public FriendshipDataStoreFactory(Context context, FriendshipCache friendshipCache, TribeApi tribeApi) {
        if (context == null || friendshipCache == null) {
            throw new IllegalArgumentException("Constructor parameters cannot be null!");
        }
        this.context = context.getApplicationContext();
        this.friendshipCache = friendshipCache;
        this.tribeApi = tribeApi;
    }

    /**
     * Create {@link FriendshipDataStore} from a friendship id.
     */
    public FriendshipDataStore create(int friendshipId) {
        FriendshipDataStore userDataStore;

        //if (!this.friendshipCache.isExpired() && this.friendshipCache.isCached(friendshipId)) {
        //    userDataStore = new DiskUserDataStore(this.userCache);
        //} else {
            userDataStore = createCloudDataStore();
        //}

        return userDataStore;
    }

    /**
     * Create {@link FriendshipDataStore} to retrieve data from the Cloud.
     */
    public FriendshipDataStore createCloudDataStore() {
        return new CloudFriendshipDataStore(this.friendshipCache, this.tribeApi);
    }
}
