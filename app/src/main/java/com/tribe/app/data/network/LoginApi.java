package com.tribe.app.data.network;

import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.network.entity.RefreshEntity;
import com.tribe.app.data.network.entity.RegisterEntity;
import com.tribe.app.data.network.entity.UsernameEntity;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.realm.PinRealm;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import rx.Observable;

public interface LoginApi {

  public static final String FACEBOOK_TOKEN = "facebook-token";
  public static final String X_VERIFY = "X-Verify-Credentials-Authorization";
  public static final String X_AUTH = "X-Auth-Service-Provider";

  @POST("/pin") Observable<PinRealm> requestCode(@Body LoginEntity loginEntity);

  @POST("/token") Observable<AccessToken> loginWithUsername(@Body LoginEntity loginEntity);

  @POST("/anonToken") Observable<AccessToken> loginWithAnonymous();

  @POST("/token") Observable<AccessToken> loginWithFacebook(@Header(FACEBOOK_TOKEN) String fbAccessToken, @Body LoginEntity loginEntity);

  @POST("/token") Observable<AccessToken> loginWithUsername(@Header(X_VERIFY) String xVerify,
      @Header(X_AUTH) String xAuth, @Body LoginEntity loginEntity);

  @POST("/refresh") Call<AccessToken> refreshToken(@Body RefreshEntity loginEntity);

  @POST("/register") Observable<AccessToken> register(@Body RegisterEntity registerEntity);

  @POST("/register") Observable<AccessToken> registerWithFacebook(@Header(FACEBOOK_TOKEN) String fbAccessToken, @Body RegisterEntity registerEntity);

  @POST("/register") Observable<AccessToken> register(@Header(X_VERIFY) String xVerify,
                                                      @Header(X_AUTH) String xAuth, @Body RegisterEntity registerEntity);

  @POST("/username") Observable<Boolean> lookupUsername(@Body UsernameEntity usernameEntity);
}