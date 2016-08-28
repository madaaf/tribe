package com.tribe.app.data.repository.chat.datasource;

import com.tribe.app.data.realm.ChatRealm;

import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.util.List;

import rx.Observable;

/**
 * Interface that represents a data store from where data is retrieved.
 */
public interface ChatDataStore {
    /**
     * Get an {@link Observable} which will emit a logged an mqtt observable.
     * @param topic The topic to subscribe to.
     */
    Observable<IMqttToken> connectAndSubscribe(String topic);

    /**
     * Get an {@link Observable} which will emit a {@link IMqttToken}.
     */
    Observable<IMqttToken> disconnect();

    /**
     * Get an {@link Observable} which will emit a {@link IMqttToken}.
     *
     * @param topic to unsubscribe from.
     */
    Observable<IMqttToken> unsubscribe(final String topic);

    /**
     * Get an {@link Observable} which will emit a {@link com.tribe.app.data.rxmqtt.impl.RxMqttMessage}.
     *
     * @param friendshipId the friendshipId for which to get the messages for
     */
    Observable<List<ChatRealm>> messages(final String friendshipId);

    /**
     * Get an {@link Observable} which will emit a {@link ChatRealm} containing info about the tribe.
     *
     * @param chatRealm the ChatRealm to save
     */
    Observable<ChatRealm> sendMessage(final ChatRealm chatRealm);

    /**
     * Get an {@link Observable} which will delete a tribe.
     *
     * @param chatRealm the ChatMessage to delete
     */
    Observable<Void> deleteMessage(final ChatRealm chatRealm);

    /**
     * Get an {@link Observable} which will emit nothing.
     *
     * @param friendshipId the friendshipId for which to get the messages for
     */
    Observable<Void> deleteConversation(final String friendshipId);

    /**
     * Get an {@link Observable} which will emit a {@link List< ChatRealm >} containing info about the messages.
     *
     * @param messageRealmList the ChatRealm List to put as seen
     */
    Observable<List<ChatRealm>> markMessageListAsRead(final List<ChatRealm> messageRealmList);

    /**
     * Get an {@link Observable} which will emit a {@link List< ChatRealm >}.
     *
     * @param recipientId the recipientId for which to get the messages in error for
     */
    Observable<List<ChatRealm>> messagesError(final String recipientId);
}
