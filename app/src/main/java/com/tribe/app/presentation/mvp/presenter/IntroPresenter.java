package com.tribe.app.presentation.mvp.presenter;

import android.os.Handler;
import android.widget.Toast;

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
import com.tribe.app.presentation.view.activity.IntroActivity;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class IntroPresenter implements Presenter {

    private final GetRequestCode cloudGetRequestCodeUseCase;
    private final DoLoginWithPhoneNumber cloudLoginUseCase;
    private final GetCloudUserInfos cloudUserInfos;

    // TODO: remove after threading is removed
    private boolean isActive1 = false;
    private boolean isActive2 = false;

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

        if (IntroActivity.uiOnlyMode) {
            goToCode(new Pin());
        } else {
            showViewLoading();
            cloudGetRequestCodeUseCase.prepare(phoneNumber);
            cloudGetRequestCodeUseCase.execute(new RequestCodeSubscriber());
        }


    }

    public void backToPhoneNumber() {

    }

    public void login(String phoneNumber, String code, String pinId) {
        showViewLoading();
        // TODO: get user id
        if (IntroActivity.uiOnlyMode) {
            isActive2 = true;
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isActive2) {
                        hideViewLoading();
                        goToConnected();
                        isActive2 = false;
                    }
                }
            }, 2000);
        } else {
            cloudLoginUseCase.prepare(phoneNumber, code, pinId);
            cloudLoginUseCase.execute(new LoginSubscriber());
        }

    }

    public void getUserInfo() {
        cloudUserInfos.execute(new UserInfoSubscriber());
    }

    public void goToHome() {
        this.introView.goToHome();
    }

    public void goToProfileInfo() { this.introView.goToProfileInfo();
    }

    public void goToCode(Pin pin) {
        this.introView.goToCode(pin);
    }

    public void goToConnected() {
        this.introView.goToConnected();
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
            goToCode(pin);
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
            // TODO: get error message from laurent
            introView.showError("You have entered the wrong pin. Please try again");
        }

        @Override
        public void onNext(AccessToken accessToken) {

            hideViewLoading();
            goToConnected();
            getUserInfo();
            Observable.timer(300, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(time -> {
                        goToHome();
                    });
        }
    }

    private final class UserInfoSubscriber extends DefaultSubscriber<User> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(User user) {
            // TODO: check if users first time logging in
            if (IntroActivity.uiOnlyMode) {
                goToProfileInfo();
            } else {
                goToHome();
            }
        }
    }

}
