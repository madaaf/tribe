package com.tribe.app.data.network;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.tribe.app.R;
import com.tribe.app.data.cache.LiveCache;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.deserializer.JsonToModel;
import com.tribe.app.data.network.entity.SubscriptionResponse;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.data.realm.MembershipRealm;
import com.tribe.app.data.realm.UserRealm;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.tribelivesdk.back.WebSocketConnection;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import rx.Observable;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

@Singleton
public class WSService extends Service {

    public static Intent getCallingIntent(Context context) {
        Intent intent = new Intent(context, WSService.class);
        return intent;
    }

    @Inject
    UserCache userCache;

    @Inject
    LiveCache liveCache;

    @Inject
    AccessToken accessToken;

    @Inject
    JsonToModel jsonToModel;

    @Inject
    @Named("webSocketApi")
    WebSocketConnection webSocketConnection;

    // VARIABLES
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initDependencyInjection();
    }

    @Override
    public void onDestroy() {
        if (subscriptions != null) subscriptions.clear();
        handleStop();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (subscriptions != null) subscriptions.clear();
        handleStart();
        return Service.START_STICKY;
    }

    private void handleStart() {
        initWebSocket();
    }

    private void handleStop() {
        if (webSocketConnection.getState().equals(WebSocketConnection.STATE_CONNECTED)) {
            webSocketConnection.disconnect(false);
        }
    }

    private void initWebSocket() {
        webSocketConnection.connect("wss://api.tribedev.pm");

        subscriptions.add(
                webSocketConnection.onStateChanged()
                        .subscribe(newState -> {
                            if (newState.equals(WebSocketConnection.STATE_CONNECTED)) {
                                initSubscriptions();
                            }
                        })
        );

        subscriptions.add(
                webSocketConnection.onMessage()
                        .subscribe(message -> {
                            Timber.d("onMessage : " + message);

                            SubscriptionResponse subscriptionResponse = jsonToModel.convertToSubscriptionResponse(message);

                            if (subscriptionResponse != null) {
                                liveCache.putOnlineMap(subscriptionResponse.getOnlineMap());
                                userCache.updateUsersAndGroups(
                                        subscriptionResponse.getUserUpdatedList(),
                                        subscriptionResponse.getGroupUpdatedList()
                                );
                            }
                        })
        );
    }

    private void initSubscriptions() {
        UserRealm userRealm = userCache.userInfosNoObs(accessToken.getUserId());

        StringBuffer subscriptionsBuffer = new StringBuffer();
        String hash = generateHash();

        Observable.zip(
                Observable.just(userRealm.getFriendships())
                        .doOnNext(friendshipList -> {
                            int count = 0;

                            for (FriendshipRealm friendshipRealm : friendshipList) {
                                if (!friendshipRealm.getSubId().equals(accessToken.getUserId())) {
                                        subscriptionsBuffer.append(
                                                getApplicationContext().getString(
                                                        R.string.subscription_updatedUser,
                                                        hash + "___u" + count,
                                                        friendshipRealm.getFriend().getId()
                                                )
                                        );
                                    subscriptionsBuffer.append(System.getProperty("line.separator"));

                                    count++;
                                }
                            }
                        }),
                Observable.just(userRealm.getMemberships())
                        .doOnNext(membershipList -> {
                            int count = 0;

                            for (MembershipRealm membershipRealm : membershipList) {
                                subscriptionsBuffer.append(
                                        getApplicationContext().getString(
                                                R.string.subscription_updatedGroup,
                                                hash + "___g" + count,
                                                membershipRealm.getGroup().getId()
                                        )
                                );
                                subscriptionsBuffer.append(System.getProperty("line.separator"));

                                count++;
                            }
                        }), (friendshipRealmRealmList, membershipRealms) -> subscriptionsBuffer)
                .subscribe(stringBuffer -> {
                    String body = subscriptionsBuffer.toString();
                    String req = getApplicationContext().getString(R.string.subscription, subscriptionsBuffer.toString())
                            + (body.contains("UserInfos") ? "\n" + getApplicationContext().getString(R.string.userfragment_infos) : "")
                            + (body.contains("GroupInfo") ? "\n" + getApplicationContext().getString(R.string.groupfragment_info_members) : "");

                    webSocketConnection.send(req);
                });
    }

    private String generateHash() {
        return "H" + UUID.randomUUID().toString().toUpperCase().replace("-", "");
    }

    private void initDependencyInjection() {
        ((AndroidApplication) getApplication()).getApplicationComponent().inject(this);
    }
}