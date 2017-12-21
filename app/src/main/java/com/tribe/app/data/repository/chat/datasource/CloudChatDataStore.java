package com.tribe.app.data.repository.chat.datasource;

import android.content.Context;
import android.net.Uri;
import com.tribe.app.R;
import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.FileApi;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.data.realm.mapper.MessageRealmDataMapper;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.presentation.utils.RXZendesk.RXZendesk;
import com.tribe.app.presentation.view.widget.chat.model.Conversation;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import com.tribe.tribelivesdk.util.JsonUtils;
import io.realm.RealmList;
import java.util.List;
import rx.Observable;
import rx.functions.Action1;
import timber.log.Timber;

/**
 * Created by madaaflak on 12/09/2017.
 */

public class CloudChatDataStore implements ChatDataStore {

  private final Context context;
  private final TribeApi tribeApi;
  private ChatCache chatCache;
  private UserCache userCache;
  private FileApi fileApi;
  private MessageRealmDataMapper messageRealmDataMapper;
  private final RXZendesk rxZendesk;

  public CloudChatDataStore(Context context, TribeApi tribeApi, FileApi fileApi,
      ChatCache chatCache, UserCache userCache, MessageRealmDataMapper messageRealmDataMapper,
      RXZendesk rxZendesk) {
    this.context = context;
    this.tribeApi = tribeApi;
    this.chatCache = chatCache;
    this.userCache = userCache;
    this.fileApi = fileApi;
    this.messageRealmDataMapper = messageRealmDataMapper;
    this.rxZendesk = rxZendesk;
  }

  @Override public Observable<List<Conversation>> getMessageSupport(int typeSupport) {
    return fileApi.getMessageSupport().doOnNext(messageSupport -> {
      // chatCache.putMessagesSupport(messageSupport.get(typeSupport).getMessages());

      RealmList<MessageRealm> list = new RealmList<>();
      for (Message message : messageSupport.get(typeSupport).getMessages()) {
        list.add(messageRealmDataMapper.transform(message));
      }

      chatCache.putMessages(list, Shortcut.SUPPORT);
    });
  }


  @Override
  public Observable<MessageRealm> createMessage(String[] userIds, String type, String data,
      String date) {
    String req =
        context.getString(R.string.messages_create, JsonUtils.arrayToJson(userIds), type, data,
            context.getString(R.string.messagefragment_info));
    Timber.i("req : " + req);
    return this.tribeApi.createMessage(req).doOnNext(messageRealm -> {
      RealmList<MessageRealm> list = new RealmList<>();
      list.add(messageRealm);
      chatCache.putMessages(list, JsonUtils.arrayToJson(userIds));
    }).doOnNext(messageRealm -> refactorMessages.call(userIds));
  }

  @Override
  public Observable<UserRealm> loadMessages(String[] userIds, String dateBefore, String dateAfter) {
    String req;
    if (dateAfter == null) {
      req = context.getString(R.string.messages_details_before, JsonUtils.arrayToJson(userIds),
          dateBefore, null, context.getString(R.string.messagefragment_info));
      Timber.i("request : " + req);
      return this.tribeApi.getUserMessage(req)
          .doOnNext(userRealm -> chatCache.putMessages(userRealm.getMessages(),
              JsonUtils.arrayToJson(userIds)))
          .doOnNext(messageRealm -> refactorMessages.call(userIds));
    } else {
      req = context.getString(R.string.messages_details_between, JsonUtils.arrayToJson(userIds),
          dateBefore, dateAfter, context.getString(R.string.messagefragment_info));
      Timber.i("request : " + req);
      return this.tribeApi.getUserMessage(req)
          .doOnNext(userRealm -> chatCache.deleteRemovedMessageFromCache(userRealm.getMessages(),
              JsonUtils.arrayToJson(userIds), dateBefore, dateAfter))
          .doOnNext(messageRealm -> refactorMessages.call(userIds));
    }
  }

  @Override public Observable<List<Message>> getMessageZendesk(String supportId) {
    return rxZendesk.getComments(supportId).doOnNext(comments -> {
      RealmList<MessageRealm> messageRealms = new RealmList<>();
      messageRealms.addAll(messageRealmDataMapper.transformMessages(comments));
      chatCache.putMessages(messageRealms, Shortcut.SUPPORT);
    });
  }

  @Override public Observable<Boolean> addMessageZendesk(String supportId, String data, Uri uri) {
    return rxZendesk.addMessageZendesk(supportId, data, uri);
  }

  @Override public Observable<List<MessageRealm>> getMessages(String[] userIds) {
    return null;
  }

  @Override public Observable<List<MessageRealm>> getMessagesImage(String[] userIds) {
    return null;
  }

  @Override public Observable<String> isTyping() {
    return null;
  }

  @Override public Observable<String> isTalking() {
    return null;
  }

  @Override public Observable<String> isReading() {
    return null;
  }

  @Override public Observable<List<MessageRealm>> onMessageReceived() {
    return null;
  }

  @Override public Observable<MessageRealm> onMessageRemoved() {
    return null;
  }

  @Override public Observable<Boolean> imTyping(String[] userIds) {
    final String request = context.getString(R.string.mutation,
        context.getString(R.string.imTyping, JsonUtils.arrayToJson(userIds)));
    return this.tribeApi.imTyping(request);
  }

  private final Action1<String[]> refactorMessages = userIds -> {
    MessageRealm latestMessage = chatCache.getLastTextMessage(userIds);
    ShortcutRealm shortcutRealm = userCache.shortcutForUserIdsNoObs(userIds);

    if (shortcutRealm != null && latestMessage != null) {
      String txt = "";
      if (shortcutRealm.isSingle()) {
        txt = latestMessage.getData();
      } else {
        txt = latestMessage.getAuthor().getDisplayName() + " : " + latestMessage.getData();
      }

      userCache.updateShortcutLastText(shortcutRealm.getId(), txt);
    }
  };
}
