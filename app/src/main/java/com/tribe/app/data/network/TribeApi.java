package com.tribe.app.data.network;

import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.realm.FriendshipRealm;
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

    @GET("/v1/friendships")
    Observable<List<FriendshipRealm>> friendships(@Query("userId") int userId);

    @POST("/token")
    Observable<UserRealm> loginWithUsername(@Body LoginEntity loginEntity);
}