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
}
