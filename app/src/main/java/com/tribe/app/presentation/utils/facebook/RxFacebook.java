package com.tribe.app.presentation.utils.facebook;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.IntDef;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.tribe.app.data.realm.ContactFBRealm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.subjects.PublishSubject;

@Singleton
public class RxFacebook {

    public static final String TAG = "RxFacebook";

    public static final int FACEBOOK_LOGIN = 0;
    public static final int FACEBOOK_GET_FRIENDS = 1;

    @IntDef({FACEBOOK_LOGIN, FACEBOOK_GET_FRIENDS})
    public @interface FacebookAccessType{}

    private Context context;
    private PublishSubject<LoginResult> loginSubject;
    private PublishSubject<List<ContactFBRealm>> friendListSubject;

    @Inject
    public RxFacebook(Context context) {
        this.context = context;
    }

    public Observable<LoginResult> requestLogin() {
        loginSubject = PublishSubject.create();
        startLoginHiddenActivity();
        return loginSubject;
    }

    public Observable<List<ContactFBRealm>> requestFriends() {
        friendListSubject = PublishSubject.create();

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/" + AccessToken.getCurrentAccessToken().getUserId() + "/friends",
                null,
                HttpMethod.GET,
                response -> {
                    List<ContactFBRealm> contactFBRealmList = new ArrayList<>();

                    try {
                        JSONArray array = response.getJSONObject().getJSONArray("data");

                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            ContactFBRealm contactFBRealm = new ContactFBRealm();
                            contactFBRealm.setId(object.getString("id"));
                            contactFBRealm.setName(object.getString("name"));
                            contactFBRealmList.add(contactFBRealm);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        friendListSubject.onNext(contactFBRealmList);
                        friendListSubject.onCompleted();
                    }
                }
        ).executeAsync();

        return friendListSubject;
    }

    void onLogin(LoginResult loginResult) {
        if (loginSubject != null && loginSubject.hasObservers()) {
            loginSubject.onNext(loginResult);
            loginSubject.onCompleted();
        }
    }

    private void startLoginHiddenActivity() {
        Intent intent = new Intent(context, FacebookHiddenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(FacebookHiddenActivity.FACEBOOK_REQUEST, FACEBOOK_LOGIN);
        context.startActivity(intent);
    }
}