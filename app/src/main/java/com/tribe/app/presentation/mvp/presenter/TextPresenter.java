package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.data.rxmqtt.impl.RxMqttMessage;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.exception.DefaultErrorBundle;
import com.tribe.app.domain.exception.ErrorBundle;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.text.ConnectAndSubscribeMQTT;
import com.tribe.app.domain.interactor.text.DisconnectMQTT;
import com.tribe.app.domain.interactor.text.SubscribingMQTT;
import com.tribe.app.domain.interactor.text.UnsubscribeMQTT;
import com.tribe.app.domain.interactor.user.DoLoginWithUsername;
import com.tribe.app.presentation.exception.ErrorMessageFactory;
import com.tribe.app.presentation.mvp.view.IntroView;
import com.tribe.app.presentation.mvp.view.TextView;
import com.tribe.app.presentation.mvp.view.View;

import org.eclipse.paho.client.mqttv3.IMqttToken;

import javax.inject.Inject;
import javax.inject.Named;

public class TextPresenter implements Presenter {

    private final ConnectAndSubscribeMQTT connectAndSubscribeMQTT;
    private final SubscribingMQTT subscribingMQTT;
    private final DisconnectMQTT disconnectMQTT;
    private final UnsubscribeMQTT unsubscribeMQTT;

    private TextView textView;

    @Inject
    public TextPresenter(@Named("connectAndSubscribe") ConnectAndSubscribeMQTT connectAndSubscribeMQTT,
                         @Named("subscribing") SubscribingMQTT subscribingMQTT,
                         @Named("disconnect") DisconnectMQTT disconnectMQTT,
                         @Named("unsubscribe") UnsubscribeMQTT unsubscribeMQTT) {
        this.connectAndSubscribeMQTT = connectAndSubscribeMQTT;
        this.subscribingMQTT = subscribingMQTT;
        this.unsubscribeMQTT = unsubscribeMQTT;
        this.disconnectMQTT = disconnectMQTT;
    }

    @Override
    public void onCreate() {
        // Unused
    }

    @Override
    public void onStart() {
        connectAndSubscribeMQTT.setTopic("bavon");
        connectAndSubscribeMQTT.execute(new ConnectAndSubscribeMQTTSubscriber());
    }

    @Override
    public void onResume() {
        // Unused
    }

    @Override
    public void onStop() {
        disconnectMQTT.execute(new DisconnectMQTTSubscriber());
    }

    @Override
    public void onPause() {
        // Unused
    }

    @Override
    public void onDestroy() {
        connectAndSubscribeMQTT.unsubscribe();
        subscribingMQTT.unsubscribe();
        disconnectMQTT.unsubscribe();
        unsubscribeMQTT.unsubscribe();
    }

    @Override
    public void attachView(View v) {
        textView = (TextView) v;
    }

    private final class ConnectAndSubscribeMQTTSubscriber extends DefaultSubscriber<IMqttToken> {

        @Override
        public void onCompleted() {
            System.out.println("ON COMPLETED SUBSCRIBE");
            subscribingMQTT.setTopic("bavon");
            subscribingMQTT.execute(new ListenSubscribingMQTTSubscriber());
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(IMqttToken token) {
            System.out.println("ON NEXT SUBSCRIBE");
        }
    }

    private final class ListenSubscribingMQTTSubscriber extends DefaultSubscriber<RxMqttMessage> {

        @Override
        public void onCompleted() {
            System.out.println("ON COMPLETED MESSAGE");
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(RxMqttMessage message) {
            System.out.println("ON NEXT MESSAGE : " + message.getMessage() + " FOR TOPIC : " + message.getTopic());
        }
    }

    private final class DisconnectMQTTSubscriber extends DefaultSubscriber<IMqttToken> {

        @Override
        public void onCompleted() {
            System.out.println("ON NEXT DISCONNECT");
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(IMqttToken token) {
            System.out.println("ON NEXT DISCONNECT");
        }
    }
}
