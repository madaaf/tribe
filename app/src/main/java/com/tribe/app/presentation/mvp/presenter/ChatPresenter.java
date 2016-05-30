package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.data.rxmqtt.impl.RxMqttMessage;
import com.tribe.app.domain.entity.Message;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.text.ConnectAndSubscribeMQTT;
import com.tribe.app.domain.interactor.text.DisconnectMQTT;
import com.tribe.app.domain.interactor.text.SubscribingMQTT;
import com.tribe.app.domain.interactor.text.UnsubscribeMQTT;
import com.tribe.app.presentation.mvp.view.MessageView;
import com.tribe.app.presentation.mvp.view.View;

import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

public class ChatPresenter implements Presenter {

    private final ConnectAndSubscribeMQTT connectAndSubscribeMQTT;
    private final SubscribingMQTT subscribingMQTT;
    private final DisconnectMQTT disconnectMQTT;
    private final UnsubscribeMQTT unsubscribeMQTT;

    private MessageView messageView;

    private String friendId;

    @Inject
    public ChatPresenter(@Named("connectAndSubscribe") ConnectAndSubscribeMQTT connectAndSubscribeMQTT,
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
    }

    @Override
    public void attachView(View v) {
        messageView = (MessageView) v;
    }

    public void subscribe(String id) {
        friendId = id;
        connectAndSubscribeMQTT.setTopic("chats/" + id + "/#");
        connectAndSubscribeMQTT.execute(new ConnectAndSubscribeMQTTSubscriber());
    }

    public void sendMessage(String str) {
        System.out.println("HEY : " + str);
    }

    public void sendTypingEvent() {
        System.out.println("Typing !");
    }

    private final class ConnectAndSubscribeMQTTSubscriber extends DefaultSubscriber<IMqttToken> {

        @Override
        public void onCompleted() {
            System.out.println("ON COMPLETED SUBSCRIBE");
            subscribingMQTT.setTopic("^chats/" + friendId + ".*");
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

    private final class ListenSubscribingMQTTSubscriber extends DefaultSubscriber<List<Message>> {

        @Override
        public void onCompleted() {
            System.out.println("ON COMPLETED MESSAGE");
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(List<Message> messageList) {
            messageView.renderMessageList(messageList);
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
