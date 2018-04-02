package com.tribe.app.presentation.utils.facebook;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.IntDef;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.tribe.app.R;
import com.tribe.app.data.realm.ContactFBRealm;
import com.tribe.app.domain.entity.FacebookEntity;
import com.tribe.app.presentation.utils.EmojiParser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import timber.log.Timber;

@Singleton public class RxFacebook {

  public static final String TAG = "RxFacebook";

  public static final int MAX_FRIEND_INVITE = 200;
  public static final int MAX_SIZE_PAGINATION = 25;
  public static final int FACEBOOK_LOGIN = 0;
  public static final int FACEBOOK_GET_FRIENDS = 1;
  public static final int FACEBOOK_GAME_REQUEST = 2;

  @IntDef({ FACEBOOK_LOGIN, FACEBOOK_GET_FRIENDS, FACEBOOK_GAME_REQUEST })
  public @interface FacebookAccessType {
  }

  private Context context;
  private PublishSubject<LoginResult> loginSubject;
  private PublishSubject<String> gameRequestSubject;
  private PublishSubject<Boolean> notifyFriendsSubject;
  private Observable<List<ContactFBRealm>> friendListObservable;
  private Observable<List<ContactFBRealm>> friendInvitableListObservable;
  private Observable<List<String>> fbIdsListObservable;
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

