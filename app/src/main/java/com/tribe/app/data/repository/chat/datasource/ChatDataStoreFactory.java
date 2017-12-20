package com.tribe.app.data.repository.chat.datasource;

import android.content.Context;
import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.FileApi;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.realm.mapper.MessageRealmDataMapper;
import com.tribe.app.presentation.utils.RXZendesk.RXZendesk;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by madaaflak on 12/09/2017.
 */

@Singleton public class ChatDataStoreFactory {

  private final Context context;
  private final TribeApi tribeApi;
  private final ChatCache chatCache;
  private final UserCache userCache;
  private final FileApi fileApi;
  private final MessageRealmDataMapper messageRealmDataMapper;
  private final RXZendesk rxZendesk;

  @Inject public ChatDataStoreFactory(Context context, TribeApi tribeApi, ChatCache chatCache,
      UserCache userCache, FileApi fileApi, MessageRealmDataMapper messageRealmDataMapper,
      RXZendesk rxZendesk) {
    this.context = context.getApplicationContext();
    this.tribeApi = tribeApi;
    this.chatCache = chatCache;
    this.userCache = userCache;
    this.fileApi = fileApi;
    this.messageRealmDataMapper = messageRealmDataMapper;
    this.rxZendesk = rxZendesk;
  }

  public ChatDataStore createCloudDataStore() {
    return new CloudChatDataStore(context, tribeApi, fileApi, chatCache, userCache,
        messageRealmDataMapper, rxZendesk);
  }

  public ChatDataStore createDiskDataStore() {
    return new DiskChatDataStore(chatCache, messageRealmDataMapper);
  }
}
