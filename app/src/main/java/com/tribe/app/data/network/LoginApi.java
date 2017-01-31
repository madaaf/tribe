package com.tribe.app.data.network;

import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.network.entity.RefreshEntity;
import com.tribe.app.data.network.entity.RegisterEntity;
import com.tribe.app.data.network.entity.UsernameEntity;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.PinRealm;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;

public interface LoginApi {

  @POST("/pin") Observable<PinRealm> requestCode(@Body LoginEntity loginEntity);

  @POST("/token") Observable<AccessToken> loginWithUsername(@Body LoginEntity loginEntity);

  @POST("/refresh") Call<AccessToken> refreshToken(@Body RefreshEntity loginEntity);

  @POST("/register") Observable<AccessToken> register(@Body RegisterEntity registerEntity);

  @POST("/username") Observable<Boolean> lookupUsername(@Body UsernameEntity usernameEntity);
}