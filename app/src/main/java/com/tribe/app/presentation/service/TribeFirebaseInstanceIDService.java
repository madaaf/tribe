package com.tribe.app.presentation.service;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.SendToken;
import com.tribe.app.presentation.AndroidApplication;

import javax.inject.Inject;

/**
 * Created by tiago on 12/07/2016.
 */
public class TribeFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "TribeFirebaseIDService";

    @Inject
    SendToken sendTokenUseCase;

    @Override
    public void onCreate() {
        super.onCreate();

        ((AndroidApplication) getApplication()).getApplicationComponent().inject(this);
    }

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        if (refreshedToken != null) {
            sendTokenUseCase.setToken(refreshedToken);
            sendTokenUseCase.execute(new SendTokenSubscriber());
        }
    }

    private final class SendTokenSubscriber extends DefaultSubscriber<Installation> {

        @Override
        public void onCompleted() {}

        @Override
        public void onError(Throwable e) {}

        @Override
        public void onNext(Installation installation) {
            // TODO WHATEVER NEEDS TO BE DONE
        }
    }
}
