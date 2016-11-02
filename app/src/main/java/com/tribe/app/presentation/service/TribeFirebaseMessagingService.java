package com.tribe.app.presentation.service;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.tribe.app.data.network.job.UpdateMessagesJob;
import com.tribe.app.presentation.AndroidApplication;

/**
 * Created by tiago on 12/07/2016.
 */
public class TribeFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "TribeFirebaseMessServ";

    @Override
    public void onCreate() {
        super.onCreate();
        ((AndroidApplication) getApplication()).getApplicationComponent().inject(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        System.out.println("HEY BACKGROUND");
        ((AndroidApplication) getApplication()).getApplicationComponent().jobManager().addJobInBackground(new UpdateMessagesJob());
    }
}
