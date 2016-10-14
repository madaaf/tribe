package com.tribe.app.presentation.utils.facebook;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.view.activity.BaseActivity;

import java.util.Arrays;

import javax.inject.Inject;

public class FacebookHiddenActivity extends BaseActivity {

    public final static String FACEBOOK_REQUEST = "FACEBOOK_REQUEST";
    private final static String DESTROYED = "DESTROYED";

    @Inject
    RxFacebook rxFacebook;

    private CallbackManager callbackManager;
    private LoginManager loginManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initDependencyInjector();
        init();

        if (savedInstanceState == null || savedInstanceState.getBoolean(DESTROYED)) {
            handleIntent(getIntent());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(DESTROYED, true);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleIntent(Intent intent) {
        int sourceType = intent.getIntExtra(FACEBOOK_REQUEST, 0);

        if (sourceType == RxFacebook.FACEBOOK_LOGIN) {
            this.loginManager.registerCallback(callbackManager,
                    new FacebookCallback<LoginResult>() {
                        @Override
                        public void onSuccess(LoginResult loginResult) {
                            rxFacebook.onLogin(loginResult);
                            finish();
                        }

                        @Override
                        public void onCancel() {
                            finish();
                        }

                        @Override
                        public void onError(FacebookException exception) {
                            Toast.makeText(FacebookHiddenActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "user_friends"));
        }
    }

    private void init() {
        callbackManager = CallbackManager.Factory.create();
        loginManager = LoginManager.getInstance();
    }

    private void initDependencyInjector() {
        DaggerUserComponent.builder()
                .activityModule(getActivityModule())
                .applicationComponent(getApplicationComponent())
                .build().inject(this);
    }
}