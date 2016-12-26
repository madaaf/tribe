package com.tribe.app.presentation.mvp.presenter;

import com.google.gson.Gson;
import com.tribe.app.R;
import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.domain.entity.ErrorLogin;
import com.tribe.app.domain.entity.Pin;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.exception.DefaultErrorBundle;
import com.tribe.app.domain.exception.ErrorBundle;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.DoLoginWithPhoneNumber;
import com.tribe.app.domain.interactor.user.GetCloudUserInfos;
import com.tribe.app.domain.interactor.user.GetRequestCode;
import com.tribe.app.presentation.exception.ErrorMessageFactory;
import com.tribe.app.presentation.mvp.view.AuthMVPView;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.StringUtils;

import java.io.IOException;

import javax.inject.Inject;

import retrofit2.adapter.rxjava.HttpException;


public class AuthPresenter implements Presenter {

    private final GetRequestCode cloudGetRequestCodeUseCase;
    private final DoLoginWithPhoneNumber cloudLoginUseCase;
    private final GetCloudUserInfos cloudUserInfos;

    private AuthMVPView introView;

    @Inject
    public AuthPresenter(GetRequestCode cloudGetRequestCodeUseCase,
                         DoLoginWithPhoneNumber cloudLoginUseCase,
                         GetCloudUserInfos cloudUserInfos) {
        this.cloudLoginUseCase = cloudLoginUseCase;
        this.cloudGetRequestCodeUseCase = cloudGetRequestCodeUseCase;
        this.cloudUserInfos = cloudUserInfos;
    }

    @Override
    public void onViewDetached() {
        cloudGetRequestCodeUseCase.unsubscribe();
        cloudLoginUseCase.unsubscribe();
    }

    @Override
    public void onViewAttached(MVPView v) {
        introView = (AuthMVPView) v;
    }

    public void requestCode(String phoneNumber) {
        showViewLoading();
        cloudGetRequestCodeUseCase.prepare(phoneNumber);
        cloudGetRequestCodeUseCase.execute(new RequestCodeSubscriber());
    }

    public LoginEntity login(String phoneNumber, String code, String pinId) {
        LoginEntity loginEntity = new LoginEntity(phoneNumber, code, pinId);

        showViewLoading();

        cloudLoginUseCase.prepare(loginEntity);
        cloudLoginUseCase.execute(new LoginSubscriber());

        return loginEntity;
    }

    public void getUserInfo() {
        cloudUserInfos.execute(new UserInfoSubscriber());
    }

    public void goToHome() {
        this.introView.goToHome();
    }

    public void loginError(ErrorLogin errorLogin) {
        this.introView.loginError(errorLogin);
    }

    public void goToCode(Pin pin) {
        this.introView.goToCode(pin);
    }

    public void goToConnected(User user) {
        this.introView.goToConnected(user);
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
            hideViewLoading();
        }

        @Override
        public void onError(Throwable e) {
            hideViewLoading();
            showErrorMessage(new DefaultErrorBundle((Exception) e));
        }

        @Override
        public void onNext(Pin pin) {
            if (pin != null && !StringUtils.isEmpty(pin.getPinId())) {
                goToCode(pin);
            } else {
                hideViewLoading();
                if (introView.context() != null) introView.showError(introView.context().getString(R.string.error_technical));
            }
        }
    }

    private final class LoginSubscriber extends DefaultSubscriber<AccessToken> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            if (e instanceof HttpException) {
                HttpException httpException = (HttpException) e;
                if (httpException.response() != null && httpException.response().errorBody() != null) {
                    try {
                        ErrorLogin errorLogin = new Gson().fromJson(httpException.response().errorBody().string(), ErrorLogin.class);
                        if (errorLogin != null && errorLogin.isVerified()) {
                            loginError(errorLogin);
                            goToConnected(null);
                        } else if (errorLogin != null && !errorLogin.isVerified()) {
                            introView.pinError(errorLogin);
                            introView.showError(EmojiParser.demojizedText(introView.context().getString(R.string.onboarding_error_wrong_pin)));
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }

            hideViewLoading();
        }

        @Override
        public void onNext(AccessToken accessToken) {
            getUserInfo();
        }
    }

    private final class UserInfoSubscriber extends DefaultSubscriber<User> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(User user) {
            hideViewLoading();
            goToConnected(user);
        }
    }
}