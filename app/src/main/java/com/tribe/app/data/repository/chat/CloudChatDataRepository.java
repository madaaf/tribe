package com.tribe.app.data.repository.chat;

import android.content.Context;

import com.tribe.app.data.realm.mapper.ChatRealmDataMapper;
import com.tribe.app.data.repository.chat.datasource.ChatDataStore;
import com.tribe.app.data.repository.chat.datasource.ChatDataStoreFactory;
import com.tribe.app.domain.entity.ChatMessage;
import com.tribe.app.domain.interactor.text.ChatRepository;

import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;

/**
 * {@link CloudChatDataRepository} for retrieving user data.
 */
@Singleton
public class CloudChatDataRepository implements ChatRepository {

    private final ChatDataStoreFactory chatDataStoreFactory;
    private final ChatRealmDataMapper chatRealmDataMapper;

    /**
     * Constructs a {@link ChatRepository}.
     *
     * @param dataStoreFactory A factory to construct different data source implementations.
     * @param realmDataMapper {@link ChatRealmDataMapper}.
     */
    @Inject
    public CloudChatDataRepository(Context context, ChatDataStoreFactory dataStoreFactory,
                                   ChatRealmDataMapper realmDataMapper) {
        this.chatDataStoreFactory = dataStoreFactory;
        this.chatRealmDataMapper = realmDataMapper;
    }

    @Override
    public Observable<IMqttToken> connectAndSubscribe(String topic) {
        return chatDataStoreFactory.createMQTTStore().connectAndSubscribe(topic);
    }

    @Override
    public Observable<List<ChatMessage>> subscribing(String topic) {
        ChatDataStore mqttChatDataStore = chatDataStoreFactory.createMQTTStore();
        ChatDataStore diskChatDataStore = chatDataStoreFactory.createDiskChatStore();

        return Observable.combineLatest(
                mqttChatDataStore.messages(topic)
                    .map(messageListRealm -> chatRealmDataMapper.transform(messageListRealm)),
                diskChatDataStore.messages(topic)
                    .map(messageListRealm -> chatRealmDataMapper.transform(messageListRealm)),
                (messageList, messageList2) -> {
                    messageList2.addAll(messageList);
                    return messageList2;
                }
        );
    }

    @Override
    public Observable<IMqttToken> disconnect() {
        return chatDataStoreFactory.createMQTTStore().disconnect();
    }

    @Override
    public Observable<IMqttToken> unsubscribe(String topic) {
        return chatDataStoreFactory.createMQTTStore().unsubscribe(topic);
    }

    @Override
    public Observable<List<ChatMessage>> messages(String friendshipId) {
        ChatDataStore cloudDataStore = chatDataStoreFactory.createCloudChatStore();
        return cloudDataStore.messages(friendshipId).map(collection -> chatRealmDataMapper.transform(collection));
    }

    @Override
    public Observable<ChatMessage> sendMessage(ChatMessage chatMessage) {
        ChatDataStore cloudDataStore = chatDataStoreFactory.createCloudChatStore();
        return cloudDataStore.sendMessage(chatRealmDataMapper.transform(chatMessage))
                .map(chatRealm -> chatRealmDataMapper.transform(chatRealm));
    }

    @Override
    public Observable<Void> deleteMessage(ChatMessage chatMessage) {
        return null;
    }

    @Override
    public Observable<Void> deleteConversation(String friendshipId) {
        return null;
    }

    @Override
    public Observable<List<ChatMessage>> markMessageListAsRead(final List<ChatMessage> messageList) {
        final ChatDataStore chatDataStore = this.chatDataStoreFactory.createCloudChatStore();
        return chatDataStore.markMessageListAsRead(chatRealmDataMapper.transform(messageList))
                .filter(chatRealmList -> messageList != null)
                .map(collection -> chatRealmDataMapper.transform(collection));
    }

    @Override
    public Observable<List<ChatMessage>> messagesError(String recipientId) {
        return null;
    }

    @Override
    public Observable<Void> updateStatuses(String recipientId) {
        final ChatDataStore chatDataStore = this.chatDataStoreFactory.createCloudChatStore();
        return chatDataStore.updateStatuses(recipientId);
    }

    @Override
    public Observable<List<ChatMessage>> manageChatHistory(boolean toGroup, String recipientId) {
        final ChatDataStore chatDataStore = this.chatDataStoreFactory.createCloudChatStore();
        return chatDataStore.manageChatHistory(toGroup, recipientId).map(collection -> chatRealmDataMapper.transform(collection));
    }
}
