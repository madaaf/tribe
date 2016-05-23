package com.tribe.app.data.rxmqtt.impl;

import com.tribe.app.data.rxmqtt.enums.RxMqttClientState;
import com.tribe.app.data.rxmqtt.enums.RxMqttExceptionType;
import com.tribe.app.data.rxmqtt.exceptions.RxMqttException;
import com.tribe.app.data.rxmqtt.exceptions.RxMqttTokenException;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Hashtable;
import java.util.regex.Pattern;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.subjects.PublishSubject;

public class RxMqttAsyncClient extends RxMqttClient {

    private MqttAsyncClient client;
    private Hashtable<String, Pattern> patternHashtable;
    private Hashtable<String, PublishSubject<RxMqttMessage>> subjectHashtable;

    public RxMqttAsyncClient(String brokerUrl, String clientId)
            throws RxMqttException {
        super();
        try {
            client = new MqttAsyncClient(brokerUrl, clientId, new MemoryPersistence());
        } catch (MqttException ex) {
            ex.printStackTrace();
            throw new RxMqttException(RxMqttExceptionType.CLIENT_INIT_ERROR);
        }
    }

    private void connect(final Subscriber<? super IMqttToken> subscriber) {
        try {
            updateState(RxMqttClientState.CONNECTING);
            client.connect(this.getConOpt(), "Context", new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    updateState(RxMqttClientState.CONNECTED);
                    subscriber.onNext(asyncActionToken);
                    subscriber.onCompleted();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    updateState(RxMqttClientState.CONNECTING_FAILED);
                    subscriber.onError(new RxMqttTokenException(exception, asyncActionToken));
                }
            });
        } catch (MqttException ex) {
            updateState(RxMqttClientState.CONNECTING_FAILED);
            subscriber.onError(ex);
        }
    }

    @Override
    public Observable<IMqttToken> connect() {
        return Observable.create(new Observable.OnSubscribe<IMqttToken>() {
            @Override
            public void call(final Subscriber<? super IMqttToken> subscriber) {
                if (null == client) {
                    updateState(RxMqttClientState.CONNECTING_FAILED);
                    subscriber.onError(new
                            RxMqttException(RxMqttExceptionType.CLIENT_NULL_ERROR));
                }

                disconnect().subscribe(new Observer<IMqttToken>() {
                    @Override
                    public void onCompleted() {
                        connect(subscriber);
                    }

                    @Override
                    public void onError(Throwable e) {
                        connect(subscriber);
                    }

                    @Override
                    public void onNext(IMqttToken iMqttToken) {

                    }
                });
            }
        });
    }

    @Override
    public Observable<IMqttToken> disconnect() {
        return Observable.create(new Observable.OnSubscribe<IMqttToken>() {
            @Override
            public void call(final Subscriber<? super IMqttToken> subscriber) {
                if (client != null && client.isConnected()) {
                    try {
                        updateState(RxMqttClientState.TRY_DISCONNECT);
                        client.disconnect("Context", new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                updateState(RxMqttClientState.TRY_DISCONNECT);
                                subscriber.onNext(asyncActionToken);
                                subscriber.onCompleted();
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                updateState(RxMqttClientState.DISCONNECTED);
                                subscriber.onError(new RxMqttTokenException(exception, asyncActionToken));
                            }
                        });
                    } catch (MqttException e) {
                        updateState(RxMqttClientState.DISCONNECTED);
                        subscriber.onError(e);
                    }
                } else {
                    subscriber.onCompleted();
                }
            }
        });
    }

    @Override
    public Observable<IMqttToken> publish(final String topic, final byte[] msg) {
        final MqttMessage message = new MqttMessage();
        message.setQos(2);
        message.setPayload(msg);

        return Observable.create(new Observable.OnSubscribe<IMqttToken>() {
            @Override
            public void call(final Subscriber<? super IMqttToken> subscriber) {
                if (null == client) subscriber.onError(new
                        RxMqttException(RxMqttExceptionType.CLIENT_NULL_ERROR));

                try {
                    client.publish(topic, message, "Context", new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            subscriber.onNext(asyncActionToken);
                            subscriber.onCompleted();
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            subscriber.onError(new RxMqttTokenException(exception, asyncActionToken));
                        }
                    });
                } catch (MqttException ex) {
                    subscriber.onError(ex);
                }
            }
        });
    }

    @Override
    public Observable<IMqttToken> subscribeTopic(final String topic, final int qos) {
        return Observable.create(new Observable.OnSubscribe<IMqttToken>() {
            @Override
            public void call(final Subscriber<? super IMqttToken> subscriber) {
                if (null == client) subscriber.onError(new
                        RxMqttException(RxMqttExceptionType.CLIENT_NULL_ERROR));

                try {
                    client.subscribe(topic, qos, "Context", new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            subscriber.onNext(asyncActionToken);
                            subscriber.onCompleted();
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            subscriber.onError(new RxMqttTokenException(exception, asyncActionToken));
                        }
                    });
                } catch (MqttException ex) {
                    subscriber.onError(ex);
                }
            }
        });
    }

    @Override
    public Observable<IMqttToken> unsubscribeTopic(final String topic) {
        return Observable.create(new Observable.OnSubscribe<IMqttToken>() {
            @Override
            public void call(final Subscriber<? super IMqttToken> subscriber) {
                if (null == client) subscriber.onError(new
                        RxMqttException(RxMqttExceptionType.CLIENT_NULL_ERROR));

                try {
                    client.unsubscribe(topic, "Context", new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            subscriber.onNext(asyncActionToken);
                            subscriber.onCompleted();
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            subscriber.onError(new RxMqttTokenException(exception, asyncActionToken));
                        }
                    });
                } catch (MqttException ex) {
                    subscriber.onError(ex);
                }
            }
        });
    }

    @Override
    public Observable<RxMqttMessage> subscribing(String regularExpression) {
        return subscribing(Pattern.compile(regularExpression));
    }

    @Override
    public synchronized Observable<RxMqttMessage> subscribing(final Pattern pattern) {
        if (null == patternHashtable && null == subjectHashtable) {
            patternHashtable = new Hashtable<>();
            subjectHashtable = new Hashtable<>();

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    updateState(RxMqttClientState.CONNECTION_LOST);
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    if (message.getPayload().length != 0) {
                        for (String key : patternHashtable.keySet()) {
                            if (patternHashtable.get(key).matcher(topic).matches()) {
                                subjectHashtable.get(key).onNext(new RxMqttMessage(topic, message));
                            }
                        }
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });
        }
        if (patternHashtable.containsKey(pattern.pattern())) {
            return subjectHashtable.get(pattern.pattern());
        } else {
            patternHashtable.put(pattern.pattern(), pattern);
            PublishSubject<RxMqttMessage> subject = PublishSubject.create();
            subjectHashtable.put(pattern.pattern(), subject);
            return subjectHashtable.get(pattern.pattern());
        }
    }

    @Override
    public void disconnectForcibly() {
        try {
            client.disconnectForcibly();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Observable<IMqttToken> checkPing(final Object userContext) {
        return Observable.create(new Observable.OnSubscribe<IMqttToken>() {
            @Override
            public void call(final Subscriber<? super IMqttToken> subscriber) {
                if (!subscriber.isUnsubscribed()) {
                    try {
                        IMqttToken mqttToken = client.checkPing(userContext, null);
                        subscriber.onNext(mqttToken);
                        subscriber.onCompleted();
                    } catch (MqttException e) {
                        subscriber.onError(new RxMqttTokenException(e, null));
                    }
                }
            }
        });

    }
}
