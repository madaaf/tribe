package com.tribe.app.data.repository.text;

import com.tribe.app.data.realm.mapper.TextRealmDataMapper;
import com.tribe.app.data.repository.text.datasource.TextDataStoreFactory;
import com.tribe.app.data.rxmqtt.impl.RxMqttMessage;
import com.tribe.app.domain.interactor.friendship.FriendshipRepository;

import org.eclipse.paho.client.mqttv3.IMqttToken;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;

/**
 * {@link TextDataRepository} for retrieving user data.
 */
@Singleton
public class TextDataRepository implements com.tribe.app.domain.interactor.text.TextRepository {

    private final TextDataStoreFactory textDataStoreFactory;
    private final TextRealmDataMapper textRealmDataMapper;

    /**
     * Constructs a {@link FriendshipRepository}.
     *
     * @param dataStoreFactory A factory to construct different data source implementations.
     * @param realmDataMapper {@link TextRealmDataMapper}.
     */
    @Inject
    public TextDataRepository(TextDataStoreFactory dataStoreFactory,
                              TextRealmDataMapper realmDataMapper) {
        this.textDataStoreFactory = dataStoreFactory;
        this.textRealmDataMapper = realmDataMapper;
    }

    @Override
    public Observable<IMqttToken> connectAndSubscribe(String topic) {
        return textDataStoreFactory.createMQTTStore().connectAndSubscribe(topic);
    }

    @Override
    public Observable<RxMqttMessage> subscribing(String topic) {
        return textDataStoreFactory.createMQTTStore().subscribing(topic);
    }

    @Override
    public Observable<IMqttToken> disconnect() {
        return textDataStoreFactory.createMQTTStore().disconnect();
    }

    @Override
    public Observable<IMqttToken> unsubscribe(String topic) {
        return textDataStoreFactory.createMQTTStore().unsubscribe(topic);
    }
}
