package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.exception.DefaultErrorBundle;
import com.tribe.app.domain.exception.ErrorBundle;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.DoLoginWithUsername;
import com.tribe.app.presentation.exception.ErrorMessageFactory;
import com.tribe.app.presentation.mvp.view.IntroView;
import com.tribe.app.presentation.mvp.view.View;

import javax.inject.Inject;

public class IntroPresenter implements Presenter {

    private final DoLoginWithUsername cloudUserUsecase;

    private IntroView introView;

    @Inject
    public IntroPresenter(DoLoginWithUsername cloudUserUseCase) {
        this.cloudUserUsecase = cloudUserUseCase;
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
        cloudUserUsecase.unsubscribe();
    }

    @Override
    public void attachView(View v) {
        introView = (IntroView) v;
    }

    public void login(String username, String password) {
        showViewLoading();
        cloudUserUsecase.prepare(username, password);
        cloudUserUsecase.execute(new LoginSubscriber());
    }

    public void goToHome() {
        this.introView.goToHome();
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
