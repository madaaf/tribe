package com.tribe.app.presentation.mvp.presenter;

import android.os.Handler;

import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.domain.entity.Pin;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.exception.DefaultErrorBundle;
import com.tribe.app.domain.exception.ErrorBundle;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.user.DoLoginWithPhoneNumber;
import com.tribe.app.domain.interactor.user.GetCloudUserInfos;
import com.tribe.app.domain.interactor.user.GetRequestCode;
import com.tribe.app.presentation.exception.ErrorMessageFactory;
import com.tribe.app.presentation.mvp.view.IntroView;
import com.tribe.app.presentation.mvp.view.View;

import javax.inject.Inject;
import javax.inject.Named;

public class IntroPresenter implements Presenter {

    private final GetRequestCode cloudGetRequestCodeUseCase;
    private final DoLoginWithPhoneNumber cloudLoginUseCase;
    private final GetCloudUserInfos cloudUserInfos;

    private IntroView introView;

    @Inject
    public IntroPresenter(GetRequestCode cloudGetRequestCodeUseCase,
                          DoLoginWithPhoneNumber cloudLoginUseCase,
                          GetCloudUserInfos cloudUserInfos) {
        this.cloudLoginUseCase = cloudLoginUseCase;
        this.cloudGetRequestCodeUseCase = cloudGetRequestCodeUseCase;
        this.cloudUserInfos = cloudUserInfos;
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
        // TODO: get pin
//                cloudGetRequestCodeUseCase.prepare(phoneNumber);
//        cloudGetRequestCodeUseCase.execute(new RequestCodeSubscriber());

        showViewLoading();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideViewLoading();
                goToCode();
            }
        }, 2000);


    }

    public void backToPhoneNumber() {

    }

    public void login(String phoneNumber, String code, String pinId) {
        showViewLoading();
        // TODO: get user id
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideViewLoading();
                goToConnected();
            }
        }, 2000);

//        cloudLoginUseCase.prepare(phoneNumber, code, pinId);
//        cloudLoginUseCase.execute(new LoginSubscriber());

    }

    public void getUserInfo() {
        cloudUserInfos.execute(new UserInfoSubscriber());
    }

    public void goToHome() {
        this.introView.goToHome();
    }

    public void goToProfileInfo() {
        this.introView.goToProfileInfo();
    }

    public void goToCode() {
        this.introView.goToCode();
    }

    public void goToConnected() {
        this.introView.goToConnected();
    }

    public void goToAccess() {
        this.introView.goToAccess();
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
            goToCode();
        }
    }

    private final class LoginSubscriber extends DefaultSubscriber<AccessToken> {

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
            showErrorMessage(new DefaultErrorBundle((Exception) e));
        }

        @Override
        public void onNext(User user) {
            if (true) {
                goToProfileInfo();
            } else {
                goToHome();
            }
        }
    }

}
