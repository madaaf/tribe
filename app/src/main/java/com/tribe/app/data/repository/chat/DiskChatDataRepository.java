package com.tribe.app.data.repository.chat;

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
 * {@link DiskChatDataRepository} for retrieving user data.
 */
@Singleton
public class DiskChatDataRepository implements ChatRepository {

    private final ChatDataStoreFactory chatDataStoreFactory;
    private final ChatRealmDataMapper chatRealmDataMapper;

    /**
     * Constructs a {@link ChatRepository}.
     *
     * @param dataStoreFactory A factory to construct different data source implementations.
     * @param realmDataMapper {@link ChatRealmDataMapper}.
     */
    @Inject
    public DiskChatDataRepository(ChatDataStoreFactory dataStoreFactory,
                                  ChatRealmDataMapper realmDataMapper) {
        this.chatDataStoreFactory = dataStoreFactory;
        this.chatRealmDataMapper = realmDataMapper;
    }

    @Override
    public Observable<IMqttToken> connectAndSubscribe(String topic) {
        return null;
    }

    @Override
    public Observable<List<ChatMessage>> subscribing(String topic) {
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
    public Observable<List<ChatMessage>> messages(String friendshipId) {
        ChatDataStore diskChatDataStore = chatDataStoreFactory.createDiskChatStore();
        return diskChatDataStore.messages(friendshipId).map(chatRealmList -> chatRealmDataMapper.transform(chatRealmList));
    }

    @Override
    public Observable<ChatMessage> sendMessage(ChatMessage chatMessage) {
        return null;
    }

    @Override
    public Observable<Void> deleteMessage(ChatMessage chatMessage) {
        ChatDataStore diskChatDataStore = chatDataStoreFactory.createDiskChatStore();
        return diskChatDataStore.deleteMessage(chatRealmDataMapper.transform(chatMessage));
    }

    @Override
    public Observable<Void> deleteConversation(String friendshipId) {
        ChatDataStore diskChatDataStore = chatDataStoreFactory.createDiskChatStore();
        return diskChatDataStore.deleteConversation(friendshipId);
    }

    @Override
    public Observable<List<ChatMessage>> markMessageListAsRead(final List<ChatMessage> chatList) {
        final ChatDataStore chatDataStore = this.chatDataStoreFactory.createDiskChatStore();
        return chatDataStore.markMessageListAsRead(chatRealmDataMapper.transform(chatList))
                .map(collection -> chatRealmDataMapper.transform(collection));
    }

    @Override
    public Observable<List<ChatMessage>> messagesError(String recipientId) {
        ChatDataStore diskChatDataStore = chatDataStoreFactory.createDiskChatStore();
        return diskChatDataStore.messagesError(recipientId).map(chatRealmList -> chatRealmDataMapper.transform(chatRealmList));
    }

    @Override
    public Observable<Void> updateStatuses(String recipientId) {
        return null;
    }

    @Override
    public Observable<List<ChatMessage>> manageChatHistory(boolean toGroup, String recipientId) {
        return null;
    }
}
