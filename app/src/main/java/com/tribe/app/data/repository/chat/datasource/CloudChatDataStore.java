package com.tribe.app.data.repository.chat.datasource;

import android.content.Context;
import android.net.Uri;

import com.tribe.app.R;
import com.tribe.app.data.cache.ChatCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.ChatRealm;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.GroupRealm;
import com.tribe.app.data.realm.MessageRealmInterface;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.data.repository.tribe.datasource.TribeDataStore;
import com.tribe.app.domain.entity.ChatMessage;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.utils.MessageSendingStatus;

import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import rx.Observable;
import rx.functions.Action1;

/**
 * {@link ChatDataStore} implementation based on the cloud api.
 */
public class CloudChatDataStore implements ChatDataStore {

    private final TribeApi tribeApi;
    private final ChatCache chatCache;
    private final UserCache userCache;
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
    public CloudChatDataStore(ChatCache chatCache, UserCache userCache, TribeApi tribeApi, AccessToken accessToken,
                              Context context, SimpleDateFormat simpleDateFormat) {
        this.chatCache = chatCache;
        this.userCache = userCache;
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
                chatServer.setMessageSendingStatus(MessageSendingStatus.STATUS_SENT);
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
                    chatServer.setMessageSendingStatus(MessageSendingStatus.STATUS_SENT);
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

    @Override
    public Observable<List<ChatRealm>> markMessageListAsRead(List<ChatRealm> messageRealmList) {
        StringBuffer buffer = new StringBuffer();

        int count = 0;
        for (ChatRealm chatRealm : messageRealmList) {
            buffer.append(context.getString(R.string.chatMessage_markAsSeen_item, "message" + chatRealm.getId(), chatRealm.getId()) + (count < messageRealmList.size() - 1 ? "," : ""));
            count++;
        }

        if (buffer.length() > 0) {
            String req = context.getString(R.string.message_markAsSeen, buffer.toString());
            return this.tribeApi.markMessageListAsSeen(req);
        }

        return Observable.empty();
    }

    @Override
    public Observable<List<ChatRealm>> messagesError(String recipientId) {
        return null;
    }

    @Override
    public Observable<List<ChatRealm>> messagesReceived(String recipientId) {
        return null;
    }

    @Override
    public Observable<Void> updateStatuses(String friendshipId) {
        StringBuffer idsMessages = new StringBuffer();

        Set<String> toIds = new HashSet<>();

        UserRealm user = userCache.userInfosNoObs(accessToken.getUserId());

        for (FriendshipRealm fr : user.getFriendships()) {
            toIds.add(fr.getFriend().getId());
        }

        for (GroupRealm gr : user.getGroups()) {
            toIds.add(gr.getId());
        }

        List<ChatRealm> statusToUpdate = chatCache.messagesToUpdateStatus(toIds);

        int countMessages = 0;
        for (ChatRealm chatRealm : statusToUpdate) {
            if (!StringUtils.isEmpty(chatRealm.getId())) {
                idsMessages.append((countMessages > 0 ? "," : "") + "\"" + chatRealm.getId() + "\"");
                countMessages++;
            }
        }

        if (countMessages > 0) {
            String req = context.getString(R.string.messages_status,
                    !StringUtils.isEmpty(idsMessages.toString()) ? context.getString(R.string.message_sent_infos, idsMessages) : "");

            return tribeApi.messages(req).doOnNext(saveToCacheMessages).map(messageRealmInterfaces -> null);
        }

        return Observable.empty();
    }

    private final Action1<List<MessageRealmInterface>> saveToCacheMessages = messageRealmList -> {
        if (messageRealmList != null && messageRealmList.size() > 0) {
            List<ChatRealm> chatRealmList = new ArrayList<>();

            for (MessageRealmInterface message : messageRealmList) {
                if (message instanceof ChatRealm) chatRealmList.add((ChatRealm) message);
            }

            CloudChatDataStore.this.chatCache.put(chatRealmList);
        }
    };
}
