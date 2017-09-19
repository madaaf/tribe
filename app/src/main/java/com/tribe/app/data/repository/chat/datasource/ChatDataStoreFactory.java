package com.tribe.app.data.repository.chat.datasource;

import android.content.Context;
import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.data.network.TribeApi;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by madaaflak on 12/09/2017.
 */

@Singleton public class ChatDataStoreFactory {

  private final Context context;
  private final TribeApi tribeApi;
  private final ChatCache chatCache;

  @Inject public ChatDataStoreFactory(Context context, TribeApi tribeApi, ChatCache chatCache) {

    this.context = context.getApplicationContext();
    this.tribeApi = tribeApi;
    this.chatCache = chatCache;
  }

  public ChatDataStore createCloudDataStore() {
    return new CloudChatDataStore(context, tribeApi, chatCache);
  }

  public ChatDataStore createDiskDataStore() {
    return new DiskChatDataStore(chatCache);
  }
}
