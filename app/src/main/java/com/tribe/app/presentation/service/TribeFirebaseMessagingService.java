package com.tribe.app.presentation.service;

import android.util.Log;

import com.f2prateek.rx.preferences.Preference;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.tribe.app.data.network.job.UpdateMessagesJob;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.scope.Theme;

import javax.inject.Inject;

/**
 * Created by tiago on 12/07/2016.
 */
public class TribeFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "TribeFirebaseMessServ";

    @Inject
    @Theme
    Preference<Integer> theme;

    @Override
    public void onCreate() {
        super.onCreate();
        ((AndroidApplication) getApplication()).getApplicationComponent().inject(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "THEME : " + theme.get());
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification ChatMessage Body: " + remoteMessage.getNotification().getBody());
        ((AndroidApplication) getApplication()).getApplicationComponent().jobManager().addJobInBackground(new UpdateMessagesJob());
    }
}
