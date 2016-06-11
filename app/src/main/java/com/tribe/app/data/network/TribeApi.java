package com.tribe.app.data.network;

import com.tribe.app.data.realm.UserRealm;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import rx.Observable;

public interface TribeApi {

    @FormUrlEncoded
    @POST("/graphql")
    Observable<UserRealm> getUserInfos(@Field("query") String query);
}