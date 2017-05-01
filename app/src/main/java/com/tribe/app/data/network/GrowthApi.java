package com.tribe.app.data.network;

import com.tribe.app.data.network.entity.LookupObject;
import java.util.List;
import retrofit2.http.Body;
import retrofit2.http.POST;
import rx.Observable;

public interface GrowthApi {

  @POST("/invitations") Observable<Void> sendInvitations(@Body List<LookupObject> lookup);
}