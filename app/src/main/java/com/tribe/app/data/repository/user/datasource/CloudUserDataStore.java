package com.tribe.app.data.repository.user.datasource;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.tribe.app.R;
import com.tribe.app.data.cache.UserCache;
import com.tribe.app.data.network.LoginApi;
import com.tribe.app.data.network.TribeApi;
import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.LocationRealm;
import com.tribe.app.data.realm.PinRealm;
import com.tribe.app.data.realm.UserRealm;

import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
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
    private final Installation installation;
    private final ReactiveLocationProvider reactiveLocationProvider;

    /**
     * Construct a {@link UserDataStore} based on connections to the api (Cloud).
     * @param userCache A {@link UserCache} to cache data retrieved from the api.
     * @param tribeApi an implementation of the api
     * @param loginApi an implementation of the login api
     * @param context the context
     * @param accessToken the access token
     */
    public CloudUserDataStore(UserCache userCache, TribeApi tribeApi, LoginApi loginApi,
                              AccessToken accessToken, Installation installation,
                              ReactiveLocationProvider reactiveLocationProvider, Context context) {
        this.userCache = userCache;
        this.tribeApi = tribeApi;
        this.loginApi = loginApi;
        this.context = context;
        this.accessToken = accessToken;
        this.installation = installation;
        this.reactiveLocationProvider = reactiveLocationProvider;
    }

    @Override
    public Observable<PinRealm> requestCode(String phoneNumber) {
        return this.loginApi
                .requestCode(new LoginEntity(phoneNumber))
                .doOnError(throwable -> {
                    AccessToken accessToken1 = new AccessToken();
                    accessToken1.setAccessToken("TO94aH0PV6LETnP8uPwQpKwh1JuMaj7pxD8ghrpmgEfJlQjHRn");
                    accessToken1.setTokenType("Bearer");
                    accessToken1.setUserId("BJgkS2rN");
                    CloudUserDataStore.this.userCache.put(accessToken1);
                });
    }

    @Override
    public Observable<AccessToken> loginWithPhoneNumber(String phoneNumber, String code, String scope) {
        return this.loginApi
                .loginWithUsername(new LoginEntity(phoneNumber, code, scope, "password"))
                .doOnError(throwable -> {
                    AccessToken accessToken1 = new AccessToken();
                    accessToken1.setAccessToken("TO94aH0PV6LETnP8uPwQpKwh1JuMaj7pxD8ghrpmgEfJlQjHRn");
                    accessToken1.setTokenType("Bearer");
                    accessToken1.setUserId("BJgkS2rN");
                    CloudUserDataStore.this.userCache.put(accessToken1);
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
        return Observable.zip(this.tribeApi.getUserInfos(context.getString(R.string.user_infos)),
                reactiveLocationProvider.getLastKnownLocation().defaultIfEmpty(null),
                (userRealm, location) -> {
                    if (location != null) {
                        LocationRealm locationRealm = new LocationRealm();
                        locationRealm.setLatitude(location.getLatitude());
                        locationRealm.setLongitude(location.getLongitude());
                        locationRealm.setHasLocation(true);
                        userRealm.setLocation(locationRealm);
                    }
                    return userRealm;
                }).doOnNext(saveToCacheUser);
    }

    @Override
    public Observable<Installation> createOrUpdateInstall(String token) {
        TelephonyManager telephonyManager = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
        String operatorName = telephonyManager.getNetworkOperatorName();
        PackageManager manager = context.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String base = context.getString(R.string.install_base,
                    accessToken.getUserId(),
                    token,
                    "android",
                    Build.VERSION.RELEASE,
                    Build.MANUFACTURER,
                    Build.MODEL,
                    info != null ? info.versionName : "UNKNOWN",
                    context.getPackageName(),
                    context.getResources().getConfiguration().locale.toString(),
                    operatorName
                );

        String req = installation == null || installation.getId() == null ? context.getString(R.string.install_create, base) : context.getString(R.string.install_update, installation.getId(), base);
        return this.tribeApi.createOrUpdateInstall(req).doOnNext(saveToCacheInstall);
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

    private final Action1<Installation> saveToCacheInstall = installRealm -> {
        if (installRealm != null) {
            CloudUserDataStore.this.userCache.put(installRealm);
        }
    };
}
