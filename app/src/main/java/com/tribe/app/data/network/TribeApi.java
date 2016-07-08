package com.tribe.app.data.network;

import com.tribe.app.data.realm.TribeRealm;
import com.tribe.app.data.realm.UserRealm;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import rx.Observable;

public interface TribeApi {

    @FormUrlEncoded
    @POST("/graphql")
    Observable<UserRealm> getUserInfos(@Field("query") String query);

    @Multipart
    @POST("/graphql")
    Observable<TribeRealm> uploadTribe(@Part("query") RequestBody query, @Part MultipartBody.Part file);
}