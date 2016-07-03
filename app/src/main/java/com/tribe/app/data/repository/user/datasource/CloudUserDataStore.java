package com.tribe.app.data.repository.user.datasource;

import android.content.Context;

import com.tribe.app.R;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.LoginApi;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.PinRealm;
import com.tribe.app.data.realm.UserRealm;

import rx.Observable;
import rx.functions.Action1;

/**
 * {@link UserDataStore} implementation based on connections to the api (Cloud).
 */
public class CloudUserDataStore implements UserDataStore {

    private final TribeApi tribeApi;
    private final LoginApi loginApi;
    private final UserCache userCache;
    private final Context context;
    private final AccessToken accessToken;

    /**
     * Construct a {@link UserDataStore} based on connections to the api (Cloud).
     * @param userCache A {@link UserCache} to cache data retrieved from the api.
     * @param tribeApi an implementation of the api
     * @param loginApi an implementation of the login api
     * @param context the context
     * @param accessToken the access token
     */
    public CloudUserDataStore(UserCache userCache, TribeApi tribeApi, LoginApi loginApi, AccessToken accessToken, Context context) {
        this.userCache = userCache;
        this.tribeApi = tribeApi;
        this.loginApi = loginApi;
        this.context = context;
        this.accessToken = accessToken;
    }

    @Override
    public Observable<PinRealm> requestCode(String phoneNumber) {
        return this.loginApi
                .requestCode(new LoginEntity(phoneNumber));
    }

    @Override
    public Observable<AccessToken> loginWithPhoneNumber(String phoneNumber, String code, String scope) {
        return this.loginApi
                .loginWithUsername(new LoginEntity(phoneNumber, code, scope, "password"))
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        AccessToken accessToken = new AccessToken();
                        accessToken.setAccessToken("TO94aH0PV6LETnP8uPwQpKwh1JuMaj7pxD8ghrpmgEfJlQjHRn");
                        accessToken.setTokenType("Bearer");
                        accessToken.setUserId("BJgkS2rN");
                        CloudUserDataStore.this.userCache.put(accessToken);
                    }
                })
                .doOnNext(saveToCacheAccessToken);
    }

    @Override
    public Observable<AccessToken> loginWithUsername(String username, String password) {
        return this.loginApi
                .loginWithUsername(new LoginEntity(username, password, "", "password"))
                .doOnNext(saveToCacheAccessToken);
    }

    @Override
    public Observable<UserRealm> userInfos(String userId) {
        return this.tribeApi.getUserInfos(context.getString(R.string.user_infos).replace("%1$s", accessToken.getUserId()))
                .doOnNext(saveToCacheUser);
    }

    private final Action1<AccessToken> saveToCacheAccessToken = accessToken -> {
        if (accessToken != null && accessToken.getAccessToken() != null) {
            CloudUserDataStore.this.userCache.put(accessToken);
        }
    };

    private final Action1<UserRealm> saveToCacheUser = userRealm -> {
        if (userRealm != null) {
            CloudUserDataStore.this.userCache.put(userRealm);
        }
    };
}
