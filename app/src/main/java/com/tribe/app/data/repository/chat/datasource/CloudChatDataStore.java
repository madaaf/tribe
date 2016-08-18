package com.tribe.app.data.repository.chat.datasource;

import android.content.Context;
import android.net.Uri;

import com.tribe.app.R;
import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.ChatRealm;
import com.tribe.app.data.repository.tribe.datasource.TribeDataStore;
import com.tribe.app.domain.entity.ChatMessage;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.view.utils.MessageStatus;

import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
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
                    chatRealm.isToGroup() ? chatRealm.getGroup().getId() : chatRealm.getFriendshipRealm().getId(),
                    chatRealm.isToGroup(),
                    chatRealm.getType(),
                    simpleDateFormat.format(chatRealm.getRecordedAt()),
                    chatRealm.getType().equals(ChatMessage.PHOTO) ? "" : chatRealm.getContent()
            );

        if (chatRealm.getType().equals(ChatMessage.TEXT)) {
            return tribeApi.sendChat(request).map(chatServer -> {
                chatServer.setMessageStatus(MessageStatus.STATUS_SENT);
                return chatCache.updateLocalWithServerRealm(chatRealm, chatServer);
            });
        } else {
            RequestBody query = RequestBody.create(MediaType.parse("text/plain"), request);

            InputStream inputStream = null;
            File file = FileUtils.getFileEnd(FileUtils.generateIdForMessage());
            try {
                inputStream = context.getContentResolver().openInputStream(Uri.parse(chatRealm.getContent()));
                FileUtils.copyInputStreamToFile(inputStream, file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (file != null && file.exists() && file.length() > 0) {
                RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
                MultipartBody.Part body = MultipartBody.Part.createFormData("content", file.getName(), requestFile);

                return tribeApi.uploadMessagePhoto(query, body).map(chatServer -> {
                    chatServer.setMessageStatus(MessageStatus.STATUS_SENT);
                    return chatCache.updateLocalWithServerRealm(chatRealm, chatServer);
                });
            }

            return null;
        }
    }

    @Override
    public Observable<Void> deleteMessage(ChatRealm chatRealm) {
        return null;
    }

    @Override
    public Observable<Void> deleteConversation(String friendshipId) {
        return null;
    }
}
