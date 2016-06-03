package com.tribe.app.data.network;

import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.realm.UserRealm;

import java.util.List;

import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

public interface TribeApi {

    @POST("/token")
    Observable<AccessToken> loginWithUsername(@Body LoginEntity loginEntity);

    @FormUrlEncoded
    @POST("/graphql")
    Observable<UserRealm> getUserInfos(@Field("query") String query);
}