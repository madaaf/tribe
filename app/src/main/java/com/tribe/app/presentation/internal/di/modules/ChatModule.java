package com.tribe.app.presentation.internal.di.modules;

import com.tribe.app.domain.interactor.text.ConnectAndSubscribeMQTT;
import com.tribe.app.domain.interactor.text.DisconnectMQTT;
import com.tribe.app.domain.interactor.text.SubscribingMQTT;
import com.tribe.app.domain.interactor.text.UnsubscribeMQTT;
import com.tribe.app.presentation.internal.di.PerActivity;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

/**
 * Dagger module that provides text related collaborators.
 */
@Module
public class ChatModule {

    public ChatModule() {
    }

    @Provides
    @PerActivity
    @Named("connectAndSubscribe")
    ConnectAndSubscribeMQTT provideConnectAndSubscribeMQTTUseCase(ConnectAndSubscribeMQTT connectAndSubscribeMQTT) {
        return connectAndSubscribeMQTT;
    }

    @Provides
    @PerActivity
    @Named("subscribing")
    SubscribingMQTT provideSubscribingMQTTUseCase(SubscribingMQTT subscribingMQTT) {
        return subscribingMQTT;
    }

    @Provides
    @PerActivity
    @Named("disconnect")
    DisconnectMQTT provideDisconnectMQTTUseCase(DisconnectMQTT disconnectMQTT) {
        return disconnectMQTT;
    }

    @Provides
    @PerActivity
    @Named("unsubscribe")
    UnsubscribeMQTT provideUnsubscribeMQTTUseCase(UnsubscribeMQTT unsubscribeMQTT) {
        return unsubscribeMQTT;
    }
}