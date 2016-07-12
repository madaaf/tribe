package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.data.realm.Installation;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.SendToken;
import com.tribe.app.presentation.mvp.view.HomeView;
import com.tribe.app.presentation.mvp.view.View;

import javax.inject.Inject;
import javax.inject.Named;

public class HomePresenter implements Presenter {

    private SendToken sendTokenUseCase;

    private HomeView homeView;

    @Inject
    public HomePresenter(@Named("sendToken") SendToken sendToken) {
        this.sendTokenUseCase = sendToken;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onStart() {
        // Unused
    }

    @Override
    public void onResume() {
        // Unused
    }

    @Override
    public void onStop() {
        // Unused
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onDestroy() {
        sendTokenUseCase.unsubscribe();
    }

    public void sendToken(String token) {
        sendTokenUseCase.setToken(token);
        sendTokenUseCase.execute(new SendTokenSubscriber());
    }

    @Override
    public void attachView(View v) {
        homeView = (HomeView) v;
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
