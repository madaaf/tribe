package com.tribe.app.presentation.utils.facebook;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.IntDef;
import android.util.Log;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
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
  public static final int FACEBOOK_GAME_REQUEST = 2;

  @IntDef({ FACEBOOK_LOGIN, FACEBOOK_GET_FRIENDS, FACEBOOK_GAME_REQUEST })
  public @interface FacebookAccessType {
  }

  private Context context;
  private PublishSubject<LoginResult> loginSubject;
  private PublishSubject<String> gameRequestSubject;
  private Observable<List<ContactFBRealm>> friendListObservable;
  private Observable<List<ContactFBRealm>> friendInvitableListObservable;
  private Observable<FacebookEntity> facebookEntityObservable;
  private LoginResult loginResult;
  private int countHandle = 0;

  @Inject public RxFacebook(Context context) {
    this.context = context;
  }

  public Observable<LoginResult> requestLogin() {
    loginSubject = PublishSubject.create();
    startLoginHiddenActivity(FACEBOOK_LOGIN);
    return loginSubject;
  }

  public Observable<String> requestGameInvite(String recipientId) {
    gameRequestSubject = PublishSubject.create();
    startGameInviteHiddenActivity(FACEBOOK_GAME_REQUEST, recipientId);
    return gameRequestSubject;
  }

  void onLogin(LoginResult loginResult) {
    this.loginResult = loginResult;

    if (loginSubject != null && loginSubject.hasObservers()) {
      loginSubject.onNext(loginResult);
      loginSubject.onCompleted();
    }

    countHandle = 0;
  }

  void onGameRequestSuccess(String id) {
    if (gameRequestSubject != null && gameRequestSubject.hasObservers()) {
      gameRequestSubject.onNext(id);
      gameRequestSubject.onCompleted();
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

  public Observable<List<ContactFBRealm>> requestInvitableFriends() {
    if (friendInvitableListObservable == null) {
      friendInvitableListObservable =
          Observable.create((Subscriber<? super List<ContactFBRealm>> subscriber) -> {
            emitFriendsInvitable(subscriber);
          }).onBackpressureBuffer().serialize();
    }

    return friendInvitableListObservable;
  }

  public void emitFriends(Subscriber subscriber) {
    if (FacebookUtils.isLoggedIn()) {
      new GraphRequest(AccessToken.getCurrentAccessToken(),
          "/" + AccessToken.getCurrentAccessToken().getUserId() + "/friends", null, HttpMethod.GET,
          response -> handleFriendList(response, subscriber, true)).executeAsync();
    } else {
      if (!subscriber.isUnsubscribed()) {
        subscriber.onNext(new ArrayList<>());
        subscriber.onCompleted();
      }
    }
  }

  public void emitFriendsInvitable(Subscriber subscriber) {
    if (FacebookUtils.isLoggedIn()) {
      new GraphRequest(AccessToken.getCurrentAccessToken(), "/" +
          AccessToken.getCurrentAccessToken().getUserId() +
          "/invitable_friends?fields=id,name&limit=20", null, HttpMethod.GET,
          response -> handleFriendList(response, subscriber, false)).executeAsync();
    } else {
      if (!subscriber.isUnsubscribed()) {
        subscriber.onNext(new ArrayList<>());
        subscriber.onCompleted();
      }
    }
  }

  private void handleFriendList(GraphResponse response, Subscriber subscriber, boolean hasApp) {
    List<ContactFBRealm> contactFBRealmList = new ArrayList<>();

    try {
      if (response.getJSONObject() != null) {
        JSONArray array = response.getJSONObject().getJSONArray("data");

        for (int i = 0; i < array.length(); i++) {
          JSONObject object = array.getJSONObject(i);
          ContactFBRealm contactFBRealm = new ContactFBRealm();
          contactFBRealm.setId(object.getString("id"));
          contactFBRealm.setName(object.getString("name"));
          contactFBRealm.setHasApp(hasApp);
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

    new GraphRequest(AccessToken.getCurrentAccessToken(), "/me?fields=id,name,email", null,
        HttpMethod.GET, response -> {
      JSONObject jsonResponse = response.getJSONObject();
      FacebookEntity facebookEntity = new FacebookEntity();

      if (jsonResponse != null) {
        try {
          facebookEntity.setName(jsonResponse.getString("name"));
          if (jsonResponse.has("email")) facebookEntity.setEmail(jsonResponse.getString("email"));
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

  private void startLoginHiddenActivity(int typeRequest) {
    Intent intent = new Intent(context, FacebookHiddenActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.putExtra(FacebookHiddenActivity.FACEBOOK_REQUEST, typeRequest);
    context.startActivity(intent);
  }

  private void startGameInviteHiddenActivity(int typeRequest, String recipientId) {
    Intent intent = new Intent(context, FacebookHiddenActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.putExtra(FacebookHiddenActivity.FACEBOOK_REQUEST, typeRequest);
    intent.putExtra(FacebookHiddenActivity.FACEBOOK_RECIPIENT_ID, recipientId);
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