  public Observable<String> requestGameInvite(ArrayList<String> recipientIdList) {
    gameRequestSubject = PublishSubject.create();
    startGameInviteHiddenActivity(FACEBOOK_GAME_REQUEST, recipientIdList);
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

  public Observable<List<ContactFBRealm>> requestInvitableFriends(int nbr) {
    if (friendInvitableListObservable == null) {
      friendInvitableListObservable =
          Observable.create((Subscriber<? super List<ContactFBRealm>> subscriber) -> {
            emitFriendsInvitable(subscriber, nbr);
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

  public void getContactsFbIdList(Subscriber subscriber, Context c, List<String> toIds) {
    if (!FacebookUtils.isLoggedIn()) {
      subscriber.onNext(null);
      subscriber.onCompleted();
      return;
    }
    AccessToken a = FacebookUtils.accessToken();
    String separator = "%2C";
    String to = "";
    for (int i = 0; i < toIds.size(); i++) {
      String id = toIds.get(i);
      if (i != toIds.size() - 1) {
        to += id + separator;
      } else {
        to += id;
      }
    }
    String url = "https://m.facebook.com/v2.9/dialog/apprequests?access_token="
        + a.getToken()
        + "&app_id="
        + a.getApplicationId()
        + "&to="
        + to
        + "&sdk=android-4.23.0&redirect_uri=fbconnect%3A%2F%2Fsuccess&message=Welcome&display=touch";

    WebView webView = new WebView(c);
    webView.setWebViewClient(new WebViewClient() {
      @Override public void onReceivedError(WebView view, WebResourceRequest request,
          WebResourceError error) {
        super.onReceivedError(view, request, error);
      }

      @RequiresApi(api = Build.VERSION_CODES.KITKAT) @Override
      public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        String cookies = CookieManager.getInstance().getCookie(url);
        Timber.d("Facebook cookies :" + cookies);

        view.evaluateJavascript(
            "(function() { return ('<html>'+document.getElementsByClassName('_5q1p')[0].innerHTML+'</html>'); })();",
            html -> {
              Log.d("HTML", html);
              int index = html.indexOf("name=\\\"to\\\"");
              if (index != -1) {
                String initialContent = html.substring(index);
                String start = "value=\\\"";
                String end = "\\\">";
                String finalContent =
                    initialContent.substring(initialContent.indexOf(start) + start.length(),
                        initialContent.indexOf(end));

                List<String> fbIdList = new ArrayList<>(Arrays.asList(finalContent.split(",")));

                subscriber.onNext(fbIdList);
                subscriber.onCompleted();
              } else {
                subscriber.onNext(new ArrayList<>());
                subscriber.onCompleted();
              }
            });
      }
    });
    webView.getSettings().setJavaScriptEnabled(true);
    webView.loadUrl(url);
    webView.setVisibility(View.VISIBLE);
  }

  public Observable<List<String>> contactsFbId(Context c, List<String> toIds) {
    fbIdsListObservable = Observable.create((Subscriber<? super List<String>> subscriber) -> {
      getContactsFbIdList(subscriber, c, toIds);
    })
        .subscribeOn(AndroidSchedulers.mainThread())
        .doOnError(throwable -> Timber.e("error getContactsFbIdList " + throwable.getMessage()));
    return fbIdsListObservable;
  }

  public Observable<Boolean> notifyFriends(Context context, ArrayList<String> toIds) {
    notifyFriendsSubject = PublishSubject.create();
    AccessToken a = FacebookUtils.accessToken();
    String separator = "%2C";
    String to = "";
    for (int i = 0; i < toIds.size(); i++) {
      String id = toIds.get(i);
      if (i != toIds.size() - 1) {
        to += id + separator;
      } else {
        to += id;
      }
    }
    String url = "https://m.facebook.com/v2.9/dialog/apprequests?access_token="
        + a.getToken()
        + "&app_id="
        + a.getApplicationId()
        + "&to="
        + to
        + "&sdk=android-4.23.0&redirect_uri=fbconnect%3A%2F%2Fsuccess&message=Welcome&display=touch";

    WebView webView = new WebView(context);
    String finalTo = to;
    webView.setWebViewClient(new WebViewClient() {
      @Override public void onReceivedError(WebView view, WebResourceRequest request,
          WebResourceError error) {
        super.onReceivedError(view, request, error);
        notifyFriendsSubject.onNext(false);
        notifyFriendsSubject.onCompleted();
      }

      @Override public boolean shouldOverrideUrlLoading(WebView webView, String url) {
        return true;
      }

      @RequiresApi(api = Build.VERSION_CODES.KITKAT) @Override
      public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        String cookies = CookieManager.getInstance().getCookie(url);
        Timber.d("Facebook cookies :" + cookies);

        webView.loadUrl("javascript:(function(){"
            + "l=document.getElementById('u_0_1');"
            + "e=document.createEvent('HTMLEvents');"
            + "e.initEvent('click',true,true);"
            + "l.dispatchEvent(e);"
            + "})()");

        Toast.makeText(context, EmojiParser.demojizedText(
            context.getResources().getString(R.string.facebook_invite_confirmation)),
            Toast.LENGTH_LONG).show();

        notifyFriendsSubject.onNext(true);
        notifyFriendsSubject.onCompleted();
      }
    });
    webView.getSettings().setJavaScriptEnabled(true);
    webView.loadUrl(url);
    webView.setVisibility(View.VISIBLE);
    return notifyFriendsSubject;
  }

  public void emitFriendsInvitable(Subscriber subscriber, int nbr) {
    if (FacebookUtils.isLoggedIn()) {
      new GraphRequest(AccessToken.getCurrentAccessToken(), "/"
          + AccessToken.getCurrentAccessToken().getUserId()
          + "/invitable_friends?fields=id,name&limit="
          + nbr, null, HttpMethod.GET,
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

    new GraphRequest(AccessToken.getCurrentAccessToken(), "/me?fields=id,name,email,age_range",
        null, HttpMethod.GET, response -> {
      JSONObject jsonResponse = response.getJSONObject();
      FacebookEntity facebookEntity = new FacebookEntity();

      if (jsonResponse != null) {
        Log.d("RxFacebook", jsonResponse.toString());
        try {
          facebookEntity.setName(jsonResponse.getString("name"));
          if (jsonResponse.has("email")) facebookEntity.setEmail(jsonResponse.getString("email"));
          facebookEntity.setId(jsonResponse.getString("id"));
          if (jsonResponse.has("age_range")) {
            JSONObject ageRange = jsonResponse.getJSONObject("age_range");
            if (ageRange.getInt("min") > 0) facebookEntity.setAgeRangeMin(ageRange.getInt("min"));
            if (ageRange.getInt("max") > 0) facebookEntity.setAgeRangeMax(ageRange.getInt("max"));
          }
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

  private void startGameInviteHiddenActivity(int typeRequest, ArrayList<String> recipientIdList) {
    Intent intent = new Intent(context, FacebookHiddenActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.putExtra(FacebookHiddenActivity.FACEBOOK_REQUEST, typeRequest);
    intent.putStringArrayListExtra(FacebookHiddenActivity.FACEBOOK_RECIPIENT_ID, recipientIdList);
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