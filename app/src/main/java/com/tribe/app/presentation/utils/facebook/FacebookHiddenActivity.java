package com.tribe.app.presentation.utils.facebook;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.model.GameRequestContent;
import com.facebook.share.widget.GameRequestDialog;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.view.activity.BaseActivity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;

public class FacebookHiddenActivity extends BaseActivity {

  public final static String FACEBOOK_REQUEST = "FACEBOOK_REQUEST";
  public final static String FACEBOOK_RECIPIENT_ID = "FACEBOOK_RECIPIENT_ID";

  @Inject RxFacebook rxFacebook;

  private CallbackManager callbackManager;
  private LoginManager loginManager;
  private GameRequestDialog gameRequestDialog;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    initDependencyInjector();
    init();

    if (savedInstanceState == null || rxFacebook.getCountHandle() <= 1) {
      rxFacebook.incrementCountHandle();
      handleIntent(getIntent());
    } else {
      finish();
    }
  }

  @Override protected void onNewIntent(Intent intent) {
    handleIntent(intent);
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    callbackManager.onActivityResult(requestCode, resultCode, data);
  }

  private void handleIntent(Intent intent) {
    int sourceType = intent.getIntExtra(FACEBOOK_REQUEST, 0);

    if (sourceType == RxFacebook.FACEBOOK_LOGIN) {
      this.loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
        @Override public void onSuccess(LoginResult loginResult) {
          rxFacebook.onLogin(loginResult);
          finish();
        }

        @Override public void onCancel() {
          rxFacebook.onLogin(null);
          finish();
        }

        @Override public void onError(FacebookException exception) {
          Toast.makeText(FacebookHiddenActivity.this, exception.getMessage(), Toast.LENGTH_LONG)
              .show();
          rxFacebook.onLogin(null);
          finish();
        }
      });
      LoginManager.getInstance()
          .logInWithReadPermissions(this, Arrays.asList("public_profile", "user_friends", "email"));
    } else if (sourceType == RxFacebook.FACEBOOK_GAME_REQUEST) {
      gameRequestDialog = new GameRequestDialog(this);
      gameRequestDialog.registerCallback(callbackManager,
          new FacebookCallback<GameRequestDialog.Result>() {
            public void onSuccess(GameRequestDialog.Result result) {
              String id = result.getRequestId();
              rxFacebook.onGameRequestSuccess(id);
              finish();
            }

            public void onCancel() {
              rxFacebook.onGameRequestSuccess(null);
              finish();
            }

            public void onError(FacebookException error) {
              finish();
            }
          });

      List<String> recipients = new ArrayList<>();
      recipients.addAll(getIntent().getExtras().getStringArrayList(FACEBOOK_RECIPIENT_ID));

      GameRequestContent content =
          new GameRequestContent.Builder().setMessage("...").setRecipients(recipients).build();
      gameRequestDialog.show(content);
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
        .build()
        .inject(this);
  }
}