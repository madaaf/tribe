package com.tribe.app.data.repository.chat;

import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.data.realm.mapper.MessageRealmDataMapper;
import com.tribe.app.data.repository.chat.datasource.ChatDataStore;
import com.tribe.app.data.repository.chat.datasource.ChatDataStoreFactory;
import com.tribe.app.data.repository.chat.datasource.DiskChatDataStore;
import com.tribe.app.data.repository.chat.datasource.MQTTChatDataStore;
import com.tribe.app.data.rxmqtt.impl.RxMqttMessage;
import com.tribe.app.domain.entity.Message;
import com.tribe.app.domain.interactor.friendship.FriendshipRepository;
import com.tribe.app.domain.interactor.text.ChatRepository;

import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * {@link ChatDataRepository} for retrieving user data.
 */
@Singleton
public class ChatDataRepository implements ChatRepository {

    private final ChatDataStoreFactory chatDataStoreFactory;
    private final MessageRealmDataMapper messageRealmDataMapper;

    /**
     * Constructs a {@link FriendshipRepository}.
     *
     * @param dataStoreFactory A factory to construct different data source implementations.
     * @param realmDataMapper {@link MessageRealmDataMapper}.
     */
    @Inject
    public ChatDataRepository(ChatDataStoreFactory dataStoreFactory,
                              MessageRealmDataMapper realmDataMapper) {
        this.chatDataStoreFactory = dataStoreFactory;
        this.messageRealmDataMapper = realmDataMapper;
    }

    @Override
    public Observable<IMqttToken> connectAndSubscribe(String topic) {
        return chatDataStoreFactory.createMQTTStore().connectAndSubscribe(topic);
    }

    @Override
    public Observable<List<Message>> subscribing(String topic) {
        ChatDataStore mqttChatDataStore = chatDataStoreFactory.createMQTTStore();
        ChatDataStore diskChatDataStore = chatDataStoreFactory.createDiskChatStore();

        return Observable.combineLatest(
                mqttChatDataStore.messages(topic)
                    .map(messageListRealm -> messageRealmDataMapper.transform(messageListRealm)),
                diskChatDataStore.messages(topic)
                    .map(messageListRealm -> messageRealmDataMapper.transform(messageListRealm)),
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
