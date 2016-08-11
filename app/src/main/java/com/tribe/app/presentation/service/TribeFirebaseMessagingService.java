package com.tribe.app.presentation.service;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by tiago on 12/07/2016.
 */
public class TribeFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "TribeFirebaseMessServ";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification ChatMessage Body: " + remoteMessage.getNotification().getBody());
        //((AndroidApplication) getApplication()).getApplicationComponent().jobManager().addJobInBackground(new UpdateTribesJob());
    }
}
