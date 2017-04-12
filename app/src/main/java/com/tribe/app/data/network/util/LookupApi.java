package com.tribe.app.data.network.util;

import com.tribe.app.data.network.entity.LoginEntity;
import com.tribe.app.data.realm.PinRealm;
import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;

public interface LookupApi {

  @POST("/v2/lookup") Observable<PinRealm> requestCode(@Body LoginEntity loginEntity);
}