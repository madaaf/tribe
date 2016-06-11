package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.domain.entity.Pin;
import com.tribe.app.domain.exception.DefaultErrorBundle;
import com.tribe.app.domain.exception.ErrorBundle;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.DoLoginWithPhoneNumber;
import com.tribe.app.domain.interactor.user.GetRequestCode;
import com.tribe.app.presentation.exception.ErrorMessageFactory;
import com.tribe.app.presentation.mvp.view.IntroView;
import com.tribe.app.presentation.mvp.view.View;

import javax.inject.Inject;

public class IntroPresenter implements Presenter {

    private final GetRequestCode cloudGetRequestCodeUseCase;
    private final DoLoginWithPhoneNumber cloudLoginUseCase;

    private IntroView introView;

    @Inject
    public IntroPresenter(GetRequestCode cloudGetRequestCodeUseCase, DoLoginWithPhoneNumber cloudLoginUseCase) {
        this.cloudLoginUseCase = cloudLoginUseCase;
        this.cloudGetRequestCodeUseCase = cloudGetRequestCodeUseCase;
    }

    @Override
    public void onCreate() {
        // Unused
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
        // Unused
    }

    @Override
    public void onDestroy() {
        cloudGetRequestCodeUseCase.unsubscribe();
        cloudLoginUseCase.unsubscribe();
    }

    @Override
    public void attachView(View v) {
        introView = (IntroView) v;
    }

    public void requestCode(String phoneNumber) {
        showViewLoading();
        cloudGetRequestCodeUseCase.prepare(phoneNumber);
        cloudGetRequestCodeUseCase.execute(new RequestCodeSubscriber());
    }

    public void login(String phoneNumber, String code, String pinId) {
        showViewLoading();
        cloudLoginUseCase.prepare(phoneNumber, code, pinId);
        cloudLoginUseCase.execute(new LoginSubscriber());
    }

    public void goToHome() {
        this.introView.goToHome();
    }

    public void goToCode(Pin pin) {
        this.introView.goToCode(pin);
    }

    private void showViewLoading() {
        this.introView.showLoading();
    }

    private void hideViewLoading() {
        this.introView.hideLoading();
    }

    private void showErrorMessage(ErrorBundle errorBundle) {
        String errorMessage = ErrorMessageFactory.create(this.introView.context(),
                errorBundle.getException());
        this.introView.showError(errorMessage);
    }

    private final class RequestCodeSubscriber extends DefaultSubscriber<Pin> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            hideViewLoading();
            showErrorMessage(new DefaultErrorBundle((Exception) e));
        }

        @Override
        public void onNext(Pin pin) {
            goToCode(pin);
        }
    }

    private final class LoginSubscriber extends DefaultSubscriber<AccessToken> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            hideViewLoading();
            showErrorMessage(new DefaultErrorBundle((Exception) e));
        }

        @Override
        public void onNext(AccessToken accessToken) {
            goToHome();
        }
    }
}
