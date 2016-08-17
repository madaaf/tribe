package com.tribe.app.data.repository.chat.datasource;

import android.content.Context;

import com.tribe.app.R;
import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.ChatRealm;
import com.tribe.app.data.repository.tribe.datasource.TribeDataStore;
import com.tribe.app.presentation.view.utils.MessageStatus;

import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.text.SimpleDateFormat;
import java.util.List;

import rx.Observable;

/**
 * {@link ChatDataStore} implementation based on the cloud api.
 */
public class CloudChatDataStore implements ChatDataStore {

    private final TribeApi tribeApi;
    private final ChatCache chatCache;
    private final Context context;
    private final AccessToken accessToken;
    private final SimpleDateFormat simpleDateFormat;

    /**
     * Construct a {@link TribeDataStore} based on connections to the api (Cloud).
     * @param chatCache A {@link ChatCache} to cache data retrieved from the api.
     * @param tribeApi an implementation of the api
     * @param context the context
     * @param accessToken the access token
     */
    public CloudChatDataStore(ChatCache chatCache, TribeApi tribeApi, AccessToken accessToken,
                              Context context, SimpleDateFormat simpleDateFormat) {
        this.chatCache = chatCache;
        this.tribeApi = tribeApi;
        this.context = context;
        this.accessToken = accessToken;
        this.simpleDateFormat = simpleDateFormat;
    }

    @Override
    public Observable<IMqttToken> connectAndSubscribe(String topic) {
        return null;
    }

    @Override
    public Observable<IMqttToken> disconnect() {
        return null;
    }

    @Override
    public Observable<IMqttToken> unsubscribe(String topic) {
        return null;
    }

    @Override
    public Observable<List<ChatRealm>> messages(String recipientId) {
        return null;
    }

    @Override
    public Observable<ChatRealm> sendMessage(ChatRealm chatRealm) {
        String request = context.getString(R.string.chat_send,
                chatRealm.getFrom().getId(),
                chatRealm.isToGroup() ?  chatRealm.getGroup().getId() : chatRealm.getFriendshipRealm().getId(),
                chatRealm.isToGroup(),
                chatRealm.getType(),
                simpleDateFormat.format(chatRealm.getRecordedAt()),
                chatRealm.getContent()
        );

        return tribeApi.sendChat(request).map(chatServer -> {
            chatServer.setMessageStatus(MessageStatus.STATUS_SENT);
            return chatCache.updateLocalWithServerRealm(chatRealm, chatServer);
        });
    }

    @Override
    public Observable<Void> deleteMessage(ChatRealm chatRealm) {
        return null;
    }
}
