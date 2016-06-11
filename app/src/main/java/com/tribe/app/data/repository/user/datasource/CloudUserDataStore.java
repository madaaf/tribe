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

    /**
     * Construct a {@link UserDataStore} based on connections to the api (Cloud).
     * @param userCache A {@link UserCache} to cache data retrieved from the api.
     * @param tribeApi an implementation of the api
     * @param loginApi an implementation of the login api
     * @param context the context
     */
    public CloudUserDataStore(UserCache userCache, TribeApi tribeApi, LoginApi loginApi, Context context) {
        this.userCache = userCache;
        this.tribeApi = tribeApi;
        this.loginApi = loginApi;
        this.context = context;
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
                .doOnNext(saveToCacheAction);
    }

    @Override
    public Observable<AccessToken> loginWithUsername(String username, String password) {
        return this.loginApi
                .loginWithUsername(new LoginEntity(username, password, "", "password"))
                .doOnNext(saveToCacheAction);
    }

    @Override
    public Observable<UserRealm> userInfos(String userId) {
        return this.tribeApi.getUserInfos(context.getString(R.string.user_infos));
    }

    private final Action1<AccessToken> saveToCacheAction = accessToken -> {
        if (accessToken != null && accessToken.getAccessToken() != null) {
            CloudUserDataStore.this.userCache.put(accessToken);
        }
    };
}
