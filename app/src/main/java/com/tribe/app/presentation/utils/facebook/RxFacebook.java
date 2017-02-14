package com.tribe.app.presentation.utils.facebook;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.IntDef;
import android.util.Log;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.tribe.app.data.realm.ContactFBRealm;
import com.tribe.app.domain.entity.FacebookEntity;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.Subscriber;
import rx.subjects.PublishSubject;

@Singleton public class RxFacebook {

  public static final String TAG = "RxFacebook";

  public static final int FACEBOOK_LOGIN = 0;
  public static final int FACEBOOK_GET_FRIENDS = 1;

  @IntDef({ FACEBOOK_LOGIN, FACEBOOK_GET_FRIENDS }) public @interface FacebookAccessType {
  }

  private Context context;
  private PublishSubject<LoginResult> loginSubject;
  private Observable<List<ContactFBRealm>> friendListObservable;
  private Observable<FacebookEntity> facebookEntityObservable;
  private LoginResult loginResult;
  private int countHandle = 0;

  @Inject public RxFacebook(Context context) {
    this.context = context;
  }

  public Observable<LoginResult> requestLogin() {
    loginSubject = PublishSubject.create();
    startLoginHiddenActivity();
    return loginSubject;
  }

  void onLogin(LoginResult loginResult) {
    this.loginResult = loginResult;

    if (loginSubject != null && loginSubject.hasObservers()) {
      loginSubject.onNext(loginResult);
      loginSubject.onCompleted();
    }

    countHandle = 0;
  }

  public Observable<List<ContactFBRealm>> requestFriends() {
    if (friendListObservable == null) {
      friendListObservable =
          Observable.create((Subscriber<? super List<ContactFBRealm>> subscriber) -> {
            emitFriends(subscriber);
          }).onBackpressureBuffer().serialize();
    }

    return friendListObservable;
  }

  public void emitFriends(Subscriber subscriber) {
    if (FacebookUtils.isLoggedIn()) {
      new GraphRequest(AccessToken.getCurrentAccessToken(),
          "/" + AccessToken.getCurrentAccessToken().getUserId() + "/friends", null, HttpMethod.GET,
          response -> {
            List<ContactFBRealm> contactFBRealmList = new ArrayList<>();

            try {
              if (response.getJSONObject() != null) {
                JSONArray array = response.getJSONObject().getJSONArray("data");

                for (int i = 0; i < array.length(); i++) {
                  JSONObject object = array.getJSONObject(i);
                  ContactFBRealm contactFBRealm = new ContactFBRealm();
                  contactFBRealm.setId(object.getString("id"));
                  contactFBRealm.setName(object.getString("name"));
                  contactFBRealmList.add(contactFBRealm);
                }
              }
            } catch (JSONException e) {
              e.printStackTrace();
            } finally {
              if (!subscriber.isUnsubscribed()) {
                subscriber.onNext(contactFBRealmList);
                subscriber.onCompleted();
              }
            }
          }).executeAsync();
    } else {
      if (!subscriber.isUnsubscribed()) {
        subscriber.onNext(new ArrayList<>());
        subscriber.onCompleted();
      }
    }
  }

  public Observable<FacebookEntity> requestInfos() {
    if (facebookEntityObservable == null) {
      facebookEntityObservable =
          Observable.create((Subscriber<? super FacebookEntity> subscriber) -> {
            emitMe(subscriber);
          }).onBackpressureBuffer().serialize();
    }

    return facebookEntityObservable;
  }

  public void emitMe(Subscriber subscriber) {
    new GraphRequest(AccessToken.getCurrentAccessToken(), "/me", null, HttpMethod.GET, response -> {
      JSONObject jsonResponse = response.getJSONObject();
      FacebookEntity facebookEntity = new FacebookEntity();

      if (jsonResponse != null) {
        try {
          facebookEntity.setName(jsonResponse.getString("name"));
          facebookEntity.setUsername(facebookEntity.getName().replaceAll("\\s", "").toLowerCase());
          facebookEntity.setId(jsonResponse.getString("id"));
          facebookEntity.setProfilePicture(
              "https://graph.facebook.com/" + facebookEntity.getId() + "/picture?type=large");
        } catch (JSONException e) {
          Log.e("JSON exception:", e.toString());
        } finally {
          if (!subscriber.isUnsubscribed()) {
            subscriber.onNext(facebookEntity);
            subscriber.onCompleted();
          }
        }
      } else if (!subscriber.isUnsubscribed()) {
        subscriber.onCompleted();
      }
    }).executeAsync();
  }

  private void startLoginHiddenActivity() {
    Intent intent = new Intent(context, FacebookHiddenActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.putExtra(FacebookHiddenActivity.FACEBOOK_REQUEST, FACEBOOK_LOGIN);
    context.startActivity(intent);
  }

  public LoginResult getLoginResult() {
    LoginResult loginResultTemp = loginResult;
    loginResult = null;
    return loginResultTemp;
  }

  public void incrementCountHandle() {
    countHandle++;
  }

  public int getCountHandle() {
    return countHandle;
  }
}