package com.tribe.app.data.network;

import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.PinRealm;

import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;

public interface LoginApi {

    @POST("/pin")
    Observable<PinRealm> requestCode(@Body LoginEntity loginEntity);

    @POST("/token")
    Observable<AccessToken> loginWithUsername(@Body LoginEntity loginEntity);
}